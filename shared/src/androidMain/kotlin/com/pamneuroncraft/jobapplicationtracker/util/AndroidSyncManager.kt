package com.pamneuroncraft.jobapplicationtracker.util

import android.content.Context
import androidx.work.*
import com.pamneuroncraft.jobapplicationtracker.domain.repository.SyncManager
import java.util.concurrent.TimeUnit

class AndroidSyncManager(private val context: Context) : SyncManager {
    override fun triggerSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("SyncWorker")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncWork",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    override fun scheduleEmailSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val emailSyncRequest = PeriodicWorkRequestBuilder<EmailSyncWorker>(
            12, TimeUnit.HOURS,
            1, TimeUnit.HOURS // 1 hour flex interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("EmailSyncWorker")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "EmailStatusSync",
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
            emailSyncRequest
        )
    }
}
