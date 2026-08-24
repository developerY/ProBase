package com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.list.messaging

/**
 * Translates biometric math into professional color theory terms.
 */
internal fun getContrastProfile(delta: Float): String = when {
    delta > 0.5f -> "High (Striking / Clear)"
    delta > 0.3f -> "Medium (Balanced)"
    else -> "Low (Blended / Muted)"
}

internal fun getTemperatureProfile(score: Float): String = when {
    score > 0.05f -> "Warm (Golden / Peach base)"
    score < -0.05f -> "Cool (Pink / Blue base)"
    else -> "Neutral (Balanced / Olive base)"
}

internal fun getDepthProfile(hairLuminance: Float, eyeLuminance: Float): String {
    val avgDarkness = (hairLuminance + eyeLuminance) / 2f
    return when {
        avgDarkness < 0.2f -> "Deep (Anchors dark colors well)"
        avgDarkness < 0.5f -> "Moderate (Versatile depth)"
        else -> "Light (Favors airy, pastel palettes)"
    }
}
