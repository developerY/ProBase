package com.zoewave.probase.kocolor.fashionista.domain

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem

/**
 * Raw input representing the observed visual system. 
 * Completely decoupled from user identity, wardrobe RAG, or environmental context.
 */
data class FashionistaObservation(
    val clothingItems: List<ClothingItem> = emptyList(),
    val cosmeticItems: List<CosmeticItem> = emptyList(),
    val clothingIds: List<String> = emptyList(),
    val cosmeticIds: List<String> = emptyList(),
    val colorsHex: List<String> = emptyList(),
    val hasVisualMassData: Boolean = false,
    val hasBiometricData: Boolean = false
)
