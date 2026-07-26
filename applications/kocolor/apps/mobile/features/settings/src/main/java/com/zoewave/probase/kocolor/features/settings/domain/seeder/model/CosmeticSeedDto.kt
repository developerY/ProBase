package com.zoewave.probase.kocolor.features.settings.domain.seeder.model

import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.core.util.color.ColorQuantizer
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import kotlinx.serialization.Serializable

@Serializable
data class CosmeticSeedDto(
    val brand: String,
    val name: String,
    val macroCategory: String,
    val microCategory: String,
    val colorHex: String,
    val imageUrl: String? = null,
    val price: Double? = null
) {
    fun toEntity(): CosmeticItemEntity {
        val macro = try {
            MacroCategory.valueOf(macroCategory)
        } catch (e: Exception) {
            MacroCategory.TOOLS
        }

        // Try to match micro category by display name or name
        val micro = MicroCategory.entries.find { 
            it.displayName.equals(microCategory, ignoreCase = true) || 
            it.name.equals(microCategory.replace(" ", "_"), ignoreCase = true)
        } ?: MicroCategory.OTHER

        return CosmeticItemEntity(
            name = name,
            brand = brand,
            macroCategory = macro,
            microCategory = micro,
            colorHex = colorHex,
            colorFamily = ColorQuantizer.snapToFamily(colorHex),
            imageUrl = imageUrl?.let { 
                when {
                    it.startsWith("//") -> "https:$it"
                    it.startsWith("http://") -> it.replace("http://", "https://")
                    else -> it
                }
            },
            price = price,
            timestamp = System.currentTimeMillis()
        )
    }
}
