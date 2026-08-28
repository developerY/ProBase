package com.zoewave.probase.kocolor.fashionista.scoring

/**
 * Versioned, reference-calibrated parameter configuration.
 * Derived offline from expert-rated reference ensembles.
 */
data class FashionistaCalibration(
    val standardId: String = "FASHIONISTA",
    val version: Int = 1,
    val featureWeights: DoubleArray = doubleArrayOf(0.20, 0.25, 0.20, 0.15, 0.20, 0.10),
    val interactionWeights: DoubleArray = DoubleArray(15) { 0.15 },
    val lambda: Double = 0.20,
    val unresolvedPenaltyWeight: Double = 0.40,
    val mu: Double = 0.50,
    val tau: Double = 0.20,
    val qMin: Double = 0.0,
    val qMax: Double = 1.0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FashionistaCalibration) return false
        return standardId == other.standardId && version == other.version
    }

    override fun hashCode(): Int {
        var result = standardId.hashCode()
        result = 31 * result + version
        return result
    }
}
