package com.pamneuroncraft.jobapplicationtracker.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNNotificationSettings
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UIKit.UIApplication
import platform.Foundation.NSURL
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class IosPermissionManager : PermissionManager {

    override fun checkPermission(permission: Permission): PermissionState {
        return when (permission) {
            Permission.NOTIFICATIONS -> {
                // UNUserNotificationCenter check is asynchronous, so we'll use a simplified version
                // or assume NOT_DETERMINED if we can't block.
                // For a more robust solution, we'd use a suspend function or a state holder.
                // For now, let's return NOT_DETERMINED to force a request if needed.
                PermissionState.NOT_DETERMINED
            }
        }
    }

    override fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (url != null) {
            UIApplication.sharedApplication.openURL(url)
        }
    }

    @Composable
    override fun RequestPermission(
        permission: Permission,
        onResult: (Boolean) -> Unit
    ) {
        LaunchedEffect(permission) {
            when (permission) {
                Permission.NOTIFICATIONS -> {
                    val center = UNUserNotificationCenter.currentNotificationCenter()
                    val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
                    center.requestAuthorizationWithOptions(options) { granted, error ->
                        MainScope().launch {
                            onResult(granted)
                        }
                    }
                }
            }
        }
    }
}

actual fun createPermissionManager(): PermissionManager = IosPermissionManager()
