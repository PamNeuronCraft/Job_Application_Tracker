package com.pamneuroncraft.jobapplicationtracker.domain.repository

data class SocialAuthResult(
    val idToken: String,
    val rawNonce: String? = null
)

interface SocialAuthManager {
    val isAppleSignInSupported: Boolean

    suspend fun signInWithGoogle(activityContext: Any?): SocialAuthResult?
    
    suspend fun signInWithApple(activityContext: Any?): SocialAuthResult?

    /**
     * Requests additional email read scopes.
     */
    suspend fun requestEmailScope(provider: com.pamneuroncraft.jobapplicationtracker.domain.model.EmailProvider, activityContext: Any?): Boolean
}

expect fun createSocialAuthManager(): SocialAuthManager
