package com.pixelvault.app.sync

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.PhotoEntity
import com.pixelvault.app.data.local.SettingsDataStore
import com.pixelvault.app.data.remote.ApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class RemoteSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val photoDao: PhotoDao,
    private val settingsDataStore: SettingsDataStore
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!settingsDataStore.remoteSyncEnabled.first()) return Result.success()
        val files = findImageFiles()
        if (files.isEmpty()) return Result.success()

        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
        var uploaded = 0

        for (file in files) {
            try {
                val bytes = file.readBytes()
                val hash = sha256(bytes)
                val filename = file.name

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
                Log.e("RemoteSyncWorker", "Failed: ${file.name}", e)
            }
        }

        return if (uploaded > 0) Result.success() else Result.retry()
    }

    private fun findImageFiles(): List<File> {
        val dirs = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            File(Environment.getExternalStorageDirectory(), "Download"),
        )
        val extensions = listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
        return dirs.filter { it.exists() }
            .flatMap { dir -> dir.walkTopDown().filter { it.isFile && it.extension.lowercase() in extensions }.toList() }
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
