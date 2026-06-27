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
    val mode: SearchMode = SearchMode.TAGS,
    val popularTags: List<String> = listOf("sunset", "portrait", "travel", "food", "nature", "city", "beach", "night", "architecture", "wildlife")
)

enum class SearchMode { TAGS, SCENES, PEOPLE }

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
                val results = when (_state.value.mode) {
                    SearchMode.TAGS -> searchApi.tagSearch(query)
                    SearchMode.SCENES -> searchApi.tagSearch(query)
                    SearchMode.PEOPLE -> emptyList()
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

    fun setMode(mode: SearchMode) {
        _state.value = _state.value.copy(mode = mode)
        onQueryChanged(_state.value.query)
    }
}
