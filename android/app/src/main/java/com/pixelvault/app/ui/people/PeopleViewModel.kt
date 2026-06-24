package com.pixelvault.app.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvault.app.data.remote.ApiService
import com.pixelvault.app.data.remote.ClusterDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PeopleState(
    val clusters: List<ClusterDto> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(PeopleState())
    val state: StateFlow<PeopleState> = _state

    init {
        loadClusters()
    }

    fun loadClusters() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val response = apiService.listClusters()
                _state.value = PeopleState(
                    clusters = response.body()?.clusters ?: emptyList(),
                    isLoading = false
                )
            } catch (_: Exception) {
                _state.value = PeopleState(isLoading = false)
            }
        }
    }
}
