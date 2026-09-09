package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.shared.Res
import com.pamneuroncraft.jobapplicationtracker.shared.cancel
import com.pamneuroncraft.jobapplicationtracker.shared.exit
import com.pamneuroncraft.jobapplicationtracker.shared.exit_app_message
import com.pamneuroncraft.jobapplicationtracker.shared.exit_app_title
import com.pamneuroncraft.jobapplicationtracker.ui.navigation.JobAddEditKey
import com.pamneuroncraft.jobapplicationtracker.ui.util.exitApp
import com.pamneuroncraft.jobapplicationtracker.ui.util.rememberPlatformContext
import com.pamneuroncraft.jobapplicationtracker.util.BackHandler
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AdaptiveJobsScreen(
    onAddJob: (JobAddEditKey) -> Unit,
    onEditJob: (String) -> Unit,
    onSummaryClick: () -> Unit,
    showPremiumShareRationale: Boolean = false
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()
    val platformContext = rememberPlatformContext()
    var showExitDialog by remember { mutableStateOf(false) }

    val canNavigateBackInScaffold = navigator.canNavigateBack()

    BackHandler(enabled = !canNavigateBackInScaffold) {
        showExitDialog = true
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth > 0.dp && maxHeight > 0.dp) {
            ListDetailPaneScaffold(
                modifier = Modifier.fillMaxSize(),
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    JobListScreen(
                        onAddJob = onAddJob,
                        onJobClick = { jobId ->
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, jobId)
                            }
                        },
                        onSummaryClick = onSummaryClick,
                        showPremiumShareRationale = showPremiumShareRationale,
                        selectedJobId = navigator.currentDestination?.contentKey
                    )
                },
                detailPane = {
                    val jobId = navigator.currentDestination?.contentKey
                    if (jobId != null) {
                        val isListVisible = navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded
                        JobDetailScreen(
                            jobId = jobId,
                            onBack = {
                                if (navigator.canNavigateBack()) {
                                    scope.launch {
                                        navigator.navigateBack()
                                    }
                                }
                            },
                            onEditJob = onEditJob,
                            isPane = isListVisible
                        )
                    } else {
                        // Placeholder for empty detail pane on large screens
                        EmptyDetailPane()
                    }
                }
            )
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(Res.string.exit_app_title)) },
            text = { Text(stringResource(Res.string.exit_app_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        exitApp(platformContext)
                    }
                ) {
                    Text(
                        text = stringResource(Res.string.exit),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false }
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyDetailPane() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.WorkOutline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Select a job to view details",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
