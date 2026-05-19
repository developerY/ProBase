package com.probase.kocolor.features.inventory.util

import androidx.compose.ui.graphics.Color

fun String?.toComposeColor(): Color {
    if (this.isNullOrBlank()) return Color.Transparent
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: IllegalArgumentException) {
        Color.Transparent
    }
}
