package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType
import com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration
import com.pamneuroncraft.jobapplicationtracker.ui.components.PermissionRationaleDialog
import com.pamneuroncraft.jobapplicationtracker.ui.util.DateFormatter
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.JobAddEditViewModel
import com.pamneuroncraft.jobapplicationtracker.util.Permission
import com.pamneuroncraft.jobapplicationtracker.util.PermissionManager
import com.pamneuroncraft.jobapplicationtracker.util.PermissionState
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import jobapplicationtracker.app.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobAddEditScreen(
    jobId: String?,
    prefilledJobName: String? = null,
    prefilledCompanyName: String? = null,
    prefilledDescription: String? = null,
    prefilledCompensation: String? = null,
    initialUrl: String? = null,
    onBack: () -> Unit,
    viewModel: JobAddEditViewModel = koinViewModel(),
    permissionManager: PermissionManager = koinInject()
) {
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showReminderMenu by remember { mutableStateOf(false) }

    LaunchedEffect(jobId) {
        viewModel.loadJob(
            jobId = jobId,
            prefilledJobName = prefilledJobName,
            prefilledCompanyName = prefilledCompanyName,
            prefilledDescription = prefilledDescription,
            prefilledCompensation = prefilledCompensation,
            initialUrl = initialUrl
        )
    }

    val jobName by viewModel.jobName
    val companyName by viewModel.companyName
    val description by viewModel.description
    val jobType by viewModel.jobType
    val compensationAmount by viewModel.compensationAmount
    val compensationType by viewModel.compensationType
    val status by viewModel.status
    val interviewDate by viewModel.interviewDate
    val reminderDuration by viewModel.reminderDuration
    val isAutoExtracting by viewModel.isAutoExtracting

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(if (jobId == null) Res.string.title_add_job else Res.string.title_edit_job)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                actions = {
                    if (isAutoExtracting) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 16.dp).size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onSaveJob(onBack) },
                        enabled = !isAutoExtracting
                    ) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(Res.string.save))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = jobName,
                onValueChange = { viewModel.onJobNameChange(it) },
                label = { Text(stringResource(Res.string.label_job_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = companyName,
                onValueChange = { viewModel.onCompanyNameChange(it) },
                label = { Text(stringResource(Res.string.label_company_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text(stringResource(Res.string.label_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = compensationAmount,
                    onValueChange = { viewModel.onCompensationAmountChange(it) },
                    label = { Text(stringResource(Res.string.label_compensation)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CompensationType.entries.forEach { type ->
                        FilterChip(
                            selected = compensationType == type,
                            onClick = { viewModel.onCompensationTypeChange(type) },
                            label = { Text(stringResource(type.labelRes)) }
                        )
                    }
                }
            }

            Text(stringResource(Res.string.label_job_type), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JobType.entries.forEach { type ->
                    FilterChip(
                        selected = jobType == type,
                        onClick = { viewModel.onJobTypeChange(type) },
                        label = { Text(stringResource(type.labelRes)) }
                    )
                }
            }

            Text(stringResource(Res.string.label_status), style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JobStatus.entries.forEach { jobStatus ->
                    FilterChip(
                        selected = status == jobStatus,
                        onClick = { viewModel.onStatusChange(jobStatus) },
                        label = { Text(stringResource(jobStatus.labelRes), style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (status == JobStatus.INTERVIEW) {
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text(stringResource(Res.string.interview_date)) },
                    supportingContent = {
                        Text(
                            text = interviewDate?.let {
                                DateFormatter.format(it, "MMM dd, yyyy")
                            } ?: stringResource(Res.string.no_date_set)
                        )
                    },
                    leadingContent = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    modifier = Modifier.clickable { showDatePicker = true }
                )

                ListItem(
                    headlineContent = { Text(stringResource(Res.string.interview_time)) },
                    supportingContent = {
                        Text(
                            text = interviewDate?.let {
                                DateFormatter.format(it, "HH:mm")
                            } ?: stringResource(Res.string.no_time_set)
                        )
                    },
                    leadingContent = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    modifier = Modifier.clickable { showTimePicker = true }
                )

                ListItem(
                    headlineContent = { Text(stringResource(Res.string.label_reminder)) },
                    supportingContent = {
                        Text(text = reminderDuration?.let { stringResource(it.labelRes) } ?: stringResource(Res.string.no_reminder_set))
                    },
                    leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    trailingContent = {
                        Box {
                            IconButton(onClick = { showReminderMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = showReminderMenu,
                                onDismissRequest = { showReminderMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.none)) },
                                    onClick = {
                                        viewModel.onReminderDurationChange(null)
                                        showReminderMenu = false
                                    }
                                )
                                ReminderDuration.entries.forEach { duration ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(duration.labelRes)) },
                                        onClick = {
                                            val state = permissionManager.checkPermission(Permission.NOTIFICATIONS)
                                            if (state == PermissionState.GRANTED) {
                                                if (interviewDate == null) {
                                                    showDatePicker = true
                                                } else {
                                                    viewModel.onReminderDurationChange(duration)
                                                }
                                            } else {
                                                showPermissionRationale = true
                                            }
                                            showReminderMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = interviewDate?.toEpochMilliseconds() ?: Clock.System.now().toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val current = interviewDate?.toLocalDateTime(TimeZone.currentSystemDefault())
                        val newDate = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
                        val updated = kotlinx.datetime.LocalDateTime(
                            newDate.year, newDate.month, newDate.dayOfMonth,
                            current?.hour ?: 9, current?.minute ?: 0
                        ).toInstant(TimeZone.currentSystemDefault())
                        
                        viewModel.onInterviewDateChange(updated)
                        if (current == null) {
                            showTimePicker = true
                        }
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

    if (showTimePicker) {
        val current = interviewDate?.toLocalDateTime(TimeZone.currentSystemDefault())
        val timePickerState = rememberTimePickerState(
            initialHour = current?.hour ?: 9,
            initialMinute = current?.minute ?: 0
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = interviewDate?.toLocalDateTime(TimeZone.currentSystemDefault()) 
                        ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    
                    val updated = kotlinx.datetime.LocalDateTime(
                        date.year, date.month, date.dayOfMonth,
                        timePickerState.hour, timePickerState.minute
                    ).toInstant(TimeZone.currentSystemDefault())
                    
                    viewModel.onInterviewDateChange(updated)
                    showTimePicker = false
                }) {
                    Text(stringResource(Res.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    if (showPermissionRationale) {
        PermissionRationaleDialog(
            onDismiss = { showPermissionRationale = false }
        )
    }
}
