package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.MacroCategory

enum class CosmeticRole(val displayName: String) {
    EYE("Eye"),
    CHEEK("Cheek"),
    LIP("Lip"),
    NAIL("Nail"),
    PREP("Prep");

    companion object {
        fun fromMacroCategory(macroCategory: MacroCategory): CosmeticRole? {
            return when (macroCategory) {
                MacroCategory.EYES -> EYE
                MacroCategory.DIMENSION -> CHEEK
                MacroCategory.LIPS -> LIP
                MacroCategory.NAILS -> NAIL
                MacroCategory.PREP -> PREP
                else -> null
            }
        }
    }
}
