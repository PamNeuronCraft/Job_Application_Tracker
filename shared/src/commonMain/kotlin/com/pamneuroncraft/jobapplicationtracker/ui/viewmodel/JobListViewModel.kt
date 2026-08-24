package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.AppConfig
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.domain.repository.AuthService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.SyncManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import androidx.paging.PagingData
import androidx.paging.cachedIn

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class JobListViewModel(
    private val jobUseCases: JobUseCases,
    private val localSettings: LocalSettings,
    private val syncManager: SyncManager,
    private val authService: AuthService,
    private val appConfig: AppConfig
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

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

    fun onRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            if (authService.isUserSignedIn() && appConfig.featureGoogleDriveBackup) {
                syncManager.triggerSync()
            }
            // Provide a small delay so the user sees the refresh indicator
            kotlinx.coroutines.delay(1000)
            _isRefreshing.value = false
        }
    }

    fun onDeleteJob(job: JobApplication) {
        viewModelScope.launch {
            jobUseCases.deleteJob(job)
        }
    }
}
