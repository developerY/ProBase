package com.zoewave.probase.kocolor.model

import kotlinx.serialization.Serializable

@Serializable
enum class CosmeticCategory {
    LIPSTICK, FOUNDATION, BLUSH, EYESHADOW, MASCARA, EYELINER, NAIL_POLISH, OTHER
}

@Serializable
data class CosmeticItem(
    val id: Long = 0,
    val name: String,
    val brand: String,
    val category: CosmeticCategory,
    val colorHex: String? = null,
    val shadeName: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
