package com.zoewave.probase.seaweed.model.finance

object Iso4217CurrencyRegistry {
    const val REGISTRY_VERSION = "2026.10-ISO4217"

    // Hardcoded dataset based on ISO 4217 standard for reproducibility across devices.
    private val CURRENCY_SCALES: Map<String, Int> = mapOf(
        // 0-decimal currencies
        "BIF" to 0, "CLP" to 0, "DJF" to 0, "GNF" to 0, "ISK" to 0,
        "JPY" to 0, "KMF" to 0, "KRW" to 0, "PYG" to 0, "RWF" to 0,
        "UGX" to 0, "VND" to 0, "VUV" to 0, "XAF" to 0, "XOF" to 0, "XPF" to 0,
        // 3-decimal currencies
        "BHD" to 3, "IQD" to 3, "JOD" to 3, "KWD" to 3, "LYD" to 3, "OMR" to 3, "TND" to 3,
        // 4-decimal currencies
        "CLF" to 4, "UYW" to 4,
        // Common 2-decimal currencies (subset for completeness in a deterministic registry)
        "USD" to 2, "EUR" to 2, "GBP" to 2, "AUD" to 2, "CAD" to 2,
        "CHF" to 2, "CNY" to 2, "SEK" to 2, "NZD" to 2, "MXN" to 2,
        "SGD" to 2, "HKD" to 2, "NOK" to 2, "TRY" to 2,
        "RUB" to 2, "INR" to 2, "BRL" to 2, "ZAR" to 2, "PLN" to 2
    )

    /**
     * Fallback strategy: Return 2 for unknown currencies if we don't strictly reject them,
     * but the spec implies we must strictly check. We'll return null for unsupported.
     */
    fun getScaleOrNull(currencyCode: String): Int? =
        CURRENCY_SCALES[currencyCode.trim().uppercase()]
}
