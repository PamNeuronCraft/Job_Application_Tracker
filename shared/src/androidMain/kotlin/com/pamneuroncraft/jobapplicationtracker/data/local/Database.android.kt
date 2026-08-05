package com.pamneuroncraft.jobapplicationtracker.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<JobDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("job_tracker.db")
    return Room.databaseBuilder<JobDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
