package com.pixelvault.app.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvault.app.data.local.ClusterDao
import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.PhotoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersonPhotosState(
    val photos: List<PhotoEntity> = emptyList(),
    val clusterName: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class PersonPhotosViewModel @Inject constructor(
    private val photoDao: PhotoDao,
    private val clusterDao: ClusterDao
) : ViewModel() {

    private val _state = MutableStateFlow(PersonPhotosState())
    val state: StateFlow<PersonPhotosState> = _state

    private var loadedClusterId: Long? = null

    fun loadCluster(clusterId: Long) {
        if (loadedClusterId == clusterId) return
        loadedClusterId = clusterId
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val name = clusterDao.getClusterById(clusterId)?.name
                val photos = photoDao.getPhotosByCluster(clusterId)
                _state.value = PersonPhotosState(photos = photos, clusterName = name, isLoading = false)
            } catch (_: Exception) {
                _state.value = PersonPhotosState(isLoading = false)
            }
        }
    }
}
