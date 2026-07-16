package com.Pamneuroncraft.jobapplicationtracker.domain.usecase

import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.Pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class BackupRestoreUseCase(
    private val repository: JobRepository
) {
    suspend fun exportToJson(outputStream: OutputStream) {
        val jobs = repository.getAllJobs().first()
        val json = Json.encodeToString(jobs)
        outputStream.write(json.toByteArray())
        outputStream.close()
    }

    suspend fun importFromJson(inputStream: InputStream) {
        val json = inputStream.bufferedReader().use { it.readText() }
        val jobs = Json.decodeFromString<List<JobApplication>>(json)
        repository.deleteAllJobs()
        jobs.forEach { repository.insertJob(it) }
    }
}
