package com.pamneuroncraft.jobapplicationtracker.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.pamneuroncraft.jobapplicationtracker.util.PermissionManager
import org.koin.compose.koinInject
import org.jetbrains.compose.resources.stringResource
import jobapplicationtracker.app.generated.resources.*

@Composable
fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    permissionManager: PermissionManager = koinInject()
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.permission_required_title)) },
        text = {
            Text(stringResource(Res.string.permission_required_desc))
        },
        confirmButton = {
            Button(
                onClick = {
                    permissionManager.openAppSettings()
                    onDismiss()
                }
            ) {
                Text(stringResource(Res.string.open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}
