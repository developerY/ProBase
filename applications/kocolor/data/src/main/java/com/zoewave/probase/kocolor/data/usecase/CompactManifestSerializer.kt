package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
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
        wardrobe: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        detailLevel: SerializationDetailLevel = SerializationDetailLevel.BALANCED
    ): String {
        val wManifest = wardrobe.joinToString(separator = "\n") { item ->
            val id = "w_${item.internalId}"
            val category = item.category.name
            val name = item.name
            val hex = item.colorHex
            val temperature = item.colorTemperature ?: "Neutral"
            val depth = item.seasonalPalette ?: "Mod"
            val material = item.material ?: "Cotton"

            when (detailLevel) {
                SerializationDetailLevel.MINIMAL -> "[$id|$category|$name|$hex]"
                SerializationDetailLevel.BALANCED -> "[$id|$category|$name|$hex|$temperature|$depth]"
                SerializationDetailLevel.EXPANDED -> "[$id|$category|$name|$hex|$temperature|$depth|$material]"
            }
        }

        val cManifest = cosmetics.joinToString(separator = "\n") { item ->
            val id = "c_${item.internalId}"
            val category = item.macroCategory.name
            val name = item.name
            val hex = item.colorHex
            val temperature = item.temperature.name
            val finish = item.finish.name
            val brand = item.brand

            when (detailLevel) {
                SerializationDetailLevel.MINIMAL -> "[$id|$category|$name|$hex]"
                SerializationDetailLevel.BALANCED -> "[$id|$category|$name|$hex|$temperature|$finish]"
                SerializationDetailLevel.EXPANDED -> "[$id|$category|$name|$hex|$temperature|$finish|$brand]"
            }
        }

        return "WARDROBE:\n$wManifest\n\nCOSMETICS:\n$cManifest"
    }
}
