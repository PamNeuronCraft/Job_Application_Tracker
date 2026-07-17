package com.pamneuroncraft.jobapplicationtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobType
import kotlinx.datetime.Instant

@Entity(tableName = "job_applications")
data class JobApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobName: String,
    val companyName: String,
    val description: String,
    val jobType: String,
    val compensation: String,
    val status: String,
    val dateAdded: Long,
    val interviewDate: Long?
) {
    fun toDomainModel(): JobApplication {
        return JobApplication(
            id = id,
            jobName = jobName,
            companyName = companyName,
            description = description,
            jobType = JobType.valueOf(jobType),
            compensation = compensation,
            status = JobStatus.valueOf(status),
            dateAdded = Instant.fromEpochMilliseconds(dateAdded),
            interviewDate = interviewDate?.let { Instant.fromEpochMilliseconds(it) }
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
                compensation = job.compensation,
                status = job.status.name,
                dateAdded = job.dateAdded.toEpochMilliseconds(),
                interviewDate = job.interviewDate?.toEpochMilliseconds()
            )
        }
    }
}
