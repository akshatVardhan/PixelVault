package com.pixelvault.app.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pixelvault.app.data.remote.ApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: ApiService
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val CHANNEL_ID = "on_this_day"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        createChannel()
        return try {
            val response = apiService.getOnThisDay()
            if (response.isSuccessful) {
                val body = response.body()
                val photos = body?.get("photos") as? List<*> ?: emptyList<Any>()
                if (photos.isNotEmpty()) {
                    showNotification(
                        title = "On This Day",
                        message = "${photos.size} photo(s) from this day in previous years"
                    )
                }
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "On This Day",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily notifications of photos from this day in past years"
            }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
