package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class EnrichmentStatus {
    PENDING, ENRICHING, COMPLETED, FAILED
}

/**
 * Represents a product identified through the AI-First Discovery Pipeline.
 * Orchestrated as: OCR -> Local AI -> Resolution -> Enrichment.
 */
@Entity(tableName = "discovered_products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val internalId: Long = 0,
    val brand: String,
    val productName: String,
    val category: String? = null,
    val size: String? = null,
    val colorHex: String? = null,
    val shadeName: String? = null,
    val imageUrl: String? = null,
    val price: Double? = null,
    val volume: String? = null,
    val ingredients: List<String> = emptyList(),
    val claims: List<String> = emptyList(),
    val directions: String? = null,
    
    // Stage 5: Web Gemini Enrichments
    val skinConcerns: List<String> = emptyList(),
    val benefits: List<String> = emptyList(),
    val ingredientDescriptions: Map<String, String> = emptyMap(),
    
    // Pipeline Metadata
    val enrichmentStatus: EnrichmentStatus = EnrichmentStatus.PENDING,
    val deterministicConfidence: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
