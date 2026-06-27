package com.pixelvault.app.ui.gallery

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.PhotoEntity
import com.pixelvault.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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
    private val apiService: ApiService,
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
                syncNow()
            } finally {
                _state.value = _state.value.copy(isSyncing = false)
                loadPhotos()
            }
        }
    }

    private suspend fun syncNow() = withContext(Dispatchers.IO) {
        val dirs = listOf(
            File(context.getExternalFilesDir(null)?.parentFile, "Pictures"),
            File("/sdcard/Pictures"),
            File("/sdcard/DCIM"),
            File("/sdcard/Download"),
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

        var uploaded = 0
        for (file in files) {
            try {
                val bytes = file.readBytes()
                val hash = sha256(bytes)
                val filename = file.name
                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

                val filePart = MultipartBody.Part.createFormData(
                    "file", filename,
                    bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )

                val response = apiService.uploadPhoto(
                    file = filePart,
                    filename = filename.toRequestBody("text/plain".toMediaTypeOrNull()),
                    hash = hash.toRequestBody("text/plain".toMediaTypeOrNull()),
                    size = bytes.size.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    createdAt = now.toRequestBody("text/plain".toMediaTypeOrNull())
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == "uploaded" || body?.status == "duplicate") {
                        val photoId = body.photoId ?: System.currentTimeMillis()
                        photoDao.insertAll(
                            listOf(
                                PhotoEntity(
                                    id = photoId,
                                    filename = filename,
                                    hash = hash,
                                    size = bytes.size.toLong(),
                                    createdAt = now,
                                    syncedAt = now,
                                    path = file.toURI().toString()
                                )
                            )
                        )
                        uploaded++
                    }
                }
            } catch (e: Exception) {
                Log.e("GalleryVM", "Sync failed for ${file.name}", e)
            }
        }
        Log.d("GalleryVM", "Synced $uploaded/${files.size} photos")
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
