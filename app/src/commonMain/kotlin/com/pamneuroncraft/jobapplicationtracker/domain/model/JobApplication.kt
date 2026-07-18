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
    val interviewDate: Instant? = null,
    val reminderDuration: ReminderDuration? = null
)

@Serializable
enum class ReminderDuration(val label: String) {
    ONE_DAY("1 day before"),
    TWO_HOURS("2 hours before"),
    THIRTY_MINUTES("30 minutes before")
}

@Serializable
enum class JobType {
    REMOTE, ONSITE, HYBRID
}

@Serializable
enum class JobStatus {
    APPLIED, INTERVIEW, OFFER, NO_OFFER
}
