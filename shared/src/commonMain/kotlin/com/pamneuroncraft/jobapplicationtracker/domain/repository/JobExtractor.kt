package com.pamneuroncraft.jobapplicationtracker.domain.repository

import kotlinx.serialization.Serializable

@Serializable
data class ExtractedJob(
    val jobName: String? = null,
    val companyName: String? = null,
    val description: String? = null,
    val compensation: String? = null
)

interface JobExtractor {
    suspend fun extractFromUrl(url: String): ExtractedJob
}
