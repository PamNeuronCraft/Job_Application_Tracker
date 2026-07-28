package com.pamneuroncraft.jobapplicationtracker.domain.usecase

data class JobUseCases(
    val getJobs: GetJobsUseCase,
    val getJobsPaged: GetJobsPagedUseCase,
    val getJobById: GetJobByIdUseCase,
    val addJob: AddJobUseCase,
    val updateJob: UpdateJobUseCase,
    val deleteJob: DeleteJobUseCase,
    val searchJobs: SearchJobsUseCase,
    val searchJobsPaged: SearchJobsPagedUseCase,
    val extractJobFromUrl: ExtractJobFromUrlUseCase
)
