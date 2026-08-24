package com.pamneuroncraft.jobapplicationtracker.ui.util

import com.pamneuroncraft.jobapplicationtracker.domain.model.AppCurrency

expect object CurrencyFormatter {
    fun format(amount: Double, currency: AppCurrency): String
}
