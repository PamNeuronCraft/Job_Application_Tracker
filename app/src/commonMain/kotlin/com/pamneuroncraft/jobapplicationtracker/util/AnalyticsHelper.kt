package com.pamneuroncraft.jobapplicationtracker.util

interface AnalyticsHelper {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun logNonFatal(throwable: Throwable)
    fun setUserId(userId: String?)
}

expect fun createAnalyticsHelper(): AnalyticsHelper
