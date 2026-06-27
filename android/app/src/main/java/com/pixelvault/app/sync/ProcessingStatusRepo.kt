package com.pixelvault.app.sync

import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessingStatusRepo @Inject constructor(
    private val photoDao: PhotoDao,
    private val settingsDataStore: SettingsDataStore
) {
    data class ProcessingStatus(
        val unprocessedCount: Int = 0,
        val totalPhotos: Int = 0,
        val lastProcessedTime: String? = null,
        val isProcessing: Boolean = false
    )

    private val _status = MutableStateFlow(ProcessingStatus())
    val status: StateFlow<ProcessingStatus> = _status.asStateFlow()

    suspend fun refresh() {
        val unprocessed = photoDao.getUnprocessedPhotos().size
        val total = photoDao.count()
        val last = settingsDataStore.lastProcessedAt.first()
        _status.value = ProcessingStatus(unprocessed, total, last, false)
    }

    fun setProcessing(processing: Boolean) {
        _status.value = _status.value.copy(isProcessing = processing)
    }
}
