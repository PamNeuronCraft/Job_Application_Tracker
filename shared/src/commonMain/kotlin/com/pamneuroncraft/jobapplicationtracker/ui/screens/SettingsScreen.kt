package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.AppConfig
import com.pamneuroncraft.jobapplicationtracker.data.local.ThemePreference
import com.pamneuroncraft.jobapplicationtracker.domain.model.AppCurrency
import com.pamneuroncraft.jobapplicationtracker.shared.*
import com.pamneuroncraft.jobapplicationtracker.ui.components.AdBanner
import com.pamneuroncraft.jobapplicationtracker.ui.components.PaidFeatureDialog
import com.pamneuroncraft.jobapplicationtracker.ui.components.PremiumBadge
import com.pamneuroncraft.jobapplicationtracker.ui.util.rememberPlatformContext
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.BackupViewModel
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.SettingsViewModel
import com.pamneuroncraft.jobapplicationtracker.util.BiometricResult
import com.pamneuroncraft.jobapplicationtracker.util.createBiometricManager
import com.pamneuroncraft.jobapplicationtracker.util.isAndroid
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val preferredCurrency by viewModel.preferredCurrency.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val isEmailSyncEnabled by viewModel.isEmailSyncEnabled.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val biometricManager = createBiometricManager()
    val uriHandler = LocalUriHandler.current
    
    val platformContext = rememberPlatformContext()
    
    var showThemeMenu by remember { mutableStateOf(false) }
    var showCurrencyMenu by remember { mutableStateOf(false) }
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
                title = { Text(stringResource(Res.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                }
            )
        },
        bottomBar = {
            if (!isPremium) {
                AdBanner(modifier = Modifier.fillMaxWidth())
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Biometrics
            ListItem(
                headlineContent = {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Enable Biometrics",
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        if (!isPremium) {
                            PremiumBadge(modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                },
                supportingContent = { Text(stringResource(Res.string.settings_biometrics_desc)) },
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

            // Email Status Sync
            ListItem(
                headlineContent = {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Email Status Sync (Gmail)",
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        if (!isPremium) {
                            PremiumBadge(modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                },
                supportingContent = { Text(stringResource(Res.string.settings_email_sync_desc)) },
                leadingContent = { Icon(Icons.Default.Email, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = isEmailSyncEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (appConfig.featureEmailSync) {
                                    viewModel.onEmailSyncEnabledChange(true, platformContext)
                                } else {
                                    paidFeatureDialogTitle = "Email Status Sync"
                                    paidFeatureDialogMessage = "Automatically tracking job status from your emails is a premium feature. Upgrade now to save time!"
                                    showPaidFeatureDialog = true
                                }
                            } else {
                                viewModel.onEmailSyncEnabledChange(false, null)
                            }
                        }
                    )
                }
            )

            // Cloud Backup
            ListItem(
                headlineContent = {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Backup and Sync",
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        if (!isPremium) {
                            PremiumBadge(modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                },
                supportingContent = { Text(stringResource(Res.string.settings_backup_desc)) },
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
                supportingContent = { Text(stringResource(Res.string.settings_subscription_desc)) },
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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Export Data (CSV)",
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        if (!isPremium) {
                            PremiumBadge(modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                },
                supportingContent = { Text(stringResource(Res.string.settings_export_desc)) },
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
                supportingContent = { Text(stringResource(Res.string.settings_theme_desc)) },
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

            // Preferred Currency
            ListItem(
                headlineContent = { Text("Currency") },
                supportingContent = { Text(stringResource(Res.string.settings_currency_desc)) },
                leadingContent = { Icon(Icons.Default.Payments, contentDescription = null) },
                trailingContent = {
                    Box {
                        TextButton(onClick = { showCurrencyMenu = true }) {
                            Text("${preferredCurrency.code} (${preferredCurrency.symbol})")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = showCurrencyMenu,
                            onDismissRequest = { showCurrencyMenu = false }
                        ) {
                            AppCurrency.entries.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text("${currency.code} (${currency.symbol})") },
                                    onClick = {
                                        viewModel.onCurrencyChange(currency)
                                        showCurrencyMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            )

            // Dynamic Color (Material You)
            if (isAndroid) {
                ListItem(
                    headlineContent = { Text("Dynamic Colors") },
                    supportingContent = { Text(stringResource(Res.string.settings_dynamic_color_desc)) },
                    leadingContent = { Icon(Icons.Default.ColorLens, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = useDynamicColor,
                            onCheckedChange = { viewModel.onUseDynamicColorChange(it) }
                        )
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // About
            Text(
                text = stringResource(Res.string.settings_about),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text(stringResource(Res.string.settings_version, appConfig.appVersion)) },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
            )

            ListItem(
                headlineContent = { Text(stringResource(Res.string.settings_contact_support)) },
                supportingContent = { Text(stringResource(Res.string.settings_contact_support_desc)) },
                leadingContent = { Icon(Icons.Default.QuestionAnswer, contentDescription = null) },
                modifier = Modifier.clickable {
                    val subject = "Support Request: Job Application Tracker (${appConfig.appVersion})"
                    val body = "\n\n--- Device Info ---\nPlatform: ${if (isAndroid) "Android" else "iOS"}\nVersion: ${appConfig.appVersion}"
                    
                    val encodedSubject = subject.replace(" ", "%20")
                    val encodedBody = body.replace(" ", "%20").replace("\n", "%0A")
                    
                    uriHandler.openUri("mailto:pamneuroncraft@gmail.com?subject=$encodedSubject&body=$encodedBody")
                }
            )
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
