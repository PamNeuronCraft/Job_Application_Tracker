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
import com.pamneuroncraft.jobapplicationtracker.ui.components.PaidFeatureDialog
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.BackupViewModel
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.SettingsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
    backupViewModel: BackupViewModel = koinViewModel(),
    appConfig: AppConfig = koinInject()
) {
    val themePreference by viewModel.themePreference.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    
    var showThemeMenu by remember { mutableStateOf(false) }
    var showPaidFeatureDialog by remember { mutableStateOf(false) }

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
                headlineContent = { Text("Enable Biometrics") },
                leadingContent = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { viewModel.onBiometricEnabledChange(it) }
                    )
                }
            )

            // Cloud Backup
            ListItem(
                headlineContent = { Text("Backup and Sync") },
                leadingContent = { Icon(Icons.Default.CloudSync, contentDescription = null) },
                modifier = Modifier.clickable {
                    if (appConfig.featureGoogleDriveBackup) {
                        if (backupViewModel.isUserSignedIn) {
                            backupViewModel.backupToCloud()
                        } else {
                            onProfileClick()
                        }
                    } else {
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
        }
    }

    if (showPaidFeatureDialog) {
        PaidFeatureDialog(
            onDismiss = { showPaidFeatureDialog = false },
            title = "Cloud Backup",
            message = "Cloud backup and sync is only available for paid users. Upgrade now to keep your data safe and synced across devices."
        )
    }
}
