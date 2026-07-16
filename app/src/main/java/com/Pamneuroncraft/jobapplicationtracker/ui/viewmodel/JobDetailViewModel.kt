package com.Pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.Pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import com.Pamneuroncraft.jobapplicationtracker.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobUseCases: JobUseCases,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _job = mutableStateOf<JobApplication?>(null)
    val job: State<JobApplication?> = _job

    fun loadJob(jobId: Int) {
        viewModelScope.launch {
            _job.value = jobUseCases.getJobById(jobId)
        }
    }

    fun onDeleteJob(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _job.value?.let {
                jobUseCases.deleteJob(it)
                onDeleted()
            }
        }
    }

    fun onUpdateInterviewDate(date: Date) {
        viewModelScope.launch {
            _job.value?.let {
                val updatedJob = it.copy(interviewDate = date)
                jobUseCases.updateJob(updatedJob)
                _job.value = updatedJob
                notificationScheduler.scheduleInterviewReminder(updatedJob)
            }
        }
    }
}
