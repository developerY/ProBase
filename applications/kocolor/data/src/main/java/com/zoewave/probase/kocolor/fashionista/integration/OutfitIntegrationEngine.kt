package com.zoewave.probase.kocolor.fashionista.integration

import com.zoewave.probase.kocolor.fashionista.domain.FashionistaObservation
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutfitIntegrationEngine @Inject constructor(
    private val cosmeticIntegrationEngine: CosmeticIntegrationEngine
) {

    /**
     * Evaluates presentation integration (face/skin biometrics + outfit).
     * If the observation is a flat-lay or outfit-only image (no wearer/face),
     * returns FeatureValue(value = 0.0, availability = 0.0) to completely bypass execution
     * on null data without polluting the score.
     */
    fun evaluate(observation: FashionistaObservation): FeatureValue {
        if (!observation.hasBiometricData) {
            return FeatureValue(value = 0.0, availability = 0.0)
        }

        val cosmeticScore = cosmeticIntegrationEngine.evaluateCosmetics(observation.cosmeticItems)
        return FeatureValue(value = cosmeticScore, availability = 1.0)
    }
}
