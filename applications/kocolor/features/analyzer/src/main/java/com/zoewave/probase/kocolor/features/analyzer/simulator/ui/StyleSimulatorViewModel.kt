package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.features.analyzer.simulator.data.StyleSimulatorEngine
import com.zoewave.probase.kocolor.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

data class StyleSimulatorUiState(
    val morningRoutineCompleted: Boolean = false,
    val circadianContext: String = "Defense & Protection",
    val wellnessScore: Double = 0.85,
    val recommendedPalette: List<String> = emptyList(),
    val recommendedClothing: List<ClothingItem> = emptyList(),
    val recommendedAccessories: List<ClothingItem> = emptyList(),
    val isAnalyzing: Boolean = false,
    val simulationStep: SimulationStep = SimulationStep.MESSAGING,
    val userMessage: String = "",
    val rationale: String? = null,
    val isLocalResult: Boolean = false
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
    private val wardrobeRepository: WardrobeRepository,
    private val simulatorEngine: StyleSimulatorEngine,
    @Named("KoColor") private val aiSettings: AiConfigurationSettings
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
            val apiKey = aiSettings.getGeminiApiKey()
            val allClothing = wardrobeRepository.getAllClothing().first()

            _uiState.update { it.copy(isAnalyzing = true, simulationStep = SimulationStep.BIO_MARKERS) }
            delay(1000)
            _uiState.update { it.copy(simulationStep = SimulationStep.ROUTINE) }
            delay(1000)
            _uiState.update { it.copy(simulationStep = SimulationStep.GENERATING) }
            
            val blueprint = if (apiKey.isNullOrBlank()) {
                simulatorEngine.architectLocalBlueprint(
                    userIntent = uiState.value.userMessage,
                    availableWardrobe = allClothing
                )
            } else {
                simulatorEngine.architectStyleBlueprint(
                    userIntent = uiState.value.userMessage,
                    circadianContext = uiState.value.circadianContext,
                    routineCompleted = uiState.value.morningRoutineCompleted,
                    wellnessScore = uiState.value.wellnessScore,
                    availableWardrobe = allClothing,
                    apiKey = apiKey
                )
            }
            
            val isLocal = apiKey.isNullOrBlank() || blueprint.rationale.startsWith("Local Architect")
            val selectedItems = allClothing.filter { it.id in blueprint.selectedItemIds }

            _uiState.update { state ->
                state.copy(
                    isAnalyzing = false,
                    simulationStep = SimulationStep.RESULT,
                    recommendedPalette = blueprint.recommendedPalette,
                    recommendedClothing = selectedItems.filter { it.category != ClothingCategory.ACCESSORIES },
                    recommendedAccessories = selectedItems.filter { it.category == ClothingCategory.ACCESSORIES },
                    rationale = blueprint.rationale,
                    isLocalResult = isLocal
                )
            }
        }
    }

    private fun saveSelectionToColorTab() {
        // Implementation logic
    }
}
