package com.pamneuroncraft.jobapplicationtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import jobapplicationtracker.app.generated.resources.*

@Composable
fun PremiumUpsellDialog(
    onDismiss: () -> Unit,
    onNavigateToSubscription: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(Res.string.welcome_to_tracker),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.upsell_desc))
                
                PremiumBenefitItem(stringResource(Res.string.benefit_ai_extraction))
                PremiumBenefitItem(stringResource(Res.string.benefit_visual_analytics))
                PremiumBenefitItem(stringResource(Res.string.benefit_google_drive))
                PremiumBenefitItem(stringResource(Res.string.benefit_ad_free))
            }
        },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                onNavigateToSubscription()
            }) {
                Text(stringResource(Res.string.learn_more))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.maybe_later))
            }
        }
    )
}

@Composable
private fun PremiumBenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
