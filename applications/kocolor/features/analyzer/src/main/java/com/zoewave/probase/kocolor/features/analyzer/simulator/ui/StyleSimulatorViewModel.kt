package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StyleSimulatorUiState(
    val morningRoutineCompleted: Boolean = false,
    val circadianContext: String = "Defense & Protection",
    val wellnessScore: Double = 0.85,
    val recommendedPalette: List<String> = emptyList(),
    val recommendedClothing: List<ClothingItem> = emptyList(),
    val recommendedAccessories: List<ClothingItem> = emptyList(),
    val isAnalyzing: Boolean = false,
    val simulationStep: SimulationStep = SimulationStep.MESSAGING,
    val userMessage: String = ""
)

enum class SimulationStep {
    MESSAGING, BIO_MARKERS, ROUTINE, GENERATING, RESULT
}

sealed class SimulatorEvent {
    data class UpdateMessage(val message: String) : SimulatorEvent()
    data object StartSimulation : SimulatorEvent()
    data object SaveToPalette : SimulatorEvent()
    data object Reset : SimulatorEvent()
}

@HiltViewModel
class StyleSimulatorViewModel @Inject constructor(
    private val routineDao: RoutineDao,
    private val clothingDao: ClothingDao,
    private val cosmeticDao: CosmeticDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(StyleSimulatorUiState())
    val uiState: StateFlow<StyleSimulatorUiState> = _uiState.asStateFlow()

    init {
        checkRoutineStatus()
    }

    private fun checkRoutineStatus() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            routineDao.getRoutinesForDay(getStartOfDay(now), getStartOfDay(now) + 86400000L)
                .map { routines ->
                    routines.find { it.time == RoutineTime.MORNING }?.steps?.all { it.isCompleted } ?: false
                }.collect { completed ->
                    _uiState.update { it.copy(morningRoutineCompleted = completed) }
                }
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun onEvent(event: SimulatorEvent) {
        when (event) {
            is SimulatorEvent.UpdateMessage -> _uiState.update { it.copy(userMessage = event.message) }
            SimulatorEvent.StartSimulation -> runSimulation()
            SimulatorEvent.SaveToPalette -> saveSelectionToColorTab()
            SimulatorEvent.Reset -> _uiState.value = StyleSimulatorUiState()
        }
    }

    private fun runSimulation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, simulationStep = SimulationStep.BIO_MARKERS) }
            kotlinx.coroutines.delay(1500)
            _uiState.update { it.copy(simulationStep = SimulationStep.ROUTINE) }
            kotlinx.coroutines.delay(1500)
            _uiState.update { it.copy(simulationStep = SimulationStep.GENERATING) }
            
            val allClothing = clothingDao.getAllClothing().first().map { it.toModel() }
            
            _uiState.update { state ->
                state.copy(
                    isAnalyzing = false,
                    simulationStep = SimulationStep.RESULT,
                    recommendedPalette = listOf("#F4D03F", "#16A085", "#2C3E50"),
                    recommendedClothing = allClothing.filter { it.category == ClothingCategory.TOPS }.take(1) +
                                         allClothing.filter { it.category == ClothingCategory.BOTTOMS }.take(1) +
                                         allClothing.filter { it.category == ClothingCategory.SHOES }.take(1),
                    recommendedAccessories = allClothing.filter { it.category == ClothingCategory.ACCESSORIES }.take(2)
                )
            }
        }
    }

    private fun saveSelectionToColorTab() {
        // Implementation logic
    }

    private fun com.zoewave.probase.kocolor.db.entity.ClothingItemEntity.toModel() = ClothingItem(
        id = id, name = name, brand = brand, category = category, colorHex = colorHex, price = price, timestamp = timestamp
    )
}
