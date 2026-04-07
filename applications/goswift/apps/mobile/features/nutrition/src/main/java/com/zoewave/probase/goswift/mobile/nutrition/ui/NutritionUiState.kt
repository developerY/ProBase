package com.zoewave.probase.goswift.mobile.nutrition.ui

sealed interface NutritionUiState {
    object Loading : NutritionUiState
    data class Success(
        val dailyCalories: Double,
        val recentMeals: List<MealLog>
    ) : NutritionUiState
}

data class MealLog(
    val id: String,
    val name: String,
    val calories: Double,
    val timestamp: Long
)

sealed interface NutritionUiEvent {
    data class AddMeal(val name: String, val calories: Double) : NutritionUiEvent
    object Refresh : NutritionUiEvent
}
