package com.pamneuroncraft.jobapplicationtracker.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
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
    val adUnitId = if (AppBuildKonfig.IS_DEBUG) {
        AppBuildKonfig.ADMOB_BANNER_UNIT_ID_DEBUG
    } else {
        AppBuildKonfig.ADMOB_BANNER_UNIT_ID_RELEASE
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color.LightGray.copy(alpha = 0.1f)),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e("AdMob", "Ad failed to load: ${error.message} (Code: ${error.code})")
                    }
                    override fun onAdLoaded() {
                        Log.d("AdMob", "Ad loaded successfully")
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
