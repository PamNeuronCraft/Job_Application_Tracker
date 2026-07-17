package com.pamneuroncraft.jobapplicationtracker

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.pamneuroncraft.jobapplicationtracker.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class JobTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this)
        
        initKoin {
            androidLogger()
            androidContext(this@JobTrackerApp)
        }
    }
}
