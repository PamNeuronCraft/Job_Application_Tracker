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
import com.pamneuroncraft.jobapplicationtracker.domain.repository.SyncManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.BillingManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.IosBillingManager
import com.pamneuroncraft.jobapplicationtracker.util.IosSyncManager

actual val platformModule = module {
    single<BillingManager> { IosBillingManager(get(), get()) }
    single<SyncManager> { IosSyncManager() }
    single<AppConfig> { CommonAppConfig(get(), false) }
    single<JobDatabase> {
        getDatabaseBuilder()
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
