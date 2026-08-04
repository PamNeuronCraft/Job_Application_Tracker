package com.pamneuroncraft.jobapplicationtracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.pamneuroncraft.jobapplicationtracker.data.local.ThemePreference

val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun JobApplicationTrackerTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    
    val colorScheme = rememberPlatformColorScheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor
    )

    PlatformThemeSideEffects(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
expect fun rememberPlatformColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean
): ColorScheme

@Composable
expect fun PlatformThemeSideEffects(darkTheme: Boolean)
