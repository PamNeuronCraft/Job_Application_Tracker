package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.pamneuroncraft.jobapplicationtracker.ui.components.PermissionRationaleDialog
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.JobAddEditViewModel
import com.pamneuroncraft.jobapplicationtracker.util.Permission
import com.pamneuroncraft.jobapplicationtracker.util.PermissionManager
import com.pamneuroncraft.jobapplicationtracker.util.PermissionState
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobAddEditScreen(
    jobId: Int?,
    prefilledJobName: String? = null,
    prefilledCompanyName: String? = null,
    prefilledDescription: String? = null,
    prefilledCompensation: String? = null,
    onBack: () -> Unit,
    viewModel: JobAddEditViewModel = koinViewModel(),
    permissionManager: PermissionManager = koinInject()
) {
    var showPermissionRationale by remember { mutableStateOf(false) }

    LaunchedEffect(jobId) {
        viewModel.loadJob(
            jobId = jobId,
            prefilledJobName = prefilledJobName,
            prefilledCompanyName = prefilledCompanyName,
            prefilledDescription = prefilledDescription,
            prefilledCompensation = prefilledCompensation
        )
    }

    val jobName by viewModel.jobName
    val companyName by viewModel.companyName
    val description by viewModel.description
    val jobType by viewModel.jobType
    val compensation by viewModel.compensation
    val status by viewModel.status
    val interviewDate by viewModel.interviewDate

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (jobId == null) "Add Job" else "Edit Job") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onSaveJob(onBack) }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
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
                label = { Text("Job Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = companyName,
                onValueChange = { viewModel.onCompanyNameChange(it) },
                label = { Text("Company Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Job Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            OutlinedTextField(
                value = compensation,
                onValueChange = { viewModel.onCompensationChange(it) },
                label = { Text("Compensation") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Job Type", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                JobType.entries.forEach { type ->
                    FilterChip(
                        selected = jobType == type,
                        onClick = { viewModel.onJobTypeChange(type) },
                        label = { Text(type.name) }
                    )
                }
            }

            Text("Status", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JobStatus.entries.forEach { jobStatus ->
                    FilterChip(
                        selected = status == jobStatus,
                        onClick = { viewModel.onStatusChange(jobStatus) },
                        label = { Text(jobStatus.name, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Interview Date Reminder", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = if (interviewDate != null) "Reminder set for $interviewDate" else "No reminder set",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = interviewDate != null,
                    onCheckedChange = { checked ->
                        if (checked) {
                            val state = permissionManager.checkPermission(Permission.NOTIFICATIONS)
                            if (state == PermissionState.GRANTED) {
                                // Mock picking a date 1 day from now
                                viewModel.onInterviewDateChange(Clock.System.now().plus(1.days))
                            } else {
                                showPermissionRationale = true
                            }
                        } else {
                            viewModel.onInterviewDateChange(null)
                        }
                    }
                )
            }
        }
    }

    if (showPermissionRationale) {
        PermissionRationaleDialog(
            onDismiss = { showPermissionRationale = false }
        )
    }
}
