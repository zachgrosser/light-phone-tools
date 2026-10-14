package com.zachgrosser.wiki

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Previews of every screen at each Light device's screen size, to check layouts without an
// emulator. In Android Studio, open this file and choose "Split" or "Design" (top right).
//
// Light Phone III: 1080 x 1240 pixels, 3.92". Light's own previews use these dp values.
// Light Flip:      480 x 640 pixels, 2.8" (announced July 2026, shipping April 2027). The dp
//                  size assumes roughly 280 dpi; the Light SDK scales text and spacing to the
//                  screen, so the proportions are what matter.

@Preview(name = "Light Phone III", widthDp = 360, heightDp = 413, showBackground = true, backgroundColor = 0xFF000000)
@Preview(name = "Light Flip", widthDp = 274, heightDp = 366, showBackground = true, backgroundColor = 0xFF000000)
annotation class LightDevicePreviews

@LightDevicePreviews
@Composable
private fun SplashPreview() = WikiTheme { SplashContent(onContinue = {}) }

@LightDevicePreviews
@Composable
private fun HomeEmptyPreview() = WikiTheme {
    HomeContent(savedArticles = emptyList(), onSearch = {}, onOpenArticle = {}, onAbout = {})
}

@LightDevicePreviews
@Composable
private fun HomeWithSavedPreview() = WikiTheme {
    HomeContent(savedArticles = SAMPLE_SAVED, onSearch = {}, onOpenArticle = {}, onAbout = {})
}

@LightDevicePreviews
@Composable
private fun ResultsPreview() = WikiTheme {
    ResultsContent(query = "light phone", results = SAMPLE_RESULTS, onBack = {}, onOpen = {})
}

@LightDevicePreviews
@Composable
private fun ArticlePreview() = WikiTheme {
    ArticleContent(
        article = SAMPLE_ARTICLE,
        isSaved = false,
        notice = null,
        onBack = {},
        onToggleSaved = {},
        onOpenSection = {},
        onOpenArticle = {},
    )
}

@LightDevicePreviews
@Composable
private fun SectionPreview() = WikiTheme {
    SectionContent(article = SAMPLE_ARTICLE, index = 0, onBack = {}, onNext = {}, onOpenArticle = {})
}

@LightDevicePreviews
@Composable
private fun ArticleEndPreview() = WikiTheme {
    // The end of an article: its credit, license, changes note and imported-text notices.
    ArticleCredit(SAMPLE_ARTICLE, showNotices = true, onOpenArticle = {})
}

@LightDevicePreviews
@Composable
private fun AboutPreview() = WikiTheme { AboutContent(onBack = {}) }

@LightDevicePreviews
@Composable
private fun LoadingPreview() = WikiTheme { MessageScreen(title = "Light Phone III", message = "Loading…", onBack = {}) }

// --- Sample content (written for these previews, not taken from Wikipedia) -------------------

/** A paragraph block where each of [links] (which must appear in [text]) is a link to an article of that name. */
private fun linked(text: String, vararg links: String, kind: BlockKind = BlockKind.Paragraph) = Block(
    kind = kind,
    text = text,
    spans = links.map { link ->
        val start = text.indexOf(link)
        require(start >= 0) { "'$link' isn't in the sample text" }
        TextSpan(start, start + link.length, SpanKind.Link, link)
    },
)

private val SAMPLE_ARTICLE = Article(
    title = "Light Phone III",
    pageId = 1,
    revisionId = 1,
    description = "Sample article for previews",
    lead = listOf(
        linked("For the earlier models, see Light Phone.", "Light Phone", kind = BlockKind.Note),
        linked(
            "This is sample text for the previews. It shows how an article's introduction reads, " +
                "with links to other articles such as minimalist mobile phone and smartphone, and " +
                "wraps over several lines the way a real paragraph would.",
            "minimalist mobile phone", "smartphone",
        ),
        linked("A second paragraph, with a link to LightOS.", "LightOS"),
    ),
    sections = listOf(
        Section(
            title = "Design",
            blocks = listOf(
                linked("Sections open one at a time. Links like this one to Android are underlined.", "Android"),
                Block(BlockKind.Subheading, "Tools", depth = 3),
                Block(BlockKind.ListItem, "Phone and messages", marker = "•"),
                Block(BlockKind.ListItem, "Maps and directions", marker = "•"),
                Block(BlockKind.ListItem, "Music and podcasts", marker = "•"),
                Block(BlockKind.Quote, "A short quotation appears like this, in italics and indented."),
            ),
        ),
        Section(title = "Software", blocks = listOf(Block(BlockKind.Paragraph, "More text."))),
        Section(title = "Reception", blocks = listOf(Block(BlockKind.Paragraph, "More text."))),
    ),
    notices = listOf(
        linked(
            "This article incorporates text from a free content work. Licensed under CC BY-SA 3.0. " +
                "Text taken from a sample report.",
            "free content",
            kind = BlockKind.Note,
        ),
    ),
)

private val SAMPLE_SAVED = listOf(
    SavedArticle("Light Phone III", 1, "Minimalist mobile phone"),
    SavedArticle("Amsterdam", 2, "Capital of the Netherlands"),
    SavedArticle("Photosynthesis", 3, "Process by which plants make food from light"),
)

private val SAMPLE_RESULTS = listOf(
    SearchResult("Light Phone", "Series of minimalist mobile phones"),
    SearchResult("Light Phone III", "Minimalist mobile phone"),
    SearchResult("Light Phone II", "Minimalist mobile phone"),
    SearchResult("Light", "Electromagnetic radiation visible to the eye"),
)
