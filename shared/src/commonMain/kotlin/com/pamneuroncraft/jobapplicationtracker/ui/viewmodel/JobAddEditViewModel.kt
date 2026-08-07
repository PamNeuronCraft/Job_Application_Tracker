package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.pamneuroncraft.jobapplicationtracker.domain.repository.NotificationService
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import kotlinx.coroutines.launch
import kotlin.time.Instant

class JobAddEditViewModel(
    private val jobUseCases: JobUseCases,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _jobName = mutableStateOf("")
    val jobName: State<String> = _jobName

    private val _companyName = mutableStateOf("")
    val companyName: State<String> = _companyName

    private val _description = mutableStateOf("")
    val description: State<String> = _description

    private val _jobType = mutableStateOf(JobType.REMOTE)
    val jobType: State<JobType> = _jobType

    private val _compensationAmount = mutableStateOf("")
    val compensationAmount: State<String> = _compensationAmount

    private val _compensationType = mutableStateOf(com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType.ANNUAL)
    val compensationType: State<com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType> = _compensationType

    private val _status = mutableStateOf(JobStatus.APPLIED)
    val status: State<JobStatus> = _status

    private val _interviewDate = mutableStateOf<Instant?>(null)
    val interviewDate: State<Instant?> = _interviewDate

    private val _reminderDuration = mutableStateOf<com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration?>(null)
    val reminderDuration: State<com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration?> = _reminderDuration

    private val _isAutoExtracting = mutableStateOf(false)
    val isAutoExtracting: State<Boolean> = _isAutoExtracting

    private var currentJobId: String? = null

    fun loadJob(
        jobId: String?,
        prefilledJobName: String? = null,
        prefilledCompanyName: String? = null,
        prefilledDescription: String? = null,
        prefilledCompensation: String? = null,
        initialUrl: String? = null
    ) {
        if (jobId != null) {
            viewModelScope.launch {
                jobUseCases.getJobById(jobId)?.let { job ->
                    currentJobId = job.id
                    _jobName.value = job.jobName
                    _companyName.value = job.companyName
                    _description.value = job.description
                    _jobType.value = job.jobType
                    _compensationAmount.value = job.compensationAmount?.toString() ?: ""
                    _compensationType.value = job.compensationType
                    _status.value = job.status
                    _interviewDate.value = job.interviewDate
                    _reminderDuration.value = job.reminderDuration
                }
            }
        } else {
            prefilledJobName?.let { _jobName.value = it }
            prefilledCompanyName?.let { _companyName.value = it }
            prefilledDescription?.let { _description.value = it }
            prefilledCompensation?.let { parseCompensation(it) }

            initialUrl?.let { url ->
                viewModelScope.launch {
                    _isAutoExtracting.value = true
                    try {
                        val extracted = jobUseCases.extractJobFromUrl(url)
                        extracted.jobName?.let { _jobName.value = it }
                        extracted.companyName?.let { _companyName.value = it }
                        extracted.description?.let { _description.value = it }
                        extracted.compensation?.let { parseCompensation(it) }
                    } catch (e: Exception) {
                        // Silent fail for auto-extract
                    } finally {
                        _isAutoExtracting.value = false
                    }
                }
            }
        }
    }

    fun onJobNameChange(value: String) { _jobName.value = value }
    fun onCompanyNameChange(value: String) { _companyName.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }
    fun onJobTypeChange(value: JobType) { _jobType.value = value }
    fun onCompensationAmountChange(value: String) { 
        // Only allow numeric input (and decimal point)
        if (value.isEmpty() || value.toDoubleOrNull() != null || value.endsWith(".")) {
            _compensationAmount.value = value
        }
    }
    fun onCompensationTypeChange(value: com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType) { 
        _compensationType.value = value 
    }
    fun onStatusChange(value: JobStatus) { _status.value = value }
    fun onInterviewDateChange(value: Instant?) { 
        _interviewDate.value = value 
        if (value == null) {
            _reminderDuration.value = null
        }
    }
    
    fun onReminderDurationChange(value: com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration?) {
        _reminderDuration.value = value
    }

    fun onSaveJob(onSaved: () -> Unit) {
        viewModelScope.launch {
            val amount = _compensationAmount.value.toDoubleOrNull()
            val job = if (currentJobId != null) {
                JobApplication(
                    id = currentJobId!!,
                    jobName = _jobName.value,
                    companyName = _companyName.value,
                    description = _description.value,
                    jobType = _jobType.value,
                    compensationAmount = amount,
                    compensationType = _compensationType.value,
                    status = _status.value,
                    interviewDate = _interviewDate.value,
                    reminderDuration = _reminderDuration.value
                )
            } else {
                JobApplication(
                    jobName = _jobName.value,
                    companyName = _companyName.value,
                    description = _description.value,
                    jobType = _jobType.value,
                    compensationAmount = amount,
                    compensationType = _compensationType.value,
                    status = _status.value,
                    interviewDate = _interviewDate.value,
                    reminderDuration = _reminderDuration.value
                )
            }
            if (currentJobId != null) {
                jobUseCases.updateJob(job)
            } else {
                jobUseCases.addJob(job)
            }
            notificationService.scheduleInterviewReminder(job)
            onSaved()
        }
    }

    private fun parseCompensation(value: String) {
        // Try to extract a number
        val numberRegex = """(\d+([.,]\d+)?)""".toRegex()
        val match = numberRegex.find(value)
        if (match != null) {
            _compensationAmount.value = match.value.replace(",", ".")
        }

        // Try to determine type
        val lowerValue = value.lowercase()
        if (lowerValue.contains("hour") || lowerValue.contains("/hr") || lowerValue.contains("/ hr") || lowerValue.contains("hrly")) {
            _compensationType.value = com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType.HOURLY
        } else {
            _compensationType.value = com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType.ANNUAL
        }
    }
}

