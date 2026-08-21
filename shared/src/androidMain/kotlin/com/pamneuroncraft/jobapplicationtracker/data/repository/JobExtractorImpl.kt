package com.pamneuroncraft.jobapplicationtracker.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
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
        val doc = Jsoup.connect(url).get()
        val pageText = doc.body().text()

        val prompt = """
            Extract job application details from the following text and return ONLY a JSON object with these keys: 
            "jobName", "companyName", "description", "compensation".
            
            For "compensation", try to extract the amount and whether it is hourly or annual. 
            Format it as a string like "50/hr" or "100000/yr". 
            
            If a value is not found, use null.
            The "description" should be a concise summary of the role.
            
            Text:
            ${pageText.take(5000)}
        """.trimIndent()

        val response = try {
            generativeModel.generateContent(content { text(prompt) })
        } catch (e: Exception) {
            android.util.Log.e("JobExtractor", "Gemini API Error: ${e.message}")
            throw e
        }
        val jsonString = response.text?.substringAfter("```json")?.substringBefore("```")?.trim() 
            ?: response.text?.trim() 
            ?: "{}"
        
        try {
            json.decodeFromString<ExtractedJob>(jsonString)
        } catch (e: Exception) {
            android.util.Log.e("JobExtractor", "Failed to parse AI response: $jsonString")
            throw Exception("Failed to parse job details from AI response")
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
            null
        }
    }
}
