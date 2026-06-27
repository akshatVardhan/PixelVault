package com.pixelvault.app.ui.gallery

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.PhotoEntity
import com.pixelvault.app.ml.MLPipelineService
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
    val isProcessing: Boolean = false
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val photoDao: PhotoDao,
    private val pipelineService: MLPipelineService,
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
            _state.value = GalleryState(photos = photos, isLoading = false)
        }
    }

    fun triggerProcessing() {
        if (_state.value.isProcessing) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true)
            try {
                processNow()
            } finally {
                _state.value = _state.value.copy(isProcessing = false)
                loadPhotos()
            }
        }
    }

    private suspend fun processNow() = withContext(Dispatchers.IO) {
        val dirs = listOf(
            File(context.getExternalFilesDir(null)?.parentFile, "Pictures"),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            File(Environment.getExternalStorageDirectory(), "Download"),
        )
        val files = mutableListOf<File>()
        for (dir in dirs) {
            if (!dir.exists()) continue
            dir.walkTopDown().forEach { file ->
                if (file.isFile && file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")) {
                    files.add(file)
                }
            }
        }
        if (files.isEmpty()) return@withContext

        var processed = 0
        for (file in files) {
            try {
                val bytes = file.readBytes()
                val hash = sha256(bytes)
                if (photoDao.getByHash(hash) != null) continue
                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
                val ids = photoDao.insertAll(
                    listOf(
                        PhotoEntity(
                            filename = file.name,
                            hash = hash,
                            size = bytes.size.toLong(),
                            createdAt = now,
                            path = Uri.fromFile(file).toString(),
                            isProcessed = false
                        )
                    )
                )
                val id = ids.firstOrNull() ?: continue
                val saved = photoDao.getPhotoById(id) ?: continue
                pipelineService.processOnePhoto(saved)
                processed++
            } catch (e: Exception) {
                Log.e("GalleryVM", "Processing failed for ${file.name}", e)
            }
        }
        Log.d("GalleryVM", "Processed $processed/${files.size} photos")
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
