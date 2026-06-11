package com.zoewave.probase.features.health.meals.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.health.meals.data.Meal
import com.zoewave.probase.features.health.meals.data.MealsRepository
import com.zoewave.probase.features.health.meals.data.MetabolicPhase
import com.zoewave.probase.features.health.meals.data.NutritionInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MealsViewModel @Inject constructor(
    private val repository: MealsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MealsUiState>(MealsUiState.Loading)
    val uiState: StateFlow<MealsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.meals.collectLatest { meals ->
                _uiState.update { currentState ->
                    if (currentState is MealsUiState.Success) {
                        currentState.copy(meals = meals)
                    } else {
                        MealsUiState.Success(meals = meals)
                    }
                }
            }
        }
    }

    fun onEvent(event: MealsUiEvent) {
        when (event) {
            is MealsUiEvent.SelectMeal -> selectMeal(event.meal)
            is MealsUiEvent.SetAddingMeal -> setAddingMeal(event.isAdding)
            is MealsUiEvent.AddCapturedMeal -> addCapturedMeal(event.imageUri)
            is MealsUiEvent.DeleteMeal -> deleteMeal(event.mealId)
            is MealsUiEvent.EditMeal -> setEditingMeal(event.meal)
            is MealsUiEvent.UpdateMeal -> updateMeal(event.meal)
            is MealsUiEvent.StartCooking -> setCookingMeal(event.meal)
            is MealsUiEvent.SetPreparationStep -> setPreparationStep(event.index)
        }
    }

    private fun selectMeal(meal: Meal?) {
        _uiState.update { state ->
            if (state is MealsUiState.Success) {
                state.copy(selectedMeal = meal)
            } else state
        }
    }

    private fun setAddingMeal(isAdding: Boolean) {
        _uiState.update { state ->
            if (state is MealsUiState.Success) {
                state.copy(isAddingMeal = isAdding)
            } else state
        }
    }

    private fun setEditingMeal(meal: Meal?) {
        _uiState.update { state ->
            if (state is MealsUiState.Success) {
                state.copy(editingMeal = meal)
            } else state
        }
    }

    private fun deleteMeal(mealId: String) {
        repository.deleteMeal(mealId)
        _uiState.update { state ->
            if (state is MealsUiState.Success) {
                state.copy(selectedMeal = null)
            } else state
        }
    }

    private fun updateMeal(meal: Meal) {
        repository.updateMeal(meal)
        setEditingMeal(null)
        selectMeal(meal)
    }

    private fun setCookingMeal(meal: Meal?) {
        _uiState.update { state ->
            if (state is MealsUiState.Success) {
                state.copy(cookingMeal = meal, currentPreparationStep = 0)
            } else state
        }
    }

    private fun setPreparationStep(index: Int) {
        _uiState.update { state ->
            if (state is MealsUiState.Success) {
                val nextIndex = index.coerceIn(0, (state.cookingMeal?.steps?.size ?: 1) - 1)
                state.copy(currentPreparationStep = nextIndex)
            } else state
        }
    }

    private fun addCapturedMeal(imageUri: String) {
        val newMeal = Meal(
            id = UUID.randomUUID().toString(),
            name = "Captured Meal",
            description = "A new meal added via camera.",
            scientificFocus = "Pending Analysis",
            phase = MetabolicPhase.MidDay,
            imageUrl = imageUri,
            nutrition = NutritionInfo(0, 0f, 0f, 0f),
            ingredients = emptyList(),
            steps = emptyList()
        )
        repository.addMeal(newMeal)
        setAddingMeal(false)
    }
}
