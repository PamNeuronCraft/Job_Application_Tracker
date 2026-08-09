package com.pamneuroncraft.jobapplicationtracker.data.repository

import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailMessage
import com.pamneuroncraft.jobapplicationtracker.domain.model.EmailProvider
import com.pamneuroncraft.jobapplicationtracker.domain.repository.EmailSyncService

class IosEmailSyncService : EmailSyncService {
    override fun hasPermission(provider: EmailProvider): Boolean {
        return false
    }

    override suspend fun fetchNewEmails(provider: EmailProvider, sinceTimestamp: Long): Result<List<EmailMessage>> {
        return Result.success(emptyList())
    }
}
