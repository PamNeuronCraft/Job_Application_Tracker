package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.data.local.ThemePreference
import com.pamneuroncraft.jobapplicationtracker.domain.repository.BillingManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.ExportManager
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.ExportJobsToCsvUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val localSettings: LocalSettings,
    private val billingManager: BillingManager,
    private val exportJobsToCsvUseCase: ExportJobsToCsvUseCase,
    private val exportManager: ExportManager
) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = localSettings.themePreferenceFlow
    val useDynamicColor: StateFlow<Boolean> = localSettings.useDynamicColorFlow
    val isPremium: StateFlow<Boolean> = billingManager.isPremium

    private val _isBiometricEnabled = MutableStateFlow(localSettings.isBiometricEnabled)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    fun onThemePreferenceChange(preference: ThemePreference) {
        localSettings.themePreference = preference
    }

    fun onUseDynamicColorChange(enabled: Boolean) {
        localSettings.useDynamicColor = enabled
    }

    fun onBiometricEnabledChange(enabled: Boolean) {
        localSettings.isBiometricEnabled = enabled
        _isBiometricEnabled.value = enabled
    }

    fun exportData() {
        viewModelScope.launch {
            val csv = exportJobsToCsvUseCase()
            exportManager.shareCsv(csv, "job_applications_export.csv")
        }
    }
}
