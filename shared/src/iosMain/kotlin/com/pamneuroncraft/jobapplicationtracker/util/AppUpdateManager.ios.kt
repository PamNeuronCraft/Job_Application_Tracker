package com.pamneuroncraft.jobapplicationtracker.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class IosAppUpdateManager : AppUpdateManager {
    override fun checkForUpdates() {
        // iOS updates are usually handled by the App Store automatically
    }
}

@Composable
actual fun rememberAppUpdateManager(): AppUpdateManager {
    return remember { IosAppUpdateManager() }
}
