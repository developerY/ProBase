package com.zoewave.probase.core.ui.util

import androidx.compose.ui.graphics.Color

/**
 * Parses a hex color string to a Compose Color.
 * Safely handles missing '#' prefix.
 * Returns [Color.Gray] if parsing fails.
 */
fun parseColor(hex: String): Color {
    return try {
        val sanitizedHex = if (hex.startsWith("#")) hex else "#$hex"
        Color(android.graphics.Color.parseColor(sanitizedHex))
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * Determines if a color is "dark" based on perceived luminance.
 * Useful for choosing contrasting text color (white on dark, black on light).
 */
fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}

/**
 * Formats a Compose Color to a hex string (e.g., "#RRGGBB").
 */
fun Color.toHex(): String {
    return String.format("#%06X", (0xFFFFFF and android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )))
}
