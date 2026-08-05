package com.pamneuroncraft.jobapplicationtracker.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class AndroidAppUpdateManager(private val activity: Activity) : AppUpdateManager {
    private val manager = AppUpdateManagerFactory.create(activity)

    override fun checkForUpdates() {
        val task = manager.appUpdateInfo
        task.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                manager.startUpdateFlowForResult(
                    info,
                    AppUpdateType.IMMEDIATE,
                    activity,
                    999 // REQUEST_CODE
                )
            }
        }
    }
}

@Composable
actual fun rememberAppUpdateManager(): AppUpdateManager {
    val context = LocalContext.current
    return remember(context) {
        AndroidAppUpdateManager(context as Activity)
    }
}
