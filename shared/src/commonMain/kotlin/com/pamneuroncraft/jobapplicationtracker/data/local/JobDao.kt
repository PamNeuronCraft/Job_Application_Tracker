package com.pamneuroncraft.jobapplicationtracker.data.local

import androidx.room.*
import androidx.paging.PagingSource
import com.pamneuroncraft.jobapplicationtracker.data.local.entity.JobApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM job_applications WHERE isDeleted = 0 ORDER BY dateAdded DESC")
    fun getAllJobs(): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE isDeleted = 0 ORDER BY dateAdded DESC")
    fun getAllJobsPaged(): PagingSource<Int, JobApplicationEntity>

    @Query("""
        SELECT job_applications.* FROM job_applications
        JOIN job_search ON job_applications.id = job_search.id
        WHERE job_search MATCH :query AND job_applications.isDeleted = 0
    """)
    fun searchJobs(query: String): Flow<List<JobApplicationEntity>>

    @Query("""
        SELECT job_applications.* FROM job_applications
        JOIN job_search ON job_applications.id = job_search.id
        WHERE job_search MATCH :query AND job_applications.isDeleted = 0
    """)
    fun searchJobsPaged(query: String): PagingSource<Int, JobApplicationEntity>

    @Query("SELECT * FROM job_applications WHERE id = :id AND isDeleted = 0")
    suspend fun getJobById(id: String): JobApplicationEntity?

    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun getJobByIdIncludingDeleted(id: String): JobApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobApplicationEntity)

    @Update
    suspend fun updateJob(job: JobApplicationEntity)

    @Delete
    suspend fun deleteJob(job: JobApplicationEntity)

    @Query("UPDATE job_applications SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteJob(id: String, timestamp: Long)

    @Query("DELETE FROM job_applications")
    suspend fun deleteAllJobs()

    @Query("UPDATE job_applications SET userId = :userId WHERE userId IS NULL")
    suspend fun updateJobsUserId(userId: String)

    @Query("SELECT * FROM job_applications WHERE userId = :userId AND isDeleted = 0")
    suspend fun getJobsByUserId(userId: String): List<JobApplicationEntity>

    @Query("SELECT * FROM job_applications WHERE userId = :userId AND (lastSyncedAt IS NULL OR updatedAt > lastSyncedAt)")
    suspend fun getUnsyncedJobs(userId: String): List<JobApplicationEntity>

    @Query("UPDATE job_applications SET lastSyncedAt = :timestamp WHERE id IN (:jobIds)")
    suspend fun markAsSynced(jobIds: List<String>, timestamp: Long)

    @Query("DELETE FROM job_applications WHERE userId = :userId AND isDeleted = 1")
    suspend fun cleanupDeletedJobs(userId: String)
}
