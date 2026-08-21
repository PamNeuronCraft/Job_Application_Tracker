package com.pamneuroncraft.jobapplicationtracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.pamneuroncraft.jobapplicationtracker.domain.model.JobStatus

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

fun getJobStatusColor(status: JobStatus, isDark: Boolean): Color {
    return if (isDark) {
        when (status) {
            JobStatus.APPLIED -> Color(0xFF1976D2) // Darker Blue
            JobStatus.INTERVIEW -> Color(0xFFB8860B) // Dark Goldenrod
            JobStatus.OFFER -> Color(0xFF388E3C) // Darker Green
            JobStatus.NO_OFFER -> Color(0xFFD32F2F) // Darker Red
        }
    } else {
        when (status) {
            JobStatus.APPLIED -> Color(0xFFBBDEFB) // Light Blue
            JobStatus.INTERVIEW -> Color(0xFFFFF9C4) // Light Yellow
            JobStatus.OFFER -> Color(0xFFC8E6C9) // Light Green
            JobStatus.NO_OFFER -> Color(0xFFFFCDD2) // Light Red
        }
    }
}