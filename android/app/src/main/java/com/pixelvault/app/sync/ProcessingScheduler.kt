package com.pixelvault.app.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessingScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val PROCESSING_WORK = "photo_processing"
        const val REMOTE_SYNC_WORK = "remote_sync"
        const val NOTIF_WORK = "daily_notification"
    }

    fun scheduleProcessing(intervalHours: Long = 12) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()
        val request = PeriodicWorkRequestBuilder<PhotoProcessingWorker>(intervalHours, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PROCESSING_WORK, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    fun scheduleRemoteSync(intervalHours: Long = 12) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()
        val request = PeriodicWorkRequestBuilder<RemoteSyncWorker>(intervalHours, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMOTE_SYNC_WORK, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    fun scheduleDailyNotification() {
        val request = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NOTIF_WORK, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    fun cancelAll() {
        WorkManager.getInstance(context).cancelUniqueWork(PROCESSING_WORK)
        WorkManager.getInstance(context).cancelUniqueWork(REMOTE_SYNC_WORK)
        WorkManager.getInstance(context).cancelUniqueWork(NOTIF_WORK)
    }
}
