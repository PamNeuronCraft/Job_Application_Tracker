package com.pamneuroncraft.jobapplicationtracker.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun rememberPlatformColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean
): ColorScheme {
    return if (darkTheme) DarkColorScheme else LightColorScheme
}

@Composable
actual fun PlatformThemeSideEffects(darkTheme: Boolean) {
    // No-op for now as iOS handles status bar style via UIViewController/Info.plist
    // or preferredStatusBarStyle in the view controller.
}
