package com.zoewave.probase.kocolor.fashionista.presentation

import com.zoewave.probase.kocolor.fashionista.domain.FashionistaScore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class FashionistaScoreMapper @Inject constructor() {

    fun mapToExplanation(domainScore: FashionistaScore): FashionistaExplanation {
        val roundedScore = domainScore.score.roundToInt().coerceIn(0, 100)
        val coveragePct = (domainScore.coverage * 100.0).roundToInt().coerceIn(0, 100)

        val interpretation = when (roundedScore) {
            in 95..100 -> "Exceptional / Editorial"
            in 90..94 -> "Outstanding"
            in 80..89 -> "Excellent"
            in 70..79 -> "Strong"
            in 55..69 -> "Competent"
            in 40..54 -> "Weak"
            else -> "Visually Unsuccessful"
        }

        val breakdown = domainScore.breakdown

        val featureExplanations = listOf(
            mapFeature("Color Harmony", "Geometric Hue & Temperature Cohesion", breakdown.colorHarmony),
            mapFeature("Silhouette", "Structural & Layering Appropriateness", breakdown.silhouette),
            mapFeature("Contrast", "Outfit & Biometric Adherence", breakdown.contrast)
        )

        return FashionistaExplanation(
            score = roundedScore,
            coveragePercentage = coveragePct,
            interpretation = interpretation,
            features = featureExplanations
        )
    }

    private fun mapFeature(name: String, title: String, scoreValue: Double): FeatureExplanation {
        val (status, value) = when {
            scoreValue >= 0.0 -> AvailabilityStatus.MEASURED to (scoreValue * 100.0).roundToInt().coerceIn(0, 100)
            else -> AvailabilityStatus.NOT_MEASURABLE to null
        }

        val explanation = when (status) {
            AvailabilityStatus.NOT_MEASURABLE -> "Not measurable from this image."
            AvailabilityStatus.PARTIAL -> "The available visual evidence indicates partial measurement confidence."
            AvailabilityStatus.MEASURED -> "The observed visual system contains fully measurable feature indicators."
        }

        return FeatureExplanation(
            name = name,
            value = value,
            availabilityStatus = status,
            title = title,
            explanation = explanation
        )
    }
}
