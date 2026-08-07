package com.pamneuroncraft.jobapplicationtracker.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidPermissionManager : PermissionManager, KoinComponent {
    private val context: Context by inject()

    override fun checkPermission(permission: Permission): PermissionState {
        return when (permission) {
            Permission.NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val status = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    if (status == PackageManager.PERMISSION_GRANTED) PermissionState.GRANTED else PermissionState.DENIED
                } else {
                    PermissionState.GRANTED
                }
            }
        }
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    @Composable
    override fun RequestPermission(
        permission: Permission,
        onResult: (Boolean) -> Unit
    ) {
        val androidPermission = when (permission) {
            Permission.NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                } else {
                    null
                }
            }
        }

        if (androidPermission == null) {
            LaunchedEffect(Unit) {
                onResult(true)
            }
            return
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResult(isGranted)
        }

        LaunchedEffect(permission) {
            launcher.launch(androidPermission)
        }
    }
}

actual fun createPermissionManager(): PermissionManager = AndroidPermissionManager()
