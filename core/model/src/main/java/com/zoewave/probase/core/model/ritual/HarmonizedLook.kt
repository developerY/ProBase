package com.zoewave.probase.core.model.ritual

import kotlinx.serialization.Serializable

@Serializable
data class HarmonizedLook(
    val targetGarment: ClothingItem,
    val recommendedLip: CosmeticItem?,
    val recommendedEye: CosmeticItem?,
    val recommendedCheek: CosmeticItem?
)
