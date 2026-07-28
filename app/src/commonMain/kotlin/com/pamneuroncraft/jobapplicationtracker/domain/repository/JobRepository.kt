package com.pamneuroncraft.jobapplicationtracker.domain.repository

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

interface JobRepository {
    fun getAllJobs(): Flow<List<JobApplication>>
    fun getAllJobsPaged(): Flow<PagingData<JobApplication>>
    fun searchJobs(query: String): Flow<List<JobApplication>>
    fun searchJobsPaged(query: String): Flow<PagingData<JobApplication>>
    suspend fun getJobById(id: String): JobApplication?
    suspend fun insertJob(job: JobApplication)
    suspend fun updateJob(job: JobApplication)
    suspend fun deleteJob(job: JobApplication)
    suspend fun deleteAllJobs()
    suspend fun getUnsyncedJobs(userId: String): List<JobApplication>
    suspend fun markAsSynced(jobIds: List<String>, timestamp: Long)
    suspend fun cleanupDeletedJobs(userId: String)
    suspend fun upsertJobFromRemote(job: JobApplication)
    suspend fun updateJobsUserId(userId: String)
}
