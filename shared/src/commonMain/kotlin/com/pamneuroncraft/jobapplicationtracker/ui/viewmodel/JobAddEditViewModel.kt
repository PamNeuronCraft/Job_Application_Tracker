package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.domain.model.AppCurrency
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.pamneuroncraft.jobapplicationtracker.domain.repository.NotificationService
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.JobUseCases
import com.pamneuroncraft.jobapplicationtracker.util.AnalyticsHelper
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Instant

class JobAddEditViewModel(
    private val jobUseCases: JobUseCases,
    private val notificationService: NotificationService,
    private val localSettings: LocalSettings,
    private val analyticsHelper: AnalyticsHelper
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

    val preferredCurrency: StateFlow<AppCurrency> = localSettings.preferredCurrencyFlow

    private val _status = mutableStateOf(JobStatus.APPLIED)
    val status: State<JobStatus> = _status

    private val _interviewDate = mutableStateOf<Instant?>(null)
    val interviewDate: State<Instant?> = _interviewDate

    private val _reminderDuration = mutableStateOf<com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration?>(null)
    val reminderDuration: State<com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration?> = _reminderDuration

    private val _isAutoExtracting = mutableStateOf(false)
    val isAutoExtracting: State<Boolean> = _isAutoExtracting

    private val _extractionError = mutableStateOf<String?>(null)
    val extractionError: State<String?> = _extractionError

    private var currentJobId: String? = null
    private var loadedJob: JobApplication? = null

    fun loadJob(
        jobId: String?,
        prefilledJobName: String? = null,
        prefilledCompanyName: String? = null,
        prefilledDescription: String? = null,
        prefilledCompensation: String? = null,
        initialUrl: String? = null
    ) {
        println("JobAddEditViewModel: loadJob called with jobId=$jobId, prefilledJobName=$prefilledJobName")
        if (jobId != null) {
            viewModelScope.launch {
                jobUseCases.getJobById(jobId)?.let { job ->
                    currentJobId = job.id
                    loadedJob = job
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
            // Reset state for new job entry
            _jobName.value = ""
            _companyName.value = ""
            _description.value = ""
            _jobType.value = JobType.REMOTE
            _compensationAmount.value = ""
            _compensationType.value = com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType.ANNUAL
            _status.value = JobStatus.APPLIED
            _interviewDate.value = null
            _reminderDuration.value = null
            _isAutoExtracting.value = false
            currentJobId = null
            loadedJob = null

            prefilledJobName?.let { _jobName.value = it }
            prefilledCompanyName?.let { _companyName.value = it }
            prefilledDescription?.let { _description.value = it }
            prefilledCompensation?.let { parseCompensation(it) }

            initialUrl?.let { url ->
                viewModelScope.launch {
                    _isAutoExtracting.value = true
                    _extractionError.value = null
                    try {
                        val extracted = jobUseCases.extractJobFromUrl(url)
                        extracted.jobName?.let { _jobName.value = it }
                        extracted.companyName?.let { _companyName.value = it }
                        extracted.description?.let { _description.value = it }
                        extracted.compensation?.let { parseCompensation(it) }
                    } catch (e: Exception) {
                        _extractionError.value = "Unable to auto-extract details from this link. Please fill in the details manually."
                        analyticsHelper.logNonFatal(e)
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
            val job = if (loadedJob != null) {
                loadedJob!!.copy(
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
            } else if (currentJobId != null) {
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
        println("JobAddEditViewModel: Parsing compensation from value: $value")
        
        // Try to extract a number or a range
        val numberRegex = """(\d+([.,]\d+)?)""".toRegex()
        val matches = numberRegex.findAll(value).toList()
        
        if (matches.size >= 2) {
            // Likely a range, calculate the average
            val val1 = matches[0].value.replace(",", ".").toDoubleOrNull() ?: 0.0
            val val2 = matches[1].value.replace(",", ".").toDoubleOrNull() ?: 0.0
            val average = (val1 + val2) / 2
            _compensationAmount.value = average.toString().removeSuffix(".0")
        } else if (matches.size == 1) {
            _compensationAmount.value = matches[0].value.replace(",", ".")
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

