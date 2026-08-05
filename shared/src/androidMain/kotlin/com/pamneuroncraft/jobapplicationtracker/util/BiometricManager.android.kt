package com.pamneuroncraft.jobapplicationtracker.util

import androidx.biometric.BiometricManager as AndroidBiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class AndroidBiometricManager(
    private val activity: FragmentActivity
) : BiometricManager {

    override fun canAuthenticate(): Boolean {
        val biometricManager = AndroidBiometricManager.from(activity)
        return biometricManager.canAuthenticate(
            AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG or 
            AndroidBiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == AndroidBiometricManager.BIOMETRIC_SUCCESS
    }

    @Composable
    override fun Authenticate(onResult: (BiometricResult) -> Unit) {
        val executor = ContextCompat.getMainExecutor(activity)
        
        LaunchedEffect(Unit) {
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onResult(BiometricResult.Success)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        onResult(BiometricResult.Error(errString.toString()))
                    }

                    override fun onAuthenticationFailed() {
                        onResult(BiometricResult.Failure)
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Job Tracker")
                .setSubtitle("Use your biometric credential to continue")
                .setAllowedAuthenticators(
                    AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG or 
                    AndroidBiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }
}

@Composable
actual fun createBiometricManager(): BiometricManager {
    val activity = LocalContext.current as FragmentActivity
    return AndroidBiometricManager(activity)
}
