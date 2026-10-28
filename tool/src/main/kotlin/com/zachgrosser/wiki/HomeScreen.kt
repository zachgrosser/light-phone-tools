package com.zachgrosser.wiki

import android.view.KeyEvent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.thelightphone.lp3Keyboard.ui.KeyboardOptions
import com.thelightphone.lp3Keyboard.ui.LayoutOptions
import com.thelightphone.lp3Keyboard.ui.viewmodel.EnQwertyLp3KeyboardViewModel
import com.thelightphone.lp3Keyboard.ui.viewmodel.Lp3KeyboardViewModel
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.rememberKeyboardOptions
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.LocalHapticsEnabled
import com.thelightphone.sdk.ui.defaultKeyboardOptions
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

private const val SPLASH_MILLIS = 2_500L
private const val KEYBOARD_KEY = "wiki-search-keyboard"

class HomeViewModel(
    private val saved: SavedArticles,
    private val filesDir: File,
) : LightViewModel<Unit>() {

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash

    private val _showAbout = MutableStateFlow(false)
    val showAbout: StateFlow<Boolean> = _showAbout

    /** The list of languages, opened from About. */
    private val _showLanguages = MutableStateFlow(false)
    val showLanguages: StateFlow<Boolean> = _showLanguages

    private val _savedArticles = MutableStateFlow<List<SavedArticle>>(emptyList())
    val savedArticles: StateFlow<List<SavedArticle>> = _savedArticles

    /** What's typed in the search box. It stays while results are open, so it's there on return. */
    val query = TextFieldState()

    /**
     * True once someone types on a hardware keyboard or keypad. The on-screen keyboard then steps
     * aside to leave more room, until the search box is tapped.
     */
    private val _usingHardwareKeyboard = MutableStateFlow(false)
    val usingHardwareKeyboard: StateFlow<Boolean> = _usingHardwareKeyboard

    private val _searches = Channel<String>(Channel.CONFLATED)

    /** Searches to run, one per submit. */
    val searches: Flow<String> = _searches.receiveAsFlow()

    private val hardwareTyping = HardwareTyping(
        state = query,
        onSearch = ::search,
        onTyped = { _usingHardwareKeyboard.value = true },
    )

    init {
        // Read during the start-up screen, which is in English whatever the language.
        viewModelScope.launch { LanguageSetting.load(filesDir) }
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

    fun search() {
        val text = query.text.toString().trim()
        if (text.isNotEmpty()) _searches.trySend(text)
    }

    fun showOnScreenKeyboard() {
        _usingHardwareKeyboard.value = false
    }

    /**
     * A key on a hardware keyboard or keypad. Typing keys go into the search box, and also skip
     * the start-up screen. Returns false for keys that aren't for typing, so LightOS can use them.
     */
    fun onHardwareKey(event: KeyEvent): Boolean {
        if (_showAbout.value || !hardwareTyping.isTypingKey(event)) return false
        _showSplash.value = false
        return hardwareTyping.onKeyEvent(event)
    }

    // Keys that nothing on screen took (for example during the start-up screen) arrive here.
    override fun onKeyDown(keyCode: Int, event: KeyEvent) = onHardwareKey(event)

    override fun onKeyUp(keyCode: Int, event: KeyEvent) = onHardwareKey(event)

    fun dismissSplash() {
        _showSplash.value = false
    }

    fun openAbout() {
        _showAbout.value = true
    }

    fun closeAbout() {
        _showAbout.value = false
    }

    fun openLanguages() {
        _showLanguages.value = true
    }

    fun closeLanguages() {
        _showLanguages.value = false
    }

    /** Switches Wiki's text and the Wikipedia it searches, then goes back to About, now in that language. */
    fun chooseLanguage(language: Language) {
        viewModelScope.launch { LanguageSetting.choose(filesDir, language) }
        closeLanguages()
    }

    override fun onBackPressed(): Boolean {
        if (_showLanguages.value) {
            closeLanguages()
            return true
        }
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

    override fun createViewModel() = HomeViewModel(SavedArticles(lightContext.filesDir), lightContext.filesDir)

    // Compose's haptics rather than sealedActivity.vibrator: Light's build server compiles against
    // an older SDK (v0.2.0) that doesn't have the vibrator yet.
    private var haptics: HapticFeedback? = null
    private var hapticsEnabled = false
    private val keyboardOptions = MutableStateFlow(defaultKeyboardOptions())

    /** Light's on-screen keyboard. Kept with this screen, so it's cleaned up when the screen closes. */
    private val keyboard: Lp3KeyboardViewModel<*> by lazy {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                EnQwertyLp3KeyboardViewModel<Unit>(
                    passedCallback = SearchKeyboardCallback(
                        state = viewModel.query,
                        onSearch = viewModel::search,
                        onHaptic = { if (hapticsEnabled) haptics?.performHapticFeedback(HapticFeedbackType.KeyboardTap) },
                    ),
                    optionsForLayout = { layout -> LayoutOptions(!layout.isRootLayout) },
                    keyboardOptionsFlow = keyboardOptions,
                ).apply { setCapsMode(true) } as T
        }
        ViewModelProvider(this, factory)[KEYBOARD_KEY, EnQwertyLp3KeyboardViewModel::class.java]
    }

    @Composable
    override fun Content() {
        val showSplash by viewModel.showSplash.collectAsState()
        val showAbout by viewModel.showAbout.collectAsState()
        val showLanguages by viewModel.showLanguages.collectAsState()
        val savedArticles by viewModel.savedArticles.collectAsState()
        val usingHardwareKeyboard by viewModel.usingHardwareKeyboard.collectAsState()

        // The user's keyboard settings from LightOS (emoji, key animation and so on).
        val options: KeyboardOptions by rememberKeyboardOptions().collectAsState()
        val userHapticsEnabled = LocalHapticsEnabled.current
        val hapticFeedback = LocalHapticFeedback.current
        SideEffect {
            keyboardOptions.value = options
            hapticsEnabled = userHapticsEnabled
            haptics = hapticFeedback
        }

        LaunchedEffect(Unit) {
            viewModel.searches.collect { query -> navigateTo({ SearchScreen(it, query, LanguageSetting.current.value) }) }
        }

        WikiTheme {
            when {
                showSplash -> SplashContent(onContinue = viewModel::dismissSplash)
                showLanguages -> LanguagesContent(onBack = viewModel::closeLanguages, onChoose = viewModel::chooseLanguage)
                showAbout -> AboutContent(onBack = viewModel::closeAbout, onLanguage = viewModel::openLanguages)
                else -> HomeContent(
                    query = viewModel.query,
                    savedArticles = savedArticles,
                    keyboard = if (usingHardwareKeyboard) null else keyboard,
                    onSearch = viewModel::search,
                    onTapSearchBox = viewModel::showOnScreenKeyboard,
                    onHardwareKey = viewModel::onHardwareKey,
                    onOpenArticle = { saved ->
                        navigateTo({ ArticleScreen(it, saved.title, Language.fromCode(saved.language)) })
                    },
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
            LightText(text = Attribution.SPLASH_SOURCE, variant = LightTextVariant.Copy, align = TextAlign.Center)
            Spacer(Modifier.height(unit * 1.5f))
            LightText(text = Attribution.SPLASH_LICENSE, variant = LightTextVariant.Detail, align = TextAlign.Center)
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

/**
 * The search box, ready to type into, with saved articles below it and Light's on-screen
 * [keyboard] at the bottom. With no [keyboard] (someone is typing on a hardware keyboard or
 * keypad), the saved list gets the room and a search button sits at the bottom instead.
 */
@Composable
internal fun HomeContent(
    query: TextFieldState,
    savedArticles: List<SavedArticle>,
    keyboard: Lp3KeyboardViewModel<*>?,
    onSearch: () -> Unit,
    onTapSearchBox: () -> Unit,
    onHardwareKey: (KeyEvent) -> Boolean,
    onOpenArticle: (SavedArticle) -> Unit,
    onAbout: () -> Unit,
) {
    val unit = 1f.gridUnitsAsDp()
    val strings = strings()
    val language = LocalLanguage.current
    val focus = remember { FocusRequester() }
    val keyboardShown = keyboard != null

    // Hardware keys reach the screen only while something on it has focus. Light's keyboard takes
    // focus when it appears; this takes it whenever the keyboard goes away.
    LaunchedEffect(keyboardShown) { focus.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // "Preview" sees each key before Light's keyboard does, so typing on a hardware
            // keyboard goes through HardwareTyping whether or not the on-screen keyboard is up.
            .onPreviewKeyEvent { onHardwareKey(it.nativeKeyEvent) }
            .focusRequester(focus)
            .focusable(),
    ) {
        LightTopBar(rightButton = LightBarButton.Text(strings.aboutButton, onClick = onAbout))
        SearchBox(
            state = query,
            onTap = onTapSearchBox,
            modifier = Modifier.padding(horizontal = unit),
        )
        LightText(
            text = strings.saved,
            variant = LightTextVariant.Detail,
            lighten = true,
            modifier = Modifier.padding(start = unit, top = unit * 1.25f, bottom = unit * 0.25f),
        )
        if (savedArticles.isEmpty()) {
            LightText(
                text = strings.savedHint,
                variant = LightTextVariant.Detail,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = unit),
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
                            .lightClickable { onOpenArticle(article) }
                            .padding(vertical = unit * 0.4f),
                    ) {
                        LightText(
                            // Articles from another language's Wikipedia say which, as in "Berlin (de)".
                            text = if (article.language == language.code) article.title else "${article.title} (${article.language})",
                            variant = LightTextVariant.Copy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        // With the keyboard up there's only room for titles.
                        if (!keyboardShown) {
                            article.description?.let {
                                LightText(text = it, variant = LightTextVariant.Detail, lighten = true)
                            }
                        }
                    }
                }
            }
        }
        if (keyboard != null) {
            SearchKeyboard(keyboard, onSearch)
        } else {
            LightBottomBar(
                items = listOf(searchButton(onSearch)),
                modifier = Modifier.padding(bottom = unit),
            )
        }
    }
}

/**
 * What Wiki is, the language setting, and every credit and policy. Only the intro and the
 * language setting are translated; the rest stays in English (see Language).
 */
@Composable
internal fun AboutContent(onBack: () -> Unit, onLanguage: () -> Unit) {
    val unit = 1f.gridUnitsAsDp()
    val strings = strings()
    val language = LocalLanguage.current
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(leftButton = backButton(onBack), center = LightTopBarCenter.Text(strings.about))
        LightScrollView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = unit),
        ) {
            AboutSection(null, strings.aboutIntro)
            LanguageRow(language, onClick = onLanguage)
            AboutSection(
                "Where articles come from",
                "Every article is written by Wikipedia's volunteer contributors. Wiki downloads it " +
                    "from Wikipedia (${language.host}), which is run by the Wikimedia Foundation, when you open it.\n\n" +
                    "To fit this screen, Wiki leaves out images, tables, infoboxes, citations and some " +
                    "formatting, and only links to other articles. The end of each article gives its " +
                    "address on Wikipedia, where you can see the full version and everyone who wrote it.",
            )
            AboutSection(
                "License",
                "Wikipedia's text is available under the ${Attribution.LICENSE_NAME} " +
                    "(${Attribution.LICENSE_SHORT}): ${Attribution.LICENSE_URL}\n\n" +
                    "You may share and adapt it if you credit Wikipedia's contributors and share your " +
                    "changes under the same license. When an article includes text from another free " +
                    "source, Wiki shows the article's credit for it. Short quotations from other works " +
                    "belong to their authors.",
            )
            AboutSection(
                "Accuracy",
                "Anyone can edit Wikipedia, so an article may be incomplete, out of date or wrong. " +
                    "Check anything important elsewhere. Wikipedia's disclaimer: ${Attribution.DISCLAIMER_URL}\n\n" +
                    "A saved article is a copy from the day you saved it. For the latest version, " +
                    "remove the star, go back, and open the article again.",
            )
            AboutSection(
                "Privacy",
                "When you search or open an article, Wiki asks Wikipedia for it directly. The " +
                    "Wikimedia Foundation receives what you searched for or opened and your phone's " +
                    "internet (IP) address, as with any website. Its privacy policy applies: " +
                    "${Attribution.PRIVACY_POLICY_URL}\n\n" +
                    "Wiki has no ads, analytics or tracking and sends nothing anywhere else. Saved " +
                    "articles stay on this phone.",
            )
            AboutSection("Trademark", Attribution.TRADEMARK_NOTICE)
            AboutSection(
                "Open source",
                "Wiki is free and open source under the MIT License: ${Attribution.SOURCE_CODE_URL}\n\n" +
                    "To report a problem or suggest a change: ${Attribution.ISSUES_URL}\n\n" +
                    "Version ${BuildConfig.VERSION_NAME}",
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

/**
 * A settings row, like LightOS's own: "Language" with the current one below it, and Light's
 * forward arrow to show that tapping opens a list.
 */
@Composable
private fun LanguageRow(language: Language, onClick: () -> Unit) {
    val unit = 1f.gridUnitsAsDp()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .lightClickable(onClick = onClick)
            .padding(top = unit * 0.5f, bottom = unit * 0.5f, end = unit),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            LightText(text = strings().language, variant = LightTextVariant.Detail, lighten = true)
            LightText(text = language.nativeName, variant = LightTextVariant.Copy)
        }
        LightIcon(LightIcons.ARROW_RIGHT, size = 1.5f, contentDescription = null)
    }
}

/**
 * The languages Wiki can be used in, each in its own name with its English name underneath (so
 * anyone who picked one by accident can find their way back). Light's select icons mark the
 * current one.
 */
@Composable
internal fun LanguagesContent(onBack: () -> Unit, onChoose: (Language) -> Unit) {
    val unit = 1f.gridUnitsAsDp()
    val current = LocalLanguage.current
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(leftButton = backButton(onBack), center = LightTopBarCenter.Text(strings().language))
        LightScrollView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = unit),
        ) {
            Language.entries.forEach { language ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .lightClickable { onChoose(language) }
                        .padding(vertical = unit * 0.4f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LightIcon(
                        if (language == current) LightIcons.SELECT_ON else LightIcons.SELECT_OFF,
                        size = 1.25f,
                        contentDescription = null,
                        modifier = Modifier.padding(end = unit * 0.75f),
                    )
                    Column {
                        LightText(text = language.nativeName, variant = LightTextVariant.Copy)
                        if (language.englishName != language.nativeName) {
                            LightText(text = language.englishName, variant = LightTextVariant.Detail, lighten = true)
                        }
                    }
                }
            }
        }
    }
}
