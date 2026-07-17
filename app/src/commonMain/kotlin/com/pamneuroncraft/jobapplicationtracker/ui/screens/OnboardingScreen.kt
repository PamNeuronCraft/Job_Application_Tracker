package com.pamneuroncraft.jobapplicationtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.util.Permission
import com.pamneuroncraft.jobapplicationtracker.util.PermissionManager
import org.koin.compose.koinInject

@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    permissionManager: PermissionManager = koinInject(),
    localSettings: LocalSettings = koinInject()
) {
    var isRequestingPermission by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Stay on Top of Your Interviews",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Get timely reminders 30 minutes before your scheduled interviews so you never miss an opportunity.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = { isRequestingPermission = true },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Get Started")
            }
        }
    }

    if (isRequestingPermission) {
        permissionManager.RequestPermission(Permission.NOTIFICATIONS) { granted ->
            isRequestingPermission = false
            localSettings.isOnboardingCompleted = true
            onOnboardingComplete()
        }
    }
}
