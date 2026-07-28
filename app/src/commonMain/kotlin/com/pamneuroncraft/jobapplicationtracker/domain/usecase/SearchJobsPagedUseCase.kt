package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class SearchJobsPagedUseCase(
    private val repository: JobRepository
) {
    operator fun invoke(query: String): Flow<PagingData<JobApplication>> {
        return repository.searchJobsPaged(query)
    }
}
