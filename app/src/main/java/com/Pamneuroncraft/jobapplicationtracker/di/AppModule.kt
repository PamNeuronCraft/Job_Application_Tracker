package com.Pamneuroncraft.jobapplicationtracker.di

import android.app.Application
import androidx.room.Room
import com.Pamneuroncraft.jobapplicationtracker.data.local.JobDatabase
import com.Pamneuroncraft.jobapplicationtracker.data.repository.JobRepositoryImpl
import com.Pamneuroncraft.jobapplicationtracker.domain.repository.JobRepository
import com.Pamneuroncraft.jobapplicationtracker.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJobDatabase(app: Application): JobDatabase {
        return Room.databaseBuilder(
            app,
            JobDatabase::class.java,
            "job_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideJobRepository(db: JobDatabase): JobRepository {
        return JobRepositoryImpl(db.jobDao)
    }

    @Provides
    @Singleton
    fun provideJobUseCases(repository: JobRepository): JobUseCases {
        return JobUseCases(
            getJobs = GetJobsUseCase(repository),
            getJobById = GetJobByIdUseCase(repository),
            addJob = AddJobUseCase(repository),
            updateJob = UpdateJobUseCase(repository),
            deleteJob = DeleteJobUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideBackupRestoreUseCase(repository: JobRepository): BackupRestoreUseCase {
        return BackupRestoreUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGoogleDriveUseCase(repository: JobRepository): GoogleDriveUseCase {
        return GoogleDriveUseCase(repository)
    }
}
