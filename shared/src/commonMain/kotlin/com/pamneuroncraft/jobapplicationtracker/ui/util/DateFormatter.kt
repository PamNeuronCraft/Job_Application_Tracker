package com.pamneuroncraft.jobapplicationtracker.ui.util

import kotlin.time.Instant


expect object DateFormatter {
    fun format(instant: Instant, pattern: String): String
}
