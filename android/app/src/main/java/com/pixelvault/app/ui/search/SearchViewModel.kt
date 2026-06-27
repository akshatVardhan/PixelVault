package com.pixelvault.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.PhotoEntity
import com.pixelvault.app.data.local.TagDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val query: String = "",
    val results: List<PhotoEntity> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val mode: SearchMode = SearchMode.TAGS,
    val popularTags: List<String> = listOf("sunset", "portrait", "travel", "food", "nature", "city", "beach", "night", "architecture", "wildlife")
)

enum class SearchMode { TAGS, SCENES, PEOPLE }

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val photoDao: PhotoDao,
    private val tagDao: TagDao
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
                    SearchMode.TAGS -> searchTags(query)
                    SearchMode.SCENES -> searchScenes(query)
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

    private suspend fun searchTags(query: String): List<PhotoEntity> {
        val photoIds = tagDao.searchPhotoIdsByLabel(query)
        return photoIds.mapNotNull { photoDao.getPhotoById(it) }
    }

    private suspend fun searchScenes(query: String): List<PhotoEntity> {
        val photoIds = tagDao.searchPhotoIdsByScene(query)
        return photoIds.mapNotNull { photoDao.getPhotoById(it) }
    }

    fun setMode(mode: SearchMode) {
        _state.value = _state.value.copy(mode = mode)
        onQueryChanged(_state.value.query)
    }
}
