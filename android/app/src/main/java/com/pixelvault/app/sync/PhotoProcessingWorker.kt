package com.pixelvault.app.sync

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pixelvault.app.data.local.AppDatabase
import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.PhotoEntity
import com.pixelvault.app.data.local.SettingsDataStore
import com.pixelvault.app.ml.MLPipelineService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class PhotoProcessingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val pipelineService: MLPipelineService,
    private val settingsDataStore: SettingsDataStore,
    private val photoDao: PhotoDao,
    private val appDatabase: AppDatabase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!settingsDataStore.mlEnabled.first()) return Result.success()
        val files = findImageFiles()
        var processed = 0
        for (file in files) {
            try {
                val bytes = file.readBytes()
                val hash = sha256(bytes)
                if (photoDao.getByHash(hash) != null) continue
                val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
                val entity = PhotoEntity(
                    filename = file.name,
                    hash = hash,
                    size = bytes.length.toLong(),
                    createdAt = now,
                    path = Uri.fromFile(file).toString(),
                    isProcessed = false
                )
                val ids = photoDao.insertAll(listOf(entity))
                val id = ids.firstOrNull() ?: continue
                val saved = photoDao.getPhotoById(id) ?: continue
                pipelineService.processOnePhoto(saved)
                processed++
            } catch (e: Exception) {
                Log.e("PhotoProcessingWorker", "Failed: ${file.name}", e)
            }
        }
        settingsDataStore.setLastProcessedAt(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date()))
        return Result.success()
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
