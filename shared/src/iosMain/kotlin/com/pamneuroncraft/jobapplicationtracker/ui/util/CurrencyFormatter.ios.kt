package com.pamneuroncraft.jobapplicationtracker.ui.util

import com.pamneuroncraft.jobapplicationtracker.domain.model.AppCurrency
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

actual object CurrencyFormatter {
    actual fun format(amount: Double, currency: AppCurrency): String {
        val formatter = NSNumberFormatter().apply {
            numberStyle = NSNumberFormatterCurrencyStyle
            currencyCode = currency.code
        }
        return formatter.stringFromNumber(NSNumber(amount)) ?: "${currency.symbol}$amount"
    }
}
