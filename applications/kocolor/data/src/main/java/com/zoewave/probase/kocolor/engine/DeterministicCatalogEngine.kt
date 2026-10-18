package com.zoewave.probase.kocolor.engine

import com.zoewave.probase.kocolor.domain.model.CatalogRecord
import com.zoewave.probase.kocolor.domain.taxonomy.*
import kotlin.math.abs

class DeterministicCatalogEngine {

    companion object {
        val ALLOWED_AI_COSMETIC_ROLES = setOf("EYE", "CHEEK", "LIP", "NAIL")
        val CHROMATIC_BEHAVIORS = setOf("OPAQUE", "SHEER")
    }

    /**
     * Filters and ranks Wardrobe & Cosmetic items while strictly enforcing:
     * 1. Provenance Policy (Hard filters only evaluate trusted provenance).
     * 2. Preflight Slot Completeness (Halts before calling Gemini if any required slot is empty).
     * 3. Category & Chromatic Isolation for Cosmetics.
     */
    fun prepareCandidatesForGemini(
        allRecords: List<CatalogRecord>,
        tempCelsius: Double,
        isRaining: Boolean,
        targetOccasion: OccasionTag,
        targetFormality: FormalityLevel,
        requiredWardrobeSlots: Set<String> = setOf("TOP", "BOTTOM", "FOOTWEAR"),
        topKPerSlot: Int = 6
    ): EnginePreflightResult {
        val activeBand = resolveThermalBand(tempCelsius)
        val allowOuterwear = activeBand !in setOf(ThermalBand.HOT, ThermalBand.WARM) || isRaining

        // --- 1. WARDROBE HARD FILTERING (Trusted Provenance Only) ---
        val eligibleWardrobe = allRecords
            .filter { it.domain == "WARDROBE" && it.wardrobeMetadata != null }
            .filter { record ->
                val w = record.wardrobeMetadata!!
                val slot = w.garmentSlot.value ?: return@filter false

                // Hard Rule A: Outerwear banned in hot/warm dry weather
                if (slot == "OUTERWEAR" && !allowOuterwear) return@filter false

                // Hard Rule B: Thermal suitability (only enforced if provenance is trusted)
                if (w.thermalSuitability.provenance.isTrustedForHardFilter) {
                    if (activeBand !in w.thermalSuitability.values) return@filter false
                }

                // Hard Rule C: Occasion check against trusted occasion tags
                val trustedOccasions = w.occasionTags
                    .filter { it.provenance.isTrustedForHardFilter }
                    .mapNotNull { it.value }
                if (trustedOccasions.isNotEmpty() && targetOccasion !in trustedOccasions) {
                    return@filter false
                }

                true
            }

        // --- 2. PREFLIGHT SLOT COMPLETENESS GATE ---
        val groupedBySlot = eligibleWardrobe.groupBy { it.wardrobeMetadata!!.garmentSlot.value!! }
        val missingSlots = requiredWardrobeSlots.filter { groupedBySlot[it].isNullOrEmpty() }.toSet()

        if (missingSlots.isNotEmpty()) {
            return EnginePreflightResult.AbortedMissingSlots(
                missingWardrobeSlots = missingSlots,
                initialWardrobeCount = allRecords.count { it.domain == "WARDROBE" }
            )
        }

        // --- 3. SOFT PREFERENCE RANKING (Includes LLM_SUGGESTED Tags) ---
        val rankedWardrobeBySlot = groupedBySlot.mapValues { (_, records) ->
            records.sortedByDescending { scoreWardrobeSoftPreferences(it, targetOccasion, targetFormality) }
                .take(topKPerSlot)
        }

        // --- 4. COSMETIC CATEGORY & CHROMATIC ISOLATION ---
        val eligibleCosmetics = allRecords
            .filter { it.domain == "COSMETICS" && it.cosmeticsMetadata != null }
            .filter { record ->
                val c = record.cosmeticsMetadata!!
                val behavior = record.commonMetadata.color.chromaticBehavior
                // Strictly forbid PREP, HAIR, COMPLEXION and non-chromatic items
                c.cosmeticCategory in ALLOWED_AI_COSMETIC_ROLES && behavior in CHROMATIC_BEHAVIORS
            }

        return EnginePreflightResult.Ready(
            wardrobeBySlot = rankedWardrobeBySlot,
            cosmeticsByRole = eligibleCosmetics.groupBy { it.cosmeticsMetadata!!.cosmeticCategory },
            compactManifestText = buildGroundedManifestPrompt(rankedWardrobeBySlot, eligibleCosmetics)
        )
    }

    private fun scoreWardrobeSoftPreferences(
        record: CatalogRecord,
        targetOccasion: OccasionTag,
        targetFormality: FormalityLevel
    ): Double {
        val w = record.wardrobeMetadata ?: return 0.0
        var score = 50.0

        // Soft Occasion boost (evaluates LLM_SUGGESTED tags)
        if (w.occasionTags.any { it.value == targetOccasion }) {
            score += 20.0
        }

        // Soft Formality alignment
        val itemFormality = w.formality.value ?: FormalityLevel.CASUAL
        val diff = abs(itemFormality.rank - targetFormality.rank)
        score += (20.0 - diff * 7.0).coerceAtLeast(0.0)

        return score
    }

    /**
     * Serializes the Compact Manifest with explicit physical materials and construction features
     * so Gemini obeys Strict Grounding Rule #1.
     */
    private fun buildGroundedManifestPrompt(
        wardrobeBySlot: Map<String, List<CatalogRecord>>,
        cosmetics: List<CatalogRecord>
    ): String = buildString {
        appendLine("WARDROBE:")
        wardrobeBySlot.values.flatten().forEach { r ->
            val c = r.commonMetadata
            val w = r.wardrobeMetadata!!
            val materials = c.material.composition.joinToString(", ") { "${it.percentage}% ${it.material}" }
            val features = w.construction.features.joinToString(", ")
            appendLine(
                "- ID: ${c.itemId} | Slot: ${w.garmentSlot.value} | Name: ${c.name} | " +
                "Hex: ${c.color.hex} (${c.color.hueFamily.value}) | " +
                "Materials: [$materials] | Features: [$features]"
            )
        }
        appendLine("\nCOSMETICS:")
        cosmetics.forEach { r ->
            val c = r.commonMetadata
            val cos = r.cosmeticsMetadata!!
            appendLine(
                "- ID: ${c.itemId} | Role: ${cos.cosmeticCategory} | Name: ${c.name} | " +
                "Shade: ${cos.shade} | Hex: ${c.color.hex} | Finish: ${cos.finish.value}"
            )
        }
    }

    private fun resolveThermalBand(tempC: Double): ThermalBand =
        ThermalBand.entries.firstOrNull { tempC >= it.minCelsius && tempC < it.maxCelsius }
            ?: ThermalBand.MILD
}

sealed class EnginePreflightResult {
    data class Ready(
        val wardrobeBySlot: Map<String, List<CatalogRecord>>,
        val cosmeticsByRole: Map<String, List<CatalogRecord>>,
        val compactManifestText: String
    ) : EnginePreflightResult()

    data class AbortedMissingSlots(
        val missingWardrobeSlots: Set<String>,
        val initialWardrobeCount: Int
    ) : EnginePreflightResult()
}
