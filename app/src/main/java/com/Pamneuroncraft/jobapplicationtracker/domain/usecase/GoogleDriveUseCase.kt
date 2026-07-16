package com.Pamneuroncraft.jobapplicationtracker.domain.usecase

import com.Pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream

class GoogleDriveUseCase(
    private val repository: JobRepository
) {
    suspend fun backupToDrive(driveService: Drive) = withContext(Dispatchers.IO) {
        val jobs = repository.getAllJobs().first()
        val json = Json.encodeToString(jobs)
        
        val fileMetadata = File().apply {
            name = "job_tracker_backup.json"
            parents = listOf("appDataFolder")
        }
        
        val content = InputStreamContent("application/json", ByteArrayInputStream(json.toByteArray()))
        
        // Find existing backup file and delete it or update it
        val query = "name = 'job_tracker_backup.json' and 'appDataFolder' in parents"
        val existingFiles = driveService.files().list().setQ(query).execute().files
        
        if (existingFiles.isNotEmpty()) {
            driveService.files().update(existingFiles[0].id, fileMetadata, content).execute()
        } else {
            driveService.files().create(fileMetadata, content).execute()
        }
    }

    suspend fun restoreFromDrive(driveService: Drive) = withContext(Dispatchers.IO) {
        val query = "name = 'job_tracker_backup.json' and 'appDataFolder' in parents"
        val existingFiles = driveService.files().list().setQ(query).execute().files
        
        if (existingFiles.isNotEmpty()) {
            val fileId = existingFiles[0].id
            val outputStream = java.io.ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            
            val json = outputStream.toString()
            val jobs = Json.decodeFromString<List<com.Pamneuroncraft.jobapplicationtracker.domain.model.JobApplication>>(json)
            
            repository.deleteAllJobs()
            jobs.forEach { repository.insertJob(it) }
        }
    }
}
