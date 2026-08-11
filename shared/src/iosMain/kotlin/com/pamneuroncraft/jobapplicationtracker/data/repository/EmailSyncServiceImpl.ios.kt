package com.pamneuroncraft.jobapplicationtracker.data.repository

import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailMessage
import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailProvider
import com.pamneuroncraft.jobapplicationtracker.domain.repository.EmailSyncService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*

class IosEmailSyncService(
    private val httpClient: HttpClient
) : EmailSyncService {

    override fun hasPermission(provider: EmailProvider): Boolean {
        // In a real iOS app, you'd check GIDSignIn.sharedInstance.hasPermissions(...)
        return false
    }

    override suspend fun fetchNewEmails(provider: EmailProvider, sinceTimestamp: Long): Result<List<EmailMessage>> = withContext(Dispatchers.Default) {
        if (provider != EmailProvider.GMAIL) return@withContext Result.failure(Exception("Unsupported provider"))
        
        try {
            // Placeholder: Get the OAuth Access Token from your Auth system
            val accessToken = "YOUR_ACCESS_TOKEN" 
            
            val query = "after:${sinceTimestamp / 1000} (subject:(application OR interview OR offer OR rejected OR update OR interest OR assessment OR status) OR \"thank you for\")"
            
            val response: JsonObject = httpClient.get("https://gmail.googleapis.com/gmail/v1/users/me/messages") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                parameter("q", query)
            }.body()

            val messages = response["messages"]?.jsonArray ?: emptyList<JsonElement>()
            val result = messages.map { msg ->
                val id = msg.jsonObject["id"]?.jsonPrimitive?.content ?: ""
                val fullMsg: JsonObject = httpClient.get("https://gmail.googleapis.com/gmail/v1/users/me/messages/$id") {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                }.body()

                val payload = fullMsg["payload"]?.jsonObject
                val headers = payload?.get("headers")?.jsonArray ?: emptyList<JsonElement>()
                
                EmailMessage(
                    id = id,
                    sender = headers.findHeader("From"),
                    subject = headers.findHeader("Subject"),
                    body = fullMsg["snippet"]?.jsonPrimitive?.content ?: "",
                    timestamp = fullMsg["internalDate"]?.jsonPrimitive?.content?.toLong() ?: 0L
                )
            }
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun List<JsonElement>.findHeader(name: String): String {
        return this.find { it.jsonObject["name"]?.jsonPrimitive?.content == name }
            ?.jsonObject?.get("value")?.jsonPrimitive?.content ?: ""
    }
}
