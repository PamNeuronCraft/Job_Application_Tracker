package com.Pamneuroncraft.jobapplicationtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.Pamneuroncraft.jobapplicationtracker.ui.navigation.*
import com.Pamneuroncraft.jobapplicationtracker.ui.screens.JobAddEditScreen
import com.Pamneuroncraft.jobapplicationtracker.ui.screens.JobDetailScreen
import com.Pamneuroncraft.jobapplicationtracker.ui.screens.JobListScreen
import com.Pamneuroncraft.jobapplicationtracker.ui.theme.JobApplicationTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobApplicationTrackerTheme {
                val navigationState = rememberNavigationState(
                    startRoute = JobListKey,
                    topLevelRoutes = setOf(JobListKey),
                )
                val navigator = remember { Navigator(navigationState) }

                val entryProvider = remember {
                    entryProvider<NavKey> {
                        entry<JobListKey> {
                            JobListScreen(
                                onAddJob = { navigator.navigate(JobAddEditKey()) },
                                onJobClick = { jobId -> navigator.navigate(JobDetailKey(jobId)) }
                            )
                        }
                        entry<JobDetailKey> { key ->
                            JobDetailScreen(
                                jobId = key.jobId,
                                onBack = { navigator.goBack() },
                                onEditJob = { jobId -> navigator.navigate(JobAddEditKey(jobId)) }
                            )
                        }
                        entry<JobAddEditKey> { key ->
                            JobAddEditScreen(
                                jobId = key.jobId,
                                onBack = { navigator.goBack() }
                            )
                        }
                    }
                }

                NavDisplay(
                    entries = navigationState.toEntries(entryProvider)
                ) {
                    navigator.goBack()
                }
            }
        }
    }
}
