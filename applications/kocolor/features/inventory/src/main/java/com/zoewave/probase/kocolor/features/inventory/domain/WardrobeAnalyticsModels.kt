package com.zoewave.probase.kocolor.features.inventory.domain

data class WardrobeAnalytics(
    val totalItems: Int = 54,
    val activeItems: Int = 47,
    val rarelyWornItems: Int = 7,
    val dna: WardrobeDna = WardrobeDna(),
    val colorDistribution: ColorDistribution = ColorDistribution(),
    val categoryDistribution: CategoryDistribution = CategoryDistribution(),
    val rotation: RotationAnalytics = RotationAnalytics(),
    val versatility: VersatilityAnalytics = VersatilityAnalytics(),
    val coverage: WardrobeCoverage = WardrobeCoverage(),
    val insights: List<WardrobeInsight> = emptyList()
)

data class WardrobeDna(
    val primaryIdentity: String = "Neutral-led",
    val temperatureBias: String = "Warm-biased",
    val depth: String = "Medium depth",
    val contrast: String = "Balanced contrast",
    val chroma: String = "Low-to-medium chroma"
)

data class ColorDistribution(
    val neutralsPct: Int = 42,
    val warmPct: Int = 31,
    val coolPct: Int = 27,
    val topColors: List<ColorStat> = emptyList()
)

data class ColorStat(
    val name: String,
    val hex: String,
    val count: Int,
    val percentage: Float
)

data class CategoryDistribution(
    val tops: Int = 16,
    val bottoms: Int = 9,
    val dresses: Int = 8,
    val shoes: Int = 7,
    val outerwear: Int = 6,
    val activewear: Int = 8
)

data class RotationAnalytics(
    val frequentlyWorn: Int = 18,
    val inRotation: Int = 29,
    val rarelyWorn: Int = 7,
    val healthScore: Int = 61,
    val mostWorn: List<GarmentSummary> = emptyList(),
    val leastWorn: List<GarmentSummary> = emptyList()
)

data class VersatilityAnalytics(
    val totalPossibleLooks: Int = 312,
    val mostVersatile: VersatileGarment = VersatileGarment()
)

data class VersatileGarment(
    val id: String = "w_41",
    val name: String = "Universal Khaki Button-Down",
    val compatibleLooksCount: Int = 18,
    val compatibleBottoms: Int = 8,
    val compatibleShoes: Int = 5,
    val compatibleOuterwear: Int = 3
)

data class WardrobeCoverage(
    val warmNeutrals: Float = 0.85f,
    val coolNeutrals: Float = 0.50f,
    val brightAccents: Float = 0.30f,
    val deepColors: Float = 0.65f
)

data class WardrobeInsight(
    val message: String,
    val isActionable: Boolean = true
)

data class GarmentSummary(
    val id: String,
    val name: String,
    val wearCount: Int
)
