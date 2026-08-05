package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository

class DeleteJobUseCase(
    private val repository: JobRepository
) {
    suspend operator fun invoke(job: JobApplication) {
        repository.deleteJob(job)
    }
}
