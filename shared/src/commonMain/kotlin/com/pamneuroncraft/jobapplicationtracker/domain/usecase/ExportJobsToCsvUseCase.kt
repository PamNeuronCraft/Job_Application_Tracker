package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import kotlinx.coroutines.flow.first

class ExportJobsToCsvUseCase(
    private val repository: JobRepository
) {
    suspend operator fun invoke(): String {
        val jobs = repository.getAllJobs().first()
        
        val header = "Job Name,Company Name,Status,Job Type,Amount,Type,Date Added\n"
        
        val rows = jobs.joinToString("\n") { job ->
            listOf(
                escapeCsv(job.jobName),
                escapeCsv(job.companyName),
                job.status.name,
                job.jobType.name,
                job.compensationAmount?.toString() ?: "",
                job.compensationType.name,
                job.dateAdded.toString()
            ).joinToString(",")
        }
        
        return header + rows
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        return if (needsQuotes) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
