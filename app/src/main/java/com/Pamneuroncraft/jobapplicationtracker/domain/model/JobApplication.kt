package com.Pamneuroncraft.jobapplicationtracker.domain.model

import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class JobApplication(
    val id: Int = 0,
    val jobName: String,
    val companyName: String,
    val description: String,
    val jobType: JobType,
    val compensation: String,
    val status: JobStatus,
    @Serializable(with = DateSerializer::class)
    val dateAdded: Date = Date(),
    @Serializable(with = DateSerializer::class)
    val interviewDate: Date? = null
)

@Serializable
enum class JobType {
    REMOTE, ONSITE, HYBRID
}

@Serializable
enum class JobStatus {
    APPLIED, INTERVIEWING, JOB_OFFER, NO_OFFER
}
