package com.pamneuroncraft.jobapplicationtracker.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface BillingManager {
    val isPremium: StateFlow<Boolean>
    
    suspend fun initialize()
    suspend fun logIn(uid: String)
    suspend fun logOut()
    suspend fun purchaseSubscription(): Result<Unit>
    suspend fun restorePurchases(): Result<Unit>
}
