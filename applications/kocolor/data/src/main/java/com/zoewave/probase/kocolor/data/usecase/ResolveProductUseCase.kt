package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.features.ai.local.data.LocalStandardizedData
import com.zoewave.probase.kocolor.db.entity.ProductEntity
import com.zoewave.probase.kocolor.db.entity.EnrichmentStatus
import javax.inject.Inject

data class DeterministicApiMetadata(
    val brand: String? = null,
    val name: String? = null,
    val ingredients: List<String> = emptyList()
)

/**
 * Orchestrates the merge of Local AI standardized data and opportunistic API metadata
 * into a single canonical ProductEntity.
 *
 * Implements Stage 3.5 & 3.75: Resolution & Confidence Scoring.
 */
class ResolveProductUseCase @Inject constructor() {

    fun execute(
        localData: LocalStandardizedData,
        apiMetadata: DeterministicApiMetadata?
    ): ProductEntity {
        // Deterministic Confidence Calculation (Master Prompt Rule)
        val confidence = calculateConfidence(localData, apiMetadata)
        
        // Canonical Resolution: API metadata wins if available
        val canonicalBrand = apiMetadata?.brand ?: localData.brand ?: "Unknown Brand"
        val canonicalName = apiMetadata?.name ?: localData.productName ?: "Unknown Product"
        val ingredients = if (apiMetadata?.ingredients?.isNotEmpty() == true) apiMetadata.ingredients else localData.ingredients

        return ProductEntity(
            brand = canonicalBrand,
            productName = canonicalName,
            category = localData.category,
            size = localData.size,
            ingredients = ingredients,
            claims = localData.claims,
            directions = localData.directions,
            deterministicConfidence = confidence,
            enrichmentStatus = EnrichmentStatus.PENDING
        )
    }

    private fun calculateConfidence(
        localData: LocalStandardizedData,
        apiMetadata: DeterministicApiMetadata?
    ): Float {
        var score = 0f
        
        // Brand Found (0.25)
        if (!localData.brand.isNullOrBlank() || !apiMetadata?.brand.isNullOrBlank()) score += 0.25f
        
        // Product Found (0.25)
        if (!localData.productName.isNullOrBlank() || !apiMetadata?.name.isNullOrBlank()) score += 0.25f
        
        // JSON Valid (0.30)
        if (!localData.brand.isNullOrBlank() && !localData.productName.isNullOrBlank()) score += 0.30f
        
        // Ingredients Found (0.20)
        if (localData.ingredients.isNotEmpty() || apiMetadata?.ingredients?.isNotEmpty() == true) score += 0.20f
        
        return score
    }
}
