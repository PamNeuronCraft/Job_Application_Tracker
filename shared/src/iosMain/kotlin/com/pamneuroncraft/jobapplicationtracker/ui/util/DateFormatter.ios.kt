package com.pamneuroncraft.jobapplicationtracker.ui.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.time.Instant

actual object DateFormatter {
    actual fun format(instant: Instant, pattern: String): String {
        val date = NSDate.dateWithTimeIntervalSince1970(instant.toEpochMilliseconds() / 1000.0)
        val dateFormatter = NSDateFormatter()
        dateFormatter.dateFormat = pattern
        return dateFormatter.stringFromDate(date)
    }
}
