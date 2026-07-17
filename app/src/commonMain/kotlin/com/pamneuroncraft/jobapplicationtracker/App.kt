package com.pamneuroncraft.jobapplicationtracker

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.ui.navigation.*
import com.pamneuroncraft.jobapplicationtracker.ui.screens.*
import com.pamneuroncraft.jobapplicationtracker.ui.theme.JobApplicationTrackerTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    JobApplicationTrackerTheme {
        val navController = rememberNavController()
        val localSettings: LocalSettings = koinInject()
        
        val startDestination = if (localSettings.isOnboardingCompleted) JobListKey else OnboardingKey

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable<OnboardingKey> {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(JobListKey) {
                            popUpTo(OnboardingKey) { inclusive = true }
                        }
                    }
                )
            }
            composable<JobListKey> {
                JobListScreen(
                    onAddJob = { key -> navController.navigate(key) },
                    onJobClick = { jobId -> navController.navigate(JobDetailKey(jobId)) },
                    onProfileClick = { navController.navigate(ProfileKey) }
                )
            }
            composable<JobDetailKey> { backStackEntry ->
                val key = backStackEntry.toRoute<JobDetailKey>()
                JobDetailScreen(
                    jobId = key.jobId,
                    onBack = { navController.popBackStack() },
                    onEditJob = { jobId -> navController.navigate(JobAddEditKey(jobId)) }
                )
            }
            composable<JobAddEditKey> { backStackEntry ->
                val key = backStackEntry.toRoute<JobAddEditKey>()
                JobAddEditScreen(
                    jobId = key.jobId,
                    prefilledJobName = key.prefilledJobName,
                    prefilledCompanyName = key.prefilledCompanyName,
                    prefilledDescription = key.prefilledDescription,
                    prefilledCompensation = key.prefilledCompensation,
                    onBack = { navController.popBackStack() }
                )
            }
            composable<ProfileKey> {
                ProfileScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
