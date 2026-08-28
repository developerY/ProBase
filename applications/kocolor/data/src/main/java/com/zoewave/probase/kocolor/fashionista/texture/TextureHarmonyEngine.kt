package com.zoewave.probase.kocolor.fashionista.texture

import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextureHarmonyEngine @Inject constructor(
    private val glcmTextureEngine: GlcmTextureEngine
) {

    fun evaluate(clothingItems: List<ClothingItem>): FeatureValue {
        if (clothingItems.isEmpty()) {
            return FeatureValue(value = 0.0, availability = 0.0)
        }
        val materials = clothingItems.mapNotNull { it.material }
        if (materials.isEmpty()) {
            return FeatureValue(value = 0.5, availability = 0.2)
        }

        return glcmTextureEngine.evaluateTexture(materials)
    }
}
