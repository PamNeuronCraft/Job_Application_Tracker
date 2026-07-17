package com.pamneuroncraft.jobapplicationtracker.domain.repository

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
    suspend fun signOut()
    fun isUserSignedIn(): Boolean
}
