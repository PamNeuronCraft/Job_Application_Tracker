package com.Pamneuroncraft.jobapplicationtracker.domain.usecase

import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.Pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository

class GetJobByIdUseCase(
    private val repository: JobRepository
) {
    suspend operator fun invoke(id: Int): JobApplication? {
        return repository.getJobById(id)
    }
}
