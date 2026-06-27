package com.pixelvault.app.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvault.app.data.local.ClusterDao
import com.pixelvault.app.data.local.ClusterEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PeopleState(
    val clusters: List<ClusterEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val clusterDao: ClusterDao
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
                val clusters = clusterDao.getAllClusters()
                _state.value = PeopleState(clusters = clusters, isLoading = false)
            } catch (_: Exception) {
                _state.value = PeopleState(isLoading = false)
            }
        }
    }

    fun renameCluster(id: Long, name: String) {
        viewModelScope.launch {
            clusterDao.renameCluster(id, name)
            loadClusters()
        }
    }
}
