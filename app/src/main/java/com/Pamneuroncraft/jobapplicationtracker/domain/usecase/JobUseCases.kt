package com.Pamneuroncraft.jobapplicationtracker.domain.usecase

data class JobUseCases(
    val getJobs: GetJobsUseCase,
    val getJobById: GetJobByIdUseCase,
    val addJob: AddJobUseCase,
    val updateJob: UpdateJobUseCase,
    val deleteJob: DeleteJobUseCase
)
