package com.pamneuroncraft.jobapplicationtracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication
import com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration
import com.pamneuroncraft.jobapplicationtracker.domain.repository.NotificationService

class AndroidNotificationService(
    private val context: Context
) : NotificationService {

    override fun scheduleInterviewReminder(job: JobApplication) {
        val interviewDate = job.interviewDate ?: return
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("JOB_NAME", job.jobName)
            putExtra("COMPANY_NAME", job.companyName)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            job.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val offsetMillis = when (job.reminderDuration) {
            ReminderDuration.ONE_DAY -> 24 * 60 * 60 * 1000L
            ReminderDuration.TWO_HOURS -> 2 * 60 * 60 * 1000L
            ReminderDuration.THIRTY_MINUTES -> 30 * 60 * 1000L
            null -> 30 * 60 * 1000L // Default to 30 mins if not specified
        }

        val reminderTime = interviewDate.toEpochMilliseconds() - offsetMillis
        
        if (reminderTime > System.currentTimeMillis()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        }
    }

    override fun showNotification(title: String, body: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "job_updates"
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Job Application Updates",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Replace with app icon later
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
