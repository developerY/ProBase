package com.zoewave.probase.kocolor.fashionista.presentation

import com.zoewave.probase.kocolor.fashionista.domain.FashionistaScore
import com.zoewave.probase.kocolor.fashionista.domain.FeatureValue
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
            mapFeature("Composition", "Category Harmony & Proportion", breakdown.composition),
            mapFeature("Color Harmony", "CIEDE2000 & Hue Distribution", breakdown.colorHarmony),
            mapFeature("Silhouette", "Center of Gravity & Mass Distribution", breakdown.silhouette),
            mapFeature("Texture Harmony", "GLCM Scale & Material Coherence", breakdown.textureHarmony),
            mapFeature("Visual Hierarchy", "Focal Point Clarity & Dominance", breakdown.visualHierarchy),
            mapFeature("Presentation Integration", "Biometric ITA & Facial Contrast", breakdown.presentationIntegration)
        )

        return FashionistaExplanation(
            score = roundedScore,
            coveragePercentage = coveragePct,
            interpretation = interpretation,
            features = featureExplanations
        )
    }

    private fun mapFeature(name: String, title: String, feature: FeatureValue): FeatureExplanation {
        val (status, value) = when {
            feature.availability >= 0.99 -> AvailabilityStatus.MEASURED to (feature.value * 100.0).roundToInt().coerceIn(0, 100)
            feature.availability > 0.0 -> AvailabilityStatus.PARTIAL to (feature.value * 100.0).roundToInt().coerceIn(0, 100)
            else -> AvailabilityStatus.NOT_MEASURABLE to null
        }

        val explanation = when (status) {
            AvailabilityStatus.NOT_MEASURABLE -> "Not measurable from this image."
            AvailabilityStatus.PARTIAL -> "The available visual evidence indicates partial measurement confidence (${(feature.availability * 100).roundToInt()}%)."
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
