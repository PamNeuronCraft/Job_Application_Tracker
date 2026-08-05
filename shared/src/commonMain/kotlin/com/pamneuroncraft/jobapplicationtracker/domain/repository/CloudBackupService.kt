package com.pamneuroncraft.jobapplicationtracker.domain.repository

interface CloudBackupService {
    val isUserSignedIn: Boolean
    suspend fun backup(json: String)
    suspend fun restore(): String?
}
