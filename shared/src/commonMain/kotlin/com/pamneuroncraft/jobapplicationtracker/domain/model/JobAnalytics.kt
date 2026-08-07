package com.pamneuroncraft.jobapplicationtracker.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class JobAnalytics(
    val totalApps: Int,
    val statusCounts: Map<JobStatus, Int>,
    
    // Conversion Funnel
    val responseRate: Double, // (Interviews / Total) * 100
    val offerRate: Double,    // (Offers / Interviews) * 100
    
    // Financials
    val averageAnnualSalary: Double?,
    val maxAnnualSalary: Double?,
    val minAnnualSalary: Double?,
    val hourlyCount: Int,
    val annualCount: Int,
    
    // Timeline/Velocity
    val appsThisWeek: Int,
    val appsLastWeek: Int,
    val averageAppsPerWeek: Double,
    
    // Distribution
    val jobTypeCounts: Map<JobType, Int>,
    val topCompanies: List<Pair<String, Int>>
)
