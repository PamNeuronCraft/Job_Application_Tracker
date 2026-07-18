package com.pamneuroncraft.jobapplicationtracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.data.local.ThemePreference
import kotlinx.coroutines.flow.*

class SettingsViewModel(
    private val localSettings: LocalSettings
) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = localSettings.themePreferenceFlow

    private val _isBiometricEnabled = MutableStateFlow(localSettings.isBiometricEnabled)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    fun onThemePreferenceChange(preference: ThemePreference) {
        localSettings.themePreference = preference
    }

    fun onBiometricEnabledChange(enabled: Boolean) {
        localSettings.isBiometricEnabled = enabled
        _isBiometricEnabled.value = enabled
    }
}
