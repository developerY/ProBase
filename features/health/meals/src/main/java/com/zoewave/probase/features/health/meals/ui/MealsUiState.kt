package com.zoewave.probase.features.health.meals.ui

import com.zoewave.probase.features.health.meals.data.Meal

sealed interface MealsUiState {
    data object Loading : MealsUiState
    data class Success(
        val meals: List<Meal>,
        val selectedMeal: Meal? = null,
        val isAddingMeal: Boolean = false,
        val editingMeal: Meal? = null,
        val cookingMeal: Meal? = null,
        val currentPreparationStep: Int = 0
    ) : MealsUiState
    data class Error(val message: String) : MealsUiState
}

sealed interface MealsUiEvent {
    data class SelectMeal(val meal: Meal?) : MealsUiEvent
    data class SetAddingMeal(val isAdding: Boolean) : MealsUiEvent
    data class AddCapturedMeal(val imageUri: String) : MealsUiEvent
    data class DeleteMeal(val mealId: String) : MealsUiEvent
    data class EditMeal(val meal: Meal?) : MealsUiEvent
    data class UpdateMeal(val meal: Meal) : MealsUiEvent
    data class StartCooking(val meal: Meal?) : MealsUiEvent
    data class SetPreparationStep(val index: Int) : MealsUiEvent
}
