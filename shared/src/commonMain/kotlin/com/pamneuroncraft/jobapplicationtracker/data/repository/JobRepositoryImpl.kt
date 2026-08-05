package com.pamneuroncraft.jobapplicationtracker.data.repository

import com.pamneuroncraft.jobapplicationtracker.data.local.JobDao
import com.pamneuroncraft.jobapplicationtracker.data.local.entity.JobApplicationEntity
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.repository.AuthService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import com.pamneuroncraft.jobapplicationtracker.domain.repository.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import androidx.paging.*

class JobRepositoryImpl(
    private val dao: JobDao,
    private val authService: AuthService,
    private val syncManager: SyncManager
) : JobRepository {

    override fun getAllJobs(): Flow<List<JobApplication>> {
        return dao.getAllJobs().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getAllJobsPaged(): Flow<PagingData<JobApplication>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { dao.getAllJobsPaged() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }

    override fun searchJobs(query: String): Flow<List<JobApplication>> {
        return dao.searchJobs(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun searchJobsPaged(query: String): Flow<PagingData<JobApplication>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { dao.searchJobsPaged(query) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }

    override suspend fun getJobById(id: String): JobApplication? {
        return dao.getJobById(id)?.toDomainModel()
    }

    override suspend fun insertJob(job: JobApplication) {
        val userId = authService.currentUser.firstOrNull()?.uid
        val jobToInsert = job.copy(
            userId = userId,
            updatedAt = Clock.System.now()
        )
        dao.insertJob(JobApplicationEntity.fromDomainModel(jobToInsert))
        syncManager.triggerSync()
    }

    override suspend fun updateJob(job: JobApplication) {
        val jobToUpdate = job.copy(updatedAt = Clock.System.now())
        dao.updateJob(JobApplicationEntity.fromDomainModel(jobToUpdate))
        syncManager.triggerSync()
    }

    override suspend fun deleteJob(job: JobApplication) {
        dao.softDeleteJob(job.id, Clock.System.now().toEpochMilliseconds())
        syncManager.triggerSync()
    }

    override suspend fun deleteAllJobs() {
        dao.deleteAllJobs()
        syncManager.triggerSync()
    }

    override suspend fun getUnsyncedJobs(userId: String): List<JobApplication> {
        return dao.getUnsyncedJobs(userId).map { it.toDomainModel() }
    }

    override suspend fun markAsSynced(jobIds: List<String>, timestamp: Long) {
        dao.markAsSynced(jobIds, timestamp)
    }

    override suspend fun cleanupDeletedJobs(userId: String) {
        dao.cleanupDeletedJobs(userId)
    }

    override suspend fun upsertJobFromRemote(job: JobApplication) {
        val localJob = dao.getJobByIdIncludingDeleted(job.id)
        if (localJob == null || job.updatedAt > Instant.fromEpochMilliseconds(localJob.updatedAt)) {
            dao.insertJob(JobApplicationEntity.fromDomainModel(job))
        }
    }

    override suspend fun updateJobsUserId(userId: String) {
        dao.updateJobsUserId(userId)
    }
}
