package com.pamneuroncraft.jobapplicationtracker.domain.repository

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    fun getAllJobs(): Flow<List<JobApplication>>
    suspend fun getJobById(id: Int): JobApplication?
    suspend fun insertJob(job: JobApplication)
    suspend fun updateJob(job: JobApplication)
    suspend fun deleteJob(job: JobApplication)
    suspend fun deleteAllJobs()
}
