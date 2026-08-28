package com.zoewave.probase.kocolor.fashionista.domain

/**
 * Encapsulates a normalized feature measurement alongside its data availability.
 * Dynamically scales the scoring equation based on available evidence.
 */
data class FeatureValue(
    val value: Double,
    val availability: Double
) {
    init {
        require(value in 0.0..1.0) { "Feature value must be between 0.0 and 1.0" }
        require(availability in 0.0..1.0) { "Feature availability must be between 0.0 and 1.0" }
    }
}
