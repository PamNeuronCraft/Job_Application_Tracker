package com.pamneuroncraft.jobapplicationtracker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.domain.repository.BillingManager
import com.pamneuroncraft.jobapplicationtracker.ui.navigation.*
import com.pamneuroncraft.jobapplicationtracker.ui.screens.*
import com.pamneuroncraft.jobapplicationtracker.ui.theme.JobApplicationTrackerTheme
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.SettingsViewModel
import com.pamneuroncraft.jobapplicationtracker.util.BiometricResult
import com.pamneuroncraft.jobapplicationtracker.util.createBiometricManager
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(initialUrl: String? = null) {
    val localSettings: LocalSettings = koinInject()
    val billingManager: BillingManager = koinInject()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val themePreference by settingsViewModel.themePreference.collectAsState()
    
    LaunchedEffect(Unit) {
        billingManager.initialize()
    }

    var isAppLocked by remember { mutableStateOf(localSettings.isBiometricEnabled) }
    var triggerBiometric by remember { mutableStateOf(isAppLocked) }

    JobApplicationTrackerTheme(themePreference = themePreference) {
        if (isAppLocked) {
            LockScreen(
                onUnlockRequested = { triggerBiometric = true },
                triggerBiometric = triggerBiometric,
                onAuthenticated = {
                    isAppLocked = false
                    triggerBiometric = false
                }
            )
        } else {
            MainAppNavigation(initialUrl = initialUrl)
        }
    }
}

@Composable
fun LockScreen(
    onUnlockRequested: () -> Unit,
    triggerBiometric: Boolean,
    onAuthenticated: () -> Unit
) {
    val biometricManager = createBiometricManager()
    
    if (triggerBiometric) {
        biometricManager.Authenticate { result ->
            if (result is BiometricResult.Success) {
                onAuthenticated()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "App Locked",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Use biometric to unlock",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(onClick = onUnlockRequested) {
                Text("Unlock Now")
            }
        }
    }
}

@Composable
fun MainAppNavigation(initialUrl: String?) {
    val navController = rememberNavController()
    val localSettings: LocalSettings = koinInject()
    val appConfig: AppConfig = koinInject()
    
    val startDestination = if (localSettings.isOnboardingCompleted) {
        if (initialUrl != null && appConfig.featureAiImport) {
            JobAddEditKey(initialUrl = initialUrl)
        } else {
            JobListKey
        }
    } else {
        OnboardingKey
    }

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
                onProfileClick = { navController.navigate(ProfileKey) },
                onSettingsClick = { navController.navigate(SettingsKey) },
                onSummaryClick = { navController.navigate(SummaryKey) },
                showPremiumShareRationale = (initialUrl != null && !appConfig.featureAiImport)
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
                initialUrl = key.initialUrl,
                onBack = { navController.popBackStack() }
            )
        }
        composable<ProfileKey> {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onSubscriptionClick = { navController.navigate(SubscriptionKey) }
            )
        }
        composable<SettingsKey> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onProfileClick = { navController.navigate(ProfileKey) },
                onSubscriptionClick = { navController.navigate(SubscriptionKey) }
            )
        }
        composable<SummaryKey> {
            SummaryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<SubscriptionKey> {
            SubscriptionScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
