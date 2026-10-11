package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.AppConfig
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailProvider
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.repository.EmailSyncService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobExtractor
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import kotlinx.coroutines.flow.first

class SyncJobStatusesFromEmailUseCase(
    private val emailSyncService: EmailSyncService,
    private val jobExtractor: JobExtractor,
    private val jobRepository: JobRepository,
    private val localSettings: LocalSettings,
    private val appConfig: AppConfig
) {
    companion object {
        const val MIN_CONFIDENCE_THRESHOLD = 0.7f
    }

    suspend operator fun invoke(): Result<Int> {
        if (!appConfig.featureEmailSync || !localSettings.isEmailSyncEnabled) return Result.success(0)

        // Currently supporting Gmail
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
        var latestProcessedTimestamp = lastSyncTime

        for (email in emails) {
            // Deduplication check by source email ID
            if (email.id.isNotEmpty() && localSettings.isEmailProcessed(email.id)) {
                if (email.timestamp > latestProcessedTimestamp) {
                    latestProcessedTimestamp = email.timestamp
                }
                continue
            }

            val extractedUpdate = jobExtractor.extractStatusUpdate(email.body, email.subject)
            
            if (extractedUpdate != null) {
                // Verify AI confidence score threshold
                if (extractedUpdate.confidence >= MIN_CONFIDENCE_THRESHOLD) {
                    val matchingJob = findMatchingJob(currentJobs, extractedUpdate.companyName, extractedUpdate.jobTitle)

                    if (matchingJob != null && isValidStatusTransition(matchingJob.status, extractedUpdate.newStatus)) {
                        jobRepository.updateJob(matchingJob.copy(status = extractedUpdate.newStatus))
                        updateCount++
                    }
                }
            }

            if (email.id.isNotEmpty()) {
                localSettings.markEmailProcessed(email.id)
            }
            if (email.timestamp > latestProcessedTimestamp) {
                latestProcessedTimestamp = email.timestamp
            }
        }

        if (latestProcessedTimestamp > lastSyncTime) {
            localSettings.lastEmailSyncTime = latestProcessedTimestamp
        }

        return Result.success(updateCount)
    }

    private fun findMatchingJob(
        jobs: List<JobApplication>,
        extractedCompanyName: String,
        extractedJobTitle: String?
    ): JobApplication? {
        val normalizedExtractedCompany = normalizeCompanyName(extractedCompanyName)
        if (normalizedExtractedCompany.isBlank()) return null

        // 1. Filter candidates matching normalized company name
        val companyCandidates = jobs.filter { job ->
            val normalizedJobCompany = normalizeCompanyName(job.companyName)
            normalizedJobCompany.contains(normalizedExtractedCompany) ||
                    normalizedExtractedCompany.contains(normalizedJobCompany)
        }

        if (companyCandidates.isEmpty()) return null

        // 2. If exactly 1 candidate, match it
        if (companyCandidates.size == 1) {
            return companyCandidates.first()
        }

        // 3. If multiple candidates for company, match by job title
        if (!extractedJobTitle.isNullOrBlank()) {
            val normalizedExtractedTitle = extractedJobTitle.lowercase().trim()
            val titleMatch = companyCandidates.find { job ->
                val normalizedJobTitle = job.jobName.lowercase().trim()
                normalizedJobTitle.contains(normalizedExtractedTitle) ||
                        normalizedExtractedTitle.contains(normalizedJobTitle)
            }
            if (titleMatch != null) return titleMatch
        }

        // Avoid updating ambiguous roles if multiple candidates exist and title cannot match
        return null
    }

    private fun normalizeCompanyName(name: String): String {
        return name.lowercase()
            .replace(Regex("\\b(inc|llc|corp|corporation|ltd|limited|co|company|group|technologies|tech|labs|solutions|gmbh|plc)\\b"), "")
            .replace(Regex("[^a-z0-9]"), "")
            .trim()
    }

    private fun isValidStatusTransition(currentStatus: JobStatus, newStatus: JobStatus): Boolean {
        if (currentStatus == newStatus) return false
        return when (currentStatus) {
            JobStatus.APPLIED -> newStatus in listOf(JobStatus.INTERVIEW, JobStatus.OFFER, JobStatus.NO_OFFER)
            JobStatus.INTERVIEW -> newStatus in listOf(JobStatus.OFFER, JobStatus.NO_OFFER)
            JobStatus.OFFER -> false // Terminal status
            JobStatus.NO_OFFER -> false // Terminal status
        }
    }
}
