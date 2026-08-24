package com.pamneuroncraft.jobapplicationtracker.domain.model

enum class AppCurrency(val code: String, val symbol: String) {
    USD("USD", "$"),
    EUR("EUR", "€"),
    GBP("GBP", "£"),
    JPY("JPY", "¥"),
    CAD("CAD", "CA$"),
    AUD("AUD", "A$"),
    INR("INR", "₹"),
    NGN("NGN", "₦");

    companion object {
        fun fromCode(code: String): AppCurrency {
            return entries.find { it.code == code } ?: USD
        }
    }
}
