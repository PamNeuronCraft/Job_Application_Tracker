package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SummaryViewModel(
    jobUseCases: JobUseCases
) : ViewModel() {

    val statusCounts: StateFlow<Map<JobStatus, Int>> = jobUseCases.getJobs()
        .map { jobs ->
            jobs.groupingBy { it.status }.eachCount()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    
    val totalJobs: StateFlow<Int> = statusCounts.map { counts ->
        counts.values.sum()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
