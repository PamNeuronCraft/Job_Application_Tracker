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

actual val platformModule = module {
    single<AppConfig> { CommonAppConfig() }
    single<JobDatabase> {
        getDatabaseBuilder(get())
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
