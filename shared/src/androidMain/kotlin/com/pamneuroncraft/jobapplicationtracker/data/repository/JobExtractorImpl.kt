package com.pamneuroncraft.jobapplicationtracker.data.repository

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.perf.FirebasePerformance
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatusUpdate
import com.pamneuroncraft.jobapplicationtracker.domain.repository.ExtractedJob
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class JobExtractorImpl(
    private val functions: FirebaseFunctions
) : JobExtractor {

    override suspend fun extractFromUrl(url: String): ExtractedJob = withContext(Dispatchers.IO) {
        val trace = FirebasePerformance.getInstance().newTrace("ai_job_extraction")
        trace.start()
        try {
            Log.d("JobExtractor", "Extracting from URL via Cloud Function: $url")

            val data = mapOf("url" to url)
            val result = functions
                .getHttpsCallable("extractJobFromUrl")
                .call(data)
                .await()

            val resultMap = result.data as? Map<*, *> ?: throw Exception("Invalid response from Cloud Function")
            Log.d("JobExtractor", "Raw Cloud Function Result: $resultMap")

            val extracted = ExtractedJob(
                jobName = resultMap["jobName"] as? String,
                companyName = resultMap["companyName"] as? String,
                description = resultMap["description"] as? String,
                compensation = resultMap["compensation"] as? String
            )
            Log.d("JobExtractor", "Successfully extracted: $extracted")
            extracted
        } catch (e: Exception) {
            Log.e("JobExtractor", "Cloud Function Error in extractFromUrl: ${e.message}", e)
            throw Exception("Failed to extract job details: ${e.message}")
        } finally {
            trace.stop()
        }
    }

    override suspend fun extractStatusUpdate(emailBody: String, subject: String): JobStatusUpdate? = withContext(Dispatchers.IO) {
        try {
            Log.d("JobExtractor", "Extracting status update via Cloud Function for subject: $subject")

            val data = mapOf(
                "subject" to subject,
                "emailBody" to emailBody
            )

            val result = functions
                .getHttpsCallable("extractStatusUpdateFromEmail")
                .call(data)
                .await()

            val resultMap = result.data as? Map<*, *> ?: return@withContext null
            Log.d("JobExtractor", "Raw Status Update Result: $resultMap")

            val companyName = resultMap["companyName"] as? String ?: return@withContext null
            val jobTitle = resultMap["jobTitle"] as? String
            val newStatusStr = resultMap["newStatus"] as? String ?: return@withContext null
            val confidence = (resultMap["confidence"] as? Number)?.toFloat() ?: 0f
            val sourceEmailId = resultMap["sourceEmailId"] as? String ?: ""

            val newStatus = try {
                JobStatus.valueOf(newStatusStr)
            } catch (_: Exception) {
                return@withContext null
            }

            JobStatusUpdate(
                companyName = companyName,
                jobTitle = jobTitle,
                newStatus = newStatus,
                confidence = confidence,
                sourceEmailId = sourceEmailId
            )
        } catch (e: Exception) {
            Log.e("JobExtractor", "Cloud Function Error in extractStatusUpdate: ${e.message}", e)
            null
        }
    }
}
