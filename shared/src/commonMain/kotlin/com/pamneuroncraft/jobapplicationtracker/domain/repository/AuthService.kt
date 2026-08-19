package com.pamneuroncraft.jobapplicationtracker.domain.repository

import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailProvider
import kotlinx.coroutines.flow.Flow

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?
)

interface AuthService {
    val currentUser: Flow<User?>
    suspend fun signUp(email: String, password: String, name: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signInWithGoogle(idToken: String): Result<Unit>
    suspend fun signInWithApple(idToken: String, rawNonce: String): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signOut()
    fun isUserSignedIn(): Boolean

    /**
     * Requests additional permissions to read emails from the specified provider.
     * @param context The platform-specific context (e.g., Activity on Android)
     */
    suspend fun requestEmailScope(provider: EmailProvider, context: Any?): Result<Unit>
}
