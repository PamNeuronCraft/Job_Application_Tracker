package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

class GetJobsUseCase(
    private val repository: JobRepository
) {
    operator fun invoke(): Flow<List<JobApplication>> {
        return repository.getAllJobs().map { jobs ->
            val now = Clock.System.now()
            jobs.sortedWith(
                compareByDescending<JobApplication> { job ->
                    // Priority 1: Future interview date
                    job.status == JobStatus.INTERVIEW && job.interviewDate != null && job.interviewDate > now
                }.thenBy { job ->
                    // Closest future interview date first
                    if (job.status == JobStatus.INTERVIEW && job.interviewDate != null && job.interviewDate > now) {
                        job.interviewDate.toEpochMilliseconds()
                    } else {
                        Long.MAX_VALUE
                    }
                }.thenByDescending { job ->
                    // Priority 2: Date added for everything else
                    job.dateAdded.toEpochMilliseconds()
                }
            )
        }
    }
}

