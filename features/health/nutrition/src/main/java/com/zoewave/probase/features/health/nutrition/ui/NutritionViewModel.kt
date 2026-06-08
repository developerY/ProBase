package com.zoewave.probase.features.health.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.health.nutrition.data.NutritionDefaults
import com.zoewave.probase.features.health.nutrition.data.NutritionRoutine
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

    private var currentRoutine = NutritionDefaults.getUnifiedRoutine()

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
        
        return currentRoutine.stages.find { 
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
        val updatedStages = currentRoutine.stages.map {
            if (it.id == stageId) it.copy(isCompleted = !it.isCompleted) else it
        }
        currentRoutine = currentRoutine.copy(stages = updatedStages)
        loadData()
    }
}
