package com.Pamneuroncraft.jobapplicationtracker.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface JobTrackerKey : NavKey

@Serializable
data object JobListKey : JobTrackerKey

@Serializable
data class JobDetailKey(val jobId: Int) : JobTrackerKey

@Serializable
data class JobAddEditKey(val jobId: Int? = null) : JobTrackerKey
