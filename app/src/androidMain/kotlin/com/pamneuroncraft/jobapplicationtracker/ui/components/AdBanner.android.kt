package com.pamneuroncraft.jobapplicationtracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.pamneuroncraft.jobapplicationtracker.AppBuildKonfig

@Composable
actual fun AdBanner(modifier: Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AppBuildKonfig.ADMOB_BANNER_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
