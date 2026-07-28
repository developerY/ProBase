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
