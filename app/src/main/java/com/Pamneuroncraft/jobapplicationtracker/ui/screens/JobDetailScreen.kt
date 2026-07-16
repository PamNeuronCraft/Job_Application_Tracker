package com.Pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.Pamneuroncraft.jobapplicationtracker.ui.theme.JobApplicationTrackerTheme
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.Pamneuroncraft.jobapplicationtracker.ui.viewmodel.JobDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: Int,
    onBack: () -> Unit,
    onEditJob: (Int) -> Unit,
    viewModel: JobDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(jobId) {
        viewModel.loadJob(jobId)
    }

    val job by viewModel.job
    
    JobDetailContent(
        job = job,
        onBack = onBack,
        onEditJob = { onEditJob(jobId) },
        onDeleteJob = { viewModel.onDeleteJob(onBack) },
        onUpdateInterviewDate = { viewModel.onUpdateInterviewDate(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailContent(
    job: JobApplication?,
    onBack: () -> Unit,
    onEditJob: () -> Unit,
    onDeleteJob: () -> Unit,
    onUpdateInterviewDate: (Date) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEditJob) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDeleteJob) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        job?.let { currentJob ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(getStatusLightColor(currentJob.status))
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailItem(label = "Job Name", value = currentJob.jobName)
                    DetailItem(label = "Company Name", value = currentJob.companyName)
                    DetailItem(label = "Description", value = currentJob.description)
                    DetailItem(label = "Job Type", value = currentJob.jobType.name)
                    DetailItem(label = "Compensation", value = currentJob.compensation)
                    DetailItem(label = "Status", value = currentJob.status.name)
                    DetailItem(label = "Date Added", value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(currentJob.dateAdded))

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Interview Date", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = currentJob.interviewDate?.let { 
                                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(it) 
                                } ?: "Not set",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Set Date")
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
                        onUpdateInterviewDate(Date(millis))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JobDetailScreenPreview() {
    JobApplicationTrackerTheme {
        JobDetailContent(
            job = JobApplication(
                id = 1,
                jobName = "Android Developer",
                companyName = "Google",
                description = "Build amazing things.",
                jobType = JobType.REMOTE,
                compensation = "150k",
                status = JobStatus.INTERVIEWING,
                dateAdded = Date(),
                interviewDate = Date()
            ),
            onBack = {},
            onEditJob = {},
            onDeleteJob = {},
            onUpdateInterviewDate = {}
        )
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

fun getStatusLightColor(status: JobStatus): Color {
    return when (status) {
        JobStatus.APPLIED -> Color(0xFFE3F2FD) // Very Light Blue
        JobStatus.INTERVIEWING -> Color(0xFFFFFDE7) // Very Light Yellow
        JobStatus.JOB_OFFER -> Color(0xFFE8F5E9) // Very Light Green
        JobStatus.NO_OFFER -> Color(0xFFFFEBEE) // Very Light Red
    }
}
