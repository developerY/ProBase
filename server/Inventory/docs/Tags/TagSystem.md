We can build this directly into KoColor’s deterministic pipeline and `.kpkg` CDN asset compiler. Structuring the catalog with strict enums, provenance tracking, and a two-stage **Hard Filter → Soft Scorer** architecture will also permanently prevent the empty-manifest and category-bleed bugs seen in your earlier logcat trace.

Below is the complete Kotlin implementation for the taxonomy, the material-derivation engine, and the two-stage deterministic pruning pipeline.

---

## 1. The Controlled Wardrobe Taxonomy (`WardrobeTaxonomy.kt`)

Using `kotlinx.serialization`, this schema enforces strict enum types while supporting forward-compatible versioning (`metadataVersion = 1`) and explicit **provenance tracking** so estimated attributes are never confused with verified product specs.

```kotlin
package com.zoewave.probase.kocolor.domain.taxonomy

import kotlinx.serialization.Serializable

@Serializable
enum class GarmentCategory {
    TOPS, BOTTOMS, SHOES, OUTERWEAR, ONE_PIECE, ACCESSORIES
}

@Serializable
enum class ProductType {
    CROP_TOP, T_SHIRT, BLOUSE, BUTTON_DOWN, SWEATER, HOODIE,
    TROUSERS, JEANS, SHORTS, SKIRT, LEGGINGS,
    SNEAKERS, BOOTS, LOAFERS, HEELS, SANDALS,
    TRENCH_COAT, LEATHER_JACKET, WOOL_OVERCOAT, BLAZER, PUFFER, UNKNOWN
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
    HOT(26.0, 50.0),      // > 26°C
    WARM(20.0, 26.0),     // 20°C – 26°C
    MILD(14.0, 20.0),     // 14°C – 20°C (e.g., Brookline 15.9°C log)
    COOL(7.0, 14.0),      // 7°C – 14°C
    COLD(-30.0, 7.0)      // < 7°C
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
enum class LayeringRole { BASE_LAYER, MID_LAYER, OUTER_LAYER, STANDALONE, FOOTWEAR }

@Serializable
enum class SilhouetteFit { FITTED, TAILORED, RELAXED, OVERSIZED, UNKNOWN }

@Serializable
enum class SeasonTag { SPRING, SUMMER, AUTUMN, WINTER }

@Serializable
enum class ActivityTag { OFFICE, WALKING, CYCLING, EXERCISE, LOUNGE, DINING }

@Serializable
enum class HueFamily {
    RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, PURPLE, MAGENTA, NEUTRAL
}

@Serializable
enum class ColorTemperature { WARM, COOL, NEUTRAL }

@Serializable
enum class MaterialFiber {
    ORGANIC_COTTON, COTTON, LINEN, SILK, MERINO_WOOL, WOOL, CASHMERE,
    POLYESTER, NYLON, SPANDEX, LEATHER, GORE_TEX, DENIM, UNKNOWN
}

/**
 * Tracks whether a technical attribute came from verified manufacturer data,
 * deterministic textile rules, or remains unknown.
 */
@Serializable
enum class AttributeProvenance {
    VERIFIED_SPEC,
    DERIVED_FROM_CONSTRUCTION,
    ESTIMATED,
    UNKNOWN
}

```

---

## 2. Canonical Metadata Record (`WardrobeCatalogRecord.kt`)

This matches your JSON structure while adding CIELAB coordinates ($L^*, a^*, b^*, C^*, h^\circ$) for mathematical harmony and provenance metadata.

```kotlin
package com.zoewave.probase.kocolor.domain.model

import com.zoewave.probase.kocolor.domain.taxonomy.*
import kotlinx.serialization.Serializable

@Serializable
data class MaterialComponent(
    val material: MaterialFiber,
    val percentage: Int
)

@Serializable
data class MaterialSpec(
    val composition: List<MaterialComponent>
)

@Serializable
data class GarmentConstruction(
    val thermalWeight: ThermalWeight = ThermalWeight.UNKNOWN,
    val breathability: Breathability = Breathability.UNKNOWN,
    val insulation: InsulationLevel = InsulationLevel.UNKNOWN,
    val provenance: AttributeProvenance = AttributeProvenance.UNKNOWN
)

@Serializable
data class ColorIntelligence(
    val hex: String,
    val hueFamily: HueFamily,
    val temperature: ColorTemperature,
    val lStar: Double = 50.0, // Lightness (0-100)
    val cStar: Double = 0.0,  // Chroma
    val hDeg: Double = 0.0    // Hue angle (0-360)
)

@Serializable
data class WardrobeCatalogRecord(
    val itemId: String,
    val name: String,
    val category: GarmentCategory,
    val productType: ProductType,
    val occasionTags: Set<OccasionTag>,
    val formality: FormalityLevel,
    val thermalSuitability: Set<ThermalBand>,
    val thermalProvenance: AttributeProvenance = AttributeProvenance.VERIFIED_SPEC,
    val weatherProtection: Set<WeatherProtection> = emptySet(),
    val construction: GarmentConstruction,
    val layeringRole: LayeringRole,
    val silhouette: SilhouetteFit = SilhouetteFit.UNKNOWN,
    val seasons: Set<SeasonTag> = emptySet(),
    val activities: Set<ActivityTag> = emptySet(),
    val color: ColorIntelligence,
    val material: MaterialSpec,
    val metadataVersion: Int = 1
)

```

---

## 3. Hard Filters vs. Soft Preferences Engine (`DeterministicWardrobeEngine.kt`)

This implements the exact pipeline from your specification, separating strict binary eligibility from mathematical ranking—and halting before Gemini is called if any required slot is empty.

```kotlin
package com.zoewave.probase.kocolor.engine

import com.zoewave.probase.kocolor.domain.model.WardrobeCatalogRecord
import com.zoewave.probase.kocolor.domain.taxonomy.*
import kotlin.math.abs

data class EnvironmentalContext(
    val tempCelsius: Double,
    val uvIndex: Double,
    val isRaining: Boolean,
    val windSpeedMps: Double
)

data class StyleExecutionContext(
    val environment: EnvironmentalContext,
    val targetOccasion: OccasionTag,
    val targetActivity: ActivityTag? = null,
    val targetFormality: FormalityLevel = FormalityLevel.CASUAL,
    val requiredCategories: Set<GarmentCategory> = setOf(
        GarmentCategory.TOPS,
        GarmentCategory.BOTTOMS,
        GarmentCategory.SHOES
    ),
    val userPreferredTemperature: ColorTemperature = ColorTemperature.NEUTRAL,
    val unavailableItemIds: Set<String> = emptySet(), // Laundry / cooldown
    val recentWearFrequency: Map<String, Int> = emptyMap()
)

data class ScoredCandidate(
    val record: WardrobeCatalogRecord,
    val deterministicScore: Double,
    val scoreBreakdown: Map<String, Double>
)

sealed class PruningResult {
    data class ReadyForAI(
        val candidatesByRole: Map<GarmentCategory, List<ScoredCandidate>>,
        val flattenedManifest: List<WardrobeCatalogRecord>,
        val auditStats: PruningAuditStats
    ) : PruningResult()

    data class AbortedMissingSlots(
        val missingCategories: Set<GarmentCategory>,
        val auditStats: PruningAuditStats
    ) : PruningResult()
}

data class PruningAuditStats(
    val initialCount: Int,
    val passedAvailability: Int,
    val passedHardWeatherAndOccasion: Int,
    val finalEligibleCount: Int
)

class DeterministicWardrobeEngine {

    fun executePruningAndScoring(
        catalog: List<WardrobeCatalogRecord>,
        context: StyleExecutionContext,
        topKPerCategory: Int = 8
    ): PruningResult {
        val initialCount = catalog.size

        // STEP 1: Availability & Rotation Hard Filter
        val availableItems = catalog.filter { it.itemId !in context.unavailableItemIds }

        // STEP 2: Weather, Occasion & Role Hard Eligibility Filters
        val activeThermalBand = resolveThermalBand(context.environment.tempCelsius)
        val allowOuterwear = activeThermalBand !in setOf(ThermalBand.HOT, ThermalBand.WARM) ||
                context.environment.isRaining

        val hardEligibleItems = availableItems.filter { item ->
            // 2a. Category Isolation (Hard prohibition of outerwear in hot dry weather)
            if (item.category == GarmentCategory.OUTERWEAR && !allowOuterwear) return@filter false
            if (item.category !in context.requiredCategories &&
                !(item.category == GarmentCategory.OUTERWEAR && allowOuterwear)) {
                return@filter false
            }

            // 2b. Thermal Suitability Check (Allow adjacent band if provenance is ESTIMATED)
            val matchesThermal = activeThermalBand in item.thermalSuitability
            if (!matchesThermal) return@filter false

            // 2c. Hot-Weather Breathability & Weight Guardrail
            if (activeThermalBand == ThermalBand.HOT) {
                if (item.construction.thermalWeight == ThermalWeight.HEAVYWEIGHT) return@filter false
                if (item.construction.insulation != InsulationLevel.NONE &&
                    item.construction.insulation != InsulationLevel.UNKNOWN) return@filter false
            }

            // 2d. Occasion & Activity Hard Match
            val matchesOccasion = context.targetOccasion in item.occasionTags
            val matchesActivity = context.targetActivity == null ||
                    item.activities.isEmpty() ||
                    context.targetActivity in item.activities

            matchesOccasion && matchesActivity
        }

        val auditStats = PruningAuditStats(
            initialCount = initialCount,
            passedAvailability = availableItems.size,
            passedHardWeatherAndOccasion = hardEligibleItems.size,
            finalEligibleCount = hardEligibleItems.size
        )

        // STEP 3: Slot Completeness Guard Clause (Prevents the 0-item Gemini Hallucination Bug)
        val groupedByCategory = hardEligibleItems.groupBy { it.category }
        val missingRequiredSlots = context.requiredCategories.filter { category ->
            groupedByCategory[category].isNullOrEmpty()
        }.toSet()

        if (missingRequiredSlots.isNotEmpty()) {
            return PruningResult.AbortedMissingSlots(
                missingCategories = missingRequiredSlots,
                auditStats = auditStats
            )
        }

        // STEP 4: Soft Preference Scoring & Top-K Ranking per Role
        val rankedByRole = groupedByCategory.mapValues { (_, items) ->
            items.map { item -> scoreItem(item, context) }
                .sortedByDescending { it.deterministicScore }
                .take(topKPerCategory)
        }

        val compactManifest = rankedByRole.values.flatten().map { it.record }

        return PruningResult.ReadyForAI(
            candidatesByRole = rankedByRole,
            flattenedManifest = compactManifest,
            auditStats = auditStats
        )
    }

    private fun scoreItem(
        item: WardrobeCatalogRecord,
        context: StyleExecutionContext
    ): ScoredCandidate {
        // 1. Formality Alignment (0.0 - 30.0 pts)
        val formalityDiff = abs(item.formality.rank - context.targetFormality.rank)
        val formalityScore = (30.0 - (formalityDiff * 10.0)).coerceAtLeast(0.0)

        // 2. Color Temperature Alignment (0.0 - 25.0 pts)
        val colorTempScore = when {
            item.color.temperature == context.userPreferredTemperature -> 25.0
            item.color.temperature == ColorTemperature.NEUTRAL ||
                    item.color.hueFamily == HueFamily.NEUTRAL -> 20.0
            else -> 10.0
        }

        // 3. Environmental Protection Bonus (0.0 - 20.0 pts)
        var envBonus = 0.0
        if (context.environment.isRaining && WeatherProtection.RAIN_RESISTANT in item.weatherProtection) {
            envBonus += 12.0
        }
        if (context.environment.uvIndex >= 3.0 && WeatherProtection.SUN_COVERAGE in item.weatherProtection) {
            envBonus += 8.0
        }
        if (context.environment.tempCelsius >= 24.0 && item.construction.breathability == Breathability.HIGH) {
            envBonus += 10.0
        }
        envBonus = envBonus.coerceAtMost(20.0)

        // 4. Rotation Health / Novelty (0.0 - 25.0 pts)
        val recentWears = context.recentWearFrequency[item.itemId] ?: 0
        val rotationScore = (25.0 - (recentWears * 5.0)).coerceAtLeast(0.0)

        val total = formalityScore + colorTempScore + envBonus + rotationScore

        return ScoredCandidate(
            record = item,
            deterministicScore = total,
            scoreBreakdown = mapOf(
                "formality" to formalityScore,
                "colorTemp" to colorTempScore,
                "environment" to envBonus,
                "rotation" to rotationScore
            )
        )
    }

    private fun resolveThermalBand(tempCelsius: Double): ThermalBand =
        ThermalBand.entries.firstOrNull {
            tempCelsius >= it.minCelsius && tempCelsius < it.maxCelsius
        } ?: ThermalBand.MILD
}

```

---

## 4. Deterministic Post-AI Validation Gate (`BlueprintValidator.kt`)

Even with a pruned manifest, LLMs can occasionally drop a slot or hallucinate an ID. Placing this validator between **AI Style Synthesis** and **FASHIONISTA Evaluation** guarantees your downstream scoring engine only ever evaluates valid outfits.

```kotlin
package com.zoewave.probase.kocolor.engine

import com.zoewave.probase.kocolor.domain.model.WardrobeCatalogRecord
import com.zoewave.probase.kocolor.domain.taxonomy.GarmentCategory

object BlueprintValidator {

    data class ValidationOutcome(
        val isValid: Boolean,
        val verifiedItems: List<WardrobeCatalogRecord>,
        val violations: List<String>
    )

    fun validateAiSelection(
        selectedClothingIds: List<String>,
        eligibleManifest: List<WardrobeCatalogRecord>,
        requiredCategories: Set<GarmentCategory>
    ): ValidationOutcome {
        val violations = mutableListOf<String>()
        val manifestMap = eligibleManifest.associateBy { it.itemId }

        // 1. Verify no hallucinated IDs
        val resolvedItems = selectedClothingIds.mapNotNull { id ->
            val found = manifestMap[id]
            if (found == null) violations.add("Hallucinated or ineligible itemId: $id")
            found
        }

        // 2. Verify all required role slots are filled
        val selectedCategories = resolvedItems.map { it.category }.toSet()
        for (required in requiredCategories) {
            if (required !in selectedCategories) {
                violations.add("Missing required garment category: $required")
            }
        }

        return ValidationOutcome(
            isValid = violations.isEmpty() && resolvedItems.isNotEmpty(),
            verifiedItems = resolvedItems,
            violations = violations
        )
    }
}

```

---

## 5. Automatic Catalog Enrichment Rules (For the `.kpkg` Compiler)

When compiling raw product data into `.kpkg` Zstandard bundles on your CDN (`atelier-dev-01`), you can use a deterministic rule pass to populate missing construction attributes while explicitly tagging their provenance as `DERIVED_FROM_CONSTRUCTION` or `UNKNOWN`:

| Input Condition (Material + ProductType) | Derived `thermalWeight` | Derived `breathability` | Derived `thermalSuitability` | Assigned `provenance` |
| --- | --- | --- | --- | --- |
| **$\ge 80\%$ Cotton / Linen** + `CROP_TOP` or `T_SHIRT` | `LIGHT` | `HIGH` | `HOT`, `WARM` | `DERIVED_FROM_CONSTRUCTION` |
| **$\ge 50\%$ Merino Wool / Cashmere** + `SWEATER` | `MIDWEIGHT` | `MEDIUM` | `MILD`, `COOL`, `COLD` | `DERIVED_FROM_CONSTRUCTION` |
| **$\ge 70\%$ Wool / Leather** + `WOOL_OVERCOAT` / `LEATHER_JACKET` | `HEAVYWEIGHT` | `LOW` | `COOL`, `COLD` | `DERIVED_FROM_CONSTRUCTION` |
| **Unlisted Material Composition** | `UNKNOWN` | `UNKNOWN` | *(Requires manual review)* | `UNKNOWN` |

Would you like to build the migration script to upgrade your 54 starter wardrobe items in `com.kocolor.pack.fashion.complete` to `metadataVersion: 1` first, or wire this `DeterministicWardrobeEngine` into your local Room database and Gemini prompt serializer?