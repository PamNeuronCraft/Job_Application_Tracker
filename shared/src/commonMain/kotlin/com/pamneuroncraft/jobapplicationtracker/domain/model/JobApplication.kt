package com.pamneuroncraft.jobapplicationtracker.domain.model

import com.benasher44.uuid.uuid4
import com.pamneuroncraft.jobapplicationtracker.Res
import com.pamneuroncraft.jobapplicationtracker.job_status_applied
import com.pamneuroncraft.jobapplicationtracker.job_status_interview
import com.pamneuroncraft.jobapplicationtracker.job_status_no_offer
import com.pamneuroncraft.jobapplicationtracker.job_status_offer
import com.pamneuroncraft.jobapplicationtracker.job_type_hybrid
import com.pamneuroncraft.jobapplicationtracker.job_type_onsite
import com.pamneuroncraft.jobapplicationtracker.job_type_remote
import com.pamneuroncraft.jobapplicationtracker.reminder_one_day
import com.pamneuroncraft.jobapplicationtracker.reminder_thirty_minutes
import com.pamneuroncraft.jobapplicationtracker.reminder_two_hours
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.serialization.Serializable

@Serializable
data class JobApplication(
    val id: String = uuid4().toString(),
    val jobName: String,
    val companyName: String,
    val description: String,
    val jobType: JobType,
    val compensationAmount: Double?,
    val compensationType: CompensationType = CompensationType.ANNUAL,
    val status: JobStatus,
    @Serializable(with = DateSerializer::class)
    val dateAdded: Instant = Clock.System.now(),
    @Serializable(with = DateSerializer::class)
    val interviewDate: Instant? = null,
    val reminderDuration: ReminderDuration? = null,
    val userId: String? = null,
    @Serializable(with = DateSerializer::class)
    val updatedAt: Instant = Clock.System.now(),
    val isDeleted: Boolean = false,
    @Serializable(with = DateSerializer::class)
    val lastSyncedAt: Instant? = null
)

@Serializable
enum class ReminderDuration(val label: String) {
    ONE_DAY("1 day before"),
    TWO_HOURS("2 hours before"),
    THIRTY_MINUTES("30 minutes before");

    val labelRes get() = when (this) {
        ONE_DAY -> Res.string.reminder_one_day
        TWO_HOURS -> Res.string.reminder_two_hours
        THIRTY_MINUTES -> Res.string.reminder_thirty_minutes
    }
}

@Serializable
enum class JobType {
    REMOTE, ONSITE, HYBRID;

    val labelRes get() = when (this) {
        REMOTE -> Res.string.job_type_remote
        ONSITE -> Res.string.job_type_onsite
        HYBRID -> Res.string.job_type_hybrid
    }
}

@Serializable
enum class JobStatus {
    APPLIED, INTERVIEW, OFFER, NO_OFFER;

    val labelRes get() = when (this) {
        APPLIED -> Res.string.job_status_applied
        INTERVIEW -> Res.string.job_status_interview
        OFFER -> Res.string.job_status_offer
        NO_OFFER -> Res.string.job_status_no_offer
    }
}
