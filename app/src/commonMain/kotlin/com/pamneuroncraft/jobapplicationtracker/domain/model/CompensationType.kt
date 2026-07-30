package com.pamneuroncraft.jobapplicationtracker.domain.model

import kotlinx.serialization.Serializable
import jobapplicationtracker.app.generated.resources.*

@Serializable
enum class CompensationType {
    HOURLY, ANNUAL;

    val labelRes get() = when (this) {
        HOURLY -> Res.string.compensation_hourly
        ANNUAL -> Res.string.compensation_annual
    }
}
