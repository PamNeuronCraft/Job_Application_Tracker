package com.pamneuroncraft.jobapplicationtracker.util

import androidx.compose.runtime.Composable

enum class Permission {
    NOTIFICATIONS
}

enum class PermissionState {
    GRANTED,
    DENIED,
    NOT_DETERMINED
}

interface PermissionManager {
    fun checkPermission(permission: Permission): PermissionState
    fun openAppSettings()
    
    @Composable
    fun RequestPermission(
        permission: Permission,
        onResult: (Boolean) -> Unit
    )
}

expect fun createPermissionManager(): PermissionManager
