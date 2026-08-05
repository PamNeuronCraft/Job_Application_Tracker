package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository

class UpdateJobUseCase(
    private val repository: JobRepository
) {
    suspend operator fun invoke(job: JobApplication) {
        repository.updateJob(job)
    }
}
