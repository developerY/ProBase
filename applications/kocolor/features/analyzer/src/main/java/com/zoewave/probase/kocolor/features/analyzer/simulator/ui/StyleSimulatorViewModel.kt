package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.features.analyzer.simulator.data.StyleSimulatorEngine
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.kocolor.model.KoColorRoute
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

sealed class SimulatorEffect {
    data object NavigateToHistory : SimulatorEffect()
}

@HiltViewModel
class StyleSimulatorViewModel @Inject constructor(
    private val routineDao: RoutineDao,
    private val wardrobeRepository: WardrobeRepository,
    private val fashionRepository: com.zoewave.probase.kocolor.data.FashionRepository,
    private val simulatorEngine: StyleSimulatorEngine,
    private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(StyleSimulatorUiState())
    val uiState: StateFlow<StyleSimulatorUiState> = _uiState.asStateFlow()

    private val _effect = kotlinx.coroutines.channels.Channel<SimulatorEffect>()
    val effect = _effect.receiveAsFlow()

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
            val userIntent = uiState.value.userMessage
            
            // 1. Manifest Pre-Filtering (Stage 2)
            val filteredWardrobe = wardrobeRepository.getShortlistByIntent(userIntent).first()

            // 2. Biological Skin Anchoring (Stage 3)
            val profile = fashionRepository.getProfile().first()
            val skinContext = profile?.let { 
                "Undertone: ${it.undertone}, Seasonal Type: ${it.seasonalType}"
            } ?: "Unknown"

            _uiState.update { it.copy(isAnalyzing = true, simulationStep = SimulationStep.BIO_MARKERS) }
            delay(1000)
            _uiState.update { it.copy(simulationStep = SimulationStep.ROUTINE) }
            delay(1000)
            _uiState.update { it.copy(simulationStep = SimulationStep.GENERATING) }
            
            val blueprint = simulatorEngine.architectStyleBlueprint(
                userIntent = userIntent,
                circadianContext = uiState.value.circadianContext,
                routineCompleted = uiState.value.morningRoutineCompleted,
                wellnessScore = uiState.value.wellnessScore,
                availableWardrobe = filteredWardrobe,
                fashionProfile = skinContext,
                apiKey = apiKey
            )
            
            val isLocal = apiKey.isNullOrBlank() || blueprint.rationale.startsWith("Local Architect")
            val selectedItems = filteredWardrobe.filter { it.id in blueprint.selectedItemIds }

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
        viewModelScope.launch {
            val state = uiState.value
            val outfitSuggestion = OutfitSuggestion(
                occasion = state.userMessage,
                advice = state.rationale ?: "",
                keyPieces = (state.recommendedClothing + state.recommendedAccessories).map { it.name },
                colorCombinations = state.recommendedPalette,
                wardrobeItemIds = (state.recommendedClothing + state.recommendedAccessories).map { it.id },
                suggestedItems = (state.recommendedClothing + state.recommendedAccessories).map { item ->
                    SuggestedPiece(
                        name = item.name,
                        category = item.category.name,
                        imageUrl = item.imageUrl,
                        description = item.notes,
                        isOwned = true
                    )
                } + listOf(
                    // Add a "Dream" item to show the power of the engine
                    SuggestedPiece(
                        name = "Atelier Silk Scarf",
                        category = "ACCESSORIES",
                        description = "A limited edition pure silk scarf from the Atelier line.",
                        isOwned = false
                    )
                )
            )

            val advice = FashionAdvice(
                title = "The ${state.userMessage.take(15)} Collection",
                summary = state.rationale ?: "AI optimized style blueprint.",
                seasonalType = SeasonalType.WINTER,
                undertone = Undertone.COOL,
                makeupSuggestions = listOf(
                    MakeupSuggestion(
                        category = "Lips",
                        advice = "A bold brick red to anchor the look.",
                        recommendedColors = listOf("#8B0000"),
                        suggestedProductName = "Terracotta Velvet Stain"
                    )
                ),
                outfitSuggestions = listOf(outfitSuggestion),
                recommendedPalette = state.recommendedPalette,
                clothesUri = state.recommendedClothing.firstOrNull()?.imageUrl
            )
            fashionRepository.saveSuggestion(advice)
            _effect.send(SimulatorEffect.NavigateToHistory)
        }
    }
}
