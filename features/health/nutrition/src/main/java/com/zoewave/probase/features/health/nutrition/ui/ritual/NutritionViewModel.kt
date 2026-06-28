package com.zoewave.probase.features.health.nutrition.ui.ritual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.health.nutrition.data.NutritionDefaults
import com.zoewave.probase.core.model.ritual.BeautyRoutine
import com.zoewave.probase.core.model.ritual.RoutineStep
import com.zoewave.probase.core.model.ritual.RoutineTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<NutritionUiState>(NutritionUiState.Loading)
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    private var currentRoutine = BeautyRoutine(
        title = "Meals Routine",
        time = RoutineTime.MEALS,
        steps = NutritionDefaults.getUnifiedRoutine().stages.mapIndexed { index, stage ->
            RoutineStep(
                id = stage.id,
                title = stage.title,
                subtitle = stage.subtitle,
                description = stage.scientificBody,
                actionLabel = stage.suggestedMealBody,
                isCompleted = stage.isCompleted,
                layeringOrder = index
            )
        },
        date = System.currentTimeMillis()
    )

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val nextWindow = calculateNextWindow()
            _uiState.value = NutritionUiState.Success(
                routine = currentRoutine,
                nextMetabolicWindow = nextWindow
            )
        }
    }

    private fun calculateNextWindow(): String? {
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val stages = NutritionDefaults.getUnifiedRoutine().stages
        
        return stages.find { 
            LocalTime.parse(it.startTime, formatter).isAfter(now)
        }?.startTime?.let {
            val time = LocalTime.parse(it, formatter)
            time.format(DateTimeFormatter.ofPattern("h:mm a"))
        }
    }

    fun onEvent(event: NutritionUiEvent) {
        when (event) {
            is NutritionUiEvent.ToggleStage -> toggleStage(event.stageId)
            NutritionUiEvent.Refresh -> loadData()
        }
    }

    private fun toggleStage(stageId: String) {
        val updatedSteps = currentRoutine.steps.map {
            if (it.id == stageId) it.copy(isCompleted = !it.isCompleted) else it
        }
        currentRoutine = currentRoutine.copy(steps = updatedSteps)
        loadData()
    }
}
