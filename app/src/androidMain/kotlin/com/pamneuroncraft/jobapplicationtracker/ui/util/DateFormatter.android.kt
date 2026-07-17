package com.pamneuroncraft.jobapplicationtracker.ui.util

import kotlinx.datetime.Instant
import java.text.SimpleDateFormat
import java.util.*

actual object DateFormatter {
    actual fun format(instant: Instant, pattern: String): String {
        val date = Date(instant.toEpochMilliseconds())
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(date)
    }
}
