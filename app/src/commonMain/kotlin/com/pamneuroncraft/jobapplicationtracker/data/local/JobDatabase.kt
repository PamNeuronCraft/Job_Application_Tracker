package com.pamneuroncraft.jobapplicationtracker.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.pamneuroncraft.jobapplicationtracker.data.local.entity.JobApplicationEntity
import com.pamneuroncraft.jobapplicationtracker.data.local.entity.JobSearchEntity

@Database(entities = [JobApplicationEntity::class, JobSearchEntity::class], version = 1, exportSchema = true)
@ConstructedBy(JobDatabaseConstructor::class)
abstract class JobDatabase : RoomDatabase() {
    abstract val jobDao: JobDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object JobDatabaseConstructor : RoomDatabaseConstructor<JobDatabase> {
    override fun initialize(): JobDatabase
}
