package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import androidx.paging.PagingData
import androidx.paging.cachedIn

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class JobListViewModel(
    private val jobUseCases: JobUseCases,
    private val localSettings: LocalSettings
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val pagedJobs: Flow<PagingData<JobApplication>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                jobUseCases.getJobsPaged()
            } else {
                jobUseCases.searchJobsPaged("*$query*")
            }
        }
        .cachedIn(viewModelScope)

    private val _shouldRequestReview = MutableSharedFlow<Unit>(replay = 0)
    val shouldRequestReview: SharedFlow<Unit> = _shouldRequestReview.asSharedFlow()

    init {
        checkIfReviewShouldBeRequested()
    }

    private fun checkIfReviewShouldBeRequested() {
        if (localSettings.isReviewRequested) return

        viewModelScope.launch {
            jobUseCases.getJobs().first().let { jobs ->
                if (jobs.size >= 5) {
                    _shouldRequestReview.emit(Unit)
                    localSettings.isReviewRequested = true
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDeleteJob(job: JobApplication) {
        viewModelScope.launch {
            jobUseCases.deleteJob(job)
        }
    }
}
