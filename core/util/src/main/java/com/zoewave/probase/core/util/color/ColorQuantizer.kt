package com.zoewave.probase.core.util.color

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.zoewave.probase.core.model.ritual.ColorFamily
import com.zoewave.probase.core.model.ritual.Temperature

object ColorQuantizer {

    /**
     * Snaps a hex color string to the closest [ColorFamily] using CIELAB ΔE distance.
     */
    fun snapToFamily(hexColor: String?): ColorFamily {
        if (hexColor == null) return ColorFamily.UNKNOWN
        
        return try {
            val colorInt = Color.parseColor(hexColor)
            val inputLab = DoubleArray(3)
            ColorUtils.colorToLAB(colorInt, inputLab)

            var closestFamily = ColorFamily.UNKNOWN
            var minDistance = Double.MAX_VALUE

            ColorFamily.entries.filter { it != ColorFamily.UNKNOWN }.forEach { target ->
                val targetInt = Color.parseColor(target.hex)
                val targetLab = DoubleArray(3)
                ColorUtils.colorToLAB(targetInt, targetLab)

                // ΔE = sqrt((L2-L1)^2 + (a2-a1)^2 + (b2-b1)^2)
                val distance = Math.sqrt(
                    Math.pow(targetLab[0] - inputLab[0], 2.0) +
                    Math.pow(targetLab[1] - inputLab[1], 2.0) +
                    Math.pow(targetLab[2] - inputLab[2], 2.0)
                )

                if (distance < minDistance) {
                    minDistance = distance
                    closestFamily = target
                }
            }
            closestFamily
        } catch (e: Exception) {
            ColorFamily.UNKNOWN
        }
    }

    /**
     * Determines temperature (WARM, COOL, NEUTRAL) from hex color.
     */
    fun determineTemperature(hexColor: String?): Temperature {
        if (hexColor.isNullOrBlank()) return Temperature.NEUTRAL
        return try {
            val colorInt = Color.parseColor(hexColor)
            val r = Color.red(colorInt) / 255f
            val g = Color.green(colorInt) / 255f
            val b = Color.blue(colorInt) / 255f

            val rbDiff = r - b
            val gbDiff = g - b
            val warmMetric = (rbDiff * 0.7f + gbDiff * 0.3f) - 0.12f
            when {
                warmMetric > 0.05f -> Temperature.WARM
                warmMetric < -0.05f -> Temperature.COOL
                else -> Temperature.NEUTRAL
            }
        } catch (e: Exception) {
            Temperature.NEUTRAL
        }
    }
}
