package com.pamneuroncraft.jobapplicationtracker.domain.repository

import androidx.compose.runtime.Composable

data class SocialAuthResult(
    val idToken: String,
    val rawNonce: String? = null
)

interface SocialAuthManager {
    @Composable
    fun RequestGoogleSignIn(onResult: (SocialAuthResult?) -> Unit)
    
    @Composable
    fun RequestAppleSignIn(onResult: (SocialAuthResult?) -> Unit)
}

expect fun createSocialAuthManager(): SocialAuthManager
