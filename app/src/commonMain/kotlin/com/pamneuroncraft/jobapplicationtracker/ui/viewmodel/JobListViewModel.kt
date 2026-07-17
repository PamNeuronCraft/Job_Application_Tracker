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

    private val _isAscending = MutableStateFlow(false)
    val isAscending: StateFlow<Boolean> = _isAscending.asStateFlow()

    val jobs: StateFlow<List<JobApplication>> = _isAscending
        .flatMapLatest { ascending ->
            jobUseCases.getJobs(ascending)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onToggleSort() {
        _isAscending.value = !_isAscending.value
    }

    fun onDeleteJob(job: JobApplication) {
        viewModelScope.launch {
            jobUseCases.deleteJob(job)
        }
    }
}
