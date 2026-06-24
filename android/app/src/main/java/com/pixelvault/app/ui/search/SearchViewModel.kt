package com.pixelvault.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val mode: SearchMode = SearchMode.SEMANTIC
)

enum class SearchMode { SEMANTIC, TAG }

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchApi: SearchApiService
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        _state.value = _state.value.copy(query = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.value = _state.value.copy(results = emptyList(), isSearching = false, hasSearched = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(500)
            _state.value = _state.value.copy(isSearching = true)
            try {
                val results = if (_state.value.mode == SearchMode.SEMANTIC) {
                    searchApi.semanticSearch(query)
                } else {
                    searchApi.tagSearch(query)
                }
                _state.value = _state.value.copy(
                    results = results,
                    isSearching = false,
                    hasSearched = true
                )
            } catch (_: Exception) {
                _state.value = _state.value.copy(isSearching = false, hasSearched = true)
            }
        }
    }

    fun toggleMode() {
        val newMode = if (_state.value.mode == SearchMode.SEMANTIC) SearchMode.TAG else SearchMode.SEMANTIC
        _state.value = _state.value.copy(mode = newMode)
        onQueryChanged(_state.value.query)
    }
}
