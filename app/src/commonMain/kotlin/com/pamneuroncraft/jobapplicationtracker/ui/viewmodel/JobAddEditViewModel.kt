package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import kotlinx.coroutines.launch

class JobAddEditViewModel(
    private val jobUseCases: JobUseCases
) : ViewModel() {

    private val _jobName = mutableStateOf("")
    val jobName: State<String> = _jobName

    private val _companyName = mutableStateOf("")
    val companyName: State<String> = _companyName

    private val _description = mutableStateOf("")
    val description: State<String> = _description

    private val _jobType = mutableStateOf(JobType.REMOTE)
    val jobType: State<JobType> = _jobType

    private val _compensation = mutableStateOf("")
    val compensation: State<String> = _compensation

    private val _status = mutableStateOf(JobStatus.APPLIED)
    val status: State<JobStatus> = _status

    private val _interviewDate = mutableStateOf<kotlinx.datetime.Instant?>(null)
    val interviewDate: State<kotlinx.datetime.Instant?> = _interviewDate

    private var currentJobId: Int? = null

    fun loadJob(
        jobId: Int?,
        prefilledJobName: String? = null,
        prefilledCompanyName: String? = null,
        prefilledDescription: String? = null,
        prefilledCompensation: String? = null
    ) {
        if (jobId != null) {
            viewModelScope.launch {
                jobUseCases.getJobById(jobId)?.let { job ->
                    currentJobId = job.id
                    _jobName.value = job.jobName
                    _companyName.value = job.companyName
                    _description.value = job.description
                    _jobType.value = job.jobType
                    _compensation.value = job.compensation
                    _status.value = job.status
                    _interviewDate.value = job.interviewDate
                }
            }
        } else {
            prefilledJobName?.let { _jobName.value = it }
            prefilledCompanyName?.let { _companyName.value = it }
            prefilledDescription?.let { _description.value = it }
            prefilledCompensation?.let { _compensation.value = it }
        }
    }

    fun onJobNameChange(value: String) { _jobName.value = value }
    fun onCompanyNameChange(value: String) { _companyName.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }
    fun onJobTypeChange(value: JobType) { _jobType.value = value }
    fun onCompensationChange(value: String) { _compensation.value = value }
    fun onStatusChange(value: JobStatus) { _status.value = value }
    fun onInterviewDateChange(value: kotlinx.datetime.Instant?) { _interviewDate.value = value }

    fun onSaveJob(onSaved: () -> Unit) {
        viewModelScope.launch {
            val job = JobApplication(
                id = currentJobId ?: 0,
                jobName = _jobName.value,
                companyName = _companyName.value,
                description = _description.value,
                jobType = _jobType.value,
                compensation = _compensation.value,
                status = _status.value,
                interviewDate = _interviewDate.value
            )
            if (currentJobId != null) {
                jobUseCases.updateJob(job)
            } else {
                jobUseCases.addJob(job)
            }
            onSaved()
        }
    }
}
