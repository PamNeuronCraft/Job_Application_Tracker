package com.pamneuroncraft.jobapplicationtracker.data.local

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemePreference {
    SYSTEM, LIGHT, DARK
}

class LocalSettings(private val settings: Settings) {
    
    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "is_onboarding_completed"
        private const val KEY_THEME_PREFERENCE = "theme_preference"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_USE_DYNAMIC_COLOR = "use_dynamic_color"
        private const val KEY_REVIEW_REQUESTED = "review_requested"
    }

    private val _themePreferenceFlow = MutableStateFlow(getInitialTheme())
    val themePreferenceFlow: StateFlow<ThemePreference> = _themePreferenceFlow.asStateFlow()

    private val _useDynamicColorFlow = MutableStateFlow(getInitialUseDynamicColor())
    val useDynamicColorFlow: StateFlow<Boolean> = _useDynamicColorFlow.asStateFlow()

    var isOnboardingCompleted: Boolean
        get() = settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = settings.putBoolean(KEY_ONBOARDING_COMPLETED, value)

    private fun getInitialTheme(): ThemePreference {
        val name = settings.getString(KEY_THEME_PREFERENCE, ThemePreference.SYSTEM.name)
        return try {
            ThemePreference.valueOf(name)
        } catch (e: Exception) {
            ThemePreference.SYSTEM
        }
    }

    var themePreference: ThemePreference
        get() = _themePreferenceFlow.value
        set(value) {
            settings.putString(KEY_THEME_PREFERENCE, value.name)
            _themePreferenceFlow.value = value
        }

    private fun getInitialUseDynamicColor(): Boolean {
        return settings.getBoolean(KEY_USE_DYNAMIC_COLOR, true)
    }

    var useDynamicColor: Boolean
        get() = _useDynamicColorFlow.value
        set(value) {
            settings.putBoolean(KEY_USE_DYNAMIC_COLOR, value)
            _useDynamicColorFlow.value = value
        }

    var isReviewRequested: Boolean
        get() = settings.getBoolean(KEY_REVIEW_REQUESTED, false)
        set(value) = settings.putBoolean(KEY_REVIEW_REQUESTED, value)

    var isBiometricEnabled: Boolean
        get() = settings.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = settings.putBoolean(KEY_BIOMETRIC_ENABLED, value)

    var isPremium: Boolean
        get() = settings.getBoolean(KEY_IS_PREMIUM, false)
        set(value) = settings.putBoolean(KEY_IS_PREMIUM, value)
}
