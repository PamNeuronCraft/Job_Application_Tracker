package com.pamneuroncraft.jobapplicationtracker.domain.repository

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.pamneuroncraft.jobapplicationtracker.AppConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.security.SecureRandom
import android.util.Base64

class AndroidSocialAuthManager : SocialAuthManager, KoinComponent {
    private val appConfig: AppConfig by inject()
    override val isAppleSignInSupported: Boolean = false

    private fun findActivity(context: Context): ComponentActivity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is ComponentActivity) return currentContext
            currentContext = currentContext.baseContext
        }
        return null
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
    }

    override suspend fun signInWithGoogle(activityContext: Any?): SocialAuthResult? {
        Log.e("SocialAuth", "signInWithGoogle starting...")
        val context = activityContext as? Context ?: run {
            Log.e("SocialAuth", "Failed: activityContext is not a Context")
            return null
        }

        val packageName = context.packageName
        Log.e("SocialAuth", "Runtime Package Name: $packageName")

        val activity = findActivity(context) ?: run {
            Log.e("SocialAuth", "Failed: Could not find ComponentActivity from context")
            return null
        }

        val credentialManager = CredentialManager.create(activity)
        val clientId = appConfig.googleWebClientId
        val nonce = generateNonce()
        Log.e("SocialAuth", "Using Client ID: $clientId")

        // Use GetGoogleIdOption for a robust and modern Google Sign-In experience
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Show all accounts on first sign-in
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true) // Still allow auto-select if one account is authorized
            .setNonce(nonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            Log.e("SocialAuth", "Calling getCredential...")
            val result = credentialManager.getCredential(activity, request)
            handleCredentialResponse(result.credential)
        } catch (e: NoCredentialException) {
            Log.e("SocialAuth", "No credentials available. Ensure SHA-1 is registered in Google Console.")
            null
        } catch (e: GetCredentialCancellationException) {
            Log.e("SocialAuth", "User cancelled the sign-in flow")
            null
        } catch (e: GetCredentialException) {
            Log.e("SocialAuth", "Credential Manager error (${e.type}): ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e("SocialAuth", "Unexpected error during sign-in: ${e.message}", e)
            null
        }
    }

    private fun handleCredentialResponse(credential: androidx.credentials.Credential): SocialAuthResult? {
        Log.e("SocialAuth", "Got credential: ${credential.type}")

        return if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.e("SocialAuth", "Success! Got ID Token")
                SocialAuthResult(idToken = googleIdTokenCredential.idToken)
            } catch (e: Exception) {
                Log.e("SocialAuth", "Failed to parse Google ID Token: ${e.message}")
                null
            }
        } else {
            Log.e("SocialAuth", "Failed: Credential is not GoogleIdTokenCredential")
            null
        }
    }

    override suspend fun signInWithApple(activityContext: Any?): SocialAuthResult? {
        return null
    }

    override suspend fun requestEmailScope(provider: com.pamneuroncraft.jobapplicationtracker.domain.model.EmailProvider, activityContext: Any?): Boolean {
        if (provider != com.pamneuroncraft.jobapplicationtracker.domain.model.EmailProvider.GMAIL) return false
        
        val context = activityContext as? Context ?: return false
        val activity = findActivity(context) ?: return false
        
        val gmailScope = "https://www.googleapis.com/auth/gmail.readonly"
        val scope = com.google.android.gms.common.api.Scope(gmailScope)
        
        val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(activity) ?: return false
        
        if (com.google.android.gms.auth.api.signin.GoogleSignIn.hasPermissions(account, scope)) {
            return true
        }

        // Trigger incremental authorization
        com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions(
            activity,
            1001, // Request code
            account,
            scope
        )
        
        // Note: In a real app, you'd need to handle the activity result.
        // For simplicity in this backbone, we assume the user might need to retry or 
        // we'll handle the result in the next iteration.
        return false 
    }
}

actual fun createSocialAuthManager(): SocialAuthManager = AndroidSocialAuthManager()
