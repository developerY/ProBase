package com.zoewave.probase.kocolor.domain.taxonomy

import kotlinx.serialization.Serializable

object KoColorThresholds {
    // 1. Thermal Bands (°C) — matches KoColorTaxonomy.ThermalBand & Phase 3 TEMP_REGEX
    const val THERMAL_HOT_MIN_C: Double = 26.0
    const val THERMAL_WARM_MIN_C: Double = 20.0
    const val THERMAL_MILD_MIN_C: Double = 14.0
    const val THERMAL_COOL_MIN_C: Double = 7.0

    // 2. Outerwear Ban Cutoff (°C) — unified across ContextEngine, Simulator, and PromptAssembler
    const val OUTERWEAR_BAN_TEMP_C: Double = 18.0

    // 3. Rotation Penalty Cutoffs
    const val ROTATION_HARD_BAN_PENALTY: Double = 0.7
    const val ROTATION_SOFT_PENALTY: Double = 0.5

    // 4. Wear History Windows
    const val RECENT_WEAR_HARD_HOURS: Int = 24
    const val RECENT_WEAR_SOFT_DAYS: Int = 3
    const val ROTATION_WINDOW_DAYS: Int = 7

    // 5. CIELAB Chroma (C*) Gates
    const val CHROMA_ACHROMATIC_MAX: Float = 10.0f
    const val CHROMA_LOW_GATE: Float = 15.0f
    const val CHROMA_MUTED_GATE: Float = 20.0f
    const val CHROMA_STATEMENT_MIN: Float = 25.0f
    const val CHROMA_HIGH_GATE: Float = 30.0f
}

@Serializable
enum class GarmentCategory {
    TOPS, BOTTOMS, SHOES, OUTERWEAR, ONE_PIECE, ACCESSORIES, ACTIVEWEAR
}

@Serializable
enum class ProductType {
    CROP_TOP, T_SHIRT, TANK_TOP, BLOUSE, BUTTON_DOWN, POLO, SWEATER, CARDIGAN, HOODIE,
    SWEATSHIRT, BODYSUIT, TROUSERS, CHINOS, JEANS, SHORTS, SKIRT, LEGGINGS, JOGGERS,
    TRACK_PANTS, DRESS, JUMPSUIT, ROMPER, SNEAKERS, RUNNING_SHOES, BOOTS, ANKLE_BOOTS,
    LOAFERS, OXFORDS, HEELS, FLATS, SANDALS, SLIDES, TRACK_JACKET, WINDBREAKER, TRENCH_COAT,
    LEATHER_JACKET, WOOL_OVERCOAT, BLAZER, DENIM_JACKET, BOMBER_JACKET, PUFFER, PARKA,
    VEST, BAG, BELT, HAT, SCARF, SUNGLASSES, WATCH, JEWELRY, UNKNOWN
}

@Serializable
enum class OccasionTag {
    CASUAL, EVERYDAY, WORK, EVENING, TRAVEL, FORMAL_EVENT, ACTIVE
}

@Serializable
enum class FormalityLevel(val rank: Int) {
    CASUAL(1),
    SMART_CASUAL(2),
    BUSINESS(3),
    FORMAL(4),
    GALA(5)
}

@Serializable
enum class ThermalBand(val minCelsius: Double, val maxCelsius: Double) {
    HOT(26.0, 50.0),
    WARM(20.0, 26.0),
    MILD(14.0, 20.0),
    COOL(7.0, 14.0),
    COLD(-30.0, 7.0)
}

@Serializable
enum class WeatherProtection {
    RAIN_RESISTANT, WIND_RESISTANT, SUN_COVERAGE, THERMAL_RETENTION
}

@Serializable
enum class ThermalWeight { LIGHT, MIDWEIGHT, HEAVYWEIGHT, UNKNOWN }

@Serializable
enum class Breathability { LOW, MEDIUM, HIGH, UNKNOWN }

@Serializable
enum class InsulationLevel { NONE, LIGHT, MEDIUM, HEAVY, UNKNOWN }

@Serializable
enum class LayeringRole { BASE_LAYER, MID_LAYER, OUTER_LAYER, STANDALONE, FOOTWEAR, ACCESSORY }

@Serializable
enum class SilhouetteFit { FITTED, TAILORED, RELAXED, OVERSIZED, UNKNOWN }

@Serializable
enum class SeasonTag { SPRING, SUMMER, AUTUMN, WINTER }

@Serializable
enum class ActivityTag { OFFICE, WALKING, CYCLING, EXERCISE, LOUNGE, DINING, COMMUTE, SOCIAL }

@Serializable
enum class HueFamily {
    RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, PURPLE, MAGENTA, NEUTRAL
}

@Serializable
enum class ColorTemperature { WARM, COOL, NEUTRAL }

@Serializable
enum class MaterialFiber {
    ORGANIC_COTTON, COTTON, LINEN, SILK, MERINO_WOOL, WOOL, CASHMERE, ALPACA, POLYESTER,
    TRICOT_POLYESTER, RECYCLED_POLYESTER, NYLON, RECYCLED_NYLON, SPANDEX, ELASTANE, VISCOSE,
    RAYON, TENCEL_LYOCELL, MODAL, ACRYLIC, LEATHER, SUEDE, VEGAN_LEATHER, GORE_TEX, DENIM,
    CANVAS, FLEECE, DOWN, SYNTHETIC_FILL, RUBBER, EVA_FOAM, MESH, COSMETIC_FORMULATION, UNKNOWN
}

@Serializable
enum class ProvenanceType {
    SOURCE_VERIFIED,
    RULE_DERIVED,
    LLM_SUGGESTED,
    HUMAN_REVIEWED,
    UNKNOWN;

    val isTrustedForHardFilter: Boolean
        get() = this == SOURCE_VERIFIED || this == HUMAN_REVIEWED || this == RULE_DERIVED
}
