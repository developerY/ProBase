package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingItem
import javax.inject.Inject
import javax.inject.Singleton

enum class SerializationDetailLevel {
    MINIMAL,   // [id|category|name|hex]
    BALANCED,  // [id|category|name|hex|temperature|depth]
    EXPANDED   // [id|category|name|hex|temperature|depth|material]
}

@Singleton
class CompactManifestSerializer @Inject constructor() {

    fun serialize(
        items: List<ClothingItem>,
        detailLevel: SerializationDetailLevel = SerializationDetailLevel.BALANCED
    ): String {
        return items.joinToString(separator = "\n") { item ->
            // Note: Mapping to available fields in ClothingItem.kt
            // Assuming colorTemperature is temperature, and seasonalPalette/contrastLevel might represent depth.
            val id = "w_${item.internalId}"
            val category = item.category.name
            val name = item.name
            val hex = item.colorHex
            val temperature = item.colorTemperature ?: "Neutral"
            val depth = item.seasonalPalette ?: "Mod"
            val material = item.material ?: "Cotton"

            when (detailLevel) {
                SerializationDetailLevel.MINIMAL ->
                    "[$id|$category|$name|$hex]"
                SerializationDetailLevel.BALANCED ->
                    "[$id|$category|$name|$hex|$temperature|$depth]"
                SerializationDetailLevel.EXPANDED ->
                    "[$id|$category|$name|$hex|$temperature|$depth|$material]"
            }
        }
    }
}
