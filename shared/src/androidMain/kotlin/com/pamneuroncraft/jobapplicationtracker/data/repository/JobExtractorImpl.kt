package com.pamneuroncraft.jobapplicationtracker.data.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.perf.FirebasePerformance
import com.pamneuroncraft.jobapplicationtracker.domain.repository.ExtractedJob
import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import kotlinx.serialization.json.Json

class JobExtractorImpl(
    private val generativeModel: GenerativeModel
) : JobExtractor {
    // Reuse a single JSON instance instead of creating one per call.
    // Creating JSON instances repeatedly can be slow and triggers the IDE warning.
    private val json = Json { ignoreUnknownKeys = true }
    override suspend fun extractFromUrl(url: String): ExtractedJob = withContext(Dispatchers.IO) {
        val trace = FirebasePerformance.getInstance().newTrace("ai_job_extraction")
        trace.start()
        try {
            val cleanUrl = """https?://[^\s]+""".toRegex().find(url)?.value ?: url
            Log.d("JobExtractor", "Extracting from clean URL: $cleanUrl")

            val doc = Jsoup.connect(cleanUrl)
                .userAgent("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36")
                .followRedirects(true)
                .timeout(15000)
                .get()
            val pageText = doc.body().text()
            
            android.util.Log.d("JobExtractor", "Extracted Page Text length: ${pageText.length}")
            android.util.Log.d("JobExtractor", "Extracted Page Text (first 500 chars): ${pageText.take(500)}")

            val prompt = """
                Extract job application details from the following text and return ONLY a JSON object with these keys: 
                "jobName", "companyName", "description", "compensation".
                
                For "compensation", extract the raw numeric amount and whether it is hourly or annual. 
                Ignore the currency symbol, but keep the numeric value.
                If a range is provided, return the average of that range as a single number string.
                Do not use abbreviations like "k" (e.g., use "100000" instead of "100k").
                Look carefully for phrases like "Salary Range", "Compensation", "Pay", or currency amounts followed by "/yr" or "/hr".
                Format it as a string like "50/hr" or "100000/yr". 
                
                If a value is not found, use null.
                The "description" should be a concise summary of the role.
                
                Text:
                ${pageText.take(10000)}
            """.trimIndent()

            val response = try {
                generativeModel.generateContent(content { text(prompt) })
            } catch (e: Exception) {
                android.util.Log.e("JobExtractor", "Gemini API Error: ${e.message}")
                throw e
            }
            val rawText = response.text ?: "{}"
            android.util.Log.d("JobExtractor", "Raw AI Response: $rawText")
            
            val jsonString = if (rawText.contains("```json")) {
                rawText.substringAfter("```json").substringBefore("```").trim()
            } else if (rawText.contains("{")) {
                // Find the first { and last }
                val firstBrace = rawText.indexOf('{')
                val lastBrace = rawText.lastIndexOf('}')
                if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                    rawText.substring(firstBrace, lastBrace + 1)
                } else {
                    rawText.trim()
                }
            } else {
                rawText.trim()
            }
            
            try {
                val extracted = json.decodeFromString<ExtractedJob>(jsonString)
                android.util.Log.d("JobExtractor", "Successfully extracted: $extracted")
                extracted
            } catch (e: Exception) {
                android.util.Log.e("JobExtractor", "Failed to parse AI response: $jsonString")
                android.util.Log.e("JobExtractor", "Exception: ${e.message}")
                throw Exception("Failed to parse job details from AI response")
            }
        } finally {
            trace.stop()
        }
    }

    override suspend fun extractStatusUpdate(emailBody: String, subject: String): com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatusUpdate? {
        val prompt = """
            Analyze the following email subject and body related to a job application.
            Determine if it indicates a status change.
            
            Return ONLY a JSON object with these keys:
            "companyName": The name of the company.
            "jobTitle": The title of the position (if found).
            "newStatus": One of: "APPLIED", "INTERVIEW", "OFFER", "REJECTED".
            "confidence": A value from 0.0 to 1.0.
            
            If it's not a clear job status update, return null.
            
            Subject: $subject
            Body: $emailBody
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(content { text(prompt) })
            val jsonString = response.text?.substringAfter("```json")?.substringBefore("```")?.trim() 
                ?: response.text?.trim() 
                ?: return null
            
            if (jsonString.lowercase().contains("null")) return null
            
            json.decodeFromString<com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatusUpdate>(jsonString)
        } catch (e: Exception) {
            android.util.Log.e("JobExtractor", "Failed to parse AI response: $prompt")
            android.util.Log.e("JobExtractor", "Exception: ${e.message}")
            null
        }
    }
}
