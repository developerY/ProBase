package com.zoewave.probase.core.util

import java.util.Locale

object CurrencyUtils {
    fun formatCents(cents: Long): String {
        val dollars = cents / 100.0
        return String.format(Locale.getDefault(), "%.2f", dollars)
    }

    fun toCents(dollars: Double): Long {
        return (dollars * 100).toLong()
    }
}
