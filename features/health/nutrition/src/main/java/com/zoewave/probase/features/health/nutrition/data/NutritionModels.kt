package com.zoewave.probase.features.health.nutrition.data

import kotlinx.serialization.Serializable

@Serializable
data class NutritionStage(
    val id: String,
    val title: String,
    val subtitle: String,
    val scientificBody: String,
    val suggestedMealTitle: String,
    val suggestedMealSubtitle: String,
    val suggestedMealBody: String,
    val startTime: String,
    val endTime: String? = null,
    val isCompleted: Boolean = false
)

@Serializable
data class NutritionRoutine(
    val stages: List<NutritionStage>
)
