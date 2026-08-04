package com.pamneuroncraft.jobapplicationtracker.util

import androidx.compose.runtime.Composable

interface AppUpdateManager {
    fun checkForUpdates()
}

@Composable
expect fun rememberAppUpdateManager(): AppUpdateManager
