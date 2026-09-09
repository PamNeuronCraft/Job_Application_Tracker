package com.pamneuroncraft.jobapplicationtracker.util

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS handles back navigation via UIKit navigation controller
}
