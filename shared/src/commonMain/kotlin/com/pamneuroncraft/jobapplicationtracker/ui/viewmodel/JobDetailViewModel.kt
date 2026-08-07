package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import com.pamneuroncraft.jobapplicationtracker.domain.repository.NotificationService
import kotlinx.coroutines.launch
import kotlin.time.Instant

class JobDetailViewModel(
    private val jobUseCases: JobUseCases,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _job = mutableStateOf<JobApplication?>(null)
    val job: State<JobApplication?> = _job

    fun loadJob(jobId: String) {
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

    fun onUpdateInterviewDate(date: Instant, reminderDuration: ReminderDuration? = null) {
        viewModelScope.launch {
            _job.value?.let {
                val updatedJob = it.copy(
                    interviewDate = date,
                    reminderDuration = reminderDuration ?: it.reminderDuration ?: ReminderDuration.THIRTY_MINUTES
                )
                jobUseCases.updateJob(updatedJob)
                _job.value = updatedJob
                notificationService.scheduleInterviewReminder(updatedJob)
            }
        }
    }
}

