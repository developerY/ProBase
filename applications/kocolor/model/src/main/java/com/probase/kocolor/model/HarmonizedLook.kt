package com.probase.kocolor.model

import com.zoewave.probase.kocolor.model.ClothingItem

data class HarmonizedLook(
    val targetGarment: ClothingItem,
    val recommendedLip: CosmeticItem?,
    val recommendedEye: CosmeticItem?,
    val recommendedCheek: CosmeticItem?
)
