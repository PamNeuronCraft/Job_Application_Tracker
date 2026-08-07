package com.pamneuroncraft.jobapplicationtracker.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics

class IosBiometricManager : BiometricManager {

    @OptIn(ExperimentalForeignApi::class)
    override fun canAuthenticate(): Boolean {
        val context = LAContext()
        return context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)
    }

    // Plain suspend function — NOT composable, NOT nested inside one.
    // The ObjC block callback now lives here instead of inside Authenticate's body.
    @OptIn(ExperimentalForeignApi::class)
    private suspend fun evaluateBiometrics(): BiometricResult = suspendCancellableCoroutine { continuation ->
        val context = LAContext()
        context.evaluatePolicy(
            policy = LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = "Unlock your Job Tracker",
            reply = { success, error ->
                val result = when {
                    success -> BiometricResult.Success
                    error != null -> BiometricResult.Error(error.localizedDescription)
                    else -> BiometricResult.Failure
                }
                if (continuation.isActive) {
                    continuation.resumeWith(Result.success(result))
                }
            }
        )
    }

    @Composable
    override fun Authenticate(onResult: (BiometricResult) -> Unit) {
        LaunchedEffect(Unit) {
            onResult(evaluateBiometrics())
        }
    }
}

@Composable
actual fun createBiometricManager(): BiometricManager = IosBiometricManager()