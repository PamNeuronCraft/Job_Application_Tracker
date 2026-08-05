package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobAnalytics
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.GetJobAnalyticsUseCase
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SummaryViewModel(
    private val getJobAnalyticsUseCase: GetJobAnalyticsUseCase
) : ViewModel() {

    val analytics: StateFlow<JobAnalytics?> = getJobAnalyticsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
