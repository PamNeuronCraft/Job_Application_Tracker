package com.pamneuroncraft.jobapplicationtracker

import androidx.compose.ui.window.ComposeUIViewController
import com.pamneuroncraft.jobapplicationtracker.di.initKoin

fun MainViewController(shortcut: String? = null) = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App(initialShortcut = shortcut)
}
