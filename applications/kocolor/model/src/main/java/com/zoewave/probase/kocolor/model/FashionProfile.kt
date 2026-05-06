package com.zoewave.probase.kocolor.model

import kotlinx.serialization.Serializable

enum class SeasonalType {
    SPRING, SUMMER, AUTUMN, WINTER, UNKNOWN
}

enum class Undertone {
    WARM, COOL, NEUTRAL, UNKNOWN
}

@Serializable
data class FashionProfile(
    val id: String = "default",
    val seasonalType: SeasonalType = SeasonalType.UNKNOWN,
    val undertone: Undertone = Undertone.UNKNOWN,
    val skinToneHex: String? = null,
    val eyeColor: String? = null,
    val hairColor: String? = null,
    val notes: String? = null,
    val recommendedPalette: List<String> = emptyList()
)

@Serializable
data class ColorPalette(
    val seasonalType: SeasonalType,
    val primaryColors: List<String>,
    val secondaryColors: List<String>,
    val neutralColors: List<String>,
    val avoidColors: List<String>
)

@Serializable
data class MakeupSuggestion(
    val category: String, // Foundation, Lip, Eye, etc.
    val advice: String,
    val recommendedColors: List<String>
)

@Serializable
data class OutfitSuggestion(
    val occasion: String,
    val advice: String,
    val keyPieces: List<String>,
    val colorCombinations: List<String>
)

@Serializable
data class FashionAdvice(
    val summary: String,
    val seasonalType: SeasonalType,
    val undertone: Undertone,
    val makeupSuggestions: List<MakeupSuggestion>,
    val outfitSuggestions: List<OutfitSuggestion>,
    val recommendedPalette: List<String>
)
