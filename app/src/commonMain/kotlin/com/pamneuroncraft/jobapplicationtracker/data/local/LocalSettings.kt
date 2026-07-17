package com.pamneuroncraft.jobapplicationtracker.data.local

import com.russhwolf.settings.Settings

class LocalSettings(private val settings: Settings) {
    
    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "is_onboarding_completed"
    }

    var isOnboardingCompleted: Boolean
        get() = settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = settings.putBoolean(KEY_ONBOARDING_COMPLETED, value)
}
