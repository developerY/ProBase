package com.zoewave.probase.kocolor.model

import kotlinx.serialization.Serializable

@Serializable
enum class CosmeticCategory {
    // Face
    PRIMER, FOUNDATION, CONCEALER, BB_CC_CREAM, SETTING_PRODUCT,
    // Cheeks
    BLUSH, BRONZER, CONTOUR, HIGHLIGHTER,
    // Eyes
    EYESHADOW, EYELINER, MASCARA, EYEBROW_PRODUCT, FALSE_LASHES,
    // Lips
    LIPSTICK, LIP_GLOSS, LIP_LINER, LIP_STAIN_TINT, LIP_PLUMPER,
    // Tools & Others
    BRUSHES_SPONGES, EYELASH_CURLER, ORGANIZERS, NAIL_POLISH, OTHER;

    val groupName: String
        get() = when (this) {
            PRIMER, FOUNDATION, CONCEALER, BB_CC_CREAM, SETTING_PRODUCT -> "Face (Base & Coverage)"
            BLUSH, BRONZER, CONTOUR, HIGHLIGHTER -> "Cheeks (Color & Dimension)"
            EYESHADOW, EYELINER, MASCARA, EYEBROW_PRODUCT, FALSE_LASHES -> "Eyes (Definition)"
            LIPSTICK, LIP_GLOSS, LIP_LINER, LIP_STAIN_TINT, LIP_PLUMPER -> "Lips (Color & Texture)"
            BRUSHES_SPONGES, EYELASH_CURLER, ORGANIZERS, NAIL_POLISH, OTHER -> "Tools & Accessories"
        }

    val displayName: String
        get() = when (this) {
            BB_CC_CREAM -> "BB/CC Cream"
            SETTING_PRODUCT -> "Setting Powder/Spray"
            EYEBROW_PRODUCT -> "Eyebrow Product"
            FALSE_LASHES -> "False Lashes"
            BRUSHES_SPONGES -> "Brushes & Sponges"
            EYELASH_CURLER -> "Eyelash Curler"
            LIP_STAIN_TINT -> "Lip Stain/Tint"
            LIP_PLUMPER -> "Lip Plumper"
            NAIL_POLISH -> "Nail Polish"
            else -> name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() }
        }
}

@Serializable
data class CosmeticItem(
    val id: Long = 0,
    val name: String,
    val brand: String,
    val category: CosmeticCategory,
    val colorHex: String? = null,
    val shadeName: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
