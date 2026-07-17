package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.repository.ExtractedJob
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import kotlinx.coroutines.launch

class ImportViewModel(
    private val jobUseCases: JobUseCases
) : ViewModel() {

    sealed class ImportState {
        object Idle : ImportState()
        object Loading : ImportState()
        data class Success(val job: ExtractedJob) : ImportState()
        data class Error(val message: String) : ImportState()
    }

    private val _importState = mutableStateOf<ImportState>(ImportState.Idle)
    val importState: State<ImportState> = _importState

    fun extractJob(url: String) {
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                val extractedJob = jobUseCases.extractJobFromUrl(url)
                _importState.value = ImportState.Success(extractedJob)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Failed to extract job details")
            }
        }
    }

    fun resetState() {
        _importState.value = ImportState.Idle
    }
}
