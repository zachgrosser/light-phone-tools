package com.zachgrosser.wiki

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightFullscreenModal
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ArticleState {
    data object Loading : ArticleState
    data class Loaded(val article: Article, val isSaved: Boolean) : ArticleState
    data class Failed(val message: String) : ArticleState
}

class ArticleViewModel(
    private val requestedTitle: String,
    private val saved: SavedArticles,
) : LightViewModel<Unit>() {

    private val _state = MutableStateFlow<ArticleState>(ArticleState.Loading)
    val state: StateFlow<ArticleState> = _state

    /** A short confirmation after saving or removing, shown under the title. */
    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice
    private var noticeJob: Job? = null

    init {
        viewModelScope.launch {
            // Saved articles open straight from the phone, with or without a connection.
            saved.find(requestedTitle)?.let { article ->
                _state.value = ArticleState.Loaded(article, isSaved = true)
                return@launch
            }
            WikiApi.fetchArticle(requestedTitle).fold(
                onSuccess = { article ->
                    _state.value = ArticleState.Loaded(article, isSaved = saved.isSaved(article.pageId))
                },
                onFailure = { error ->
                    _state.value = ArticleState.Failed(
                        when (error) {
                            is ArticleNotFoundException ->
                                "Wikipedia doesn't have an article called “$requestedTitle”."
                            else -> errorMessage(error) + " Saved articles can still be read offline."
                        },
                    )
                },
            )
        }
    }

    fun toggleSaved() {
        val loaded = _state.value as? ArticleState.Loaded ?: return
        viewModelScope.launch {
            val nowSaved = if (loaded.isSaved) {
                saved.remove(loaded.article.pageId)
                false
            } else {
                saved.save(loaded.article, aliases = listOf(requestedTitle))
                true
            }
            _state.value = loaded.copy(isSaved = nowSaved)
            showNotice(if (nowSaved) "Saved for reading offline." else "Removed from saved.")
        }
    }

    private fun showNotice(message: String) {
        noticeJob?.cancel()
        _notice.value = message
        noticeJob = viewModelScope.launch {
            delay(2_500)
            _notice.value = null
        }
    }
}

/** An article's title, intro and list of sections. */
class ArticleScreen(
    sealedActivity: SealedLightActivity,
    private val title: String,
) : LightScreen<Unit, ArticleViewModel>(sealedActivity) {

    override val viewModelClass: Class<ArticleViewModel>
        get() = ArticleViewModel::class.java

    override fun createViewModel() = ArticleViewModel(title, SavedArticles(lightContext.filesDir))

    private fun openArticle(target: String) {
        navigateTo({ ArticleScreen(it, target) })
    }

    @Composable
    override fun Content() {
        val state by viewModel.state.collectAsState()
        val notice by viewModel.notice.collectAsState()

        WikiTheme {
            when (val current = state) {
                ArticleState.Loading -> MessageScreen(
                    title = title,
                    message = "Loading…",
                    onBack = { goBack() },
                )

                is ArticleState.Failed -> LightFullscreenModal(
                    message = current.message,
                    onClose = { goBack() },
                )

                is ArticleState.Loaded -> ArticleContent(
                    article = current.article,
                    isSaved = current.isSaved,
                    notice = notice,
                    onBack = { goBack() },
                    onToggleSaved = viewModel::toggleSaved,
                    onOpenSection = { index ->
                        navigateTo({ SectionScreen(it, current.article, index) })
                    },
                    onOpenArticle = ::openArticle,
                )
            }
        }
    }
}

@Composable
internal fun ArticleContent(
    article: Article,
    isSaved: Boolean,
    notice: String?,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    onOpenSection: (Int) -> Unit,
    onOpenArticle: (String) -> Unit,
) {
    val unit = 1f.gridUnitsAsDp()
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(
            leftButton = backButton(onBack),
            center = LightTopBarCenter.Text(article.title),
            rightButton = LightBarButton.LightIcon(
                icon = if (isSaved) LightIcons.STAR else LightIcons.STAR_OUTLINE,
                onClick = onToggleSaved,
                contentDescription = if (isSaved) "Remove from saved" else "Save for reading offline",
            ),
        )
        LightScrollView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = unit),
        ) {
            LightText(text = article.title, variant = LightTextVariant.Heading)
            article.description?.let {
                LightText(text = it, variant = LightTextVariant.Detail, lighten = true)
            }
            LightText(
                text = notice ?: "",
                variant = LightTextVariant.Detail,
                modifier = Modifier.padding(bottom = unit * 0.5f),
            )
            ArticleBlocks(article.lead, onOpenArticle)

            if (article.sections.isNotEmpty()) {
                LightText(
                    text = "Contents",
                    variant = LightTextVariant.Detail,
                    lighten = true,
                    modifier = Modifier.padding(top = unit, bottom = unit * 0.25f),
                )
                article.sections.forEachIndexed { index, section ->
                    LightText(
                        text = section.title,
                        variant = LightTextVariant.Copy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .lightClickable { onOpenSection(index) }
                            .padding(vertical = unit * 0.4f),
                    )
                }
            }
            ArticleCredit(article, showNotices = true, onOpenArticle = onOpenArticle)
        }
    }
}
