package com.pamneuroncraft.jobapplicationtracker.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = JobApplicationEntity::class)
@Entity(tableName = "job_search")
data class JobSearchEntity(
    val jobName: String,
    val companyName: String,
    val description: String
)
