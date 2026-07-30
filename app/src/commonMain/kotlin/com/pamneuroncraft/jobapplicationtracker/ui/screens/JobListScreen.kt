package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.paging.*
import androidx.paging.compose.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.flow.flow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.AppConfig
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobType
import com.pamneuroncraft.jobapplicationtracker.domain.model.CompensationType
import com.pamneuroncraft.jobapplicationtracker.ui.components.AdBanner
import com.pamneuroncraft.jobapplicationtracker.ui.components.PaidFeatureDialog
import com.pamneuroncraft.jobapplicationtracker.ui.navigation.JobAddEditKey
import com.pamneuroncraft.jobapplicationtracker.ui.util.DateFormatter
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.ImportViewModel
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.JobListViewModel
import com.pamneuroncraft.jobapplicationtracker.ui.theme.JobApplicationTrackerTheme
import com.pamneuroncraft.jobapplicationtracker.ui.theme.getJobStatusColor
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.StringResource
import jobapplicationtracker.app.generated.resources.*

import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.ProfileViewModel

enum class PaidFeatureType(val titleRes: StringResource, val messageRes: StringResource) {
    SUMMARY(
        Res.string.paid_feature_summary_title,
        Res.string.paid_feature_summary_message
    ),
    AI_IMPORT(
        Res.string.paid_feature_ai_import_title,
        Res.string.paid_feature_ai_import_message
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    onAddJob: (JobAddEditKey) -> Unit,
    onJobClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSummaryClick: () -> Unit,
    showPremiumShareRationale: Boolean = false,
    viewModel: JobListViewModel = koinViewModel(),
    importViewModel: ImportViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
    appConfig: AppConfig = koinInject()
) {
    val pagedJobs = viewModel.pagedJobs.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val user by profileViewModel.currentUser.collectAsState()
    
    var showAddOptions by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var paidFeatureToShow by remember { 
        mutableStateOf(if (showPremiumShareRationale) PaidFeatureType.AI_IMPORT else null) 
    }

    JobListContent(
        jobs = pagedJobs,
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        userInitial = user?.displayName?.firstOrNull()?.toString() ?: user?.email?.firstOrNull()?.toString() ?: "G",
        onAddJob = { showAddOptions = true },
        onJobClick = onJobClick,
        onProfileClick = onProfileClick,
        onSettingsClick = onSettingsClick,
        onSummaryClick = {
            if (appConfig.featureSummary) {
                onSummaryClick()
            } else {
                paidFeatureToShow = PaidFeatureType.SUMMARY
            }
        },
        onDeleteJob = { viewModel.onDeleteJob(it) },
        showAds = !appConfig.featureGoogleDriveBackup
    )

    paidFeatureToShow?.let { feature ->
        PaidFeatureDialog(
            onDismiss = { paidFeatureToShow = null },
            title = stringResource(feature.titleRes),
            message = stringResource(feature.messageRes)
        )
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
                    text = stringResource(Res.string.add_new_application),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ListItem(
                    headlineContent = { Text(stringResource(Res.string.manual_entry)) },
                    leadingContent = { Icon(Icons.Default.EditNote, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showAddOptions = false
                        onAddJob(JobAddEditKey())
                    }
                )
                if (appConfig.featureAiImport) {
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.import_from_url_ai)) },
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
        title = { Text(stringResource(Res.string.import_from_url)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state is ImportViewModel.ImportState.Idle) {
                    Text(stringResource(Res.string.import_from_url_description))
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text(stringResource(Res.string.job_url)) },
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
                        Text(stringResource(Res.string.extracting_job_details))
                    }
                }

                if (state is ImportViewModel.ImportState.Error) {
                    Text(
                        text = (state as ImportViewModel.ImportState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(stringResource(Res.string.try_manual_entry))
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
                        Text(stringResource(Res.string.extract))
                    }
                }
                is ImportViewModel.ImportState.Error -> {
                    Button(onClick = onProceedManually) {
                        Text(stringResource(Res.string.enter_manually))
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
                    Text(stringResource(Res.string.cancel))
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListContent(
    jobs: LazyPagingItems<JobApplication>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    userInitial: String,
    onAddJob: () -> Unit,
    onJobClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSummaryClick: () -> Unit,
    onDeleteJob: (JobApplication) -> Unit,
    showAds: Boolean
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.job_applications)) },
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
                    IconButton(onClick = onSummaryClick) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = stringResource(Res.string.summary)
                        )
                    }
                    
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(Res.string.settings)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddJob) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.add_job))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(Res.string.search_jobs)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(Res.string.clear_search))
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    count = jobs.itemCount,
                    key = jobs.itemKey { it.id }
                ) { index ->
                    val job = jobs[index]
                    if (job != null) {
                        JobItem(
                            job = job,
                            onDelete = { onDeleteJob(job) },
                            onClick = { onJobClick(job.id) }
                        )
                    }
                }

                when (val state = jobs.loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            Text(
                                text = stringResource(Res.string.error_loading_more, state.error.message ?: ""),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    else -> {}
                }
            }

            if (jobs.loadState.refresh is LoadState.Loading && jobs.itemCount == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (jobs.loadState.refresh is LoadState.NotLoading && jobs.itemCount == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(Res.string.no_jobs_found))
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
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it != SwipeToDismissBoxValue.Settled) {
                onDelete()
            }
            false // Item will be removed by the list update, so we don't need to transition to 'dismissed'
        }
    )

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
                Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.delete), tint = Color.White)
            }
        },
        content = {
            val containerColor = getJobStatusColor(job.status, isSystemInDarkTheme())
            Card(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColorFor(containerColor)
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
    val jobs = flow {
        emit(PagingData.from(listOf(
            JobApplication(id = "1", jobName = "Android Developer", companyName = "Google", description = "", jobType = JobType.REMOTE, compensationAmount = 150000.0, compensationType = CompensationType.ANNUAL, status = JobStatus.APPLIED),
            JobApplication(id = "2", jobName = "Kotlin Engineer", companyName = "JetBrains", description = "", jobType = JobType.HYBRID, compensationAmount = 140000.0, compensationType = CompensationType.ANNUAL, status = JobStatus.INTERVIEW)
        )))
    }.collectAsLazyPagingItems()

    JobApplicationTrackerTheme {
        JobListContent(
            jobs = jobs,
            searchQuery = "",
            onSearchQueryChange = {},
            userInitial = "G",
            onAddJob = {},
            onJobClick = {},
            onProfileClick = {},
            onSettingsClick = {},
            onSummaryClick = {},
            onDeleteJob = {},
            showAds = false
        )
    }
}


