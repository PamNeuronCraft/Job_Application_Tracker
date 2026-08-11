package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailProvider
import com.pamneuroncraft.jobapplicationtracker.domain.repository.EmailSyncService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobExtractor
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import kotlinx.coroutines.flow.first

class SyncJobStatusesFromEmailUseCase(
    private val emailSyncService: EmailSyncService,
    private val jobExtractor: JobExtractor,
    private val jobRepository: JobRepository,
    private val localSettings: LocalSettings
) {
    suspend operator fun invoke(): Result<Int> {
        if (!localSettings.isEmailSyncEnabled) return Result.success(0)

        // Currently only supporting Gmail
        val provider = EmailProvider.GMAIL
        if (!emailSyncService.hasPermission(provider)) {
            return Result.failure(Exception("Permission not granted for $provider"))
        }

        val lastSyncTime = localSettings.lastEmailSyncTime
        val emailsResult = emailSyncService.fetchNewEmails(provider, lastSyncTime)
        
        if (emailsResult.isFailure) return Result.failure(emailsResult.exceptionOrNull()!!)

        val emails = emailsResult.getOrNull() ?: emptyList()
        if (emails.isEmpty()) return Result.success(0)

        var updateCount = 0
        val currentJobs = jobRepository.getAllJobs().first()

        for (email in emails) {
            // Use Gemini to extract status update from email
            val extractedUpdate = jobExtractor.extractStatusUpdate(email.body, email.subject)
            
            if (extractedUpdate != null) {
                // Find matching job in database
                val matchingJob = currentJobs.find { 
                    it.companyName.equals(extractedUpdate.companyName, ignoreCase = true) 
                }

                if (matchingJob != null && matchingJob.status != extractedUpdate.newStatus) {
                    jobRepository.updateJob(matchingJob.copy(status = extractedUpdate.newStatus))
                    updateCount++
                }
            }
        }

        localSettings.lastEmailSyncTime = emails.maxOf { it.timestamp }
        return Result.success(updateCount)
    }
}
