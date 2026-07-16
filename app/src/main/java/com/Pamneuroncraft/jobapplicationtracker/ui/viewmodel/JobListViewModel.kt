package com.Pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.Pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class JobListViewModel @Inject constructor(
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
