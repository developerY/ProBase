package com.zoewave.probase.core.model.ritual

import kotlinx.serialization.Serializable

/**
 * Represents the structured payload from the Static CDN for initial data seeding.
 */
@Serializable
data class StarterPackResponse(
    val version: Int,
    val cosmetics: List<CosmeticItem>,
    val clothing: List<ClothingItem> = emptyList()
)
