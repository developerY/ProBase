package com.zoewave.probase.kocolor.features.routines.data

import com.zoewave.probase.kocolor.model.RoutineStep
import com.zoewave.probase.kocolor.model.RoutineTime

object RoutineDefaults {
    fun getMorningRoutine(): List<RoutineStep> = listOf(
        RoutineStep("m1", "Brush Teeth", "Common hygiene practice."),
        RoutineStep("m2", "Floss", "Essential for dental health."),
        RoutineStep("m3", "Oil-Based Cleanser", "Korean Skincare: Removes oil-based impurities.", isRecommended = true),
        RoutineStep("m4", "Water-Based Cleanser", "Korean Skincare: Removes remaining debris.", isRecommended = true),
        RoutineStep("m5", "Toner", "Korean Skincare: Balances skin pH.", isRecommended = true),
        RoutineStep("m6", "Essence", "Korean Skincare: Hydrates and aids cell turnover.", isRecommended = true),
        RoutineStep("m7", "Serum/Ampoule", "Korean Skincare: Targets specific concerns.", isRecommended = true),
        RoutineStep("m8", "Eye Cream", "Korean Skincare: Protects delicate eye area.", isRecommended = true),
        RoutineStep("m9", "Moisturizer", "Korean Skincare: Seals in hydration.", isRecommended = true),
        RoutineStep("m10", "Sunscreen", "Korean Skincare: Essential daytime protection.", isRecommended = true)
    )

    fun getEveningRoutine(): List<RoutineStep> = listOf(
        RoutineStep("e1", "Brush Teeth", "Common hygiene practice."),
        RoutineStep("e2", "Floss", "Essential for dental health."),
        RoutineStep("e3", "Double Cleanse", "Korean Skincare: Thoroughly remove makeup and SPF.", isRecommended = true),
        RoutineStep("e4", "Exfoliator", "Korean Skincare: Use 1-2 times a week for dead skin removal.", isRecommended = true),
        RoutineStep("e5", "Toner", "Korean Skincare: Prep skin for absorption.", isRecommended = true),
        RoutineStep("e6", "Essence", "Korean Skincare: Deep hydration.", isRecommended = true),
        RoutineStep("e7", "Sheet Mask", "Korean Skincare: Use occasionally for extra nourishment.", isRecommended = true),
        RoutineStep("e8", "Night Cream/Sleeping Mask", "Korean Skincare: Overnight repair.", isRecommended = true)
    )
}
