package com.zachgrosser.wiki

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val SPLASH_MILLIS = 2_500L

class HomeViewModel(private val saved: SavedArticles) : LightViewModel<Unit>() {

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash

    private val _showAbout = MutableStateFlow(false)
    val showAbout: StateFlow<Boolean> = _showAbout

    private val _savedArticles = MutableStateFlow<List<SavedArticle>>(emptyList())
    val savedArticles: StateFlow<List<SavedArticle>> = _savedArticles

    init {
        viewModelScope.launch {
            delay(SPLASH_MILLIS)
            _showSplash.value = false
        }
    }

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        super.onScreenShow(screen)
        // Something may have been saved or removed while another screen was open.
        viewModelScope.launch { _savedArticles.value = saved.list() }
    }

    fun dismissSplash() {
        _showSplash.value = false
    }

    fun openAbout() {
        _showAbout.value = true
    }

    fun closeAbout() {
        _showAbout.value = false
    }

    override fun onBackPressed(): Boolean {
        if (_showAbout.value) {
            closeAbout()
            return true
        }
        return false
    }
}

@InitialScreen
class HomeScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, HomeViewModel>(sealedActivity) {

    override val viewModelClass: Class<HomeViewModel>
        get() = HomeViewModel::class.java

    override fun createViewModel() = HomeViewModel(SavedArticles(lightContext.filesDir))

    @Composable
    override fun Content() {
        val showSplash by viewModel.showSplash.collectAsState()
        val showAbout by viewModel.showAbout.collectAsState()
        val savedArticles by viewModel.savedArticles.collectAsState()

        WikiTheme {
            when {
                showSplash -> SplashContent(onContinue = viewModel::dismissSplash)
                showAbout -> AboutContent(onBack = viewModel::closeAbout)
                else -> HomeContent(
                    savedArticles = savedArticles,
                    onSearch = { navigateTo({ SearchScreen(it) }) },
                    onOpenArticle = { title -> navigateTo({ ArticleScreen(it, title) }) },
                    onAbout = viewModel::openAbout,
                )
            }
        }
    }
}

/**
 * Shown each time the tool starts: credits Wikipedia and states the license. Tap to skip.
 * Centred on screens tall enough to hold it, and scrollable on shorter ones (like the Light Flip).
 */
@Composable
internal fun SplashContent(onContinue: () -> Unit) {
    val unit = 1f.gridUnitsAsDp()
    BoxWithConstraints(modifier = Modifier.fillMaxSize().lightClickable(onClick = onContinue)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 2f.gridUnitsAsDp(), vertical = unit),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LightText(text = "Wiki", variant = LightTextVariant.Subtitle, align = TextAlign.Center)
            Spacer(Modifier.height(unit * 1.5f))
            LightText(text = Attribution.SOURCE, variant = LightTextVariant.Copy, align = TextAlign.Center)
            Spacer(Modifier.height(unit * 1.5f))
            LightText(text = Attribution.LICENSE, variant = LightTextVariant.Detail, align = TextAlign.Center)
            Spacer(Modifier.height(unit))
            LightText(
                text = Attribution.NOT_AFFILIATED,
                variant = LightTextVariant.Detail,
                lighten = true,
                align = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun HomeContent(
    savedArticles: List<SavedArticle>,
    onSearch: () -> Unit,
    onOpenArticle: (String) -> Unit,
    onAbout: () -> Unit,
) {
    val unit = 1f.gridUnitsAsDp()
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(
            center = LightTopBarCenter.Text("Wiki"),
            rightButton = LightBarButton.Text("ABOUT", onClick = onAbout),
        )
        LightTextField(
            label = "Search",
            value = "",
            placeholder = "Search Wikipedia",
            onClick = onSearch,
            modifier = Modifier.padding(horizontal = unit),
        )
        LightText(
            text = "Saved",
            variant = LightTextVariant.Detail,
            lighten = true,
            modifier = Modifier.padding(start = unit, top = unit * 2f, bottom = unit * 0.5f),
        )
        if (savedArticles.isEmpty()) {
            LightText(
                text = "Tap the star on an article to keep it here for reading offline.",
                variant = LightTextVariant.Detail,
                modifier = Modifier.padding(horizontal = unit),
            )
        } else {
            LightScrollView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = unit),
            ) {
                savedArticles.forEach { article ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .lightClickable { onOpenArticle(article.title) }
                            .padding(vertical = unit * 0.5f),
                    ) {
                        LightText(text = article.title, variant = LightTextVariant.Copy)
                        article.description?.let {
                            LightText(text = it, variant = LightTextVariant.Detail, lighten = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AboutContent(onBack: () -> Unit) {
    val unit = 1f.gridUnitsAsDp()
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(leftButton = backButton(onBack), center = LightTopBarCenter.Text("About"))
        LightScrollView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = unit),
        ) {
            AboutSection(null, "Wiki lets you search and read Wikipedia, the free encyclopedia, on your Light Phone.")
            AboutSection(
                "Source and license",
                "All article text comes from Wikipedia ($WIKI_HOST) and is written by its volunteer " +
                    "contributors. ${Attribution.LICENSE} ${Attribution.LICENSE_URL}\n\n" +
                    "Every article ends with a link to its page on Wikipedia, where you can find the " +
                    "full article, its edit history and its authors.",
            )
            AboutSection(
                "What's left out",
                "To keep things simple, images, tables, infoboxes and citations are not shown.",
            )
            AboutSection(
                "Privacy",
                "Your searches and the articles you open are requested directly from Wikipedia. " +
                    "Saved articles are stored only on this phone and can be read without a " +
                    "connection. Nothing else leaves the phone, and nothing is tracked.",
            )
            AboutSection(
                "Trademark",
                "Wikipedia is a trademark of the Wikimedia Foundation. ${Attribution.NOT_AFFILIATED}",
            )
            Spacer(Modifier.height(unit * 2f))
        }
    }
}

@Composable
private fun AboutSection(title: String?, body: String) {
    val unit = 1f.gridUnitsAsDp()
    title?.let {
        LightText(
            text = it,
            variant = LightTextVariant.Subheading,
            modifier = Modifier.padding(top = unit, bottom = unit * 0.25f),
        )
    }
    LightText(
        text = body,
        variant = LightTextVariant.Paragraph,
        modifier = Modifier.padding(bottom = unit * 0.5f),
    )
}
