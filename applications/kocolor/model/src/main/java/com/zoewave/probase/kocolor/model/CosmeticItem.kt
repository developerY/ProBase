package com.zoewave.probase.kocolor.model

import kotlinx.serialization.Serializable

@Serializable
enum class CosmeticCategory {
    // Face (Base & Coverage)
    /** Preps skin and extends makeup wear. */
    PRIMER,
    /** Evens out skin tone (liquid, powder, cream). */
    FOUNDATION,
    /** Covers imperfections and brightens under-eyes. */
    CONCEALER,
    /** Lighter alternatives for coverage and skincare benefits. */
    BB_CC_CREAM,
    /** Locks makeup in place. */
    SETTING_PRODUCT,

    // Cheeks (Color & Dimension)
    /** Adds color to the cheeks (powder, cream, liquid). */
    BLUSH,
    /** Adds warmth for a sun-kissed look. */
    BRONZER,
    /** Creates shadows for sculpting. */
    CONTOUR,
    /** Adds radiance to high points of the face. */
    HIGHLIGHTER,

    // Eyes (Definition)
    /** Adds color and depth to eyelids. */
    EYESHADOW,
    /** Defines the lash line. */
    EYELINER,
    /** Volumizes, lengthens, or darkens lashes. */
    MASCARA,
    /** Pencils, gels, and powders to fill and shape brows. */
    EYEBROW_PRODUCT,
    /** Adds volume and length. */
    FALSE_LASHES,

    // Lips (Color & Texture)
    /** Provides pigmented color (matte, satin, cream). */
    LIPSTICK,
    /** Adds shine. */
    LIP_GLOSS,
    /** Defines and prevents bleeding. */
    LIP_LINER,
    /** Provides long-lasting sheer color. */
    LIP_STAIN_TINT,
    /** Temporarily increases lip volume. */
    LIP_PLUMPER,

    // Tools & Accessories
    /** For application and blending. */
    BRUSHES_SPONGES,
    /** Curls lashes. */
    EYELASH_CURLER,
    /** For storage. */
    ORGANIZERS,

    // Others
    NAIL_POLISH,
    OTHER;

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

    val description: String
        get() = when (this) {
            PRIMER -> "Preps skin and extends makeup wear."
            FOUNDATION -> "Evens out skin tone (liquid, powder, cream)."
            CONCEALER -> "Covers imperfections and brightens under-eyes."
            BB_CC_CREAM -> "Lighter alternatives for coverage and skincare benefits."
            SETTING_PRODUCT -> "Locks makeup in place."
            BLUSH -> "Adds color to the cheeks (powder, cream, liquid)."
            BRONZER -> "Adds warmth for a sun-kissed look."
            CONTOUR -> "Creates shadows for sculpting."
            HIGHLIGHTER -> "Adds radiance to high points of the face."
            EYESHADOW -> "Adds color and depth to eyelids."
            EYELINER -> "Defines the lash line."
            MASCARA -> "Volumizes, lengthens, or darkens lashes."
            EYEBROW_PRODUCT -> "Pencils, gels, and powders to fill and shape brows."
            FALSE_LASHES -> "Adds volume and length."
            LIPSTICK -> "Provides pigmented color (matte, satin, cream)."
            LIP_GLOSS -> "Adds shine."
            LIP_LINER -> "Defines and prevents bleeding."
            LIP_STAIN_TINT -> "Provides long-lasting sheer color."
            LIP_PLUMPER -> "Temporarily increases lip volume."
            BRUSHES_SPONGES -> "For application and blending."
            EYELASH_CURLER -> "Curls lashes."
            ORGANIZERS -> "For storage."
            NAIL_POLISH -> "Adds color to your nails."
            OTHER -> "Other beauty essentials."
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
