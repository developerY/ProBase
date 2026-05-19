package com.probase.kocolor.model

data class HarmonizedLook(
    val targetGarment: ClothingItem,
    val recommendedLip: CosmeticItem?,
    val recommendedEye: CosmeticItem?,
    val recommendedCheek: CosmeticItem?
)
