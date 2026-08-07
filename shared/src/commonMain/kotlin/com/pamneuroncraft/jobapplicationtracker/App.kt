package com.pamneuroncraft.jobapplicationtracker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.domain.repository.BillingManager
import com.pamneuroncraft.jobapplicationtracker.ui.components.PremiumBadge
import com.pamneuroncraft.jobapplicationtracker.ui.navigation.*
import com.pamneuroncraft.jobapplicationtracker.ui.screens.*
import com.pamneuroncraft.jobapplicationtracker.ui.theme.JobApplicationTrackerTheme
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.SettingsViewModel
import com.pamneuroncraft.jobapplicationtracker.util.BiometricResult
import com.pamneuroncraft.jobapplicationtracker.util.createBiometricManager
import com.pamneuroncraft.jobapplicationtracker.util.rememberAppUpdateManager
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    initialUrl: String? = null,
    initialShortcut: String? = null
) {
    val localSettings: LocalSettings = koinInject()
    val billingManager: BillingManager = koinInject()
    val settingsViewModel: SettingsViewModel = koinViewModel()
    val themePreference by settingsViewModel.themePreference.collectAsState()
    val useDynamicColor by settingsViewModel.useDynamicColor.collectAsState()
    val appUpdateManager = rememberAppUpdateManager()
    
    LaunchedEffect(Unit) {
        billingManager.initialize()
        appUpdateManager.checkForUpdates()
    }

    var isAppLocked by remember { mutableStateOf(localSettings.isBiometricEnabled) }
    var triggerBiometric by remember { mutableStateOf(isAppLocked) }

    JobApplicationTrackerTheme(
        themePreference = themePreference,
        dynamicColor = useDynamicColor
    ) {
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
            MainAppNavigation(
                initialUrl = initialUrl,
                initialShortcut = initialShortcut
            )
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
fun MainAppNavigation(
    initialUrl: String?,
    initialShortcut: String? = null
) {
    val navController = rememberNavController()
    val localSettings: LocalSettings = koinInject()
    val appConfig: AppConfig = koinInject()
    val billingManager: BillingManager = koinInject()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val startDestination = if (localSettings.isOnboardingCompleted) {
        when {
            initialUrl != null && appConfig.featureAiImport -> JobAddEditKey(initialUrl = initialUrl)
            initialShortcut == "add_job" -> JobAddEditKey()
            initialShortcut == "summary" -> SummaryKey
            else -> JobListKey
        }
    } else {
        OnboardingKey
    }

    // Top-level destinations that should show the navigation suite
    val topLevelDestinations = listOf(
        JobListKey,
        SummaryKey,
        ProfileKey,
        SettingsKey
    )

    val showNavigationSuite = currentDestination?.hierarchy?.any { dest ->
        topLevelDestinations.any { topLevel -> dest.hasRoute(topLevel::class) }
    } == true

    val isPremium by billingManager.isPremium.collectAsState()

    if (showNavigationSuite) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                item(
                    selected = currentDestination.hierarchy.any { it.hasRoute(JobListKey::class) },
                    onClick = {
                        navController.navigate(JobListKey) {
                            popUpTo(JobListKey) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Work, contentDescription = null) },
                    label = { Text("Jobs") }
                )
                item(
                    selected = currentDestination.hierarchy.any { it.hasRoute(SummaryKey::class) },
                    onClick = {
                        navController.navigate(SummaryKey) {
                            popUpTo(JobListKey) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { 
                        BadgedBox(
                            badge = {
                                if (!isPremium) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = Color(0xFFFFD700)
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null)
                        }
                    },
                    label = { Text("Summary") }
                )
                item(
                    selected = currentDestination.hierarchy.any { it.hasRoute(ProfileKey::class) },
                    onClick = {
                        navController.navigate(ProfileKey) {
                            popUpTo(JobListKey) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") }
                )
                item(
                    selected = currentDestination.hierarchy.any { it.hasRoute(SettingsKey::class) },
                    onClick = {
                        navController.navigate(SettingsKey) {
                            popUpTo(JobListKey) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        ) {
            AppNavHost(navController, startDestination, initialUrl, appConfig)
        }
    } else {
        AppNavHost(navController, startDestination, initialUrl, appConfig)
    }
}

@Composable
private fun AppNavHost(
    navController: androidx.navigation.NavHostController,
    startDestination: Any,
    initialUrl: String?,
    appConfig: AppConfig
) {
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
            AdaptiveJobsScreen(
                onAddJob = { key -> navController.navigate(key) },
                onEditJob = { jobId -> navController.navigate(JobAddEditKey(jobId)) },
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
