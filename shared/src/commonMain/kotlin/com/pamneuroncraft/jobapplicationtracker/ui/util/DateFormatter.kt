package com.pamneuroncraft.jobapplicationtracker.ui.util

import kotlinx.datetime.Instant

expect object DateFormatter {
    fun format(instant: Instant, pattern: String): String
}
