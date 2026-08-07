package com.pamneuroncraft.jobapplicationtracker.di

import com.google.ai.client.generativeai.GenerativeModel
import com.pamneuroncraft.jobapplicationtracker.data.local.JobDatabase
import com.pamneuroncraft.jobapplicationtracker.data.local.getDatabaseBuilder
import org.koin.dsl.module
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.pamneuroncraft.jobapplicationtracker.AppBuildKonfig
import kotlinx.coroutines.Dispatchers

import com.pamneuroncraft.jobapplicationtracker.data.repository.JobExtractorImpl
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobExtractor

import com.pamneuroncraft.jobapplicationtracker.notification.AndroidNotificationService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.NotificationService

import com.pamneuroncraft.jobapplicationtracker.AppConfig
import com.pamneuroncraft.jobapplicationtracker.CommonAppConfig
import com.pamneuroncraft.jobapplicationtracker.util.AndroidSyncManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.SyncManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.BillingManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.AndroidBillingManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.ExportManager
import com.pamneuroncraft.jobapplicationtracker.util.AndroidExportManager

actual val platformModule = module {
    single<BillingManager> { AndroidBillingManager(get(), get()) }
    single<SyncManager> { AndroidSyncManager(get()) }
    single<ExportManager> { AndroidExportManager(get()) }
    single<AppConfig> { CommonAppConfig(get(), AppBuildKonfig.IS_DEBUG) }
    single<JobDatabase> {
        getDatabaseBuilder(get())
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    
    single {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = if (AppBuildKonfig.IS_DEBUG) AppBuildKonfig.GEMINI_API_KEY_DEBUG else AppBuildKonfig.GEMINI_API_KEY_RELEASE
        )
    }

    single<JobExtractor> { JobExtractorImpl(get()) }
    single<NotificationService> { AndroidNotificationService(get()) }
}
