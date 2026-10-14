package com.zachgrosser.wiki

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.rememberKeyboardOptions
import com.thelightphone.sdk.ui.LightFullscreenModal
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextInputEditor
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SearchMode {
    data object Typing : SearchMode
    data object Searching : SearchMode
    data class Results(val query: String, val results: List<SearchResult>) : SearchMode
}

data class SearchUiState(
    val mode: SearchMode = SearchMode.Typing,
    /** Bumped each time the keyboard opens, so it starts fresh. */
    val typingSession: Int = 0,
    val error: String? = null,
)

class SearchViewModel : LightViewModel<Unit>() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state

    /** The last thing searched for, so the text is still there when coming back. */
    var lastQuery: String = ""
        private set

    private var searchJob: Job? = null

    fun search(input: CharSequence) {
        val query = input.toString().trim()
        if (query.isEmpty()) {
            _state.update { it.copy(error = "Type something to search for.") }
            return
        }
        lastQuery = query
        _state.update { it.copy(mode = SearchMode.Searching, error = null) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            WikiApi.search(query).fold(
                onSuccess = { results ->
                    _state.update { it.copy(mode = SearchMode.Results(query, results)) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            mode = SearchMode.Typing,
                            typingSession = it.typingSession + 1,
                            error = errorMessage(error),
                        )
                    }
                },
            )
        }
    }

    fun backToTyping() {
        searchJob?.cancel()
        _state.update { it.copy(mode = SearchMode.Typing, typingSession = it.typingSession + 1) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    override fun onBackPressed(): Boolean {
        if (_state.value.mode != SearchMode.Typing) {
            backToTyping()
            return true
        }
        return false
    }
}

class SearchScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, SearchViewModel>(sealedActivity) {

    override val viewModelClass: Class<SearchViewModel>
        get() = SearchViewModel::class.java

    override fun createViewModel() = SearchViewModel()

    @Composable
    override fun Content() {
        val state by viewModel.state.collectAsState()
        val textState = rememberTextFieldState(viewModel.lastQuery)
        val keyboardOptions = rememberKeyboardOptions()

        WikiTheme {
            when (val mode = state.mode) {
                SearchMode.Typing -> LightTextInputEditor(
                    title = "Search Wikipedia",
                    state = textState,
                    onSubmit = viewModel::search,
                    onBack = { goBack() },
                    keyboardOptionsFlow = keyboardOptions,
                    submitIcon = LightIcons.SEARCH,
                    singleLine = true,
                    initialCaps = true,
                    editorKey = state.typingSession,
                    modifier = Modifier.fillMaxSize(),
                )

                SearchMode.Searching -> MessageScreen(
                    title = "Search",
                    message = "Searching…",
                    onBack = viewModel::backToTyping,
                )

                is SearchMode.Results -> ResultsContent(
                    query = mode.query,
                    results = mode.results,
                    onBack = viewModel::backToTyping,
                    onOpen = { title -> navigateTo({ ArticleScreen(it, title) }) },
                )
            }

            state.error?.let { message ->
                LightFullscreenModal(message = message, onClose = viewModel::dismissError)
            }
        }
    }
}

@Composable
internal fun ResultsContent(
    query: String,
    results: List<SearchResult>,
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val unit = 1f.gridUnitsAsDp()
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(leftButton = backButton(onBack), center = LightTopBarCenter.Text(query))
        if (results.isEmpty()) {
            LightText(
                text = "No articles found for “$query”.",
                variant = LightTextVariant.Copy,
                modifier = Modifier.padding(horizontal = unit, vertical = unit),
            )
            return@Column
        }
        LightScrollView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = unit),
        ) {
            results.forEach { result ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .lightClickable { onOpen(result.title) }
                        .padding(vertical = unit * 0.5f),
                ) {
                    LightText(text = result.title, variant = LightTextVariant.Copy)
                    result.description?.let {
                        LightText(text = it, variant = LightTextVariant.Detail, lighten = true)
                    }
                }
            }
        }
    }
}
