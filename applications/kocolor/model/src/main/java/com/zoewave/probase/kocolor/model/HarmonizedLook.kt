package com.zoewave.probase.kocolor.model

import kotlinx.serialization.Serializable

@Serializable
data class HarmonizedLook(
    val targetGarment: ClothingItem,
    val recommendedLip: CosmeticItem?,
    val recommendedEye: CosmeticItem?,
    val recommendedCheek: CosmeticItem?
)
