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
import com.pamneuroncraft.jobapplicationtracker.domain.usecase.*
import com.pamneuroncraft.jobapplicationtracker.ui.viewmodel.*
import com.pamneuroncraft.jobapplicationtracker.util.PermissionManager
import com.pamneuroncraft.jobapplicationtracker.util.createPermissionManager
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<JobRepository> { JobRepositoryImpl(get<JobDatabase>().jobDao) }
    single { Settings() }
    single { LocalSettings(get()) }
    single { createPermissionManager() }
    single<AuthService> { FirebaseAuthService() }
    single<CloudBackupService> { FirebaseCloudBackupService() }
}

val useCaseModule = module {
    single { GetJobsUseCase(get()) }
    single { GetJobByIdUseCase(get()) }
    single { AddJobUseCase(get()) }
    single { UpdateJobUseCase(get()) }
    single { DeleteJobUseCase(get()) }
    single { ExtractJobFromUrlUseCase(get<JobExtractor>()) }
    single { CloudBackupUseCase(get(), get()) }
    single {
        JobUseCases(
            getJobs = get(),
            getJobById = get(),
            addJob = get(),
            updateJob = get(),
            deleteJob = get(),
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
}

