package com.zachgrosser.wiki

import kotlinx.serialization.Serializable

/**
 * A Wikipedia article reduced to plain text: the intro ("lead") plus its top-level sections.
 * This is what gets shown on screen and what gets written to disk for offline reading.
 */
@Serializable
data class Article(
    val title: String,
    val pageId: Long,
    val revisionId: Long,
    val description: String? = null,
    val lead: List<Block>,
    val sections: List<Section>,
    /**
     * Credit notices for text the article imported from other free sources ("This article
     * incorporates text from…"). Wikipedia shows these under Sources or References, which Wiki
     * otherwise leaves out, and its terms of use ask reusers to keep them.
     */
    val notices: List<Block> = emptyList(),
)

@Serializable
data class Section(
    val title: String,
    val blocks: List<Block>,
)

/**
 * One paragraph-like piece of text.
 *
 * [depth] is the nesting level for list items and indents, or the heading level (3, 4, …)
 * for [BlockKind.Subheading]. [marker] is the bullet ("•") or number ("2.") for list items.
 */
@Serializable
data class Block(
    val kind: BlockKind,
    val text: String,
    val spans: List<TextSpan> = emptyList(),
    val depth: Int = 0,
    val marker: String? = null,
)

@Serializable
enum class BlockKind { Paragraph, ListItem, Quote, Note, Indent, Subheading }

/** A styled range of [Block.text]. Links carry the title of the article they point to. */
@Serializable
data class TextSpan(
    val start: Int,
    val end: Int,
    val kind: SpanKind,
    val target: String? = null,
)

@Serializable
enum class SpanKind { Link, Bold, Italic }

/** An entry in the list of saved articles. */
@Serializable
data class SavedArticle(
    val title: String,
    val pageId: Long,
    val description: String? = null,
    val aliases: List<String> = emptyList(),
    val savedAtMillis: Long = 0,
)

/** One row in the search results. */
@Serializable
data class SearchResult(
    val title: String,
    val description: String? = null,
)
