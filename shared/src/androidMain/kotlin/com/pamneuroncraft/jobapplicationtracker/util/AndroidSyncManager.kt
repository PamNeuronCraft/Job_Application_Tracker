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
}
