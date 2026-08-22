package com.zoewave.probase.kocolor.model.calibration

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
    BRIGHT_WINTER
}

@Serializable
data class FacialContrastVector(
    val skinLuminance: Float,
    val hairLuminance: Float,
    val eyeLuminance: Float,
    val contrastDelta: Float
)

@Serializable
data class PhenotypeProfile(
    val season: ColorSeason,
    val undertone: Float, // -1.0 Cool to 1.0 Warm
    val contrastVector: FacialContrastVector,
    val optimalPaletteHexCodes: List<String>
)
