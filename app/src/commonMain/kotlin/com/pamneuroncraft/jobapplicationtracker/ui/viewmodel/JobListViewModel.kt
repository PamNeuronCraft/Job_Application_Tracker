package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class JobListViewModel(
    private val jobUseCases: JobUseCases
) : ViewModel() {

    val jobs: StateFlow<List<JobApplication>> = jobUseCases.getJobs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onDeleteJob(job: JobApplication) {
        viewModelScope.launch {
            jobUseCases.deleteJob(job)
        }
    }
}
