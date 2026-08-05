package com.pamneuroncraft.jobapplicationtracker.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AndroidAnalyticsHelper(
    context: Context
) : AnalyticsHelper {
    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun logEvent(name: String, params: Map<String, Any>) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }
        analytics.logEvent(name, bundle)
    }

    override fun logNonFatal(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun setUserId(userId: String?) {
        analytics.setUserId(userId)
        crashlytics.setUserId(userId ?: "")
    }
}

class AndroidAnalyticsHelperFactory : KoinComponent {
    private val context: Context by inject()
    fun create(): AnalyticsHelper = AndroidAnalyticsHelper(context)
}

actual fun createAnalyticsHelper(): AnalyticsHelper = AndroidAnalyticsHelperFactory().create()
