package com.pamneuroncraft.jobapplicationtracker.domain.model

import kotlinx.serialization.Serializable
import com.pamneuroncraft.jobapplicationtracker.shared.*

@Serializable
enum class CompensationType {
    HOURLY, ANNUAL;

    val labelRes get() = when (this) {
        HOURLY -> Res.string.compensation_hourly
        ANNUAL -> Res.string.compensation_annual
    }
}
