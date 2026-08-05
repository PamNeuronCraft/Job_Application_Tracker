package com.pamneuroncraft.jobapplicationtracker.domain.repository

interface ExportManager {
    fun shareCsv(content: String, fileName: String)
}
