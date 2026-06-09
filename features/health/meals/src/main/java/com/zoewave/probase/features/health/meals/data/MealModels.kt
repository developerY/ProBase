package com.zoewave.probase.features.health.meals.data

import kotlinx.serialization.Serializable

@Serializable
enum class MetabolicPhase {
    Morning, // Anabolic / mTOR
    MidDay,  // Microbiome Homeostasis
    Evening  // Catabolic / Autophagy / Repair
}

@Serializable
data class Meal(
    val id: String,
    val name: String,
    val description: String,
    val scientificFocus: String,
    val phase: MetabolicPhase,
    val imageUrl: String? = null,
    val nutrition: NutritionInfo,
    val ingredients: List<Ingredient>,
    val steps: List<MealStep>
)

@Serializable
data class NutritionInfo(
    val calories: Int,
    val protein: Float, // in grams
    val carbs: Float,
    val fat: Float,
    val fiber: Float? = null
)

@Serializable
data class Ingredient(
    val name: String,
    val amount: String
)

@Serializable
data class MealStep(
    val order: Int,
    val instruction: String,
    val imageUrl: String? = null
)
