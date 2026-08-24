package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.data.local.ThemePreference
import com.pamneuroncraft.jobapplicationtracker.domain.model.AppCurrency
import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailProvider
import com.pamneuroncraft.jobapplicationtracker.domain.repository.AuthService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.BillingManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.ExportManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.SyncManager
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.ExportJobsToCsvUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val localSettings: LocalSettings,
    private val billingManager: BillingManager,
    private val authService: AuthService,
    private val syncManager: SyncManager,
    private val exportJobsToCsvUseCase: ExportJobsToCsvUseCase,
    private val exportManager: ExportManager
) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = localSettings.themePreferenceFlow
    val useDynamicColor: StateFlow<Boolean> = localSettings.useDynamicColorFlow
    val preferredCurrency: StateFlow<AppCurrency> = localSettings.preferredCurrencyFlow
    val isPremium: StateFlow<Boolean> = billingManager.isPremium

    private val _isBiometricEnabled = MutableStateFlow(localSettings.isBiometricEnabled)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isEmailSyncEnabled = MutableStateFlow(localSettings.isEmailSyncEnabled)
    val isEmailSyncEnabled: StateFlow<Boolean> = _isEmailSyncEnabled.asStateFlow()

    init {
        // Sync email if enabled on start
        if (localSettings.isEmailSyncEnabled) {
            syncManager.scheduleEmailSync()
        }

        // Reconcile premium settings if subscription expires
        isPremium.onEach { premium ->
            if (!premium) {
                if (_isBiometricEnabled.value) {
                    onBiometricEnabledChange(false)
                }
                if (_isEmailSyncEnabled.value) {
                    localSettings.isEmailSyncEnabled = false
                    _isEmailSyncEnabled.value = false
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onThemePreferenceChange(preference: ThemePreference) {
        localSettings.themePreference = preference
    }

    fun onUseDynamicColorChange(enabled: Boolean) {
        localSettings.useDynamicColor = enabled
    }

    fun onCurrencyChange(currency: AppCurrency) {
        localSettings.preferredCurrency = currency
    }

    fun onBiometricEnabledChange(enabled: Boolean) {
        localSettings.isBiometricEnabled = enabled
        _isBiometricEnabled.value = enabled
    }

    fun onEmailSyncEnabledChange(enabled: Boolean, context: Any?, provider: EmailProvider = EmailProvider.GMAIL) {
        viewModelScope.launch {
            if (enabled) {
                val result = authService.requestEmailScope(provider, context)
                if (result.isSuccess) {
                    localSettings.isEmailSyncEnabled = true
                    _isEmailSyncEnabled.value = true
                    syncManager.scheduleEmailSync()
                }
            } else {
                localSettings.isEmailSyncEnabled = false
                _isEmailSyncEnabled.value = false
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            val csv = exportJobsToCsvUseCase()
            exportManager.shareCsv(csv, "job_applications_export.csv")
        }
    }
}
