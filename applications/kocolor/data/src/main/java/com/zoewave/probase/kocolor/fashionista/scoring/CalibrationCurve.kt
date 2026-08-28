package com.zoewave.probase.kocolor.fashionista.scoring

import com.zoewave.probase.kocolor.fashionista.math.Logistic
import javax.inject.Inject
import javax.inject.Singleton

object FashionistaSemanticRanges {
    val EXCEPTIONAL_EDITORIAL = 95.0..100.0
    val OUTSTANDING = 90.0..94.999
    val EXCELLENT = 80.0..89.999
    val STRONG = 70.0..79.999
    val COMPETENT = 55.0..69.999
    val WEAK = 40.0..54.999
    val VISUALLY_UNSUCCESSFUL = 0.0..39.999

    fun getInterpretation(score: Double): String {
        return when (score) {
            in EXCEPTIONAL_EDITORIAL -> "Exceptional / Editorial"
            in OUTSTANDING -> "Outstanding"
            in EXCELLENT -> "Excellent"
            in STRONG -> "Strong"
            in COMPETENT -> "Competent"
            in WEAK -> "Weak"
            else -> "Visually Unsuccessful"
        }
    }
}

@Singleton
class CalibrationCurve @Inject constructor() {

    fun mapToScore(q: Double, calibration: FashionistaCalibration): Double {
        return Logistic.logisticScaling(q, calibration.mu, calibration.tau)
    }
}
