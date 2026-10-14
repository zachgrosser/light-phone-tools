package com.zachgrosser.wiki

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.isSpecified
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.designVerticalPxToSp
import com.thelightphone.sdk.ui.gridUnitsAsDp

/** Applies the user's LightOS theme (light or dark) and fills the screen with its background. */
@Composable
fun WikiTheme(content: @Composable () -> Unit) {
    val colors by LightThemeController.colors.collectAsState()
    LightTheme(colors = colors) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightThemeTokens.colors.background),
        ) {
            content()
        }
    }
}

fun backButton(onClick: () -> Unit) = LightBarButton.LightIcon(
    icon = LightIcons.BACK,
    onClick = onClick,
    contentDescription = "Back",
)

/** A top bar plus a centred message, used while loading. */
@Composable
fun MessageScreen(title: String, message: String, onBack: (() -> Unit)? = null) {
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(
            leftButton = onBack?.let { backButton(it) },
            center = LightTopBarCenter.Text(title),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 1f.gridUnitsAsDp()),
            contentAlignment = Alignment.Center,
        ) {
            LightText(text = message, variant = LightTextVariant.Copy, align = TextAlign.Center)
        }
    }
}

/** Body text: the theme's paragraph style with a little more room between lines for reading. */
@Composable
fun readingStyle(): TextStyle = scaled(LightThemeTokens.typography.paragraph, lineHeightMultiplier = 1.4f)

@Composable
fun noteStyle(): TextStyle = scaled(LightThemeTokens.typography.detail, lineHeightMultiplier = 1.45f)

/** One of Light's text styles, scaled to the screen exactly as LightText does. */
@Composable
fun lightStyle(base: TextStyle): TextStyle = base.copy(
    fontSize = base.fontSize.value.designVerticalPxToSp(),
    lineHeight = if (base.lineHeight.isSpecified) base.lineHeight.value.designVerticalPxToSp() else base.lineHeight,
    letterSpacing = if (base.letterSpacing.isSpecified) {
        base.letterSpacing.value.designVerticalPxToSp()
    } else {
        base.letterSpacing
    },
)

/** Light's type sizes are in "design pixels" and need scaling to the screen, like LightText does. */
@Composable
private fun scaled(base: TextStyle, lineHeightMultiplier: Float): TextStyle = base.copy(
    fontSize = base.fontSize.value.designVerticalPxToSp(),
    lineHeight = (base.fontSize.value * lineHeightMultiplier).designVerticalPxToSp(),
    letterSpacing = if (base.letterSpacing.isSpecified) {
        base.letterSpacing.value.designVerticalPxToSp()
    } else {
        base.letterSpacing
    },
)

/** Draws a list of article blocks. Tapping a link calls [onOpenArticle] with its title. */
@Composable
fun ArticleBlocks(blocks: List<Block>, onOpenArticle: (String) -> Unit) {
    blocks.forEach { block -> BlockView(block, onOpenArticle) }
}

@Composable
private fun BlockView(block: Block, onOpenArticle: (String) -> Unit) {
    val unit = 1f.gridUnitsAsDp()
    val colors = LightThemeTokens.colors
    when (block.kind) {
        BlockKind.Subheading -> LightText(
            text = block.text,
            variant = if (block.depth <= 3) LightTextVariant.Subheading else LightTextVariant.Copy,
            modifier = Modifier.padding(top = unit, bottom = 0.5f.gridUnitsAsDp()),
        )

        BlockKind.ListItem -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = unit * block.depth, bottom = 0.4f.gridUnitsAsDp()),
        ) {
            Text(
                text = block.marker ?: "•",
                style = readingStyle(),
                color = colors.content,
                modifier = Modifier.width(unit * 1.25f),
            )
            LinkedText(block, readingStyle(), colors.content, onOpenArticle, Modifier.weight(1f))
        }

        BlockKind.Note -> LinkedText(
            block = block,
            style = noteStyle().copy(fontStyle = FontStyle.Italic),
            color = colors.contentSecondary,
            onOpenArticle = onOpenArticle,
            modifier = Modifier.padding(bottom = 0.75f.gridUnitsAsDp()),
        )

        BlockKind.Quote -> LinkedText(
            block = block,
            style = readingStyle().copy(fontStyle = FontStyle.Italic),
            color = colors.content,
            onOpenArticle = onOpenArticle,
            modifier = Modifier.padding(start = unit, bottom = 0.75f.gridUnitsAsDp()),
        )

        BlockKind.Indent -> LinkedText(
            block = block,
            style = readingStyle(),
            color = colors.content,
            onOpenArticle = onOpenArticle,
            modifier = Modifier.padding(start = unit * (block.depth + 1), bottom = 0.5f.gridUnitsAsDp()),
        )

        BlockKind.Paragraph -> LinkedText(
            block = block,
            style = readingStyle(),
            color = colors.content,
            onOpenArticle = onOpenArticle,
            modifier = Modifier.padding(bottom = 0.75f.gridUnitsAsDp()),
        )
    }
}

@Composable
private fun LinkedText(
    block: Block,
    style: TextStyle,
    color: Color,
    onOpenArticle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnOpen by rememberUpdatedState(onOpenArticle)
    val text = remember(block) { block.toAnnotatedString { target -> currentOnOpen(target) } }
    Text(text = text, style = style, color = color, modifier = modifier.fillMaxWidth())
}

private val LINK_STYLES = TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline))

private fun Block.toAnnotatedString(onOpen: (String) -> Unit): AnnotatedString = buildAnnotatedString {
    append(text)
    for (span in spans) {
        val start = span.start.coerceIn(0, text.length)
        val end = span.end.coerceIn(start, text.length)
        if (start == end) continue
        when (span.kind) {
            SpanKind.Bold -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
            SpanKind.Italic -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
            SpanKind.Link -> {
                val target = span.target ?: continue
                addLink(
                    LinkAnnotation.Clickable(
                        tag = target,
                        styles = LINK_STYLES,
                        linkInteractionListener = { onOpen(target) },
                    ),
                    start,
                    end,
                )
            }
        }
    }
}

/**
 * The credit required by CC BY-SA, shown at the end of an article and of each section. With
 * [showNotices], also the article's own credit notices for text it imported from other sources.
 */
@Composable
fun ArticleCredit(article: Article, showNotices: Boolean, onOpenArticle: (String) -> Unit) {
    val unit = 1f.gridUnitsAsDp()
    Column(modifier = Modifier.padding(top = unit * 1.5f, bottom = unit * 2f)) {
        LightText(
            text = Attribution.forArticle(article.title),
            variant = LightTextVariant.Detail,
            lighten = true,
        )
        if (showNotices && article.notices.isNotEmpty()) {
            LightText(
                text = "This article also credits:",
                variant = LightTextVariant.Detail,
                lighten = true,
                modifier = Modifier.padding(top = unit * 0.75f, bottom = unit * 0.25f),
            )
            ArticleBlocks(article.notices, onOpenArticle)
        }
    }
}
