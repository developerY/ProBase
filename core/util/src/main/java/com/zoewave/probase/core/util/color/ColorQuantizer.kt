package com.zoewave.probase.core.util.color

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.zoewave.probase.core.model.ritual.ColorFamily

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
}
