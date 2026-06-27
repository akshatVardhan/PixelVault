package com.pixelvault.app.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvault.app.data.remote.ApiService
import com.pixelvault.app.data.remote.PhotoDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersonPhotosState(
    val photos: List<PhotoDto> = emptyList(),
    val isLoading: Boolean = true,
    val clusterName: String? = null
)

@HiltViewModel
class PersonPhotosViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(PersonPhotosState())
    val state: StateFlow<PersonPhotosState> = _state

    private var loadedClusterId: Int? = null

    fun loadCluster(clusterId: Int) {
        if (loadedClusterId == clusterId) return
        loadedClusterId = clusterId
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val response = apiService.getClusterPhotos(clusterId)
                val clusterName = try {
                    apiService.listClusters().body()?.clusters?.find { it.id == clusterId }?.name
                } catch (_: Exception) { null }
                _state.value = PersonPhotosState(
                    photos = response.body()?.photos ?: emptyList(),
                    isLoading = false,
                    clusterName = clusterName
                )
            } catch (_: Exception) {
                _state.value = PersonPhotosState(isLoading = false)
            }
        }
    }
}
