package com.pamneuroncraft.jobapplicationtracker.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun PaidFeatureDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Premium Feature") },
        text = {
            Text("Cloud backup and sync is only available for paid users. Upgrade now to keep your data safe and synced across devices.")
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}
