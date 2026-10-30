package com.zachgrosser.wiki

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Section 1 of the Wikipedia article "Light Phone" (revision 1368520722), as returned by
// action=parse. Text by Wikipedia contributors, CC BY-SA 4.0:
// https://en.wikipedia.org/wiki/Light_Phone — long stylesheets and citations shortened.
private val LIGHT_PHONE_SECTION = """
<div class="mw-content-ltr mw-parser-output" lang="en" dir="ltr"><div class="mw-heading mw-heading2"><h2 id="Light_Phone">Light Phone</h2></div>
<style data-mw-deduplicate="TemplateStyles:r1368532237">.mw-parser-output .hatnote{font-style:italic}.mw-parser-output div.hatnote{padding-left:1.6em}.mw-parser-output .hatnote+.mw-empty-elt+.hatnote{margin-top:-0.5em} a > b <i>not text</i></style><div role="note" class="hatnote navigation-not-searchable">Main article: <a href="/wiki/Light_Phone_(1st_generation)" title="Light Phone (1st generation)">Light Phone (1st generation)</a></div>
<figure class="mw-default-size" typeof="mw:File/Thumb"><a href="/wiki/File:Lightphone-1.svg" class="mw-file-description"><img src="//thumb.wikimedia.org/wikipedia/commons/thumb/d/d8/Lightphone-1.svg/250px-Lightphone-1.svg.png?utm_source=en.wikipedia.org&amp;utm_campaign=parser" decoding="async" loading="lazy" width="180" height="264" class="mw-file-element mw-file-upright" /></a><figcaption>The Light Phone (1st generation)</figcaption></figure>
<div class="mw-heading mw-heading3"><h3 id="Release">Release</h3></div>
<p>The <a href="/wiki/Light_Phone_(1st_generation)" title="Light Phone (1st generation)">Light Phone</a> was announced on May 13, 2015, via <a href="/wiki/Kickstarter" title="Kickstarter">Kickstarter</a>, and released in April 2017. During its campaign, the Light Phone raised $415,127 from 3,187 backers, over double the initial goal of $200,000.<sup id="cite&#95;ref-:0&#95;1-0" class="reference"><a href="#cite_note-:0-1"><span class="cite-bracket">&#91;</span>1<span class="cite-bracket">&#93;</span></a></sup>
</p>
<div class="mw-heading mw-heading3"><h3 id="Features_and_specifications">Features and specifications</h3></div>
<p>The Light Phone is only capable of making and receiving phone calls and stores up to 10 <a href="/wiki/Speed_dial" title="Speed dial">speed dial numbers</a>. Upon launch, it came with a prepaid <a href="/wiki/2G" title="2G">2G</a> <a href="/wiki/SIM_card" title="SIM card">SIM card</a> with 500 minutes of call time, assigned to a separate phone number.<sup id="cite&#95;ref-2" class="reference"><a href="#cite_note-2"><span class="cite-bracket">&#91;</span>2<span class="cite-bracket">&#93;</span></a></sup> The Light Phone features a small two-tone analog-digital display and a <a href="/wiki/Phone_connector_(audio)" title="Phone connector (audio)">headphone jack</a> to assist with phone calls.
</p><p>There used to be a smartphone application allowing for redirection of calls from the user's smartphone to the Light Phone. This app is no longer accessible.<sup id="cite&#95;ref-3" class="reference"><a href="#cite_note-3"><span class="cite-bracket">&#91;</span>3<span class="cite-bracket">&#93;</span></a></sup>
</p>
<div class="mw-references-wrap"><ol class="references">
<li id="cite&#95;note-:0-1"><span class="mw-cite-backlink"><b><a href="#cite_ref-:0_1-0">^</a></b></span> <span class="reference-text"> <span class="error mw-ext-cite-error" lang="en" dir="ltr">Cite error: The named reference <code>:0</code> was invoked but never defined (see the <a href="/wiki/Help:Cite_errors/Cite_error_references_no_text" title="Help:Cite errors/Cite error references no text">help page</a>).</span></span>
</li>
<li id="cite&#95;note-2"><span class="mw-cite-backlink"><b><a href="#cite_ref-2">^</a></b></span> <span class="reference-text"><cite class="citation web cs1"><a rel="nofollow" class="external text" href="https://www.dezeen.com/2017/05/06/minimalist-light-phone-design-used-little-as-possible-mobile-technology/">"The Light Phone: minimalist phone designed to be used as little as possible"</a>. <i>Dezeen</i>. May 6, 2017.</cite><span title="ctx&#95;ver=Z39.88-2004&amp;rft.genre=unknown" class="Z3988"></span></span>
</li>
</ol></div></div>
""".trimIndent()

// Made up, to cover the odd cases.
private val SYNTHETIC_PAGE = """
<div class="mw-content-ltr mw-parser-output" lang="en" dir="ltr"><div class="shortdescription nomobile noexcerpt noprint searchaux" style="display:none">Small domesticated mammal</div>
<!-- a comment with <p>markup</p> in it -->
<div role="note" class="hatnote navigation-not-searchable">For other uses, see <a href="/wiki/Cat_(disambiguation)" class="mw-disambig" title="Cat (disambiguation)">Cat (disambiguation)</a>.</div>
<table class="infobox biota"><tbody><tr><th>Domestic cat</th></tr><tr><td>Infobox text</td></tr></tbody></table>
<p>The <b>cat</b> (<i><b>Felis catus</b></i>) is a <a href="/wiki/Carnivore" title="Carnivore">carnivorous</a> mammal&#160;&amp; pet. It weighs 4&nbsp;kg on the 19<sup>th</sup> day, covers 3&#160;km<sup>2</sup>, drinks H<sub>2</sub>O<sup class="noprint Inline-Template"><i>[citation needed]</i></sup>.<span style="display: none">hidden sort key</span></p>
<p>Links: <a href="/wiki/Help:Contents">help</a>, <a href="/w/index.php?title=Missing_page&amp;action=edit&amp;redlink=1" class="new">red link</a>, <a class="external text" href="https://example.com">outside</a>, <a href="/wiki/Caf%C3%A9_au_lait#History">caf&#233; note</a>, <a href="/wiki/Star_Wars:_Episode_IV">a colon title</a>, <a class="mw-selflink selflink">this page</a>.<br>After a line break.</p>
<p class="mw-empty-elt">
</p>
<div class="mw-heading mw-heading2"><h2 id="Math">Math</h2></div>
<p>Euler wrote <span class="mwe-math-element mwe-math-element-inline"><span class="mwe-math-mathml-a11y" style="display: none;"><math alttext="{\displaystyle e^{i\pi }+1=0}"><semantics><mrow><mi>e</mi></mrow><annotation encoding="application/x-tex">{\displaystyle e^{i\pi }+1=0}</annotation></semantics></math></span><img src="x.svg" class="mwe-math-fallback-image-inline mw-invert" alt="{\displaystyle e^{i\pi }+1=0}"></span> in 1748.</p>
<dl><dd><span class="mwe-math-element mwe-math-element-block"><span class="mwe-math-mathml-a11y" style="display: none;"><math alttext="{\displaystyle x\leq \infty }"></math></span></span></dd></dl>
<div class="mw-heading mw-heading2"><h2 id="Lists">Lists</h2></div>
<ul><li>First <a href="/wiki/Dog" title="Dog">dog</a>
<ul><li>Nested item</li></ul></li>
<li>Second</li></ul>
<ol><li>One</li><li>Two</li></ol>
<blockquote class="templatequote"><p>A quoted line.</p></blockquote>
<div class="mw-heading mw-heading3"><h3 id="Empty">Only a table</h3></div>
<table class="wikitable"><tr><td>cells are skipped</td></tr></table>
<div class="mw-heading mw-heading2"><h2 id="Gallery">Gallery</h2></div>
<ul class="gallery mw-gallery-traditional"><li class="gallerybox">Image caption</li></ul>
<div class="mw-heading mw-heading2"><h2 id="See_also">See also</h2></div>
<div class="div-col"><ul><li><a href="/wiki/Lion" title="Lion">Lion</a></li></ul></div>
<div class="mw-heading mw-heading2"><h2 id="References">References</h2></div>
<div class="reflist"><ol class="references"><li>Some citation</li></ol></div>
<div class="mw-heading mw-heading3"><h3 id="Sources">Sources</h3></div>
<ul><li>A book that should not appear</li></ul>
<p><span class="noviewer" typeof="mw:File"><a href="/wiki/File:Free_logo.svg" class="mw-file-description"><img src="x.png" width="12" height="12"></a></span>&nbsp;This article incorporates text from a <a href="/wiki/Free_content" title="Free content">free content</a> work. Licensed under CC BY-SA 3.0. Text taken from <i>A Report on Cats</i>, FAO.</p>
<p>This article incorporates text from a <a href="/wiki/Free_content">free content</a> work. Licensed under CC BY-SA 3.0. Text taken from <i>A Report on Cats</i>, FAO.</p>
<div class="mw-heading mw-heading2"><h2 id="External_links">External links</h2></div>
<ul><li><a class="external text" href="https://example.org">Official site</a></li></ul>
<div role="navigation" class="navbox" aria-labelledby="Felines"><table class="nowraplinks"><tr><td>Lion · Tiger</td></tr></table></div>
</div>
""".trimIndent()

class ArticleParserTest {

    private val lightPhone = ArticleParser.parse("Light Phone", 66400326, 1368520722, LIGHT_PHONE_SECTION)
    private val synthetic = ArticleParser.parse("Cat", 6678, 1, SYNTHETIC_PAGE)

    private fun Block.linkTargets() = spans.filter { it.kind == SpanKind.Link }.map { it.target }

    private fun Block.spanText(span: TextSpan) = text.substring(span.start, span.end)

    @Test
    fun `real article keeps the section, subheadings and paragraphs`() {
        assertTrue(lightPhone.lead.isEmpty())
        assertEquals(listOf("Light Phone"), lightPhone.sections.map { it.title })
        val blocks = lightPhone.sections.single().blocks
        assertEquals(
            listOf(
                BlockKind.Note, BlockKind.Subheading, BlockKind.Paragraph,
                BlockKind.Subheading, BlockKind.Paragraph, BlockKind.Paragraph,
            ),
            blocks.map { it.kind },
        )
        assertEquals("Main article: Light Phone (1st generation)", blocks[0].text)
        assertEquals(listOf("Light Phone (1st generation)"), blocks[0].linkTargets())
        assertEquals("Release", blocks[1].text)
        assertEquals(3, blocks[1].depth)
        assertTrue(blocks[2].text.startsWith("The Light Phone was announced on May 13, 2015, via Kickstarter,"))
        assertTrue(blocks[2].text.endsWith("initial goal of $200,000."), blocks[2].text)
        assertEquals("This app is no longer accessible.", blocks[5].text.substringAfter("Light Phone. "))
    }

    @Test
    fun `real article links point at article titles`() {
        val paragraph = lightPhone.sections.single().blocks[4]
        val links = paragraph.spans.filter { it.kind == SpanKind.Link }
        assertEquals(listOf("speed dial numbers", "2G", "SIM card", "headphone jack"), links.map { paragraph.spanText(it) })
        assertEquals(listOf("Speed dial", "2G", "SIM card", "Phone connector (audio)"), links.map { it.target })
    }

    @Test
    fun `real article drops citations, captions and styles`() {
        val allText = lightPhone.sections.flatMap { it.blocks }.joinToString("\n") { it.text }
        listOf("[1]", "[2]", "Cite error", "Dezeen", "figcaption", "The Light Phone (1st generation)\n", "hatnote", "not text")
            .forEach { assertFalse(it in allText, "unexpected '$it' in:\n$allText") }
    }

    @Test
    fun `reads the short description and the hatnote before the intro`() {
        assertEquals("Small domesticated mammal", synthetic.description)
        assertEquals(BlockKind.Note, synthetic.lead.first().kind)
        assertEquals(listOf("Cat (disambiguation)"), synthetic.lead.first().linkTargets())
    }

    @Test
    fun `intro text is cleaned up`() {
        val intro = synthetic.lead[1]
        assertEquals(
            "The cat (Felis catus) is a carnivorous mammal & pet. It weighs 4 kg on the 19th day, " +
                "covers 3 km², drinks H₂O.",
            intro.text,
        )
        val bold = intro.spans.filter { it.kind == SpanKind.Bold }.map { intro.spanText(it) }
        assertEquals(listOf("cat", "Felis catus"), bold)
        val italic = intro.spans.filter { it.kind == SpanKind.Italic }.map { intro.spanText(it) }
        assertEquals(listOf("Felis catus"), italic)
        assertEquals(listOf("Carnivore"), intro.linkTargets())
    }

    @Test
    fun `only links to other articles are kept`() {
        val block = synthetic.lead[2]
        assertEquals(
            "Links: help, red link, outside, café note, a colon title, this page.\nAfter a line break.",
            block.text,
        )
        assertEquals(listOf("Café au lait", "Star Wars: Episode IV"), block.linkTargets())
        assertEquals(3, synthetic.lead.size)
    }

    @Test
    fun `formulas become readable text`() {
        val math = synthetic.sections.first { it.title == "Math" }.blocks
        assertEquals("Euler wrote e^{iπ}+1=0 in 1748.", math[0].text.replace("π }", "π}"))
        assertEquals(BlockKind.Indent, math[1].kind)
        assertEquals("x≤∞", math[1].text.replace(" ", ""))
    }

    @Test
    fun `lists keep their nesting and numbering`() {
        val blocks = synthetic.sections.first { it.title == "Lists" }.blocks
        val items = blocks.filter { it.kind == BlockKind.ListItem }
        assertEquals(listOf("First dog", "Nested item", "Second", "One", "Two"), items.map { it.text })
        assertEquals(listOf(0, 1, 0, 0, 0), items.map { it.depth })
        assertEquals(listOf("•", "•", "•", "1.", "2."), items.map { it.marker })
        assertEquals(listOf("Dog"), items[0].linkTargets())
        assertEquals(BlockKind.Quote, blocks.last().kind)
        assertEquals("A quoted line.", blocks.last().text)
    }

    @Test
    fun `empty subheadings, empty sections and reference sections are dropped`() {
        assertEquals(listOf("Math", "Lists", "See also"), synthetic.sections.map { it.title })
        assertFalse(synthetic.sections.flatMap { it.blocks }.any { it.kind == BlockKind.Subheading })
        val seeAlso = synthetic.sections.last().blocks.single()
        assertEquals("Lion", seeAlso.text)
        assertEquals(listOf("Lion"), seeAlso.linkTargets())
        val everything = (synthetic.lead + synthetic.sections.flatMap { it.blocks }).joinToString { it.text }
        listOf("Infobox", "cells are skipped", "Image caption", "citation", "book", "Official site", "Tiger", "hidden")
            .forEach { assertFalse(it in everything, "unexpected '$it'") }
    }

    @Test
    fun `credit notices for imported text are kept from left-out sections`() {
        val notice = synthetic.notices.single()
        assertEquals(BlockKind.Note, notice.kind)
        assertEquals(
            "This article incorporates text from a free content work. Licensed under CC BY-SA 3.0. " +
                "Text taken from A Report on Cats, FAO.",
            notice.text,
        )
        assertEquals(listOf("Free content"), notice.linkTargets())
        assertEquals("free content", notice.spanText(notice.spans.first { it.kind == SpanKind.Link }))
        assertTrue(lightPhone.notices.isEmpty())
    }

    @Test
    fun `other languages keep their own reference sections, and skip their file and category links`() {
        val html = """
            <div class="mw-parser-output"><p>Intro mit <a href="/wiki/Datei:Foto.jpg">Bild</a> und <a href="/wiki/Rhein">Rhein</a>.</p>
            <div class="mw-heading mw-heading2"><h2>Geschichte</h2></div><p>Text.</p>
            <div class="mw-heading mw-heading2"><h2>Literatur</h2></div><ul><li>Ein Buch.</li></ul>
            <div class="mw-heading mw-heading2"><h2>Einzelnachweise</h2></div><ol class="references"><li>Quelle.</li></ol>
            <div class="klappleiste mw-collapsible navileiste navigation-not-searchable center"><div class="klappleiste-kopf">Städte in Deutschland</div></div>
            <div class="hintergrundfarbe1 rahmenfarbe1 navigation-not-searchable normdaten-typ-g">Normdaten (Geografikum): GND</div>
            <div class="mw-heading mw-heading2"><h2>References</h2></div><p>An English heading on German Wikipedia stays.</p></div>
        """.trimIndent()
        val article = ArticleParser.parse("Köln", 1, 1, html, Language.German)

        assertEquals("de", article.language)
        // Everything under Einzelnachweise goes by class, so the section ends up empty.
        assertEquals(listOf("Geschichte", "Literatur", "References"), article.sections.map { it.title })
        assertEquals(listOf("Rhein"), article.lead.single().spans.mapNotNull { it.target })
    }

    @Test
    fun `link targets are decoded`() {
        assertEquals("Café au lait", ArticleParser.linkTarget("/wiki/Caf%C3%A9_au_lait#History"))
        assertEquals("C++", ArticleParser.linkTarget("/wiki/C%2B%2B"))
        assertEquals("AC/DC", ArticleParser.linkTarget("/wiki/AC/DC"))
        assertNull(ArticleParser.linkTarget("/wiki/File:Cat.jpg"))
        assertNull(ArticleParser.linkTarget("/wiki/Category:Cats"))
        assertNull(ArticleParser.linkTarget("#cite_note-1"))
        assertNull(ArticleParser.linkTarget("https://en.wikipedia.org/wiki/Cat"))
    }
}
