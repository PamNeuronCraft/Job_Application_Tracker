package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.AppConfig
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.pamneuroncraft.jobapplicationtracker.ui.components.AdBanner
import com.pamneuroncraft.jobapplicationtracker.ui.components.PaidFeatureDialog
import com.pamneuroncraft.jobapplicationtracker.ui.navigation.JobAddEditKey
import com.pamneuroncraft.jobapplicationtracker.ui.util.DateFormatter
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.BackupViewModel
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.ImportViewModel
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.JobListViewModel
import com.pamneuroncraft.jobapplicationtracker.ui.theme.JobApplicationTrackerTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import org.jetbrains.compose.ui.tooling.preview.Preview

import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    onAddJob: (JobAddEditKey) -> Unit,
    onJobClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: JobListViewModel = koinViewModel(),
    backupViewModel: BackupViewModel = koinViewModel(),
    importViewModel: ImportViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
    appConfig: AppConfig = koinInject()
) {
    val jobs by viewModel.jobs.collectAsState()
    val isAscending by viewModel.isAscending.collectAsState()
    val user by profileViewModel.currentUser.collectAsState()
    
    var showAddOptions by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showPaidFeatureDialog by remember { mutableStateOf(false) }

    JobListContent(
        jobs = jobs,
        isAscending = isAscending,
        userInitial = user?.displayName?.firstOrNull()?.toString() ?: user?.email?.firstOrNull()?.toString() ?: "G",
        onAddJob = { showAddOptions = true },
        onJobClick = onJobClick,
        onProfileClick = onProfileClick,
        onToggleSort = { viewModel.onToggleSort() },
        onDeleteJob = { viewModel.onDeleteJob(it) },
        onBackupToDrive = { 
            if (!appConfig.featureGoogleDriveBackup || !backupViewModel.isUserSignedIn) {
                showPaidFeatureDialog = true
            } else {
                backupViewModel.backupToCloud()
            }
        },
        isCloudBackupEnabled = backupViewModel.isUserSignedIn,
        showAds = !appConfig.featureGoogleDriveBackup
    )

    if (showPaidFeatureDialog) {
        PaidFeatureDialog(onDismiss = { showPaidFeatureDialog = false })
    }

    if (showAddOptions) {
        ModalBottomSheet(
            onDismissRequest = { showAddOptions = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Add New Application",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ListItem(
                    headlineContent = { Text("Manual Entry") },
                    leadingContent = { Icon(Icons.Default.EditNote, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showAddOptions = false
                        onAddJob(JobAddEditKey())
                    }
                )
                if (appConfig.featureAiImport) {
                    ListItem(
                        headlineContent = { Text("Import from URL (AI)") },
                        leadingContent = { Icon(Icons.Default.Link, contentDescription = null) },
                        modifier = Modifier.clickable {
                            showAddOptions = false
                            showImportDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showImportDialog) {
        ImportFromUrlDialog(
            viewModel = importViewModel,
            onDismiss = { 
                showImportDialog = false
                importViewModel.resetState()
            },
            onImportSuccess = { extractedJob ->
                showImportDialog = false
                importViewModel.resetState()
                onAddJob(
                    JobAddEditKey(
                        prefilledJobName = extractedJob.jobName,
                        prefilledCompanyName = extractedJob.companyName,
                        prefilledDescription = extractedJob.description,
                        prefilledCompensation = extractedJob.compensation
                    )
                )
            },
            onProceedManually = {
                showImportDialog = false
                importViewModel.resetState()
                onAddJob(JobAddEditKey())
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportFromUrlDialog(
    viewModel: ImportViewModel,
    onDismiss: () -> Unit,
    onImportSuccess: (com.pamneuroncraft.jobapplicationtracker.domain.repository.ExtractedJob) -> Unit,
    onProceedManually: () -> Unit
) {
    val state by viewModel.importState
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import from URL") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state is ImportViewModel.ImportState.Idle) {
                    Text("Paste the job posting URL below. Gemini AI will try to extract the details for you.")
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Job URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                if (state is ImportViewModel.ImportState.Loading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Extracting job details...")
                    }
                }

                if (state is ImportViewModel.ImportState.Error) {
                    Text(
                        text = (state as ImportViewModel.ImportState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text("Would you like to try entering the details manually instead?")
                }
            }
        },
        confirmButton = {
            when (state) {
                is ImportViewModel.ImportState.Idle -> {
                    Button(
                        onClick = { viewModel.extractJob(url) },
                        enabled = url.isNotBlank()
                    ) {
                        Text("Extract")
                    }
                }
                is ImportViewModel.ImportState.Error -> {
                    Button(onClick = onProceedManually) {
                        Text("Enter Manually")
                    }
                }
                is ImportViewModel.ImportState.Success -> {
                    LaunchedEffect(state) {
                        onImportSuccess((state as ImportViewModel.ImportState.Success).job)
                    }
                }
                else -> {}
            }
        },
        dismissButton = {
            if (state !is ImportViewModel.ImportState.Loading) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListContent(
    jobs: List<JobApplication>,
    isAscending: Boolean,
    userInitial: String,
    onAddJob: () -> Unit,
    onJobClick: (Int) -> Unit,
    onProfileClick: () -> Unit,
    onToggleSort: () -> Unit,
    onDeleteJob: (JobApplication) -> Unit,
    onBackupToDrive: () -> Unit,
    isCloudBackupEnabled: Boolean,
    showAds: Boolean
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Job Applications") },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitial.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSort) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = if (isAscending) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    
                    IconButton(onClick = onBackupToDrive) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Cloud Backup",
                            tint = if (isCloudBackupEnabled) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                LocalContentColor.current.copy(alpha = 0.38f)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddJob) {
                Icon(Icons.Default.Add, contentDescription = "Add Job")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = jobs,
                    key = { it.id }
                ) { job ->
                    JobItem(
                        job = job,
                        onDelete = { onDeleteJob(job) },
                        onClick = { onJobClick(job.id) }
                    )
                }
            }
            
            if (showAds) {
                AdBanner(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobItem(
    job: JobApplication,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState()
    
    if (swipeState.currentValue != SwipeToDismissBoxValue.Settled) {
        LaunchedEffect(swipeState.currentValue) {
            onDelete()
            swipeState.reset()
        }
    }

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            val color = when (swipeState.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Color.Red.copy(alpha = 0.5f)
                SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.5f)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (swipeState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) 
                    Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        },
        content = {
            Card(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = getStatusColor(job.status)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = job.jobName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = DateFormatter.format(job.dateAdded, "MMM dd, yyyy"),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun JobListScreenPreview() {
    JobApplicationTrackerTheme {
        JobListContent(
            jobs = listOf(
                JobApplication(id = 1, jobName = "Android Developer", companyName = "Google", description = "", jobType = JobType.REMOTE, compensation = "150k", status = JobStatus.APPLIED),
                JobApplication(id = 2, jobName = "Kotlin Engineer", companyName = "JetBrains", description = "", jobType = JobType.HYBRID, compensation = "140k", status = JobStatus.INTERVIEWING)
            ),
            isAscending = false,
            userInitial = "G",
            onAddJob = {},
            onJobClick = {},
            onProfileClick = {},
            onToggleSort = {},
            onDeleteJob = {},
            onBackupToDrive = {},
            isCloudBackupEnabled = true,
            showAds = false
        )
    }
}

fun getStatusColor(status: JobStatus): Color {
    return when (status) {
        JobStatus.APPLIED -> Color(0xFFBBDEFB) // Light Blue
        JobStatus.INTERVIEWING -> Color(0xFFFFF9C4) // Light Yellow
        JobStatus.JOB_OFFER -> Color(0xFFC8E6C9) // Light Green
        JobStatus.NO_OFFER -> Color(0xFFFFCDD2) // Light Red
    }
}
