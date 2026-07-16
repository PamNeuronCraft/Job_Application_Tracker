package com.Pamneuroncraft.jobapplicationtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobType
import java.util.Date

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
            dateAdded = Date(dateAdded),
            interviewDate = interviewDate?.let { Date(it) }
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
                dateAdded = job.dateAdded.time,
                interviewDate = job.interviewDate?.time
            )
        }
    }
}
