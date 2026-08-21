package com.pamneuroncraft.jobapplicationtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.pamneuroncraft.jobapplicationtracker.AppBuildKonfig

@Composable
actual fun AdBanner(modifier: Modifier) {
    if (AppBuildKonfig.IS_DEBUG) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.LightGray.copy(alpha = 0.1f)),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AppBuildKonfig.ADMOB_BANNER_UNIT_ID_DEBUG
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            android.util.Log.e("AdMob", "Ad failed to load: ${error.message} (Code: ${error.code})")
                        }
                        override fun onAdLoaded() {
                            android.util.Log.d("AdMob", "Ad loaded successfully")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    } else {
        // Placeholder for release builds to avoid real ads during testing
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.LightGray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ad Banner Placeholder",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
