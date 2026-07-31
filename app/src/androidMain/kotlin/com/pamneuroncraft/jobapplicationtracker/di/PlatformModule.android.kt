package com.pamneuroncraft.jobapplicationtracker.di

import com.google.ai.client.generativeai.GenerativeModel
import com.pamneuroncraft.jobapplicationtracker.data.local.JobDatabase
import com.pamneuroncraft.jobapplicationtracker.data.local.getDatabaseBuilder
import org.koin.dsl.module
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

import com.pamneuroncraft.jobapplicationtracker.data.repository.JobExtractorImpl
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobExtractor

import com.pamneuroncraft.jobapplicationtracker.notification.AndroidNotificationService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.NotificationService

import com.pamneuroncraft.jobapplicationtracker.AppConfig
import com.pamneuroncraft.jobapplicationtracker.CommonAppConfig
import com.pamneuroncraft.jobapplicationtracker.BuildConfig
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
    single<AppConfig> { CommonAppConfig(get(), BuildConfig.DEBUG) }
    single<JobDatabase> {
        getDatabaseBuilder(get())
            .addMigrations(
                JobDatabase.MIGRATION_3_4, 
                JobDatabase.MIGRATION_4_5, 
                JobDatabase.MIGRATION_5_6,
                JobDatabase.MIGRATION_7_8
            )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    
    single {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "YOUR_GEMINI_API_KEY"
        )
    }

    single<JobExtractor> { JobExtractorImpl(get()) }
    single<NotificationService> { AndroidNotificationService(get()) }
}
