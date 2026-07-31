package com.pamneuroncraft.jobapplicationtracker.util

class IosAnalyticsHelper : AnalyticsHelper {
    override fun logEvent(name: String, params: Map<String, Any>) {
        // TODO: Implement with Firebase iOS SDK
    }

    override fun logNonFatal(throwable: Throwable) {
        // TODO: Implement with Firebase iOS SDK
    }

    override fun setUserId(userId: String?) {
        // TODO: Implement with Firebase iOS SDK
    }
}

actual fun createAnalyticsHelper(): AnalyticsHelper = IosAnalyticsHelper()
