package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.domain.model.*
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

class GetJobAnalyticsUseCase(
    private val repository: JobRepository
) {
    operator fun invoke(): Flow<JobAnalytics> {
        return repository.getAllJobs().map { jobs ->
            val total = jobs.size
            val statusCounts = jobs.groupingBy { it.status }.eachCount()
            
            // Conversion Funnel
            val reachedInterview = jobs.count { 
                it.status == JobStatus.INTERVIEW || it.status == JobStatus.OFFER || it.status == JobStatus.NO_OFFER 
            }
            val reachedOffer = jobs.count { it.status == JobStatus.OFFER }
            
            val responseRate = if (total > 0) (reachedInterview.toDouble() / total) * 100 else 0.0
            val offerRate = if (reachedInterview > 0) (reachedOffer.toDouble() / reachedInterview) * 100 else 0.0
            
            // Financials
            val annualSalaries = jobs.mapNotNull { job ->
                job.compensationAmount?.let { amount ->
                    if (job.compensationType == CompensationType.HOURLY) {
                        amount * 2080 // Standard 40h * 52 weeks
                    } else {
                        amount
                    }
                }
            }
            
            val avgSalary = if (annualSalaries.isNotEmpty()) annualSalaries.average() else null
            val maxSalary = annualSalaries.maxOrNull()
            val minSalary = annualSalaries.minOrNull()
            val hourlyCount = jobs.count { it.compensationType == CompensationType.HOURLY && it.compensationAmount != null }
            val annualCount = jobs.count { it.compensationType == CompensationType.ANNUAL && it.compensationAmount != null }
            
            // Timeline (Week-based)
            val now = Clock.System.now()
            val systemTZ = TimeZone.currentSystemDefault()
            val today = now.toLocalDateTime(systemTZ).date
            
            val startOfThisWeek = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
            val startOfLastWeek = startOfThisWeek.minus(7, DateTimeUnit.DAY)
            
            val appsThisWeek = jobs.count { 
                it.dateAdded.toLocalDateTime(systemTZ).date >= startOfThisWeek 
            }
            val appsLastWeek = jobs.count { 
                val date = it.dateAdded.toLocalDateTime(systemTZ).date
                date in startOfLastWeek..<startOfThisWeek
            }
            
            // Average apps per week (since first app)
            val firstAppDate = jobs.minByOrNull { it.dateAdded }?.dateAdded?.toLocalDateTime(systemTZ)?.date
            val weeksActive = if (firstAppDate != null) {
                val days = firstAppDate.daysUntil(today)
                (days / 7).coerceAtLeast(1)
            } else 1
            val avgAppsPerWeek = total.toDouble() / weeksActive
            
            // Distribution
            val jobTypeCounts = jobs.groupingBy { it.jobType }.eachCount()
            val topCompanies = jobs.groupingBy { it.companyName }
                .eachCount()
                .toList()
                .sortedByDescending { it.second }
                .take(5)
            
            JobAnalytics(
                totalApps = total,
                statusCounts = statusCounts,
                responseRate = responseRate,
                offerRate = offerRate,
                averageAnnualSalary = avgSalary,
                maxAnnualSalary = maxSalary,
                minAnnualSalary = minSalary,
                hourlyCount = hourlyCount,
                annualCount = annualCount,
                appsThisWeek = appsThisWeek,
                appsLastWeek = appsLastWeek,
                averageAppsPerWeek = avgAppsPerWeek,
                jobTypeCounts = jobTypeCounts,
                topCompanies = topCompanies
            )
        }
    }
}
