package com.pamneuroncraft.jobapplicationtracker.domain.repository

data class SocialAuthResult(
    val idToken: String,
    val rawNonce: String? = null
)

interface SocialAuthManager {
    val isAppleSignInSupported: Boolean

    suspend fun signInWithGoogle(activityContext: Any?): SocialAuthResult?
    
    suspend fun signInWithApple(activityContext: Any?): SocialAuthResult?
}

expect fun createSocialAuthManager(): SocialAuthManager
