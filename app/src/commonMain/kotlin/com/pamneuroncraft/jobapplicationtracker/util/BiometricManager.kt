package com.pamneuroncraft.jobapplicationtracker.util

import androidx.compose.runtime.Composable

interface BiometricManager {
    fun canAuthenticate(): Boolean
    
    @Composable
    fun Authenticate(
        onResult: (BiometricResult) -> Unit
    )
}

sealed class BiometricResult {
    data object Success : BiometricResult()
    data class Error(val message: String) : BiometricResult()
    data object Failure : BiometricResult()
}

@Composable
expect fun createBiometricManager(): BiometricManager
