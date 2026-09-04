package com.zoewave.probase.kocolor.fashionista.scoring

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class ColorHarmonicsEvaluator @Inject constructor() {

    fun evaluate(blueprint: StyleBlueprint): Float {
        val palette = blueprint.recommendedPalette
        if (palette.isEmpty()) return 75.0f

        val hslList = palette.mapNotNull { parseToHsl(it) }
        if (hslList.size < 2) return 80.0f

        var totalDeltaAngle = 0f
        var pairs = 0

        for (i in hslList.indices) {
            for (j in i + 1 until hslList.size) {
                val h1 = hslList[i][0]
                val h2 = hslList[j][0]
                var diff = abs(h1 - h2)
                if (diff > 180f) diff = 360f - diff
                totalDeltaAngle += diff
                pairs++
            }
        }

        val avgAngle = if (pairs > 0) totalDeltaAngle / pairs else 0f

        return when {
            // Analogous harmony (0° - 45° delta)
            avgAngle in 0f..45f -> 95.0f
            // Complementary harmony (135° - 180° delta)
            avgAngle in 135f..180f -> 92.0f
            // Triadic harmony (90° - 135° delta)
            avgAngle in 90f..135f -> 88.0f
            // Color Clash (45° - 90° uncalibrated delta)
            else -> 72.0f
        }
    }

    private fun parseToHsl(hex: String): FloatArray? {
        return try {
            val colorInt = Color.parseColor(hex)
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(colorInt, hsl)
            hsl
        } catch (e: Exception) {
            null
        }
    }
}
