package com.Pamneuroncraft.jobapplicationtracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.Pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleInterviewReminder(job: JobApplication) {
        val interviewDate = job.interviewDate ?: return
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("JOB_NAME", job.jobName)
            putExtra("COMPANY_NAME", job.companyName)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            job.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule reminder 30 minutes before the interview
        val reminderTime = interviewDate.time - (30 * 60 * 1000)
        
        if (reminderTime > System.currentTimeMillis()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // Handle the case where SCHEDULE_EXACT_ALARM is not granted
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        }
    }
}
