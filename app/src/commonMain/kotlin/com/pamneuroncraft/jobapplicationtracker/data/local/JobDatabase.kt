package com.pamneuroncraft.jobapplicationtracker.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.pamneuroncraft.jobapplicationtracker.data.local.entity.JobApplicationEntity

@Database(entities = [JobApplicationEntity::class], version = 1, exportSchema = false)
@ConstructedBy(JobDatabaseConstructor::class)
abstract class JobDatabase : RoomDatabase() {
    abstract val jobDao: JobDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object JobDatabaseConstructor : RoomDatabaseConstructor<JobDatabase> {
    override fun initialize(): JobDatabase
}
