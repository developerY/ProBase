package com.zoewave.probase.kocolor.features.settings.domain.seeder.model

import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ColorFamily
import com.zoewave.probase.core.util.color.ColorQuantizer
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import kotlinx.serialization.Serializable

@Serializable
data class WardrobeSeedDto(
    val brand: String,
    val name: String,
    val macroCategory: String,
    val microCategory: String,
    val colorHex: String?,
    val imageUrl: String? = null,
    val price: Double? = null
) {
    fun toEntity(): ClothingItemEntity {
        val category = when (macroCategory) {
            "OUTERWEAR", "TOPS" -> ClothingCategory.TOPS
            "BOTTOMS" -> ClothingCategory.BOTTOMS
            "SHOES" -> ClothingCategory.SHOES
            "ACCESSORIES" -> ClothingCategory.ACCESSORIES
            else -> ClothingCategory.OTHER
        }

        return ClothingItemEntity(
            name = name,
            brand = brand,
            category = category,
            colorHex = colorHex,
            colorFamily = colorHex?.let { ColorQuantizer.snapToFamily(it) } ?: ColorFamily.UNKNOWN,
            imageUrl = imageUrl,
            price = price,
            timestamp = System.currentTimeMillis()
        )
    }
}
