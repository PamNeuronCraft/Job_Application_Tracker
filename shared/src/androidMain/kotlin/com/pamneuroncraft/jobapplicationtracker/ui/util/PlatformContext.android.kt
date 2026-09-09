package com.pamneuroncraft.jobapplicationtracker.ui.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPlatformContext(): Any? = LocalContext.current

actual fun exitApp(context: Any?) {
    (context as? Activity)?.finish()
}
