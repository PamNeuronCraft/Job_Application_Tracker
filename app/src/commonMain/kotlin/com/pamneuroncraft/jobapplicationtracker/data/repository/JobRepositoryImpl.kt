package com.pamneuroncraft.jobapplicationtracker.data.repository

import com.pamneuroncraft.jobapplicationtracker.data.local.JobDao
import com.pamneuroncraft.jobapplicationtracker.data.local.entity.JobApplicationEntity
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JobRepositoryImpl(
    private val dao: JobDao
) : JobRepository {

    override fun getAllJobs(): Flow<List<JobApplication>> {
        return dao.getAllJobs().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getJobById(id: Int): JobApplication? {
        return dao.getJobById(id)?.toDomainModel()
    }

    override suspend fun insertJob(job: JobApplication) {
        dao.insertJob(JobApplicationEntity.fromDomainModel(job))
    }

    override suspend fun updateJob(job: JobApplication) {
        dao.updateJob(JobApplicationEntity.fromDomainModel(job))
    }

    override suspend fun deleteJob(job: JobApplication) {
        dao.deleteJob(JobApplicationEntity.fromDomainModel(job))
    }

    override suspend fun deleteAllJobs() {
        dao.deleteAllJobs()
    }
}
