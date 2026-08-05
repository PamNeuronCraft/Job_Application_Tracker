package com.pamneuroncraft.jobapplicationtracker.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import org.jetbrains.compose.resources.stringResource
import com.pamneuroncraft.jobapplicationtracker.*

@Composable
fun PaidFeatureDialog(
    onDismiss: () -> Unit,
    title: String = stringResource(Res.string.premium_feature),
    message: String = stringResource(Res.string.premium_feature_default_msg)
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Text(message)
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(Res.string.got_it))
            }
        }
    )
}
