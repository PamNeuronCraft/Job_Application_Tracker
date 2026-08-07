package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.AppConfig
import com.pamneuroncraft.jobapplicationtracker.data.local.ThemePreference
import com.pamneuroncraft.jobapplicationtracker.ui.components.AdBanner
import com.pamneuroncraft.jobapplicationtracker.ui.components.PaidFeatureDialog
import com.pamneuroncraft.jobapplicationtracker.ui.components.PremiumBadge
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.BackupViewModel
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.SettingsViewModel
import com.pamneuroncraft.jobapplicationtracker.util.BiometricResult
import com.pamneuroncraft.jobapplicationtracker.util.createBiometricManager
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    onSubscriptionClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
    backupViewModel: BackupViewModel = koinViewModel(),
    appConfig: AppConfig = koinInject()
) {
    val themePreference by viewModel.themePreference.collectAsState()
    val useDynamicColor by viewModel.useDynamicColor.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val biometricManager = createBiometricManager()
    
    var showThemeMenu by remember { mutableStateOf(false) }
    var showPaidFeatureDialog by remember { mutableStateOf(false) }
    var paidFeatureDialogTitle by remember { mutableStateOf("") }
    var paidFeatureDialogMessage by remember { mutableStateOf("") }
    var triggerBiometricVerification by remember { mutableStateOf(false) }

    if (triggerBiometricVerification) {
        biometricManager.Authenticate { result ->
            triggerBiometricVerification = false
            if (result is BiometricResult.Success) {
                viewModel.onBiometricEnabledChange(true)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Biometrics
            ListItem(
                headlineContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable Biometrics")
                        if (!isPremium) {
                            PremiumBadge(modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                },
                leadingContent = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (appConfig.featureBiometrics) {
                                    if (biometricManager.canAuthenticate()) {
                                        triggerBiometricVerification = true
                                    } else {
                                        // Handle no biometrics enrolled
                                    }
                                } else {
                                    paidFeatureDialogTitle = "Biometric Lock"
                                    paidFeatureDialogMessage = "Biometric lock is a premium feature. Upgrade now to secure your job applications with fingerprint or face recognition."
                                    showPaidFeatureDialog = true
                                }
                            } else {
                                viewModel.onBiometricEnabledChange(false)
                            }
                        }
                    )
                }
            )

            // Cloud Backup
            ListItem(
                headlineContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Backup and Sync")
                        if (!isPremium) {
                            PremiumBadge(modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                },
                leadingContent = { Icon(Icons.Default.CloudSync, contentDescription = null) },
                trailingContent = {
                    Text(
                        text = if (isPremium) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable {
                    if (appConfig.featureGoogleDriveBackup) {
                        if (backupViewModel.isUserSignedIn) {
                            backupViewModel.backupToCloud()
                        } else {
                            onProfileClick()
                        }
                    } else {
                        paidFeatureDialogTitle = "Cloud Backup"
                        paidFeatureDialogMessage = "Cloud backup and sync is only available for paid users. Upgrade now to keep your data safe and synced across devices."
                        showPaidFeatureDialog = true
                    }
                }
            )

            // Subscription
            ListItem(
                headlineContent = { Text("Subscription") },
                leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
                trailingContent = {
                    Text(
                        text = if (isPremium) "Subscribed" else "Disabled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPremium) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable { onSubscriptionClick() }
            )

            // Export Data
            ListItem(
                headlineContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Export Data (CSV)")
                        if (!isPremium) {
                            PremiumBadge(modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null) },
                modifier = Modifier.clickable {
                    if (appConfig.featureExport) {
                        viewModel.exportData()
                    } else {
                        paidFeatureDialogTitle = "Export Data"
                        paidFeatureDialogMessage = "Exporting your job applications to a CSV file is a premium feature. Upgrade now to back up your data locally."
                        showPaidFeatureDialog = true
                    }
                }
            )

            // Theme
            ListItem(
                headlineContent = { Text("Theme") },
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                trailingContent = {
                    Box {
                        TextButton(onClick = { showThemeMenu = true }) {
                            Text(themePreference.name.lowercase().replaceFirstChar { it.uppercase() })
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            ThemePreference.entries.forEach { preference ->
                                DropdownMenuItem(
                                    text = { Text(preference.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        viewModel.onThemePreferenceChange(preference)
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )

            // Dynamic Color (Material You)
            ListItem(
                headlineContent = { Text("Dynamic Colors") },
                supportingContent = { Text("Use colors from your wallpaper (Android 12+)") },
                leadingContent = { Icon(Icons.Default.ColorLens, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = useDynamicColor,
                        onCheckedChange = { viewModel.onUseDynamicColorChange(it) }
                    )
                }
            )

            if (!isPremium) {
                Spacer(modifier = Modifier.weight(1f))
                AdBanner(modifier = Modifier.fillMaxWidth())
            }
        }
    }

    if (showPaidFeatureDialog) {
        PaidFeatureDialog(
            onDismiss = { showPaidFeatureDialog = false },
            title = paidFeatureDialogTitle,
            message = paidFeatureDialogMessage
        )
    }
}
