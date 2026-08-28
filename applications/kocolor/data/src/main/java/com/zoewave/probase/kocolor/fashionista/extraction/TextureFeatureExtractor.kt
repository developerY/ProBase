package com.zoewave.probase.kocolor.fashionista.extraction

import com.zoewave.probase.kocolor.fashionista.domain.FashionistaObservation
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import com.zoewave.probase.kocolor.fashionista.texture.TextureHarmonyEngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextureFeatureExtractor @Inject constructor(
    private val textureHarmonyEngine: TextureHarmonyEngine
) {

    fun extract(observation: FashionistaObservation): FeatureValue {
        return textureHarmonyEngine.evaluate(observation.clothingItems)
    }
}
