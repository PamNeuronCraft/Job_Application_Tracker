package com.pamneuroncraft.jobapplicationtracker.domain.model

import kotlinx.serialization.Serializable

enum class EmailProvider {
    GMAIL, OUTLOOK
}

data class EmailMessage(
    val id: String,
    val sender: String,
    val subject: String,
    val body: String,
    val timestamp: Long
)

@Serializable
data class JobStatusUpdate(
    val companyName: String,
    val jobTitle: String?,
    val newStatus: JobStatus,
    val confidence: Float,
    val sourceEmailId: String
)
