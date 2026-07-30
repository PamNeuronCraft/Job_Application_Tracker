package com.pamneuroncraft.jobapplicationtracker.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.pamneuroncraft.jobapplicationtracker.data.local.entity.JobApplicationEntity
import com.pamneuroncraft.jobapplicationtracker.data.local.entity.JobSearchEntity
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(entities = [JobApplicationEntity::class, JobSearchEntity::class], version = 8, exportSchema = false)
@ConstructedBy(JobDatabaseConstructor::class)
abstract class JobDatabase : RoomDatabase() {
    abstract val jobDao: JobDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE job_applications ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE job_applications ADD COLUMN lastSyncedAt INTEGER")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_job_applications_userId ON job_applications (userId)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_job_applications_status ON job_applications (status)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_job_applications_updatedAt ON job_applications (updatedAt)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS job_search USING FTS4(jobName, companyName, description, content=`job_applications`)")
                connection.execSQL("INSERT INTO job_search(job_search) VALUES('rebuild')")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(connection: SQLiteConnection) {
                // Drop and recreate to fix quoting issue: content='job_applications' -> content=`job_applications`
                connection.execSQL("DROP TABLE IF EXISTS job_search")
                connection.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS job_search USING FTS4(jobName, companyName, description, content=`job_applications`)")
                connection.execSQL("INSERT INTO job_search(job_search) VALUES('rebuild')")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(connection: SQLiteConnection) {
                // 1. Create new table with updated schema
                connection.execSQL("""
                    CREATE TABLE IF NOT EXISTS `job_applications_new` (
                        `id` TEXT NOT NULL, 
                        `jobName` TEXT NOT NULL, 
                        `companyName` TEXT NOT NULL, 
                        `description` TEXT NOT NULL, 
                        `jobType` TEXT NOT NULL, 
                        `compensationAmount` REAL, 
                        `compensationType` TEXT NOT NULL, 
                        `status` TEXT NOT NULL, 
                        `dateAdded` INTEGER NOT NULL, 
                        `interviewDate` INTEGER, 
                        `reminderDuration` TEXT, 
                        `userId` TEXT, 
                        `updatedAt` INTEGER NOT NULL, 
                        `isDeleted` INTEGER NOT NULL DEFAULT 0, 
                        `lastSyncedAt` INTEGER, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // 2. Copy data from old table to new table
                connection.execSQL("""
                    INSERT INTO `job_applications_new` (
                        id, jobName, companyName, description, jobType, compensationAmount, compensationType, 
                        status, dateAdded, interviewDate, reminderDuration, userId, updatedAt, isDeleted, lastSyncedAt
                    )
                    SELECT 
                        id, jobName, companyName, description, jobType, NULL, 'ANNUAL', 
                        status, dateAdded, interviewDate, reminderDuration, userId, updatedAt, isDeleted, lastSyncedAt
                    FROM `job_applications`
                """.trimIndent())

                // 3. Drop old table
                connection.execSQL("DROP TABLE `job_applications`")

                // 4. Rename new table to original name
                connection.execSQL("ALTER TABLE `job_applications_new` RENAME TO `job_applications`")

                // 5. Recreate indices
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_job_applications_userId ON job_applications (userId)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_job_applications_status ON job_applications (status)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_job_applications_updatedAt ON job_applications (updatedAt)")

                // 6. Rebuild FTS table
                connection.execSQL("DROP TABLE IF EXISTS job_search")
                connection.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS job_search USING FTS4(jobName, companyName, description, content=`job_applications`)")
                connection.execSQL("INSERT INTO job_search(job_search) VALUES('rebuild')")
            }
        }
    }
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object JobDatabaseConstructor : RoomDatabaseConstructor<JobDatabase> {
    override fun initialize(): JobDatabase
}
