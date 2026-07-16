package com.Pamneuroncraft.jobapplicationtracker.domain.usecase

import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.Pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetJobsUseCase(
    private val repository: JobRepository
) {
    operator fun invoke(isAscending: Boolean = false): Flow<List<JobApplication>> {
        return repository.getAllJobs().map { jobs ->
            if (isAscending) {
                jobs.sortedBy { it.dateAdded }
            } else {
                jobs.sortedByDescending { it.dateAdded }
            }
        }
    }
}
