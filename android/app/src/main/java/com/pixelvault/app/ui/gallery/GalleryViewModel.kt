package com.pixelvault.app.ui.gallery

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.PhotoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class GalleryState(
    val photos: List<PhotoEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isSyncing: Boolean = false,
    val unprocessedCount: Int = 0
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val photoDao: PhotoDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(GalleryState())
    val state: StateFlow<GalleryState> = _state

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val photos = photoDao.getAllPhotos()
            val unprocessed = photoDao.getUnprocessedPhotos().size
            _state.value = GalleryState(photos = photos, isLoading = false, unprocessedCount = unprocessed)
        }
    }

    fun triggerSync() {
        if (_state.value.isSyncing) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true)
            try {
                scanLocalPhotos()
            } finally {
                _state.value = _state.value.copy(isSyncing = false)
                loadPhotos()
            }
        }
    }

    private suspend fun scanLocalPhotos() = withContext(Dispatchers.IO) {
        val extensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
        val dirs = listOf(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.parentFile?.let {
                File(it, "Pictures")
            },
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            File(Environment.getExternalStorageDirectory(), "Download"),
        )
        val files = dirs.filterNotNull().filter { it.exists() }
            .flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.extension.lowercase() in extensions }.toList() }

        if (files.isEmpty()) return@withContext

        var scanned = 0
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
        for (file in files) {
            try {
                val bytes = file.readBytes()
                val hash = sha256(bytes)
                if (photoDao.getByHash(hash) != null) continue
                photoDao.insertAll(
                    listOf(
                        PhotoEntity(
                            id = 0,
                            filename = file.name,
                            hash = hash,
                            size = bytes.size.toLong(),
                            createdAt = now,
                            syncedAt = now,
                            path = Uri.fromFile(file).toString(),
                            isProcessed = false
                        )
                    )
                )
                scanned++
            } catch (e: Exception) {
                Log.e("GalleryVM", "Scan failed for ${file.name}", e)
            }
        }
        Log.d("GalleryVM", "Scanned $scanned/${files.size} photos")
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
