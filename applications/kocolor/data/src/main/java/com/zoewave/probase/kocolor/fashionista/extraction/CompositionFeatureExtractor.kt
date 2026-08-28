package com.zoewave.probase.kocolor.fashionista.extraction

import com.zoewave.probase.kocolor.fashionista.composition.CompositionEngine
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaObservation
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompositionFeatureExtractor @Inject constructor(
    private val compositionEngine: CompositionEngine
) {

    fun extract(observation: FashionistaObservation): FeatureValue {
        return compositionEngine.evaluate(observation.clothingItems)
    }
}
