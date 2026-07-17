package com.pamneuroncraft.jobapplicationtracker.data.local

import androidx.room.*
import com.pamneuroncraft.jobapplicationtracker.data.local.entity.JobApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM job_applications ORDER BY dateAdded DESC")
    fun getAllJobs(): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun getJobById(id: Int): JobApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobApplicationEntity)

    @Update
    suspend fun updateJob(job: JobApplicationEntity)

    @Delete
    suspend fun deleteJob(job: JobApplicationEntity)

    @Query("DELETE FROM job_applications")
    suspend fun deleteAllJobs()
}
