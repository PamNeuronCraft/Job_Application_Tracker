package com.pamneuroncraft.jobapplicationtracker.domain.repository

import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailMessage
import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailProvider

interface EmailSyncService {
    /**
     * Checks if the app has permission to sync from the specified provider.
     */
    fun hasPermission(provider: EmailProvider): Boolean

    /**
     * Fetches new emails since the last sync timestamp.
     */
    suspend fun fetchNewEmails(provider: EmailProvider, sinceTimestamp: Long): Result<List<EmailMessage>>
}
