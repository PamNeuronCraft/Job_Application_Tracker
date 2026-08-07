package com.pamneuroncraft.jobapplicationtracker

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.FirebaseFirestore
import com.pamneuroncraft.jobapplicationtracker.di.initKoin
import com.pamneuroncraft.jobapplicationtracker.domain.repository.BillingManager
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import androidx.core.content.edit

class JobTrackerApp : Application() {
    private val billingManager: BillingManager by inject()

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this)

        if (BuildConfig.DEBUG) {
            FirebaseFirestore.setLoggingEnabled(true)
        }

        handleFirestoreCacheRecovery()
        
        initKoin {
            androidLogger()
            androidContext(this@JobTrackerApp)
        }

        MainScope().launch {
            billingManager.initialize()
        }
    }

    /**
     * Recovery logic for Firestore "Internal Error" (26.4.1) panics.
     * Clears local persistence if it hasn't been cleared before in this version
     * to recover from SQLite corruption caused by the 1MB chunking feature.
     */
    private fun handleFirestoreCacheRecovery() {
        val prefs = getSharedPreferences("firestore_recovery", MODE_PRIVATE)
        val isCleared = prefs.getBoolean("cache_cleared_26_4_1", false)

        if (!isCleared) {
            MainScope().launch {
                try {
                    // This is a last resort to fix "Internal Error (26.4.1)"
                    // It clears all unsynced local data and forces a fresh sync.
                    FirebaseFirestore.getInstance().clearPersistence()
                    prefs.edit { putBoolean("cache_cleared_26_4_1", true) }
                } catch (e: Exception) {
                    // Silently fail if persistence can't be cleared (e.g. Firestore not initialized)
                }
            }
        }
    }
}
