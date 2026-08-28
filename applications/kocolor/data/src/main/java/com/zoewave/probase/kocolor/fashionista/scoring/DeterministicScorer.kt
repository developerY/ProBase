package com.zoewave.probase.kocolor.fashionista.scoring

import com.zoewave.probase.kocolor.fashionista.domain.FashionistaFeatureVector
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
import javax.inject.Inject
import javax.inject.Singleton

data class ScoringResult(
    val q: Double,
    val coverage: Double
)

@Singleton
class DeterministicScorer @Inject constructor(
    private val interactionModel: InteractionModel
) {

    fun calculateQ(
        featureVector: FashionistaFeatureVector,
        calibration: FashionistaCalibration
    ): ScoringResult {
        val features = interactionModel.extractFeatures(featureVector)
        val w = calibration.featureWeights
        val wInter = calibration.interactionWeights

        // 1. Calculate Q_base
        var baseNum = 0.0
        var baseDen = 0.0
        var totalPossibleWeight = 0.0

        for (i in features.indices) {
            val f = features[i]
            val weight = w.getOrElse(i) { 0.20 }
            baseNum += weight * f.value * f.availability
            baseDen += weight * f.availability
            totalPossibleWeight += weight
        }

        // Zero-Availability Fail-Safe
        if (baseDen == 0.0 || totalPossibleWeight == 0.0) {
            return ScoringResult(q = 0.0, coverage = 0.0)
        }

        val qBase = baseNum / baseDen
        val coverage = (baseDen / totalPossibleWeight).coerceIn(0.0, 1.0)

        // 2. Calculate Q_interaction
        var interNum = 0.0
        var interDen = 0.0
        var pairIndex = 0

        for (i in 0 until features.size - 1) {
            for (j in i + 1 until features.size) {
                val f1 = features[i]
                val f2 = features[j]
                val weight = wInter.getOrElse(pairIndex) { 0.15 }
                pairIndex++

                val availProduct = f1.availability * f2.availability
                interNum += weight * (f1.value * f2.value) * availProduct
                interDen += weight * availProduct
            }
        }

        // Interaction Denominator Fail-Safe
        val effectiveLambda: Double
        val qInteraction: Double

        if (interDen == 0.0) {
            effectiveLambda = 0.0
            qInteraction = qBase
        } else {
            effectiveLambda = calibration.lambda
            qInteraction = interNum / interDen
        }

        // 3. Unresolved Complexity Penalty (P_unresolved)
        val hierarchy = featureVector.visualHierarchy
        val texture = featureVector.textureHarmony
        val pUnresolved = if (hierarchy.availability > 0.0 && hierarchy.value < 0.3 && texture.value > 0.7) {
            calibration.unresolvedPenaltyWeight
        } else {
            0.0
        }

        // 4. Blended Q calculation
        val blended = (1.0 - effectiveLambda) * qBase + effectiveLambda * qInteraction
        val finalQ = (blended - pUnresolved).coerceIn(calibration.qMin, calibration.qMax)

        return ScoringResult(q = finalQ, coverage = coverage)
    }
}
