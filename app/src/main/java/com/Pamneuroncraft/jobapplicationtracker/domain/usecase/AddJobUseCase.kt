package com.Pamneuroncraft.jobapplicationtracker.domain.usecase

import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.Pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository

class AddJobUseCase(
    private val repository: JobRepository
) {
    suspend operator fun invoke(job: JobApplication) {
        repository.insertJob(job)
    }
}
