package com.pamneuroncraft.jobapplicationtracker.data.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailMessage
import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailProvider
import com.pamneuroncraft.jobapplicationtracker.domain.repository.EmailSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidEmailSyncService(
    private val context: Context
) : EmailSyncService {
    
    private val scope = "https://www.googleapis.com/auth/gmail.readonly"

    override fun hasPermission(provider: EmailProvider): Boolean {
        if (provider != EmailProvider.GMAIL) return false
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return GoogleSignIn.hasPermissions(account, Scope(scope))
    }

    override suspend fun fetchNewEmails(provider: EmailProvider, sinceTimestamp: Long): Result<List<EmailMessage>> = withContext(Dispatchers.IO) {
        if (provider != EmailProvider.GMAIL) return@withContext Result.failure(Exception("Unsupported provider"))
        
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context) 
                ?: return@withContext Result.failure(Exception("Not signed in"))
            
            val credential = GoogleAccountCredential.usingOAuth2(context, listOf(scope))
            credential.selectedAccount = account.account
            
            val service = Gmail.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("Job Application Tracker")
                .build()

            // Search for job-related emails using a broader query based on common recruitment patterns
            val query = "after:${sinceTimestamp / 1000} (subject:(application OR interview OR offer OR rejected OR update OR interest OR assessment OR status) OR \"thank you for\")"
            val response = service.users().messages().list("me").setQ(query).execute()
            
            val messages = response.messages ?: emptyList()
            val result = messages.map { msg ->
                val fullMsg = service.users().messages().get("me", msg.id).execute()
                EmailMessage(
                    id = fullMsg.id,
                    sender = fullMsg.payload.headers.find { it.name == "From" }?.value ?: "",
                    subject = fullMsg.payload.headers.find { it.name == "Subject" }?.value ?: "",
                    body = fullMsg.snippet ?: "", 
                    timestamp = fullMsg.internalDate
                )
            }
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
