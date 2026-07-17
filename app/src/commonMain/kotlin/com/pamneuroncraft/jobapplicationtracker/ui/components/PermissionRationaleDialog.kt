package com.pamneuroncraft.jobapplicationtracker.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.pamneuroncraft.jobapplicationtracker.util.PermissionManager
import org.koin.compose.koinInject

@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    permissionManager: PermissionManager = koinInject()
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Permission Required") },
        text = {
            Text("To schedule interview reminders, the app needs permission to show notifications. Please grant this permission in the system settings.")
        },
        confirmButton = {
            Button(
                onClick = {
                    permissionManager.openAppSettings()
                    onDismiss()
                }
            ) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
