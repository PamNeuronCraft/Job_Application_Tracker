package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository

class GetJobByIdUseCase(
    private val repository: JobRepository
) {
    suspend operator fun invoke(id: String): JobApplication? {
        return repository.getJobById(id)
    }
}
