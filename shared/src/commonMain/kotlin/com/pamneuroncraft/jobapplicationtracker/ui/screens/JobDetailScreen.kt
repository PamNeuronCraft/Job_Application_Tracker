package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.domain.model.AppCurrency
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration
import com.pamneuroncraft.jobapplicationtracker.shared.*
import com.pamneuroncraft.jobapplicationtracker.ui.theme.JobApplicationTrackerTheme
import com.pamneuroncraft.jobapplicationtracker.ui.theme.getJobStatusColor
import com.pamneuroncraft.jobapplicationtracker.ui.util.CurrencyFormatter
import com.pamneuroncraft.jobapplicationtracker.ui.util.DateFormatter
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.JobDetailViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: String,
    onBack: () -> Unit,
    onEditJob: (String) -> Unit,
    isPane: Boolean = false,
    viewModel: JobDetailViewModel = koinViewModel()
) {
    LaunchedEffect(jobId) {
        viewModel.loadJob(jobId)
    }

    val job by viewModel.job
    val preferredCurrency by viewModel.preferredCurrency.collectAsState()
    
    JobDetailContent(
        job = job,
        preferredCurrency = preferredCurrency,
        onBack = onBack,
        onEditJob = { onEditJob(jobId) },
        onDeleteJob = { viewModel.onDeleteJob(onBack) },
        onUpdateInterviewDate = { date, duration -> viewModel.onUpdateInterviewDate(date, duration) },
        isPane = isPane
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailContent(
    job: JobApplication?,
    preferredCurrency: AppCurrency,
    onBack: () -> Unit,
    onEditJob: () -> Unit,
    onDeleteJob: () -> Unit,
    onUpdateInterviewDate: (Instant, ReminderDuration?) -> Unit,
    isPane: Boolean = false
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val containerColor = if (job != null) getJobStatusColor(job.status, isDark) else MaterialTheme.colorScheme.surface
    val contentColor = contentColorFor(containerColor)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.job_details)) },
                navigationIcon = {
                    if (!isPane) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onEditJob) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.edit))
                    }
                    IconButton(onClick = onDeleteJob) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor
                )
            )
        },
        containerColor = containerColor,
        contentColor = contentColor
    ) { padding ->
        job?.let { currentJob ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailItem(label = stringResource(Res.string.label_job_name), value = currentJob.jobName)
                    DetailItem(label = stringResource(Res.string.label_company_name), value = currentJob.companyName)
                    DetailItem(label = stringResource(Res.string.label_description), value = currentJob.description)
                    DetailItem(label = stringResource(Res.string.label_job_type), value = stringResource(currentJob.jobType.labelRes))
                    DetailItem(
                        label = stringResource(Res.string.label_compensation), 
                        value = currentJob.compensationAmount?.let { amount ->
                            val formatted = CurrencyFormatter.format(amount, preferredCurrency)
                            "$formatted / ${if (currentJob.compensationType == com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType.HOURLY) "hr" else "yr"}"
                        } ?: stringResource(Res.string.not_set)
                    )
                    DetailItem(label = stringResource(Res.string.label_status), value = stringResource(currentJob.status.labelRes))
                    DetailItem(label = stringResource(Res.string.label_date_added), value = DateFormatter.format(currentJob.dateAdded, "MMM dd, yyyy"))

                    if (currentJob.status == JobStatus.INTERVIEW) {
                        HorizontalDivider(color = LocalContentColor.current.copy(alpha = 0.2f))
                        DetailItem(
                            label = stringResource(Res.string.label_interview_date_time),
                            value = currentJob.interviewDate?.let {
                                DateFormatter.format(it, "MMM dd, yyyy HH:mm")
                            } ?: stringResource(Res.string.not_set)
                        )
                        DetailItem(
                            label = stringResource(Res.string.label_reminder),
                            value = currentJob.reminderDuration?.let { stringResource(it.labelRes) } ?: stringResource(Res.string.no_reminder_set)
                        )
                    }

                    HorizontalDivider(color = LocalContentColor.current.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(stringResource(Res.string.quick_action_set_interview), style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = currentJob.interviewDate?.let { 
                                    DateFormatter.format(it, "MMM dd, yyyy")
                                } ?: stringResource(Res.string.not_set),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(Res.string.set_date))
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onUpdateInterviewDate(Instant.fromEpochMilliseconds(millis), null)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview
@Composable
fun JobDetailScreenPreview() {
    JobApplicationTrackerTheme {
        JobDetailContent(
            job = JobApplication(
                id = "1",
                jobName = "Android Developer",
                companyName = "Google",
                description = "Build amazing things.",
                jobType = JobType.REMOTE,
                compensationAmount = 150000.0,
                compensationType = com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType.ANNUAL,
                status = JobStatus.INTERVIEW,
            ),
            preferredCurrency = AppCurrency.USD,
            onBack = {},
            onEditJob = {},
            onDeleteJob = {},
            onUpdateInterviewDate = { _, _ -> }
        )
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium, 
            color = LocalContentColor.current.copy(alpha = 0.8f)
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyLarge, 
            fontWeight = FontWeight.Medium
        )
    }
}


