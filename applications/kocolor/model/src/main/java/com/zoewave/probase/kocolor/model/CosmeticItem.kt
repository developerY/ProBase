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

    // AI Assisted
    /** Item captured by camera, awaiting analysis. */
    AI_PENDING,

    // Others
    NAIL_POLISH,
    OTHER;

    val groupName: String
        get() = when (this) {
            PRIMER, FOUNDATION, CONCEALER, BB_CC_CREAM, SETTING_PRODUCT -> "Face (Base & Coverage)"
            BLUSH, BRONZER, CONTOUR, HIGHLIGHTER -> "Cheeks (Color & Dimension)"
            EYESHADOW, EYELINER, MASCARA, EYEBROW_PRODUCT, FALSE_LASHES -> "Eyes (Definition)"
            LIPSTICK, LIP_GLOSS, LIP_LINER, LIP_STAIN_TINT, LIP_PLUMPER -> "Lips (Color & Texture)"
            BRUSHES_SPONGES, EYELASH_CURLER, ORGANIZERS, AI_PENDING, NAIL_POLISH, OTHER -> "Tools & Accessories"
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
            AI_PENDING -> "New Capture"
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
            AI_PENDING -> "Awaiting Gemini analysis."
            NAIL_POLISH -> "Adds color to your nails."
            OTHER -> "Other beauty essentials."
        }

    val suggestions: List<String>
        get() = when (this) {
            PRIMER -> listOf("Hydrating primers for dry skin", "Mattifying primers for oily skin", "Color-correcting primers to cancel redness")
            FOUNDATION -> listOf("Liquid for a natural finish", "Powder for oily skin", "Full coverage for formal events")
            CONCEALER -> listOf("Peach tones for dark circles", "Green tones for redness", "Use a shade lighter to brighten under-eyes")
            BB_CC_CREAM -> listOf("Perfect for 'no-makeup' days", "Combines skincare and coverage", "Usually contains SPF")
            SETTING_PRODUCT -> listOf("Spray for a dewy finish", "Loose powder for the T-zone", "Pressed powder for touch-ups")
            BLUSH -> listOf("Peach for warm undertones", "Rose for cool undertones", "Cream blush for a dewy glow")
            BRONZER -> listOf("Apply where the sun naturally hits", "Matte for subtle warmth", "Shimmer for a glow")
            CONTOUR -> listOf("Cool-toned shades for shadows", "Apply under cheekbones and jawline", "Blend well to avoid harsh lines")
            HIGHLIGHTER -> listOf("Apply to high points (cheekbones, brow bone)", "Liquid for subtle glow", "Powder for intense shine")
            EYESHADOW -> listOf("Neutral palettes for everyday", "Shimmers for center of the lid", "Darker shades for the outer corner")
            EYELINER -> listOf("Liquid for precise wings", "Pencil for smoky looks", "Gel for long-lasting wear")
            MASCARA -> listOf("Wiggle at the base for volume", "Waterproof for long events", "Brown for a softer look")
            EYEBROW_PRODUCT -> listOf("Gel for quick grooming", "Pencil for hair-like strokes", "Powder for a soft, natural look")
            FALSE_LASHES -> listOf("Trim to fit your eye shape", "Use a thin layer of glue", "Wait 30s for glue to get tacky")
            LIPSTICK -> listOf("Matte for long wear", "Satin for comfort", "Nude for everyday versatility")
            LIP_GLOSS -> listOf("Use alone or over lipstick", "Clear for shine", "Plumping for a fuller look")
            LIP_LINER -> listOf("Prevents feathering", "Outline then fill for longevity", "Pick a shade close to your natural lip color")
            LIP_STAIN_TINT -> listOf("Great for gradient lips", "Very long-lasting", "Feels weightless on lips")
            LIP_PLUMPER -> listOf("Expect a slight tingle", "Apply before gloss", "Hydrating formulas are best")
            BRUSHES_SPONGES -> listOf("Dampen sponges for seamless blending", "Synthetic for creams", "Natural hair for powders")
            EYELASH_CURLER -> listOf("Curl before mascara", "Pulse 3 times for a lift", "Replace pads every 3-6 months")
            ORGANIZERS -> listOf("Clear acrylic for visibility", "Stackable to save space", "Keep brushes vertical")
            NAIL_POLISH -> listOf("Use base coat to prevent staining", "Two thin coats are better than one thick", "Top coat for shine and durability")
            OTHER -> listOf("Cotton swabs for cleanup", "Brush cleaner", "Micellar water for quick corrections")
            AI_PENDING -> emptyList()
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
    val imageUrl: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
