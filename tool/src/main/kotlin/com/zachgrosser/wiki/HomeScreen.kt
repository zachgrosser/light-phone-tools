package com.zachgrosser.wiki

import android.view.KeyEvent
import androidx.compose.foundation.focusable
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
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LocalHapticsEnabled
import com.thelightphone.sdk.ui.defaultKeyboardOptions
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import kotlinx.coroutines.Dispatchers
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

    // The credits screen only shows the first time Wiki opens.
    private val welcomedFile = File(filesDir, "welcomed")
    private val _showSplash = MutableStateFlow(!welcomedFile.exists())
    val showSplash: StateFlow<Boolean> = _showSplash

    private val _savedArticles = MutableStateFlow<List<SavedArticle>>(emptyList())
    val savedArticles: StateFlow<List<SavedArticle>> = _savedArticles

    val query = TextFieldState()

    // Light's keyboard steps aside after the first key from a hardware keyboard or keypad.
    private val _usingHardwareKeyboard = MutableStateFlow(false)
    val usingHardwareKeyboard: StateFlow<Boolean> = _usingHardwareKeyboard

    private val _searches = Channel<String>(Channel.CONFLATED)
    val searches: Flow<String> = _searches.receiveAsFlow()

    private val hardwareTyping = HardwareTyping(
        state = query,
        onSearch = ::search,
        onTyped = { _usingHardwareKeyboard.value = true },
    )

    init {
        viewModelScope.launch { LanguageSetting.load(filesDir) }
        if (_showSplash.value) {
            viewModelScope.launch {
                delay(SPLASH_MILLIS)
                dismissSplash()
            }
        }
    }

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        super.onScreenShow(screen)
        viewModelScope.launch { _savedArticles.value = saved.list() }
    }

    fun search() {
        val text = query.text.toString().trim()
        if (text.isNotEmpty()) _searches.trySend(text)
    }

    fun showOnScreenKeyboard() {
        _usingHardwareKeyboard.value = false
    }

    // Returns false for keys that aren't for typing, so LightOS still gets them.
    fun onHardwareKey(event: KeyEvent): Boolean {
        if (!hardwareTyping.isTypingKey(event)) return false
        dismissSplash()
        return hardwareTyping.onKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent) = onHardwareKey(event)

    override fun onKeyUp(keyCode: Int, event: KeyEvent) = onHardwareKey(event)

    fun dismissSplash() {
        if (!_showSplash.value) return
        _showSplash.value = false
        viewModelScope.launch(Dispatchers.IO) { runCatching { welcomedFile.createNewFile() } }
    }
}

@InitialScreen
class HomeScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, HomeViewModel>(sealedActivity) {

    override val viewModelClass: Class<HomeViewModel>
        get() = HomeViewModel::class.java

    override fun createViewModel() = HomeViewModel(SavedArticles(lightContext.filesDir), lightContext.filesDir)

    // Not sealedActivity.vibrator: Light's build server is on SDK 0.2.0, which doesn't have it.
    private var haptics: HapticFeedback? = null
    private var hapticsEnabled = false
    private val keyboardOptions = MutableStateFlow(defaultKeyboardOptions())

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
        val savedArticles by viewModel.savedArticles.collectAsState()
        val usingHardwareKeyboard by viewModel.usingHardwareKeyboard.collectAsState()

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
            if (showSplash) {
                SplashContent(onContinue = viewModel::dismissSplash)
            } else {
                HomeContent(
                    query = viewModel.query,
                    savedArticles = savedArticles,
                    keyboard = if (usingHardwareKeyboard) null else keyboard,
                    onSearch = viewModel::search,
                    onTapSearchBox = viewModel::showOnScreenKeyboard,
                    onHardwareKey = viewModel::onHardwareKey,
                    onOpenArticle = { saved ->
                        navigateTo({ ArticleScreen(it, saved.title, Language.fromCode(saved.language)) })
                    },
                    onSettings = { navigateTo({ SettingsScreen(it) }) },
                )
            }
        }
    }
}

@Composable
internal fun SplashContent(onContinue: () -> Unit) {
    val unit = 1f.gridUnitsAsDp()
    // Scrolls on short screens like the Light Flip's.
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

// With no keyboard (someone's typing on a hardware one), saved articles get the room and a
// search button takes the bottom.
@Composable
internal fun HomeContent(
    query: TextFieldState,
    savedArticles: List<SavedArticle>,
    keyboard: Lp3KeyboardViewModel<*>?,
    onSearch: () -> Unit,
    onTapSearchBox: () -> Unit,
    onHardwareKey: (KeyEvent) -> Boolean,
    onOpenArticle: (SavedArticle) -> Unit,
    onSettings: () -> Unit,
) {
    val unit = 1f.gridUnitsAsDp()
    val strings = strings()
    val language = LocalLanguage.current
    val focus = remember { FocusRequester() }
    val keyboardShown = keyboard != null

    // Hardware keys only arrive while something here has focus. Light's keyboard takes it when
    // it's up, so grab it back when it goes away.
    LaunchedEffect(keyboardShown) { focus.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // Preview, so hardware typing is handled before Light's keyboard sees it.
            .onPreviewKeyEvent { onHardwareKey(it.nativeKeyEvent) }
            .focusRequester(focus)
            .focusable(),
    ) {
        LightTopBar(
            rightButton = LightBarButton.LightIcon(
                icon = LightIcons.SETTINGS,
                onClick = onSettings,
                contentDescription = strings.settings,
            ),
        )
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
                            text = if (article.language == language.code) article.title else "${article.title} (${article.language})",
                            variant = LightTextVariant.Copy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
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
