package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository

class GetJobByIdUseCase(
    private val repository: JobRepository
) {
    suspend operator fun invoke(id: Int): JobApplication? {
        return repository.getJobById(id)
    }
}
