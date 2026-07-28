package com.pamneuroncraft.jobapplicationtracker.util

import com.pamneuroncraft.jobapplicationtracker.domain.repository.SyncManager

class IosSyncManager : SyncManager {
    override fun triggerSync() {
        // iOS background sync not implemented yet
    }
}
