package com.pamneuroncraft.jobapplicationtracker.domain.repository

import com.pamneuroncraft.jobapplicationtracker.domain.model.JobApplication

interface NotificationService {
    fun scheduleInterviewReminder(job: JobApplication)
}
