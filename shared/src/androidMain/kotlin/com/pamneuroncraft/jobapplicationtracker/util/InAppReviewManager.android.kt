package com.pamneuroncraft.jobapplicationtracker.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewManagerFactory

class AndroidInAppReviewManager(private val activity: Activity) : InAppReviewManager {
    private val manager = ReviewManagerFactory.create(activity)

    override fun requestReview() {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (request.isSuccessful) {
                val reviewInfo = task.result
                manager.launchReviewFlow(activity, reviewInfo)
            }
        }
    }
}

@Composable
actual fun rememberInAppReviewManager(): InAppReviewManager {
    val context = LocalContext.current
    return remember(context) {
        AndroidInAppReviewManager(context as Activity)
    }
}
