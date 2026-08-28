package com.zoewave.probase.kocolor.fashionista.extraction

import com.zoewave.probase.kocolor.fashionista.color.ChromaticHarmonyEngine
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaObservation
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ColorFeatureExtractor @Inject constructor(
    private val chromaticHarmonyEngine: ChromaticHarmonyEngine
) {

    fun extract(observation: FashionistaObservation): FeatureValue {
        val colors = if (observation.colorsHex.isNotEmpty()) {
            observation.colorsHex
        } else {
            observation.clothingItems.map { it.colorHex } + observation.cosmeticItems.map { it.colorHex }
        }
        return chromaticHarmonyEngine.evaluate(colors)
    }
}
