package com.pamneuroncraft.jobapplicationtracker.ui.util

import kotlinx.datetime.Instant
import platform.Foundation.*

actual object DateFormatter {
    actual fun format(instant: Instant, pattern: String): String {
        val date = NSDate.dateWithTimeIntervalSince1970(instant.toEpochMilliseconds() / 1000.0)
        val dateFormatter = NSDateFormatter()
        dateFormatter.dateFormat = pattern
        return dateFormatter.stringFromDate(date)
    }
}
