package com.pamneuroncraft.jobapplicationtracker.di

import com.pamneuroncraft.jobapplicationtracker.data.local.JobDatabase
import com.pamneuroncraft.jobapplicationtracker.data.local.LocalSettings
import com.pamneuroncraft.jobapplicationtracker.data.repository.FirebaseAuthService
import com.pamneuroncraft.jobapplicationtracker.data.repository.FirebaseCloudBackupService
import com.pamneuroncraft.jobapplicationtracker.data.repository.JobRepositoryImpl
import com.pamneuroncraft.jobapplicationtracker.domain.repository.AuthService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.CloudBackupService
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobExtractor
import com.pamneuroncraft.jobapplicationtracker.domain.repository.SocialAuthManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.createSocialAuthManager
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.*
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.*
import com.pamneuroncraft.jobapplicationtracker.util.PermissionManager
import com.pamneuroncraft.jobapplicationtracker.util.createPermissionManager
import com.pamneuroncraft.jobapplicationtracker.util.createAnalyticsHelper
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<JobRepository> { JobRepositoryImpl(get<JobDatabase>().jobDao, get(), get()) }
    single { Settings() }
    single { LocalSettings(get()) }
    single { createPermissionManager() }
    single { createAnalyticsHelper() }
    single<AuthService> { FirebaseAuthService(get()) }
    single<CloudBackupService> { FirebaseCloudBackupService() }
    single<SocialAuthManager> { createSocialAuthManager() }
}

val useCaseModule = module {
    single { GetJobsUseCase(get()) }
    single { GetJobsPagedUseCase(get()) }
    single { GetJobByIdUseCase(get()) }
    single { AddJobUseCase(get()) }
    single { UpdateJobUseCase(get()) }
    single { DeleteJobUseCase(get()) }
    single { SearchJobsUseCase(get()) }
    single { SearchJobsPagedUseCase(get()) }
    single { ExtractJobFromUrlUseCase(get<JobExtractor>()) }
    single { GetJobAnalyticsUseCase(get()) }
    single { ExportJobsToCsvUseCase(get()) }
    single { CloudBackupUseCase(get(), get()) }
    single { SyncJobStatusesFromEmailUseCase(get(), get(), get(), get()) }
    single {
        JobUseCases(
            getJobs = get(),
            getJobsPaged = get(),
            getJobById = get(),
            addJob = get(),
            updateJob = get(),
            deleteJob = get(),
            searchJobs = get(),
            searchJobsPaged = get(),
            extractJobFromUrl = get()
        )
    }
}

val viewModelModule = module {
    viewModelOf(::JobListViewModel)
    viewModelOf(::JobAddEditViewModel)
    viewModelOf(::JobDetailViewModel)
    viewModelOf(::BackupViewModel)
    viewModelOf(::ImportViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::SummaryViewModel)
    viewModelOf(::SubscriptionViewModel)
}

