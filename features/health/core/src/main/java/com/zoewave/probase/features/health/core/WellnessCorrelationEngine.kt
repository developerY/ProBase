package com.zoewave.probase.features.health.core

import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SkinInsight(
    val trigger: String,
    val manifestation: String,
    val recommendation: String,
    val severity: Float // 0 to 1
)

/**
 * Engine for correlating lifestyle markers (nutrition, sleep, stress) with skin performance.
 */
@Singleton
class WellnessCorrelationEngine @Inject constructor() {

    /**
     * Identifies skin health triggers based on lifestyle data.
     */
    fun analyzeTriggers(
        sleepHours: Float,
        sugarIntake: String, // "Low", "Medium", "High"
        stressLevel: Int // 1 to 10
    ): List<SkinInsight> {
        val insights = mutableListOf<SkinInsight>()

        if (sleepHours < 6.0f) {
            insights.add(SkinInsight(
                trigger = "Sleep Deprivation",
                manifestation = "Puffiness & Dark Circles",
                recommendation = "Use a caffeine-based eye serum and increase water intake.",
                severity = 0.8f
            ))
        }

        if (sugarIntake == "High") {
            insights.add(SkinInsight(
                trigger = "High Glycemic Diet",
                manifestation = "Inflammatory Acne",
                recommendation = "Reduce refined sugar intake and use anti-inflammatory ingredients like Green Tea.",
                severity = 0.7f
            ))
        }

        if (stressLevel > 7) {
            insights.add(SkinInsight(
                trigger = "Chronic Stress",
                manifestation = "Skin Sensitivity & Redness",
                recommendation = "Focus on barrier repair with ceramides and niacinamide.",
                severity = 0.9f
            ))
        }

        return insights
    }
}
