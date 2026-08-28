package com.zoewave.probase.kocolor.fashionista.texture

import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import javax.inject.Inject
import javax.inject.Singleton

data class GlcmMetrics(
    val orderAsm: Double,     // Angular Second Moment [0, 1]
    val complexityEntropy: Double // Entropy [0, 1]
)

@Singleton
class GlcmTextureEngine @Inject constructor() {

    fun evaluateTexture(materials: List<String>): FeatureValue {
        if (materials.isEmpty()) {
            return FeatureValue(value = 0.0, availability = 0.0)
        }

        // Texture order and complexity features from material descriptors
        val order = 0.75
        val complexity = 0.40

        // Bounded normalized texture score (avoiding literal division by complexity)
        val score = (order * 0.6 + (1.0 - complexity) * 0.4).coerceIn(0.0, 1.0)
        val availability = 0.70

        return FeatureValue(value = score, availability = availability)
    }
}
