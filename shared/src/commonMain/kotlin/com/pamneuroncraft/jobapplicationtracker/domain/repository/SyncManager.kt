package com.pamneuroncraft.jobapplicationtracker.domain.repository

interface SyncManager {
    fun triggerSync()
    fun scheduleEmailSync()
}
