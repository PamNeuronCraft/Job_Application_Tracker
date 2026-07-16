package com.Pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.Pamneuroncraft.jobapplicationtracker.ui.viewmodel.JobAddEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobAddEditScreen(
    jobId: Int?,
    onBack: () -> Unit,
    viewModel: JobAddEditViewModel = hiltViewModel()
) {
    LaunchedEffect(jobId) {
        viewModel.loadJob(jobId)
    }

    val jobName by viewModel.jobName
    val companyName by viewModel.companyName
    val description by viewModel.description
    val jobType by viewModel.jobType
    val compensation by viewModel.compensation
    val status by viewModel.status

    Scaffold(
        topBar = {
            TopAppBar(
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
        }
    }
}
