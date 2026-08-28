package com.zoewave.probase.kocolor.fashionista.extraction

import com.zoewave.probase.kocolor.fashionista.domain.FashionistaObservation
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import com.zoewave.probase.kocolor.fashionista.silhouette.SilhouetteEngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SilhouetteFeatureExtractor @Inject constructor(
    private val silhouetteEngine: SilhouetteEngine
) {

    fun extract(observation: FashionistaObservation): FeatureValue {
        return silhouetteEngine.evaluate(observation.clothingItems)
    }
}
