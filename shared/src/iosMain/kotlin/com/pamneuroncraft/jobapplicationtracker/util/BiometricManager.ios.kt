package com.pamneuroncraft.jobapplicationtracker.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.cinterop.ExperimentalForeignApi

class IosBiometricManager : BiometricManager {
    
    @OptIn(ExperimentalForeignApi::class)
    override fun canAuthenticate(): Boolean {
        val context = LAContext()
        return context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, null)
    }

    @Composable
    override fun Authenticate(onResult: (BiometricResult) -> Unit) {
        LaunchedEffect(Unit) {
            val context = LAContext()
            context.evaluatePolicy(
                policy = LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                localizedReason = "Unlock your Job Tracker",
                reply = { success, error ->
                    MainScope().launch {
                        if (success) {
                            onResult(BiometricResult.Success)
                        } else {
                            if (error != null) {
                                onResult(BiometricResult.Error(error.localizedDescription))
                            } else {
                                onResult(BiometricResult.Failure)
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
actual fun createBiometricManager(): BiometricManager = IosBiometricManager()
