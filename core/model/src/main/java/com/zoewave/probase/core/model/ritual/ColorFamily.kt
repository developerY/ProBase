package com.zoewave.probase.core.model.ritual

import kotlinx.serialization.Serializable

@Serializable
enum class ColorFamily(val hex: String, val displayName: String) {
    BLACK("#000000", "Black"),
    WHITE("#FFFFFF", "White"),
    COOL_GREY("#808080", "Cool Grey"),
    WARM_TAUPE("#B38B6D", "Warm Taupe"),
    TRUE_RED("#FF0000", "True Red"),
    BURGUNDY("#800020", "Burgundy"),
    NAVY("#000080", "Navy"),
    SKY_BLUE("#87CEEB", "Sky Blue"),
    EMERALD("#50C878", "Emerald"),
    OLIVE("#808000", "Olive"),
    MUSTARD("#FFDB58", "Mustard"),
    YELLOW("#FFFF00", "Yellow"),
    ORANGE("#FFA500", "Orange"),
    PEACH("#FFDAB9", "Peach"),
    LAVENDER("#E6E6FA", "Lavender"),
    DEEP_PURPLE("#800080", "Deep Purple"),
    UNKNOWN("#CCCCCC", "Unknown")
}
