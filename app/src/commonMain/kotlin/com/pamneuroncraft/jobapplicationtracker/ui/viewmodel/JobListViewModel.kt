package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import androidx.paging.PagingData
import androidx.paging.cachedIn

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class JobListViewModel(
    private val jobUseCases: JobUseCases
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

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDeleteJob(job: JobApplication) {
        viewModelScope.launch {
            jobUseCases.deleteJob(job)
        }
    }
}
