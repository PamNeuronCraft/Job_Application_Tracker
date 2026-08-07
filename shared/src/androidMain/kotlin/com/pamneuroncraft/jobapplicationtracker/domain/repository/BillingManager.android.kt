package com.pamneuroncraft.jobapplicationtracker.domain.repository

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.models.CacheFetchPolicy
import com.pamneuroncraft.jobapplicationtracker.AppBuildKonfig
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.component.KoinComponent
import kotlin.coroutines.resume

class AndroidBillingManager(
    private val localSettings: LocalSettings,
    private val appConfig: com.pamneuroncraft.jobapplicationtracker.AppConfig
) : BillingManager, KoinComponent {

    private val _isPremium = MutableStateFlow(localSettings.isPremium || appConfig.isDebug)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    override suspend fun initialize() {
        val apiKey = if (appConfig.isDebug) {
            AppBuildKonfig.REVENUECAT_API_KEY_ANDROID_DEBUG
        } else {
            AppBuildKonfig.REVENUECAT_API_KEY_ANDROID_RELEASE
        }
        Purchases.configure(apiKey = apiKey) {
            appUserId = null 
        }
        updateEntitlementStatus()
    }

    override suspend fun logIn(uid: String) {
        suspendCancellableCoroutine { continuation ->
            Purchases.sharedInstance.logIn(
                newAppUserID = uid,
                onError = { continuation.resume(Unit) },
                onSuccess = { info, _ -> 
                    val hasPremium = info.entitlements.active.containsKey("com.pamneuroncraft.jobapplicationtracker Pro")
                    _isPremium.value = hasPremium || appConfig.isDebug
                    localSettings.isPremium = hasPremium
                    continuation.resume(Unit)
                }
            )
        }
    }

    override suspend fun logOut() {
        suspendCancellableCoroutine { continuation ->
            Purchases.sharedInstance.logOut(
                onError = { continuation.resume(Unit) },
                onSuccess = { info -> 
                    val hasPremium = info.entitlements.active.containsKey("com.pamneuroncraft.jobapplicationtracker Pro")
                    _isPremium.value = hasPremium || appConfig.isDebug
                    localSettings.isPremium = hasPremium
                    continuation.resume(Unit)
                }
            )
        }
    }

    private suspend fun updateEntitlementStatus() {
        try {
            val hasPremium = suspendCancellableCoroutine { continuation ->
                Purchases.sharedInstance.getCustomerInfo(
                    fetchPolicy = CacheFetchPolicy.CACHE_ONLY,
                    onError = { continuation.resume(false) },
                    onSuccess = { info -> 
                        continuation.resume(info.entitlements.active.containsKey("com.pamneuroncraft.jobapplicationtracker Pro"))
                    }
                )
            }
            _isPremium.value = hasPremium || appConfig.isDebug
            localSettings.isPremium = hasPremium
        } catch (e: Exception) {
            // Keep local
        }
    }

    override suspend fun purchaseSubscription(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        Purchases.sharedInstance.getOfferings(
            onError = { continuation.resume(Result.failure(Exception(it.message))) },
            onSuccess = { offerings ->
                val packageToBuy = offerings.current?.monthly
                if (packageToBuy != null) {
                    Purchases.sharedInstance.purchase(
                        packageToBuy,
                        onError = { error, userCancelled ->
                            if (userCancelled) {
                                continuation.resume(Result.failure(Exception("User cancelled")))
                            } else {
                                continuation.resume(Result.failure(Exception(error.message)))
                            }
                        },
                        onSuccess = { _, customerInfo ->
                            val hasPremium = customerInfo.entitlements.active.containsKey("com.pamneuroncraft.jobapplicationtracker Pro")
                            _isPremium.value = hasPremium || appConfig.isDebug
                            localSettings.isPremium = hasPremium
                            continuation.resume(Result.success(Unit))
                        }
                    )
                } else {
                    continuation.resume(Result.failure(Exception("No monthly offering found")))
                }
            }
        )
    }

    override suspend fun restorePurchases(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        Purchases.sharedInstance.restorePurchases(
            onError = { continuation.resume(Result.failure(Exception(it.message))) },
            onSuccess = { customerInfo ->
                val hasPremium = customerInfo.entitlements.active.containsKey("com.pamneuroncraft.jobapplicationtracker Pro")
                _isPremium.value = hasPremium || appConfig.isDebug
                localSettings.isPremium = hasPremium
                continuation.resume(Result.success(Unit))
            }
        )
    }
}
