package com.zoewave.probase.kocolor.fashionista.extraction

import com.zoewave.probase.kocolor.fashionista.domain.FashionistaObservation
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import com.zoewave.probase.kocolor.fashionista.hierarchy.VisualHierarchyEngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HierarchyFeatureExtractor @Inject constructor(
    private val visualHierarchyEngine: VisualHierarchyEngine
) {

    fun extract(observation: FashionistaObservation): FeatureValue {
        return visualHierarchyEngine.evaluate(observation.clothingItems)
    }
}
