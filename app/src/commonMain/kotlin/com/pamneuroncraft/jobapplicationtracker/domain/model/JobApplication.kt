package com.pamneuroncraft.jobapplicationtracker.domain.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Serializable
data class JobApplication(
    val id: Int = 0,
    val jobName: String,
    val companyName: String,
    val description: String,
    val jobType: JobType,
    val compensation: String,
    val status: JobStatus,
    val dateAdded: Instant = Clock.System.now(),
    val interviewDate: Instant? = null
)

@Serializable
enum class JobType {
    REMOTE, ONSITE, HYBRID
}

@Serializable
enum class JobStatus {
    APPLIED, INTERVIEWING, JOB_OFFER, NO_OFFER
}
