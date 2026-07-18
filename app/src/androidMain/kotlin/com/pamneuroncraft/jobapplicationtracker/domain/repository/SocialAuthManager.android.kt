package com.pamneuroncraft.jobapplicationtracker.domain.repository

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.pamneuroncraft.jobapplicationtracker.AppBuildKonfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidSocialAuthManager : SocialAuthManager, KoinComponent {
    private val context: Context by inject()

    @Composable
    override fun RequestGoogleSignIn(onResult: (SocialAuthResult?) -> Unit) {
        val credentialManager = CredentialManager.create(context)
        
        LaunchedEffect(Unit) {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(AppBuildKonfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                
                if (credential is CustomCredential && 
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    onResult(SocialAuthResult(idToken = googleIdTokenCredential.idToken))
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    @Composable
    override fun RequestAppleSignIn(onResult: (SocialAuthResult?) -> Unit) {
        LaunchedEffect(Unit) {
            onResult(null)
        }
    }
}

actual fun createSocialAuthManager(): SocialAuthManager = AndroidSocialAuthManager()
