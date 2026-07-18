package com.pamneuroncraft.jobapplicationtracker.data.repository

import com.pamneuroncraft.jobapplicationtracker.domain.repository.AuthService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.User
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.OAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FirebaseAuthService : AuthService {
    private val auth = Firebase.auth

    override val currentUser: Flow<User?> = auth.authStateChanged.map { firebaseUser ->
        firebaseUser?.let {
            User(
                uid = it.uid,
                email = it.email,
                displayName = it.displayName,
            )
        }
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password)
            result.user?.updateProfile(displayName = name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.credential(idToken, null)
            auth.signInWithCredential(credential)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithApple(idToken: String, rawNonce: String): Result<Unit> {
        return try {
            val credential = OAuthProvider.credential(
                providerId = "apple.com",
                idToken = idToken,
                rawNonce = rawNonce
            )
            auth.signInWithCredential(credential)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
}
