package com.pixelvault.app.sync

import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SyncStatus(
    val lastSync: String? = null,
    val totalPhotos: Int = 0,
    val isSyncing: Boolean = false
)

@Singleton
class SyncStatusRepo @Inject constructor(
    private val apiService: ApiService,
    private val photoDao: PhotoDao
) {
    private val _status = MutableStateFlow(SyncStatus())
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    suspend fun refresh() {
        _status.value = _status.value.copy(isSyncing = true)
        try {
            val remote = apiService.syncStatus()
            val localCount = photoDao.count()
            val localSync = photoDao.lastSyncTime()
            _status.value = SyncStatus(
                lastSync = remote.body()?.lastSync ?: localSync,
                totalPhotos = maxOf(remote.body()?.totalPhotos ?: 0, localCount),
                isSyncing = false
            )
        } catch (_: Exception) {
            _status.value = _status.value.copy(
                totalPhotos = photoDao.count(),
                lastSync = photoDao.lastSyncTime(),
                isSyncing = false
            )
        }
    }
}
