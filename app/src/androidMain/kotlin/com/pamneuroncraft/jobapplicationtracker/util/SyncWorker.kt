package com.pamneuroncraft.jobapplicationtracker.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.pamneuroncraft.jobapplicationtracker.AppConfig
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.repository.AuthService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.CloudBackupService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val jobRepository: JobRepository by inject()
    private val authService: AuthService by inject()
    private val cloudBackupService: CloudBackupService by inject()
    private val appConfig: AppConfig by inject()

    override suspend fun doWork(): ListenableWorker.Result {
        Log.e("SyncWorker", "Sync starting...")

        if (!appConfig.featureGoogleDriveBackup) {
            Log.e("SyncWorker", "Sync skipped: Feature disabled in config")
            return ListenableWorker.Result.success()
        }

        if (!authService.isUserSignedIn()) {
            Log.e("SyncWorker", "Sync skipped: User not signed in")
            return ListenableWorker.Result.success()
        }

        return try {
            val uid = authService.currentUser.first()?.uid ?: return ListenableWorker.Result.success()

            // 1. Pull Phase: Download remote changes
            Log.e("SyncWorker", "Pull phase starting...")
            val remoteJson = cloudBackupService.restore()
            if (remoteJson != null) {
                try {
                    val remoteJobs = Json.decodeFromString<List<JobApplication>>(remoteJson)
                    remoteJobs.forEach { job ->
                        jobRepository.upsertJobFromRemote(job)
                    }
                    Log.e("SyncWorker", "Pull phase completed: ${remoteJobs.size} jobs processed")
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Pull phase failed to parse: ${e.message}")
                }
            }

            // 2. Push Phase: Upload local changes
            Log.e("SyncWorker", "Push phase starting...")
            val unsyncedJobs = jobRepository.getUnsyncedJobs(uid)
            
            if (unsyncedJobs.isNotEmpty()) {
                val diffJson = Json.encodeToString(unsyncedJobs)
                cloudBackupService.backup(diffJson)
                
                val timestamp = Clock.System.now().toEpochMilliseconds()
                jobRepository.markAsSynced(unsyncedJobs.map { it.id }, timestamp)
                Log.e("SyncWorker", "Push phase completed: ${unsyncedJobs.size} jobs uploaded")
            } else {
                Log.e("SyncWorker", "Push phase: Nothing to sync")
            }

            jobRepository.cleanupDeletedJobs(uid)
            
            Log.e("SyncWorker", "Full sync completed successfully")
            ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed: ${e.message}", e)
            if (runAttemptCount < 3) {
                ListenableWorker.Result.retry()
            } else {
                ListenableWorker.Result.failure()
            }
        }
    }
}
