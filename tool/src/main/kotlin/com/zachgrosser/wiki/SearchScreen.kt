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
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SearchState {
    data object Searching : SearchState
    data class Results(val results: List<SearchResult>) : SearchState
    data class Failed(val error: Throwable) : SearchState
}

class SearchViewModel(language: Language, query: String) : LightViewModel<Unit>() {

    private val _state = MutableStateFlow<SearchState>(SearchState.Searching)
    val state: StateFlow<SearchState> = _state

    init {
        viewModelScope.launch {
            _state.value = WikiApi.search(language, query).fold(
                onSuccess = { SearchState.Results(it) },
                onFailure = { SearchState.Failed(it) },
            )
        }
    }
}

class SearchScreen(
    sealedActivity: SealedLightActivity,
    private val query: String,
    private val language: Language,
) : LightScreen<Unit, SearchViewModel>(sealedActivity) {

    override val viewModelClass: Class<SearchViewModel>
        get() = SearchViewModel::class.java

    override fun createViewModel() = SearchViewModel(language, query)

    @Composable
    override fun Content() {
        val state by viewModel.state.collectAsState()

        WikiTheme {
            when (val current = state) {
                SearchState.Searching -> MessageScreen(title = query, message = strings().searching, onBack = { goBack() })
                is SearchState.Failed -> MessageScreen(
                    title = query,
                    message = errorMessage(current.error, strings()),
                    onBack = { goBack() },
                )
                is SearchState.Results -> ResultsContent(
                    query = query,
                    results = current.results,
                    onBack = { goBack() },
                    onOpen = { title -> navigateTo({ ArticleScreen(it, title, language) }) },
                )
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
                text = strings().noResults(query),
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
