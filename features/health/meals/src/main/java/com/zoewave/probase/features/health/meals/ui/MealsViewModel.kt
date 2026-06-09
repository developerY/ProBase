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

    fun selectMeal(meal: Meal?) {
        _uiState.update { state ->
            if (state is MealsUiState.Success) {
                state.copy(selectedMeal = meal)
            } else state
        }
    }

    fun setAddingMeal(isAdding: Boolean) {
        _uiState.update { state ->
            if (state is MealsUiState.Success) {
                state.copy(isAddingMeal = isAdding)
            } else state
        }
    }

    fun addCapturedMeal(imageUri: String) {
        // In a real app, we might use AI to analyze the image.
        // For now, we'll add a placeholder meal with the captured image.
        val newMeal = Meal(
            id = UUID.randomUUID().toString(),
            name = "Captured Meal",
            description = "A new meal added via camera.",
            scientificFocus = "Pending Analysis",
            phase = MetabolicPhase.MidDay, // Default to MidDay or detect based on time
            imageUrl = imageUri,
            nutrition = NutritionInfo(0, 0f, 0f, 0f), // Placeholders
            ingredients = emptyList(),
            steps = emptyList()
        )
        repository.addMeal(newMeal)
        setAddingMeal(false)
    }
}
