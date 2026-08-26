package com.pamneuroncraft.jobapplicationtracker.di

import com.pamneuroncraft.jobapplicationtracker.data.local.JobDatabase
import com.pamneuroncraft.jobapplicationtracker.data.local.getDatabaseBuilder
import org.koin.dsl.module
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

import com.pamneuroncraft.jobapplicationtracker.domain.repository.ExtractedJob
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobExtractor
import com.pamneuroncraft.jobapplicationtracker.domain.repository.EmailSyncService

import com.pamneuroncraft.jobapplicationtracker.domain.repository.NotificationService

import com.pamneuroncraft.jobapplicationtracker.AppConfig
import com.pamneuroncraft.jobapplicationtracker.CommonAppConfig
import com.pamneuroncraft.jobapplicationtracker.domain.repository.SyncManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.BillingManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.IosBillingManager
import com.pamneuroncraft.jobapplicationtracker.domain.repository.ExportManager
import com.pamneuroncraft.jobapplicationtracker.util.IosSyncManager

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSDate
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNNotificationRequest
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Clock
import kotlin.time.Instant

import platform.Foundation.NSBundle

actual val platformModule = module {
    single<BillingManager> { IosBillingManager(get(), get()) }
    single<SyncManager> { IosSyncManager() }
    single {
        io.ktor.client.HttpClient(io.ktor.client.engine.darwin.Darwin) {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(kotlinx.serialization.json.Json { ignoreUnknownKeys = true })
            }
        }
    }
    single<ExportManager> {
        object : ExportManager {
            @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
            override fun shareCsv(content: String, fileName: String) {
                val tempDir = NSTemporaryDirectory()
                val fileURL = NSURL.fileURLWithPath(tempDir).URLByAppendingPathComponent(fileName)
                
                if (fileURL != null) {
                    val nsString = NSString.create(string = content)
                    nsString.writeToURL(fileURL, true, NSUTF8StringEncoding, null)
                    
                    val activityController = UIActivityViewController(
                        activityItems = listOf(fileURL),
                        applicationActivities = null
                    )
                    
                    val windowScene = UIApplication.sharedApplication.connectedScenes
                        .mapNotNull { it as? UIWindowScene }
                        .firstOrNull { it.activationState == platform.UIKit.UISceneActivationStateForegroundActive }
                    
                    windowScene?.keyWindow?.rootViewController?.presentViewController(
                        activityController,
                        animated = true,
                        completion = null
                    )
                }
            }
        }
    }
    single<AppConfig> { 
        val version = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String
        val build = NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String
        val fullVersion = if (version != null && build != null) "$version ($build)" else version ?: "1.0.0"
        CommonAppConfig(get(), false, false, fullVersion) 
    }
    single<JobDatabase> {
        getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single<JobExtractor> {
        object : JobExtractor {
            override suspend fun extractFromUrl(url: String): ExtractedJob {
                return ExtractedJob(description = "URL extraction not supported on iOS yet")
            }

            override suspend fun extractStatusUpdate(emailBody: String, subject: String): com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatusUpdate? {
                return null
            }
        }
    }

    single<EmailSyncService> { com.pamneuroncraft.jobapplicationtracker.data.repository.IosEmailSyncService(get()) }

    single<NotificationService> {
        object : NotificationService {
            override fun scheduleInterviewReminder(job: com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication) {
                // ... (Existing reminder code)
                val interviewDate = job.interviewDate ?: return
                val center = UNUserNotificationCenter.currentNotificationCenter()
                
                center.requestAuthorizationWithOptions(
                    UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
                ) { granted, _ ->
                    if (granted) {
                        val content = UNMutableNotificationContent().apply {
                            setTitle("Interview Reminder")
                            setBody("Don't forget: Interview with ${job.companyName} for ${job.jobName}")
                            setSound(platform.UserNotifications.UNNotificationSound.defaultSound)
                        }

                        val offsetSeconds = when (job.reminderDuration) {
                            com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration.ONE_DAY -> 24 * 60 * 60.0
                            com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration.TWO_HOURS -> 2 * 60 * 60.0
                            com.pamneuroncraft.jobapplicationtracker.domain.model.ReminderDuration.THIRTY_MINUTES -> 30 * 60.0
                            null -> 30 * 60.0
                        }

                        val now = Clock.System.now()
                        val triggerTime = (interviewDate.toEpochMilliseconds() - now.toEpochMilliseconds()).toDouble() / 1000.0 - offsetSeconds
                        
                        if (triggerTime > 0) {
                            val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(triggerTime, false)
                            val request = UNNotificationRequest.requestWithIdentifier(
                                job.id,
                                content,
                                trigger
                            )
                            center.addNotificationRequest(request, null)
                        }
                    }
                }
            }

            override fun showNotification(title: String, body: String) {
                val center = UNUserNotificationCenter.currentNotificationCenter()
                center.requestAuthorizationWithOptions(
                    UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
                ) { granted, _ ->
                    if (granted) {
                        val content = UNMutableNotificationContent().apply {
                            setTitle(title)
                            setBody(body)
                            setSound(platform.UserNotifications.UNNotificationSound.defaultSound)
                        }
                        
                        val request = UNNotificationRequest.requestWithIdentifier(
                            Clock.System.now().toEpochMilliseconds().toString(),
                            content,
                            null // Trigger immediately
                        )
                        center.addNotificationRequest(request, null)
                    }
                }
            }
        }
    }
}
