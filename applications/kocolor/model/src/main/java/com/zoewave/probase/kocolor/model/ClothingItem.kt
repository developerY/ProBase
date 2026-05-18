package com.zoewave.probase.kocolor.model

import kotlinx.serialization.Serializable

@Serializable
enum class ClothingCategory {
    TOPS, BOTTOMS, SHOES, ACCESSORIES, OTHER;

    val displayName: String
        get() = when (this) {
            TOPS -> "Tops"
            BOTTOMS -> "Bottoms"
            SHOES -> "Shoes"
            ACCESSORIES -> "Accessories"
            OTHER -> "Other"
        }
}

@Serializable
data class ClothingItem(
    val id: Long = 0,
    val name: String,
    val brand: String? = null,
    val category: ClothingCategory,
    val colorHex: String? = null,
    val size: String? = null,
    val material: String? = null,
    val price: Double? = null,
    val imageUrl: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
