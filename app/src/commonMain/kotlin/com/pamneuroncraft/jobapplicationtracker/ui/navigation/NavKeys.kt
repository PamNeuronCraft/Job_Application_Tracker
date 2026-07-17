package com.pamneuroncraft.jobapplicationtracker.ui.navigation

import kotlinx.serialization.Serializable

sealed interface JobTrackerKey

@Serializable
data object JobListKey : JobTrackerKey

@Serializable
data object OnboardingKey : JobTrackerKey

@Serializable
data class JobDetailKey(val jobId: Int) : JobTrackerKey

@Serializable
data class JobAddEditKey(
    val jobId: Int? = null,
    val prefilledJobName: String? = null,
    val prefilledCompanyName: String? = null,
    val prefilledDescription: String? = null,
    val prefilledCompensation: String? = null
) : JobTrackerKey

@Serializable
data object ProfileKey : JobTrackerKey
