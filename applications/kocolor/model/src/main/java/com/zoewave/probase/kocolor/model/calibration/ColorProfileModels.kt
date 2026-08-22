package com.zoewave.probase.kocolor.model.calibration

import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone
import kotlinx.serialization.Serializable

@Serializable
enum class ColorSeason {
    BRIGHT_SPRING,
    TRUE_SPRING,
    LIGHT_SPRING,
    LIGHT_SUMMER,
    TRUE_SUMMER,
    SOFT_SUMMER,
    SOFT_AUTUMN,
    TRUE_AUTUMN,
    DEEP_AUTUMN,
    DEEP_WINTER,
    TRUE_WINTER,
    BRIGHT_WINTER;

    fun toSeasonalType(): SeasonalType = when (this) {
        BRIGHT_SPRING, TRUE_SPRING, LIGHT_SPRING -> SeasonalType.SPRING
        LIGHT_SUMMER, TRUE_SUMMER, SOFT_SUMMER -> SeasonalType.SUMMER
        SOFT_AUTUMN, TRUE_AUTUMN, DEEP_AUTUMN -> SeasonalType.AUTUMN
        DEEP_WINTER, TRUE_WINTER, BRIGHT_WINTER -> SeasonalType.WINTER
    }
}

@Serializable
data class FacialContrastVector(
    val skinLuminance: Float,
    val hairLuminance: Float,
    val eyeLuminance: Float,
    val contrastDelta: Float
)

@Serializable
data class ColorProfile(
    val season: ColorSeason,
    val undertone: Float, // -1.0 Cool to 1.0 Warm
    val contrastVector: FacialContrastVector,
    val optimalPaletteHexCodes: List<String>
) {
    fun toFashionProfile(): FashionProfile = FashionProfile(
        seasonalType = season.toSeasonalType(),
        undertone = when {
            undertone > 0.3f -> Undertone.WARM
            undertone < -0.3f -> Undertone.COOL
            else -> Undertone.NEUTRAL
        },
        recommendedPalette = optimalPaletteHexCodes
    )
}

