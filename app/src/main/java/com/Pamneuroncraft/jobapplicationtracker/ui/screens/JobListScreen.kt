package com.Pamneuroncraft.jobapplicationtracker.ui.screens

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.services.drive.DriveScopes
import com.Pamneuroncraft.jobapplicationtracker.notification.getGoogleDriveService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.Pamneuroncraft.jobapplicationtracker.ui.theme.JobApplicationTrackerTheme
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.Pamneuroncraft.jobapplicationtracker.ui.viewmodel.BackupViewModel
import com.Pamneuroncraft.jobapplicationtracker.ui.viewmodel.JobListViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    onAddJob: () -> Unit,
    onJobClick: (Int) -> Unit,
    viewModel: JobListViewModel = hiltViewModel(),
    backupViewModel: BackupViewModel = hiltViewModel()
) {
    val jobs by viewModel.jobs.collectAsState()
    val isAscending by viewModel.isAscending.collectAsState()
    
    JobListContent(
        jobs = jobs,
        isAscending = isAscending,
        onAddJob = onAddJob,
        onJobClick = onJobClick,
        onToggleSort = { viewModel.onToggleSort() },
        onDeleteJob = { viewModel.onDeleteJob(it) },
        onExport = { backupViewModel.exportData(it) },
        onImport = { backupViewModel.importData(it) },
        onBackupToDrive = { backupViewModel.backupToDrive(it) },
        onRestoreFromDrive = { backupViewModel.restoreFromDrive(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListContent(
    jobs: List<JobApplication>,
    isAscending: Boolean,
    onAddJob: () -> Unit,
    onJobClick: (Int) -> Unit,
    onToggleSort: () -> Unit,
    onDeleteJob: (JobApplication) -> Unit,
    onExport: (java.io.OutputStream) -> Unit,
    onImport: (java.io.InputStream) -> Unit,
    onBackupToDrive: (com.google.api.services.drive.Drive) -> Unit,
    onRestoreFromDrive: (com.google.api.services.drive.Drive) -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { outputStream ->
                onExport(outputStream)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { inputStream ->
                onImport(inputStream)
            }
        }
    }

    val driveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            val driveService = getGoogleDriveService(context, account)
            onBackupToDrive(driveService)
        }
    }

    val restoreDriveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            val driveService = getGoogleDriveService(context, account)
            onRestoreFromDrive(driveService)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Applications") },
                actions = {
                    IconButton(onClick = onToggleSort) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = if (isAscending) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Export JSON") },
                            onClick = {
                                showMenu = false
                                exportLauncher.launch("job_applications.json")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import JSON") },
                            onClick = {
                                showMenu = false
                                importLauncher.launch(arrayOf("application/json"))
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Backup to Google Drive") },
                            onClick = {
                                showMenu = false
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA))
                                    .build()
                                driveLauncher.launch(GoogleSignIn.getClient(context, gso).signInIntent)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Restore from Google Drive") },
                            onClick = {
                                showMenu = false
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestEmail()
                                    .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA))
                                    .build()
                                restoreDriveLauncher.launch(GoogleSignIn.getClient(context, gso).signInIntent)
                            }
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(job.dateAdded),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun JobListScreenPreview() {
    JobApplicationTrackerTheme {
        JobListContent(
            jobs = listOf(
                JobApplication(id = 1, jobName = "Android Developer", companyName = "Google", description = "", jobType = JobType.REMOTE, compensation = "150k", status = JobStatus.APPLIED),
                JobApplication(id = 2, jobName = "Kotlin Engineer", companyName = "JetBrains", description = "", jobType = JobType.HYBRID, compensation = "140k", status = JobStatus.INTERVIEWING)
            ),
            isAscending = false,
            onAddJob = {},
            onJobClick = {},
            onToggleSort = {},
            onDeleteJob = {},
            onExport = {},
            onImport = {},
            onBackupToDrive = {},
            onRestoreFromDrive = {}
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
