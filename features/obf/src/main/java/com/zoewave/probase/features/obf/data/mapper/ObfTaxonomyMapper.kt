package com.zoewave.probase.features.obf.data.mapper

import com.zoewave.probase.kocolor.model.MicroCategory

object ObfTaxonomyMapper {

    /**
     * Translates messy OBF category tags into strict KoColor MicroCategories.
     */
    fun extractMicroCategory(tags: List<String>?): MicroCategory {
        if (tags == null) return MicroCategory.AI_PENDING

        // Convert all tags to lowercase for safe matching
        val normalizedTags = tags.map { it.lowercase() }

        return when {
            normalizedTags.any { it.contains("foundation") || it.contains("fond-de-teint") } -> MicroCategory.FOUNDATION
            normalizedTags.any { it.contains("concealer") || it.contains("anti-cernes") } -> MicroCategory.CONCEALER
            normalizedTags.any { it.contains("lipstick") || it.contains("rouge-a-levres") } -> MicroCategory.LIPSTICK
            normalizedTags.any { it.contains("mascara") } -> MicroCategory.MASCARA
            normalizedTags.any { it.contains("blush") || it.contains("fard à joues") } -> MicroCategory.BLUSH
            normalizedTags.any { it.contains("eyeshadow") || it.contains("fard à paupières") } -> MicroCategory.EYESHADOW
            normalizedTags.any { it.contains("cleanser") || it.contains("nettoyant") } -> MicroCategory.CLEANSER
            normalizedTags.any { it.contains("moisturizer") || it.contains("hydratant") } -> MicroCategory.MOISTURIZER
            normalizedTags.any { it.contains("serum") || it.contains("sérum") } -> MicroCategory.SERUM
            normalizedTags.any { it.contains("sunscreen") || it.contains("solaire") } -> MicroCategory.SPF
            else -> MicroCategory.AI_PENDING
        }
    }

    /**
     * Extracts comma-separated INCI ingredients into a clean list.
     */
    fun parseIngredients(ingredientsText: String?): List<String> {
        if (ingredientsText.isNullOrBlank()) return emptyList()
        return ingredientsText
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
    }
}
