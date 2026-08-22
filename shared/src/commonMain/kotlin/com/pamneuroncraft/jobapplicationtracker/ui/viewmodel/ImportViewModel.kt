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
                val message = when {
                    e.message?.contains("403") == true -> "AI service access denied. Configuration error."
                    e.message?.contains("404") == true -> "AI model not found. This can happen if the API key is invalid."
                    e.message?.contains("429") == true -> "Too many requests. Please try again later."
                    e.message?.contains("Field 'details' is required") == true -> "AI service connection error. Please verify your internet and try again."
                    e.message?.contains("Method doesn't allow unregistered callers") == true -> "AI service identity error. API key may be missing."
                    else -> "Failed to extract job details. Please try manual entry."
                }
                _importState.value = ImportState.Error(message)
            }
        }
    }

    fun resetState() {
        _importState.value = ImportState.Idle
    }
}
