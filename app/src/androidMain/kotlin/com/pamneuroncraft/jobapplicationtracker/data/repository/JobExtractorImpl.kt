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
    override suspend fun extractFromUrl(url: String): ExtractedJob = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(url).get()
        val pageText = doc.body().text()

        val prompt = """
            Extract job application details from the following text and return ONLY a JSON object with these keys: 
            "jobName", "companyName", "description", "compensation".
            If a value is not found, use null.
            The "description" should be a concise summary of the role.
            
            Text:
            ${pageText.take(5000)}
        """.trimIndent()

        val response = generativeModel.generateContent(content { text(prompt) })
        val jsonString = response.text?.substringAfter("```json")?.substringBefore("```")?.trim() 
            ?: response.text?.trim() 
            ?: "{}"
        
        Json { ignoreUnknownKeys = true }.decodeFromString<ExtractedJob>(jsonString)
    }
}
