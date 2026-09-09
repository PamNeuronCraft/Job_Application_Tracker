package com.pamneuroncraft.jobapplicationtracker.ui.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPlatformContext(): Any? = null

actual fun exitApp(context: Any?) {
    // No-op for iOS
}
