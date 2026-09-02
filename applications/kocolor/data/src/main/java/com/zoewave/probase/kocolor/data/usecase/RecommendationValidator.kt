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

        // 2. Rationale Cross-Check & Sanitization (Strips references to unselected products)
        val clothingMap = clothingCandidates.mapNotNull { it.clothingItem }.associateBy { it.name }
        val cosmeticMap = cosmeticCandidates.mapNotNull { it.cosmeticItem }.associateBy { it.name }

        var sanitizedRationale = rawBlueprint.rationale
        clothingMap.forEach { (name, item) ->
            val id = "w_${item.internalId}"
            if (id !in filteredClothingIds && sanitizedRationale.contains(name, ignoreCase = true)) {
                Log.w("RecommendationValidator", "Sanitizing rationale: stripping reference to unselected clothing '$name'")
                sanitizedRationale = sanitizedRationale.replace(Regex("(?i)[^.]*\\b${Regex.escape(name)}\\b[^.]*\\."), "")
            }
        }

        cosmeticMap.forEach { (name, item) ->
            val id = "c_${item.internalId}"
            if (id !in filteredCosmeticIds && sanitizedRationale.contains(name, ignoreCase = true)) {
                Log.w("RecommendationValidator", "Sanitizing rationale: stripping reference to unselected cosmetic '$name'")
                sanitizedRationale = sanitizedRationale.replace(Regex("(?i)[^.]*\\b${Regex.escape(name)}\\b[^.]*\\."), "")
            }
        }

        val sanitizedBlueprint = rawBlueprint.copy(
            rationale = sanitizedRationale.trim(),
            selectedClothingIds = filteredClothingIds,
            selectedCosmeticIds = filteredCosmeticIds
        )

        return ValidationResult(
            isValid = errors.isEmpty(),
            sanitizedBlueprint = sanitizedBlueprint,
            validationErrors = errors
        )
    }
}
