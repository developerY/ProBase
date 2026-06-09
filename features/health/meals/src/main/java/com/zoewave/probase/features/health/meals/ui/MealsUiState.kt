package com.zoewave.probase.features.health.meals.ui

import com.zoewave.probase.features.health.meals.data.Meal

sealed interface MealsUiState {
    data object Loading : MealsUiState
    data class Success(
        val meals: List<Meal>,
        val selectedMeal: Meal? = null,
        val isAddingMeal: Boolean = false
    ) : MealsUiState
    data class Error(val message: String) : MealsUiState
}
