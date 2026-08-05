package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import com.pamneuroncraft.jobapplicationtracker.domain.repository.CloudBackupService
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class CloudBackupUseCase(
    private val repository: JobRepository,
    private val cloudBackupService: CloudBackupService
) {
    suspend fun backupToCloud() {
        val jobs = repository.getAllJobs().first()
        val json = Json.encodeToString(jobs)
        cloudBackupService.backup(json)
    }

    suspend fun restoreFromCloud() {
        val json = cloudBackupService.restore() ?: return
        val jobs = Json.decodeFromString<List<com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication>>(json)
        
        repository.deleteAllJobs()
        jobs.forEach { repository.insertJob(it) }
    }
}
