package com.zoewave.probase.kocolor.features.analyzer.calibration

import com.zoewave.probase.kocolor.model.calibration.ColorSeason
import com.zoewave.probase.kocolor.model.calibration.FacialContrastVector
import javax.inject.Inject

class ColorSeasonClassifier @Inject constructor() {

    fun classify(vector: FacialContrastVector, undertone: Float): ColorSeason {
        val isWarm = undertone > 0
        val isHighContrast = vector.contrastDelta > 0.6f
        val isLowContrast = vector.contrastDelta < 0.3f

        return when {
            isWarm -> {
                when {
                    isHighContrast -> ColorSeason.DEEP_AUTUMN
                    isLowContrast -> ColorSeason.LIGHT_SPRING
                    undertone > 0.5f -> ColorSeason.TRUE_SPRING
                    else -> ColorSeason.TRUE_AUTUMN
                }
            }
            else -> { // Cool
                when {
                    isHighContrast -> ColorSeason.TRUE_WINTER
                    isLowContrast -> ColorSeason.LIGHT_SUMMER
                    undertone < -0.5f -> ColorSeason.TRUE_SUMMER
                    else -> ColorSeason.SOFT_SUMMER
                }
            }
        }
    }

    fun getOptimalPalette(season: ColorSeason): List<String> {
        return when (season) {
            ColorSeason.BRIGHT_SPRING -> listOf("#FF5F5F", "#FFD700", "#00FF00")
            ColorSeason.TRUE_SPRING -> listOf("#FF6347", "#FFE4B5", "#98FB98")
            ColorSeason.LIGHT_SPRING -> listOf("#FFA07A", "#FFFACD", "#AFEEEE")
            ColorSeason.LIGHT_SUMMER -> listOf("#FFB6C1", "#F0F8FF", "#B0E0E6")
            ColorSeason.TRUE_SUMMER -> listOf("#DB7093", "#E6E6FA", "#ADD8E6")
            ColorSeason.SOFT_SUMMER -> listOf("#C71585", "#D8BFD8", "#778899")
            ColorSeason.SOFT_AUTUMN -> listOf("#B22222", "#F5F5DC", "#556B2F")
            ColorSeason.TRUE_AUTUMN -> listOf("#A52A2A", "#DEB887", "#006400")
            ColorSeason.DEEP_AUTUMN -> listOf("#800000", "#CD853F", "#2F4F4F")
            ColorSeason.DEEP_WINTER -> listOf("#4B0082", "#F0FFFF", "#000080")
            ColorSeason.TRUE_WINTER -> listOf("#FF00FF", "#FFFFFF", "#0000FF")
            ColorSeason.BRIGHT_WINTER -> listOf("#FF1493", "#E0FFFF", "#00BFFF")
        }
    }
}
