package com.zachgrosser.wiki

import java.io.ByteArrayOutputStream

/**
 * Turns the HTML from Wikipedia's `action=parse` API into an [Article] of plain-text blocks.
 *
 * Keeps paragraphs, headings, lists, quotes and hatnotes ("Main article: …"), with links to
 * other articles plus bold and italic. Drops what doesn't suit a small text-only screen:
 * images, tables, infoboxes, citation markers, navigation boxes and the reference sections.
 */
object ArticleParser {

    fun parse(title: String, pageId: Long, revisionId: Long, html: String): Article {
        val builder = ArticleBuilder()
        HtmlTokenizer(html).tokenize(builder)
        return builder.build(title, pageId, revisionId)
    }

    /** Sections that are mostly citations or outbound links, which aren't useful here. */
    internal val DROPPED_SECTIONS = setOf(
        "references", "notes", "footnotes", "citations", "sources", "bibliography",
        "external links", "further reading", "works cited", "cited sources",
        "general references", "general and cited sources", "explanatory notes",
        "notes and references", "references and notes", "literature",
    )

    private val NAMESPACES = setOf(
        "special", "file", "image", "media", "category", "template", "help", "wikipedia",
        "portal", "draft", "talk", "user", "user talk", "wikipedia talk", "file talk",
        "template talk", "help talk", "category talk", "portal talk", "draft talk",
        "module", "mediawiki", "timedtext", "book", "project", "wp", "gadget", "topic",
    )

    /**
     * Matches the credit notices Wikipedia shows for imported text, such as "This article
     * incorporates text from a free content work" or "…incorporates public domain material from…".
     */
    internal val ATTRIBUTION_NOTICE = Regex("""\bincorporates\b[^.]*\b(text|material)\b""", RegexOption.IGNORE_CASE)

    /** Returns the article title an internal link points to, or null for anything else. */
    fun linkTarget(href: String?): String? {
        if (href == null || !href.startsWith("/wiki/")) return null
        val raw = href.substring("/wiki/".length).substringBefore('#').substringBefore('?')
        val title = percentDecode(raw).replace('_', ' ').trim()
        if (title.isEmpty()) return null
        val colon = title.indexOf(':')
        if (colon > 0 && title.substring(0, colon).trim().lowercase() in NAMESPACES) return null
        return title
    }

    internal fun percentDecode(value: String): String {
        if ('%' !in value) return value
        val out = StringBuilder(value.length)
        val pending = ByteArrayOutputStream()
        fun flushBytes() {
            if (pending.size() > 0) {
                out.append(String(pending.toByteArray(), Charsets.UTF_8))
                pending.reset()
            }
        }
        var i = 0
        while (i < value.length) {
            val c = value[i]
            val hex = if (c == '%' && i + 2 < value.length) value.substring(i + 1, i + 3).toIntOrNull(16) else null
            if (hex != null) {
                pending.write(hex)
                i += 3
            } else {
                flushBytes()
                out.append(c)
                i++
            }
        }
        flushBytes()
        return out.toString()
    }
}

/** Receives the pieces of an HTML document, in order. */
internal interface HtmlHandler {
    fun onStartTag(name: String, attributes: Map<String, String>, selfClosing: Boolean)
    fun onEndTag(name: String)
    fun onText(text: String)
}

/**
 * A small, forgiving HTML tokenizer. Wikipedia's HTML is well-formed, so this doesn't try to
 * handle every quirk of the HTML spec. Comments are dropped and the contents of `<style>` and
 * `<script>` are skipped without being reported.
 */
internal class HtmlTokenizer(private val html: String) {

    fun tokenize(handler: HtmlHandler) {
        var i = 0
        val n = html.length
        while (i < n) {
            val lt = html.indexOf('<', i)
            if (lt < 0) {
                handler.onText(decodeEntities(html.substring(i)))
                break
            }
            if (lt > i) handler.onText(decodeEntities(html.substring(i, lt)))
            i = when {
                html.startsWith("<!--", lt) -> {
                    val end = html.indexOf("-->", lt + 4)
                    if (end < 0) n else end + 3
                }
                html.startsWith("<!", lt) || html.startsWith("<?", lt) -> {
                    val end = html.indexOf('>', lt)
                    if (end < 0) n else end + 1
                }
                lt + 1 < n && html[lt + 1] == '/' -> readEndTag(lt, handler)
                lt + 1 < n && html[lt + 1].isLetter() -> readStartTag(lt, handler)
                else -> {
                    handler.onText("<")
                    lt + 1
                }
            }
        }
    }

    private fun readEndTag(lt: Int, handler: HtmlHandler): Int {
        var j = lt + 2
        val nameStart = j
        while (j < html.length && isNameChar(html[j])) j++
        val name = html.substring(nameStart, j).lowercase()
        val close = html.indexOf('>', j)
        if (name.isNotEmpty()) handler.onEndTag(name)
        return if (close < 0) html.length else close + 1
    }

    private fun readStartTag(lt: Int, handler: HtmlHandler): Int {
        var j = lt + 1
        val nameStart = j
        while (j < html.length && isNameChar(html[j])) j++
        val name = html.substring(nameStart, j).lowercase()
        val attributes = HashMap<String, String>()
        var selfClosing = false
        while (j < html.length) {
            val c = html[j]
            when {
                c == '>' -> {
                    j++
                    break
                }
                c == '/' -> {
                    selfClosing = j + 1 < html.length && html[j + 1] == '>'
                    j++
                }
                c.isWhitespace() -> j++
                else -> {
                    val attrStart = j
                    while (j < html.length && !html[j].isWhitespace() && html[j] != '=' &&
                        html[j] != '>' && html[j] != '/'
                    ) j++
                    val attrName = html.substring(attrStart, j).lowercase()
                    while (j < html.length && html[j].isWhitespace()) j++
                    var value = ""
                    if (j < html.length && html[j] == '=') {
                        j++
                        while (j < html.length && html[j].isWhitespace()) j++
                        if (j < html.length && (html[j] == '"' || html[j] == '\'')) {
                            val quote = html[j]
                            val end = html.indexOf(quote, j + 1).let { if (it < 0) html.length else it }
                            value = html.substring(j + 1, end)
                            j = end + 1
                        } else {
                            val valueStart = j
                            while (j < html.length && !html[j].isWhitespace() && html[j] != '>') j++
                            value = html.substring(valueStart, j)
                        }
                    }
                    if (attrName.isNotEmpty()) attributes[attrName] = decodeEntities(value)
                    if (attrStart == j) j++ // never get stuck on an unexpected character
                }
            }
        }
        if (name in RAW_TEXT_ELEMENTS && !selfClosing) {
            val end = html.indexOf("</$name", j, ignoreCase = true)
            if (end < 0) return html.length
            val close = html.indexOf('>', end)
            return if (close < 0) html.length else close + 1
        }
        // Void elements (<br>, <img>, …) never have an end tag, so report them as self-closing.
        handler.onStartTag(name, attributes, selfClosing || name in VOID_ELEMENTS)
        return j
    }

    private fun isNameChar(c: Char) = c.isLetterOrDigit() || c == '-' || c == ':'

    companion object {
        val VOID_ELEMENTS = setOf(
            "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta",
            "param", "source", "track", "wbr",
        )
        private val RAW_TEXT_ELEMENTS = setOf("style", "script")

        private val NAMED_ENTITIES = mapOf(
            "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'",
            "nbsp" to " ", "ndash" to "–", "mdash" to "—", "minus" to "−",
            "times" to "×", "divide" to "÷", "hellip" to "…", "lsquo" to "‘", "rsquo" to "’",
            "ldquo" to "“", "rdquo" to "”", "laquo" to "«", "raquo" to "»", "middot" to "·",
            "deg" to "°", "plusmn" to "±", "prime" to "′", "Prime" to "″", "thinsp" to " ",
            "ensp" to " ", "emsp" to " ", "zwj" to "‍", "zwnj" to "‌",
            "shy" to "", "copy" to "©", "reg" to "®", "trade" to "™", "sect" to "§",
            "para" to "¶", "frac12" to "½", "frac14" to "¼", "frac34" to "¾", "bull" to "•",
        )

        fun decodeEntities(text: String): String {
            if ('&' !in text) return text
            val out = StringBuilder(text.length)
            var i = 0
            while (i < text.length) {
                val c = text[i]
                if (c == '&') {
                    val semi = text.indexOf(';', i + 1)
                    if (semi > i + 1 && semi - i <= 12) {
                        val entity = text.substring(i + 1, semi)
                        val decoded = when {
                            entity.startsWith("#x") || entity.startsWith("#X") ->
                                entity.substring(2).toIntOrNull(16)?.let(::codePointToString)
                            entity.startsWith("#") ->
                                entity.substring(1).toIntOrNull()?.let(::codePointToString)
                            else -> NAMED_ENTITIES[entity]
                        }
                        if (decoded != null) {
                            out.append(decoded)
                            i = semi + 1
                            continue
                        }
                    }
                }
                out.append(c)
                i++
            }
            return out.toString()
        }

        private fun codePointToString(codePoint: Int): String? =
            if (codePoint in 1..0x10FFFF) String(Character.toChars(codePoint)) else null
    }
}

/** Walks the tokens and collects blocks, sections, links and styles. */
private class ArticleBuilder : HtmlHandler {

    private enum class Role {
        Paragraph, ListItem, List, DefinitionList, Indent, Term, Quote, Note, Heading,
        Description, Container, Inline,
    }

    private class Frame(
        val name: String,
        val role: Role,
        val span: SpanKind? = null,
        val linkTarget: String? = null,
        var spanStart: Int = 0,
        val ordered: Boolean = false,
        var counter: Int = 0,
        val marker: String? = null,
        val headingLevel: Int = 0,
    )

    private val stack = ArrayList<Frame>()
    private var skipUntilDepth = -1

    private val text = StringBuilder()
    private val spans = ArrayList<TextSpan>()
    private val heading = StringBuilder()
    private val description = StringBuilder()

    private val lead = ArrayList<Block>()
    private val sections = ArrayList<Pair<String, ArrayList<Block>>>()
    private var current: ArrayList<Block> = lead
    private val notices = ArrayList<Block>()
    private var droppedLevel: Int? = null

    private val skipping get() = skipUntilDepth >= 0
    private val inHeading get() = stack.any { it.role == Role.Heading }
    private val inDescription get() = stack.any { it.role == Role.Description }

    override fun onStartTag(name: String, attributes: Map<String, String>, selfClosing: Boolean) {
        if (skipping) {
            if (!selfClosing) stack.add(Frame(name, Role.Container))
            return
        }
        val classes = attributes["class"]?.split(' ', '\t', '\n')?.filter { it.isNotEmpty() }?.toSet().orEmpty()

        if (name == "math") {
            val formula = attributes["alttext"]?.let(::readableFormula)
            if (!formula.isNullOrEmpty()) appendText(" $formula ")
            if (!selfClosing) {
                stack.add(Frame(name, Role.Container))
                skipUntilDepth = stack.size - 1
            }
            return
        }
        if ("shortdescription" in classes && !selfClosing) {
            stack.add(Frame(name, Role.Description))
            return
        }
        if (shouldSkip(name, classes, attributes)) {
            if (!selfClosing) {
                stack.add(Frame(name, Role.Container))
                skipUntilDepth = stack.size - 1
            }
            return
        }
        if (name == "br") {
            if (!inHeading && !inDescription) lineBreak()
            return
        }
        if (selfClosing) return

        val frame = when (name) {
            "p", "pre" -> Frame(name, Role.Paragraph)
            "ul", "ol" -> Frame(name, Role.List, ordered = name == "ol")
            "li" -> {
                val list = stack.lastOrNull { it.role == Role.List }
                val marker = if (list?.ordered == true) {
                    list.counter++
                    "${list.counter}."
                } else {
                    "•"
                }
                Frame(name, Role.ListItem, marker = marker)
            }
            "dl" -> Frame(name, Role.DefinitionList)
            "dd" -> Frame(name, Role.Indent)
            "dt" -> Frame(name, Role.Term, span = SpanKind.Bold)
            "blockquote" -> Frame(name, Role.Quote)
            "h1", "h2", "h3", "h4", "h5", "h6" -> Frame(name, Role.Heading, headingLevel = name[1] - '0')
            "a" -> Frame(name, Role.Inline, span = SpanKind.Link, linkTarget = ArticleParser.linkTarget(attributes["href"]))
            "b", "strong" -> Frame(name, Role.Inline, span = SpanKind.Bold)
            "i", "em" -> Frame(name, Role.Inline, span = SpanKind.Italic)
            in BLOCK_CONTAINERS -> {
                val isNote = attributes["role"] == "note" || "hatnote" in classes
                Frame(name, if (isNote) Role.Note else Role.Container)
            }
            else -> Frame(name, Role.Inline)
        }

        if (frame.role != Role.Inline) flushBlock()
        if (frame.role == Role.Heading) heading.setLength(0)
        frame.spanStart = text.length
        stack.add(frame)
    }

    override fun onEndTag(name: String) {
        val index = stack.indexOfLast { it.name == name }
        if (index < 0) return
        while (stack.size > index) {
            val frame = stack.removeAt(stack.size - 1)
            if (skipping) {
                if (stack.size <= skipUntilDepth) skipUntilDepth = -1
                continue
            }
            closeFrame(frame)
        }
    }

    override fun onText(text: String) {
        if (skipping) return
        when {
            inDescription -> appendCollapsed(description, text)
            inHeading -> appendCollapsed(heading, text)
            else -> appendText(text)
        }
    }

    private fun closeFrame(frame: Frame) {
        when (frame.role) {
            Role.Inline -> closeSpan(frame)
            Role.Heading -> onHeading(frame.headingLevel, heading.toString().trim())
            Role.Description -> Unit
            else -> {
                // Put the frame back while flushing so the block gets the right kind (list item,
                // quote, …) and a term's bold style is closed along with it.
                stack.add(frame)
                flushBlock()
                stack.removeAt(stack.size - 1)
            }
        }
    }

    private fun closeSpan(frame: Frame) {
        val kind = frame.span ?: return
        if (kind == SpanKind.Link && frame.linkTarget == null) return
        addSpan(frame.spanStart, text.length, kind, frame.linkTarget)
    }

    private fun addSpan(rawStart: Int, rawEnd: Int, kind: SpanKind, target: String?) {
        var start = rawStart.coerceIn(0, text.length)
        var end = rawEnd.coerceIn(0, text.length)
        while (start < end && text[start].isWhitespace()) start++
        while (end > start && text[end - 1].isWhitespace()) end--
        if (end > start) spans.add(TextSpan(start, end, kind, target))
    }

    private fun appendText(value: String) {
        val script = stack.lastOrNull { it.name == "sup" || it.name == "sub" }?.name
        val mapped = when (script) {
            "sup" -> toScript(value, SUPERSCRIPTS, "^")
            "sub" -> toScript(value, SUBSCRIPTS, "_")
            else -> value
        }
        appendCollapsed(text, mapped)
    }

    /**
     * "km<sup>2</sup>" reads as "km²" and "H<sub>2</sub>O" as "H₂O". Other superscripts with
     * letters are usually ordinals ("19<sup>th</sup>") and stay as they are; anything else gets
     * a "^" or "_" prefix.
     */
    private fun toScript(value: String, table: Map<Char, Char>, fallbackPrefix: String): String {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) return value
        return when {
            trimmed.all { it in table } -> trimmed.map { table.getValue(it) }.joinToString("")
            fallbackPrefix == "^" && trimmed.any { it.isLetter() } -> trimmed
            else -> fallbackPrefix + trimmed
        }
    }

    private fun lineBreak() {
        trimTrailingSpaces()
        if (text.isNotEmpty() && text.last() != '\n') text.append('\n')
    }

    private fun trimTrailingSpaces() {
        while (text.isNotEmpty() && (text.last() == ' ' || text.last() == '\n')) {
            text.setLength(text.length - 1)
        }
    }

    private fun flushBlock() {
        trimTrailingSpaces()
        val keep = text.isNotBlank() &&
            (droppedLevel == null || ArticleParser.ATTRIBUTION_NOTICE.containsMatchIn(text))
        if (keep) {
            // Close styles that are still open; they continue in the next block.
            stack.forEach { frame ->
                val kind = frame.span ?: return@forEach
                if (kind == SpanKind.Link && frame.linkTarget == null) return@forEach
                addSpan(frame.spanStart, text.length, kind, frame.linkTarget)
            }
            // Drop leading spaces (including the no-break space some templates start with).
            var leading = 0
            while (leading < text.length && text[leading].isWhitespace()) leading++
            text.delete(0, leading)
            val blockSpans = spans
                .map { it.copy(start = (it.start - leading).coerceAtLeast(0), end = it.end - leading) }
                .filter { it.end > it.start }
                .sortedBy { it.start }
            if (droppedLevel == null) {
                current.add(makeBlock(text.toString(), blockSpans))
            } else {
                // Inside a left-out section (Sources, References…), keep only credit notices.
                notices.add(Block(BlockKind.Note, text.toString(), blockSpans))
            }
        }
        text.setLength(0)
        spans.clear()
        stack.forEach { it.spanStart = 0 }
    }

    private fun makeBlock(value: String, blockSpans: List<TextSpan>): Block {
        val listDepth = stack.count { it.role == Role.List }
        val indentDepth = stack.count { it.role == Role.DefinitionList }
        for (frame in stack.asReversed()) {
            when (frame.role) {
                Role.ListItem -> return Block(
                    BlockKind.ListItem, value, blockSpans,
                    depth = (listDepth - 1).coerceAtLeast(0) + indentDepth,
                    marker = frame.marker,
                )
                Role.Indent, Role.Term -> return Block(
                    BlockKind.Indent, value, blockSpans,
                    depth = (indentDepth - 1).coerceAtLeast(0) + listDepth,
                )
                Role.Quote -> return Block(BlockKind.Quote, value, blockSpans)
                Role.Note -> return Block(BlockKind.Note, value, blockSpans)
                else -> Unit
            }
        }
        return Block(BlockKind.Paragraph, value, blockSpans)
    }

    private fun onHeading(level: Int, title: String) {
        heading.setLength(0)
        val dropped = droppedLevel
        if (dropped != null) {
            if (level > dropped) return
            droppedLevel = null
        }
        if (title.isEmpty()) return
        if (title.lowercase() in ArticleParser.DROPPED_SECTIONS) {
            droppedLevel = level
            return
        }
        if (level <= 2) {
            val blocks = ArrayList<Block>()
            sections.add(title to blocks)
            current = blocks
        } else {
            current.add(Block(BlockKind.Subheading, title, depth = level))
        }
    }

    fun build(title: String, pageId: Long, revisionId: Long): Article {
        flushBlock()
        return Article(
            title = title,
            pageId = pageId,
            revisionId = revisionId,
            description = description.toString().trim().ifEmpty { null },
            lead = withoutEmptySubheadings(lead),
            sections = sections
                .map { (name, blocks) -> Section(name, withoutEmptySubheadings(blocks)) }
                .filter { section -> section.blocks.any { it.kind != BlockKind.Subheading } },
            notices = notices.distinctBy { it.text },
        )
    }

    /** Removes subheadings that ended up with no text under them (e.g. only a table). */
    private fun withoutEmptySubheadings(blocks: List<Block>): List<Block> {
        val result = ArrayList<Block>(blocks.size)
        for (i in blocks.indices) {
            val block = blocks[i]
            if (block.kind == BlockKind.Subheading) {
                val next = blocks.subList(i + 1, blocks.size)
                    .firstOrNull { it.kind != BlockKind.Subheading || it.depth <= block.depth }
                if (next == null || next.kind == BlockKind.Subheading) continue
            }
            result.add(block)
        }
        return result
    }

    private fun shouldSkip(name: String, classes: Set<String>, attributes: Map<String, String>): Boolean {
        if (name in SKIPPED_ELEMENTS) return true
        if (classes.any { it in SKIPPED_CLASSES }) return true
        if (attributes["id"] == "coordinates") return true
        if (attributes["role"] == "navigation") return true
        val style = attributes["style"]?.replace(" ", "")?.lowercase().orEmpty()
        return "display:none" in style && "mwe-math-mathml-a11y" !in classes
    }

    companion object {
        private val BLOCK_CONTAINERS = setOf(
            "div", "section", "center", "article", "aside", "main", "header", "footer",
            "details", "summary",
        )

        private val SUPERSCRIPTS = mapOf(
            '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴', '5' to '⁵', '6' to '⁶',
            '7' to '⁷', '8' to '⁸', '9' to '⁹', '+' to '⁺', '-' to '⁻', '−' to '⁻', '=' to '⁼',
            '(' to '⁽', ')' to '⁾', 'n' to 'ⁿ', 'i' to 'ⁱ',
        )

        private val SUBSCRIPTS = mapOf(
            '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄', '5' to '₅', '6' to '₆',
            '7' to '₇', '8' to '₈', '9' to '₉', '+' to '₊', '-' to '₋', '−' to '₋', '=' to '₌',
            '(' to '₍', ')' to '₎',
        )

        private val SKIPPED_ELEMENTS = setOf(
            "table", "figure", "figcaption", "caption", "img", "audio", "video", "svg",
            "noscript", "map", "object", "iframe", "button", "input", "select", "textarea",
            "form", "template", "annotation", "annotation-xml",
        )

        private val SKIPPED_CLASSES = setOf(
            "reference", "noprint", "navbox", "navbox-styles", "infobox", "sidebar", "metadata",
            "ambox", "mbox-small", "sistersitebox", "side-box", "thumb", "tmulti", "gallery",
            "mw-references-wrap", "references", "reflist", "refbegin", "mw-empty-elt", "toc",
            "mw-editsection", "mw-cite-backlink", "authority-control", "portalbox",
            "clade-gallery", "haudio", "spoken-wikipedia", "mw-ext-cite-error", "catlinks",
            "printfooter", "mw-jump-link", "navigation-only", "Z3988",
        )

        private val LATEX_SYMBOLS = mapOf(
            "pi" to "π", "alpha" to "α", "beta" to "β", "gamma" to "γ", "delta" to "δ",
            "epsilon" to "ε", "varepsilon" to "ε", "theta" to "θ", "lambda" to "λ", "mu" to "μ",
            "sigma" to "σ", "tau" to "τ", "phi" to "φ", "varphi" to "φ", "omega" to "ω",
            "Gamma" to "Γ", "Delta" to "Δ", "Theta" to "Θ", "Lambda" to "Λ", "Sigma" to "Σ",
            "Phi" to "Φ", "Omega" to "Ω", "infty" to "∞", "cdot" to "·", "times" to "×",
            "div" to "÷", "pm" to "±", "leq" to "≤", "le" to "≤", "geq" to "≥", "ge" to "≥",
            "neq" to "≠", "ne" to "≠", "approx" to "≈", "equiv" to "≡", "sqrt" to "√",
            "sum" to "Σ", "prod" to "Π", "int" to "∫", "partial" to "∂", "nabla" to "∇",
            "rightarrow" to "→", "to" to "→", "leftarrow" to "←", "Rightarrow" to "⇒",
            "in" to "∈", "notin" to "∉", "subset" to "⊂", "cup" to "∪", "cap" to "∩",
            "ldots" to "…", "dots" to "…", "cdots" to "⋯", "circ" to "∘", "degree" to "°",
        )

        private val LATEX_DROPPED = setOf(
            "displaystyle", "textstyle", "scriptstyle", "left", "right", "mathrm", "mathbf",
            "mathit", "mathsf", "mathtt", "operatorname", "text", "textrm", "textit",
            "textbf", "rm", "bf", "it", "big", "Big", "bigg", "Bigg",
        )

        /** Makes the LaTeX that Wikipedia includes with each formula a little easier to read. */
        fun readableFormula(latex: String): String {
            val out = StringBuilder()
            var i = 0
            while (i < latex.length) {
                val c = latex[i]
                if (c == '\\' && i + 1 < latex.length) {
                    var j = i + 1
                    if (latex[j].isLetter()) {
                        while (j < latex.length && latex[j].isLetter()) j++
                        val command = latex.substring(i + 1, j)
                        when {
                            command in LATEX_SYMBOLS -> out.append(LATEX_SYMBOLS.getValue(command))
                            command in LATEX_DROPPED -> Unit
                            else -> out.append('\\').append(command)
                        }
                        i = j
                    } else {
                        // Spacing commands such as "\," and "\ ".
                        if (latex[j] == '{' || latex[j] == '}') out.append(latex[j]) else out.append(' ')
                        i = j + 1
                    }
                    continue
                }
                out.append(c)
                i++
            }
            var result = out.toString().replace(Regex("\\s+"), " ").trim()
                .replace("{ ", "{").replace(" }", "}")
            // "{\displaystyle e^{i\pi}+1=0}" leaves a pair of outer braces behind.
            while (result.startsWith("{") && result.endsWith("}") && bracesBalancedInside(result)) {
                result = result.substring(1, result.length - 1).trim()
            }
            return result
        }

        private fun bracesBalancedInside(value: String): Boolean {
            var depth = 0
            for (i in 1 until value.length - 1) {
                when (value[i]) {
                    '{' -> depth++
                    '}' -> if (--depth < 0) return false
                }
            }
            return depth == 0
        }
    }
}

private fun appendCollapsed(target: StringBuilder, value: String) {
    for (c in value) {
        if (c == ' ' || c == '\n' || c == '\t' || c == '\r' || c == '\u000C') {
            if (target.isEmpty() || target.last() == ' ' || target.last() == '\n') continue
            target.append(' ')
        } else {
            target.append(c)
        }
    }
}
