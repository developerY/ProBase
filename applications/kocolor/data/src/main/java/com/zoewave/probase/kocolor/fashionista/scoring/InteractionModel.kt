package com.zoewave.probase.kocolor.fashionista.scoring

import com.zoewave.probase.kocolor.fashionista.domain.FashionistaFeatureVector
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InteractionModel @Inject constructor() {

    fun extractFeatures(vector: FashionistaFeatureVector): List<FeatureValue> {
        return listOf(
            vector.composition,
            vector.colorHarmony,
            vector.silhouette,
            vector.textureHarmony,
            vector.visualHierarchy,
            vector.presentationIntegration
        )
    }
}
