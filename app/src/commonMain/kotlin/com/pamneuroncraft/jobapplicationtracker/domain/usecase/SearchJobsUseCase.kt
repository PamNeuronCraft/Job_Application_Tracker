package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import kotlinx.coroutines.flow.Flow

class SearchJobsUseCase(
    private val repository: JobRepository
) {
    operator fun invoke(query: String): Flow<List<JobApplication>> {
        return repository.searchJobs(query)
    }
}
