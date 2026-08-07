package com.pamneuroncraft.jobapplicationtracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType
import com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration
import kotlin.time.Instant

@Entity(
    tableName = "job_applications",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["status"]),
        Index(value = ["updatedAt"]),
        Index(value = ["dateAdded"])
    ]
)
data class JobApplicationEntity(
    @PrimaryKey val id: String,
    val jobName: String,
    val companyName: String,
    val description: String,
    val jobType: String,
    val compensationAmount: Double?,
    val compensationType: String,
    val status: String,
    val dateAdded: Long,
    val interviewDate: Long?,
    val reminderDuration: String?,
    val userId: String?,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val lastSyncedAt: Long? = null
) {
    fun toDomainModel(): JobApplication {
        return JobApplication(
            id = id,
            jobName = jobName,
            companyName = companyName,
            description = description,
            jobType = JobType.valueOf(jobType),
            compensationAmount = compensationAmount,
            compensationType = CompensationType.valueOf(compensationType),
            status = JobStatus.valueOf(status),
            dateAdded = Instant.fromEpochMilliseconds(dateAdded),
            interviewDate = interviewDate?.let { Instant.fromEpochMilliseconds(it) },
            reminderDuration = reminderDuration?.let { ReminderDuration.valueOf(it) },
            userId = userId,
            updatedAt = Instant.fromEpochMilliseconds(updatedAt),
            isDeleted = isDeleted,
            lastSyncedAt = lastSyncedAt?.let { Instant.fromEpochMilliseconds(it) }
        )
    }

    companion object {
        fun fromDomainModel(job: JobApplication): JobApplicationEntity {
            return JobApplicationEntity(
                id = job.id,
                jobName = job.jobName,
                companyName = job.companyName,
                description = job.description,
                jobType = job.jobType.name,
                compensationAmount = job.compensationAmount,
                compensationType = job.compensationType.name,
                status = job.status.name,
                dateAdded = job.dateAdded.toEpochMilliseconds(),
                interviewDate = job.interviewDate?.toEpochMilliseconds(),
                reminderDuration = job.reminderDuration?.name,
                userId = job.userId,
                updatedAt = job.updatedAt.toEpochMilliseconds(),
                isDeleted = job.isDeleted,
                lastSyncedAt = job.lastSyncedAt?.toEpochMilliseconds()
            )
        }
    }
}
