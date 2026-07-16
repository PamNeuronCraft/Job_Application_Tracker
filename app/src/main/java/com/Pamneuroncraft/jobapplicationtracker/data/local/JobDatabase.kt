package com.Pamneuroncraft.jobapplicationtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.Pamneuroncraft.jobapplicationtracker.data.local.entity.JobApplicationEntity

@Database(entities = [JobApplicationEntity::class], version = 1, exportSchema = false)
abstract class JobDatabase : RoomDatabase() {
    abstract val jobDao: JobDao
}
