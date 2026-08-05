package com.pamneuroncraft.jobapplicationtracker.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import platform.StoreKit.SKStoreReviewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene
import platform.UIKit.UISceneActivationStateForegroundActive

class IosInAppReviewManager : InAppReviewManager {
    override fun requestReview() {
        val scene = UIApplication.sharedApplication.connectedScenes
            .mapNotNull { it as? UIWindowScene }
            .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
        
        if (scene != null) {
            SKStoreReviewController.requestReviewInScene(scene)
        } else {
            // Fallback for older iOS versions
            @Suppress("DEPRECATION")
            SKStoreReviewController.requestReview()
        }
    }
}

@Composable
actual fun rememberInAppReviewManager(): InAppReviewManager {
    return remember { IosInAppReviewManager() }
}
