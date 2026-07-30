package com.pamneuroncraft.jobapplicationtracker.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformThemeSideEffects(darkTheme: Boolean) {
    // No-op for now as iOS handles status bar style via UIViewController/Info.plist
    // or preferredStatusBarStyle in the view controller.
}
