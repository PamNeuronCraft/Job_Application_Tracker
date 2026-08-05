package com.pamneuroncraft.jobapplicationtracker.util

import androidx.compose.runtime.Composable

interface InAppReviewManager {
    fun requestReview()
}

@Composable
expect fun rememberInAppReviewManager(): InAppReviewManager
