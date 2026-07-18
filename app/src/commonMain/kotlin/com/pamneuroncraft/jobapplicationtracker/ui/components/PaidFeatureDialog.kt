package com.pamneuroncraft.jobapplicationtracker.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun PaidFeatureDialog(
    onDismiss: () -> Unit,
    title: String = "Premium Feature",
    message: String = "This feature is only available for paid users. Upgrade now to unlock all premium capabilities."
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Text(message)
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}
