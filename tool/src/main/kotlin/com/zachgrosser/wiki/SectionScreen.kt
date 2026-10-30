package com.zachgrosser.wiki

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable

class SectionScreen(
    sealedActivity: SealedLightActivity,
    private val article: Article,
    startIndex: Int,
) : SimpleLightScreen<Unit>(sealedActivity) {

    // Kept on the screen, not in composition, so it survives a trip to a linked article.
    private var index by mutableIntStateOf(startIndex.coerceIn(0, (article.sections.size - 1).coerceAtLeast(0)))

    @Composable
    override fun Content() {
        WikiTheme {
            SectionContent(
                article = article,
                index = index,
                onBack = { goBack() },
                onNext = { index += 1 },
                onOpenArticle = { target -> navigateTo({ ArticleScreen(it, target, Language.fromCode(article.language)) }) },
            )
        }
    }
}

@Composable
internal fun SectionContent(
    article: Article,
    index: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onOpenArticle: (String) -> Unit,
) {
    val section = article.sections.getOrNull(index) ?: return
    val next = article.sections.getOrNull(index + 1)
    val unit = 1f.gridUnitsAsDp()

    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(
            leftButton = backButton(onBack),
            center = LightTopBarCenter.Text(article.title),
        )
        key(index) {
            LightScrollView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = unit),
            ) {
                LightText(
                    text = section.title,
                    variant = LightTextVariant.Heading,
                    modifier = Modifier.padding(bottom = unit * 0.5f),
                )
                ArticleBlocks(section.blocks, onOpenArticle)
                if (next != null) {
                    LightText(
                        text = strings().next(next.title),
                        variant = LightTextVariant.Copy,
                        underline = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .lightClickable(onClick = onNext)
                            .padding(top = unit),
                    )
                }
                ArticleCredit(article, showNotices = false, onOpenArticle = onOpenArticle)
            }
        }
    }
}
