package com.pixelvault.app.sync

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pixelvault.app.data.local.PhotoDao
import com.pixelvault.app.data.local.PhotoEntity
import com.pixelvault.app.data.remote.ApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val photoDao: PhotoDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val uris = queryMediaStore() ?: return Result.retry()

        var uploaded = 0
        for (uri in uris) {
            try {
                val bytes = applicationContext.contentResolver.readBytes(uri) ?: continue
                val hash = sha256(bytes)
                val filename = getFileName(uri) ?: "photo_${System.currentTimeMillis()}.jpg"

                val filePart = MultipartBody.Part.createFormData(
                    "file", filename,
                    bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                )

                val response = apiService.uploadPhoto(
                    file = filePart,
                    filename = filename.toRequestBody("text/plain".toMediaTypeOrNull()),
                    hash = hash.toRequestBody("text/plain".toMediaTypeOrNull()),
                    size = bytes.size.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    createdAt = (getDateTaken(uri) ?: "").toRequestBody("text/plain".toMediaTypeOrNull())
                )

                if (response.isSuccessful && response.body()?.status == "uploaded") {
                    val photoId = response.body()?.photoId ?: System.currentTimeMillis()
                    val syncedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
                    photoDao.insertAll(
                        listOf(
                            PhotoEntity(
                                id = photoId,
                                filename = filename,
                                hash = hash,
                                size = bytes.size.toLong(),
                                createdAt = getDateTaken(uri) ?: syncedAt,
                                syncedAt = syncedAt,
                                path = uri.toString()
                            )
                        )
                    )
                    uploaded++
                }
            } catch (_: Exception) {
                continue
            }
        }

        return if (uploaded > 0 || uris.isEmpty()) Result.success() else Result.retry()
    }

    private fun queryMediaStore(): List<Uri>? {
        val uris = mutableListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val collection = if (android.os.Build.VERSION.SDK_INT >= 29) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        applicationContext.contentResolver.query(
            collection, projection, null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                uris.add(Uri.withAppendedPath(collection, cursor.getString(idCol)))
            }
        }
        return uris.ifEmpty { null }
    }

    private fun getFileName(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        applicationContext.contentResolver.query(uri, projection, null, null, null)?.use {
            if (it.moveToFirst()) return it.getString(0)
        }
        return uri.lastPathSegment
    }

    private fun getDateTaken(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATE_TAKEN)
        applicationContext.contentResolver.query(uri, projection, null, null, null)?.use {
            if (it.moveToFirst()) {
                val ms = it.getLong(0)
                if (ms > 0) {
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = ms
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                    return sdf.format(cal.time)
                }
            }
        }
        return null
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }

    private fun ContentResolver.readBytes(uri: Uri): ByteArray? {
        return openInputStream(uri)?.use { it.readBytes() }
    }
}
