package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.Finish
import com.zoewave.probase.core.model.ritual.Temperature
import com.zoewave.probase.core.util.color.ColorQuantizer
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
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
        wardrobeProvenance: List<CandidateProvenance>,
        cosmetics: List<CosmeticItem>,
        detailLevel: SerializationDetailLevel = SerializationDetailLevel.BALANCED
    ): String {
        val wManifest = wardrobeProvenance.joinToString(separator = "\n") { prov ->
            val item = prov.clothingItem ?: return@joinToString ""
            val id = "w_${item.internalId}"
            val category = item.category.name
            val name = item.name
            val hex = item.colorHex
            val temperature = item.colorTemperature?.takeIf { it != "UNKNOWN" } ?: ColorQuantizer.determineTemperature(item.colorHex).name
            val depth = item.seasonalPalette?.takeIf { it != "UNKNOWN" } ?: "Balanced"
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
            val temperature = if (item.temperature != Temperature.UNKNOWN) item.temperature.name else ColorQuantizer.determineTemperature(item.colorHex).name
            val finish = if (item.finish != Finish.UNKNOWN) item.finish.name else "SATIN"
            val brand = item.brand.ifBlank { "KoColor" }

            when (detailLevel) {
                SerializationDetailLevel.MINIMAL -> "[$id|$category|$name|$hex]"
                SerializationDetailLevel.BALANCED -> "[$id|$category|$name|$hex|$temperature|$finish]"
                SerializationDetailLevel.EXPANDED -> "[$id|$category|$name|$hex|$temperature|$finish|$brand]"
            }
        }

        return "WARDROBE:\n$wManifest\n\nCOSMETICS:\n$cManifest"
    }
}
