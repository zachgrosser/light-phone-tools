package com.zachgrosser.wiki

import kotlinx.serialization.Serializable

// An article as plain text: the intro (lead) and its top-level sections. Saved as-is for offline reading.
@Serializable
data class Article(
    val title: String,
    val pageId: Long,
    val revisionId: Long,
    val description: String? = null,
    val lead: List<Block>,
    val sections: List<Section>,
    // "This article incorporates text from…" credits. Wikimedia's terms ask reusers to keep them.
    val notices: List<Block> = emptyList(),
    // Articles saved before 1.1 have no language and are English.
    val language: String = Language.English.code,
)

@Serializable
data class Section(
    val title: String,
    val blocks: List<Block>,
)

// depth is the nesting level for lists and indents, or the heading level for subheadings.
// marker is a list item's bullet or number.
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

@Serializable
data class TextSpan(
    val start: Int,
    val end: Int,
    val kind: SpanKind,
    val target: String? = null,
)

@Serializable
enum class SpanKind { Link, Bold, Italic }

@Serializable
data class SavedArticle(
    val title: String,
    val pageId: Long,
    val description: String? = null,
    val aliases: List<String> = emptyList(),
    val savedAtMillis: Long = 0,
    val language: String = Language.English.code,
)

@Serializable
data class SearchResult(
    val title: String,
    val description: String? = null,
)
