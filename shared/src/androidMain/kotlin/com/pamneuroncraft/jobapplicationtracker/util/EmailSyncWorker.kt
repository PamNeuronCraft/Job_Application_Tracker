package com.pamneuroncraft.jobapplicationtracker.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pamneuroncraft.jobapplicationtracker.domain.repository.NotificationService
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.SyncJobStatusesFromEmailUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class EmailSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val syncUseCase: SyncJobStatusesFromEmailUseCase by inject()
    private val notificationService: NotificationService by inject()
    private val analyticsHelper: AnalyticsHelper by inject()

    override suspend fun doWork(): Result {
        Log.e("EmailSyncWorker", "Background email sync starting...")

        return try {
            val result = syncUseCase()
            
            if (result.isSuccess) {
                val updatedCount = result.getOrNull() ?: 0
                if (updatedCount > 0) {
                    notificationService.showNotification(
                        title = "Job Status Updated",
                        body = "We found $updatedCount status updates in your emails."
                    )
                    analyticsHelper.logEvent("email_sync_success", mapOf("updates" to updatedCount.toString()))
                }
                Log.e("EmailSyncWorker", "Email sync completed. Updates: $updatedCount")
                Result.success()
            } else {
                val error = result.exceptionOrNull()
                Log.e("EmailSyncWorker", "Email sync failed: ${error?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("EmailSyncWorker", "Unexpected error in EmailSyncWorker: ${e.message}", e)
            analyticsHelper.logNonFatal(e)
            Result.failure()
        }
    }
}
