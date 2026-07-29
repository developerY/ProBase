package com.zoewave.probase.kocolor.features.colors.domain.model

enum class HarmonyMode {
    EXACT,
    COMPLEMENTARY,
    SPLIT_COMPLEMENTARY,
    ANALOGOUS,
    TRIADIC,
    TETRADIC,
    MONOCHROMATIC
}

data class ColorSignature(
    val hex: String,
    val sourceId: Long,
    val sourceType: SourceType,
    val name: String? = null
)

enum class SourceType {
    WARDROBE,
    VANITY
}

data class StylistEdit(
    val title: String = "The Stylist's Edit",
    val primaryInsight: String,
    val recommendation: String,
    val anchorColors: List<String>,
    val buttonText: String = "SHOP THE EDIT"
)
