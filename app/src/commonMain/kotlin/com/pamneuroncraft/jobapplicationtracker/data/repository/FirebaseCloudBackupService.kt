package com.pamneuroncraft.jobapplicationtracker.data.repository

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.repository.CloudBackupService
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class FirebaseCloudBackupService : CloudBackupService {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    override val isUserSignedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun backup(json: String) {
        val uid = auth.currentUser?.uid ?: return
        val jobs = Json.decodeFromString<List<JobApplication>>(json)
        
        val userJobsCollection = firestore.collection("users").document(uid).collection("jobs")
        
        // Simple strategy: Overwrite all jobs
        // 1. Delete existing (Optional, but ensures sync if some were deleted locally)
        // For simplicity in KMP GitLive SDK, we might just set documents by their ID
        
        jobs.forEach { job ->
            userJobsCollection.document(job.id.toString()).set(JobApplication.serializer(), job)
        }
    }

    override suspend fun restore(): String? {
        val uid = auth.currentUser?.uid ?: return null
        val userJobsCollection = firestore.collection("users").document(uid).collection("jobs")
        
        val snapshot = userJobsCollection.get()
        val jobs = snapshot.documents.map { it.data(JobApplication.serializer()) }
        
        return if (jobs.isNotEmpty()) Json.encodeToString(jobs) else null
    }
}
