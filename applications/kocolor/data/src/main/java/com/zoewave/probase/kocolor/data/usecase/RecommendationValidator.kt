package com.zoewave.probase.kocolor.data.usecase

import android.util.Log
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deterministic Post-LLM Validator.
 * Intercepts, validates, and sanitizes Gemini recommendation output before it hits UI state flows.
 */
@Singleton
class RecommendationValidator @Inject constructor() {

    data class ValidationResult(
        val isValid: Boolean,
        val sanitizedBlueprint: StyleBlueprint,
        val validationErrors: List<String> = emptyList()
    )

    /**
     * Validates and sanitizes a raw [StyleBlueprint] against available candidate manifests.
     */
    fun validateAndSanitize(
        rawBlueprint: StyleBlueprint,
        clothingCandidates: List<CandidateProvenance>,
        cosmeticCandidates: List<CandidateProvenance>
    ): ValidationResult {
        val errors = mutableListOf<String>()

        val validClothingIds = clothingCandidates.mapNotNull { prov ->
            prov.clothingItem?.let { item -> "w_${item.internalId}" }
        }.toSet() + clothingCandidates.mapNotNull { it.clothingItem?.remoteId }.toSet()

        val validCosmeticIds = cosmeticCandidates.mapNotNull { prov ->
            prov.cosmeticItem?.let { item -> "c_${item.internalId}" }
        }.toSet() + cosmeticCandidates.mapNotNull { it.cosmeticItem?.remoteId }.toSet()

        // 1. ID Existence Check
        val filteredClothingIds = rawBlueprint.selectedClothingIds.filter { id ->
            if (id in validClothingIds) {
                true
            } else {
                errors.add("Invalid or unmanifested clothing ID removed: $id")
                false
            }
        }

        val filteredCosmeticIds = rawBlueprint.selectedCosmeticIds.filter { id ->
            if (id in validCosmeticIds) {
                true
            } else {
                errors.add("Invalid or unmanifested cosmetic ID removed: $id")
                false
            }
        }

        // 1b. Mandatory Locked Anchor Enforcement (Enforces locked anchors like w_50)
        val lockedAnchorIds = clothingCandidates.filter {
            it.retrievalReason.contains("LOCKED ANCHOR", ignoreCase = true)
        }.mapNotNull { prov -> prov.clothingItem?.let { "w_${it.internalId}" } }

        val finalClothingIds = filteredClothingIds.toMutableList()
        lockedAnchorIds.forEach { anchorId ->
            if (anchorId !in finalClothingIds) {
                Log.w("RecommendationValidator", "Re-injecting omitted locked anchor $anchorId into selection")
                val anchorItem = clothingCandidates.find { it.clothingItem?.let { item -> "w_${item.internalId}" } == anchorId }?.clothingItem
                if (anchorItem != null) {
                    // Remove any item sharing the same category (e.g. remove w_51 if w_50 is locked shoes)
                    finalClothingIds.removeAll { id ->
                        val item = clothingCandidates.find { it.clothingItem?.let { c -> "w_${c.internalId}" } == id }?.clothingItem
                        item != null && item.category == anchorItem.category && id != anchorId
                    }
                    finalClothingIds.add(anchorId)
                }
            }
        }

        // 2. Cosmetic Role Category Firewall Enforcement
        val finalCosmeticIds = filteredCosmeticIds.filter { id ->
            val item = cosmeticCandidates.find { it.cosmeticItem?.let { c -> "c_${c.internalId}" } == id || it.cosmeticItem?.remoteId == id }?.cosmeticItem
            if (item != null) {
                val role = CosmeticRole.fromMacroCategory(item.macroCategory)
                if (role == null || role == CosmeticRole.PREP) {
                    errors.add("Invalid cosmetic role '${item.macroCategory}' removed for item: $id")
                    Log.w("RecommendationValidator", "Sanitizing blueprint: removed unmapped/PREP cosmetic role for '$id'")
                    false
                } else {
                    true
                }
            } else {
                false
            }
        }

        // 2b. Role Coverage & Cardinality Assertion (Asserts all requested cosmetic roles are present)
        val requestedCosmeticRoles = cosmeticCandidates.mapNotNull {
            it.cosmeticItem?.let { c -> CosmeticRole.fromMacroCategory(c.macroCategory) }
        }.filter { it != CosmeticRole.PREP }.toSet()

        val selectedCosmeticRoles = finalCosmeticIds.mapNotNull { id ->
            val item = cosmeticCandidates.find { it.cosmeticItem?.let { c -> "c_${c.internalId}" } == id || it.cosmeticItem?.remoteId == id }?.cosmeticItem
            item?.let { CosmeticRole.fromMacroCategory(it.macroCategory) }
        }.toSet()

        if (requestedCosmeticRoles.isNotEmpty() && selectedCosmeticRoles.size < requestedCosmeticRoles.size) {
            val missingRoles = requestedCosmeticRoles - selectedCosmeticRoles
            val errorMsg = "Validation failed: Missing required cosmetic roles [${missingRoles.joinToString { it.displayName }}]"
            Log.e("RecommendationValidator", errorMsg)
            errors.add(errorMsg)
        }

        // 3. Rationale Cross-Check & Sanitization (Strips references to unselected products)
        val clothingMap = clothingCandidates.mapNotNull { it.clothingItem }.associateBy { it.name }
        val cosmeticMap = cosmeticCandidates.mapNotNull { it.cosmeticItem }.associateBy { it.name }

        var sanitizedRationale = rawBlueprint.rationale
        clothingMap.forEach { (name, item) ->
            val id = "w_${item.internalId}"
            if (id !in finalClothingIds && sanitizedRationale.contains(name, ignoreCase = true)) {
                Log.w("RecommendationValidator", "Sanitizing rationale: stripping reference to unselected clothing '$name'")
                sanitizedRationale = sanitizedRationale.replace(Regex("(?i)(?<!\\d)[^.]*\\b${Regex.escape(name)}\\b[^.]*\\.(?!\\d)"), "")
            }
        }

        cosmeticMap.forEach { (name, item) ->
            val id = "c_${item.internalId}"
            if (id !in finalCosmeticIds && sanitizedRationale.contains(name, ignoreCase = true)) {
                Log.w("RecommendationValidator", "Sanitizing rationale: stripping reference to unselected cosmetic '$name'")
                sanitizedRationale = sanitizedRationale.replace(Regex("(?i)(?<!\\d)[^.]*\\b${Regex.escape(name)}\\b[^.]*\\.(?!\\d)"), "")
            }
        }

        val sanitizedBlueprint = rawBlueprint.copy(
            rationale = sanitizedRationale.trim(),
            selectedClothingIds = finalClothingIds,
            selectedCosmeticIds = finalCosmeticIds
        )

        return ValidationResult(
            isValid = errors.isEmpty(),
            sanitizedBlueprint = sanitizedBlueprint,
            validationErrors = errors
        )
    }
}
