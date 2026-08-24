package com.pamneuroncraft.jobapplicationtracker.ui.util

import com.pamneuroncraft.jobapplicationtracker.domain.model.AppCurrency
import java.text.NumberFormat
import java.util.Currency

actual object CurrencyFormatter {
    actual fun format(amount: Double, currency: AppCurrency): String {
        val format = NumberFormat.getCurrencyInstance()
        try {
            format.currency = Currency.getInstance(currency.code)
        } catch (e: Exception) {
            // Fallback to symbol if code is not recognized
        }
        return format.format(amount)
    }
}
