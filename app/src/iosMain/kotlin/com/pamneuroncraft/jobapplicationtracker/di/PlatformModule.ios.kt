package com.pamneuroncraft.jobapplicationtracker.di

import com.pamneuroncraft.jobapplicationtracker.data.local.JobDatabase
import com.pamneuroncraft.jobapplicationtracker.data.local.getDatabaseBuilder
import org.koin.dsl.module
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

import com.pamneuroncraft.jobapplicationtracker.domain.repository.ExtractedJob
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobExtractor

import com.pamneuroncraft.jobapplicationtracker.domain.repository.NotificationService

import com.pamneuroncraft.jobapplicationtracker.AppConfig
import com.pamneuroncraft.jobapplicationtracker.CommonAppConfig

actual val platformModule = module {
    single<AppConfig> { CommonAppConfig() }
    single<JobDatabase> {
        getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single<JobExtractor> {
        object : JobExtractor {
            override suspend fun extractFromUrl(url: String): ExtractedJob {
                return ExtractedJob(description = "URL extraction not supported on iOS yet")
            }
        }
    }

    single<NotificationService> {
        object : NotificationService {
            override fun scheduleInterviewReminder(job: com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication) {}
        }
    }
}
