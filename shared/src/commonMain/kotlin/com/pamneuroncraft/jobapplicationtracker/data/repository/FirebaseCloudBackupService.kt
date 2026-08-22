package com.pamneuroncraft.jobapplicationtracker.data.repository

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.repository.CloudBackupService
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class FirebaseCloudBackupService : CloudBackupService {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    override val isUserSignedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun backup(json: String) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val jobs = Json.decodeFromString<List<JobApplication>>(json)
        
        val userJobsCollection = firestore.collection("users").document(uid).collection("jobs")
        
        // Split jobs into chunks of 500 (Firestore batch limit)
        val chunks = jobs.chunked(500)
        
        chunks.forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { job ->
                val doc = userJobsCollection.document(job.id)
                if (job.isDeleted) {
                    batch.delete(doc)
                } else {
                    batch.set(doc, JobApplication.serializer(), job)
                }
            }
            
            try {
                batch.commit()
            } catch (e: Exception) {
                // Log batch commit error - ideally using a cross-platform logger or returning result
                println("FirebaseCloudBackupService: Batch commit failed: ${e.message}")
                e.printStackTrace()
                // Rethrow to allow caller (SyncWorker) to handle retry/failure logic
                throw e
            }
        }
    }

    override suspend fun restore(): String? = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext null
        val userJobsCollection = firestore.collection("users").document(uid).collection("jobs")
        
        try {
            val snapshot = userJobsCollection.get()
            val jobs = snapshot.documents.map { it.data(JobApplication.serializer()) }
            
            if (jobs.isNotEmpty()) Json.encodeToString(jobs) else null
        } catch (e: Exception) {
            println("FirebaseCloudBackupService: Restore failed: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun deleteAllUserData(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Not signed in"))
        val userJobsCollection = firestore.collection("users").document(uid).collection("jobs")
        
        try {
            val snapshot = userJobsCollection.get()
            val chunks = snapshot.documents.chunked(500)
            
            chunks.forEach { chunk ->
                val batch = firestore.batch()
                chunk.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
            }
            
            // Also delete the user document itself
            firestore.collection("users").document(uid).delete()
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("FirebaseCloudBackupService: Delete all data failed: ${e.message}")
            Result.failure(e)
        }
    }
}
