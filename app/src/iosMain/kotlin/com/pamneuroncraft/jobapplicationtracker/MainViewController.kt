package com.pamneuroncraft.jobapplicationtracker

import androidx.compose.ui.window.ComposeUIViewController
import com.pamneuroncraft.jobapplicationtracker.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}
