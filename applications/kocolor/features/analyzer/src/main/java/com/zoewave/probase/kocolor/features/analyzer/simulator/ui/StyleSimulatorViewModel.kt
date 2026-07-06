package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.features.analyzer.simulator.data.StyleSimulatorEngine
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.kocolor.model.KoColorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
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
    val userMessage: String = "",
    val rationale: String? = null,
    val isLocalResult: Boolean = false,
    val userPortraitUri: String? = null
)

enum class SimulationStep {
    MESSAGING, BIO_MARKERS, ROUTINE, GENERATING, RESULT
}

sealed class SimulatorEvent {
    data class UpdateMessage(val message: String) : SimulatorEvent()
    data object StartSimulation : SimulatorEvent()
    data object SaveToPalette : SimulatorEvent()
    data object Reset : SimulatorEvent()
    data object CapturePortrait : SimulatorEvent()
}

sealed class SimulatorEffect {
    data object NavigateToHistory : SimulatorEffect()
    data class NavigateToCamera(val target: String) : SimulatorEffect()
}

@HiltViewModel
class StyleSimulatorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val routineDao: RoutineDao,
    private val wardrobeRepository: WardrobeRepository,
    private val fashionRepository: com.zoewave.probase.kocolor.data.FashionRepository,
    private val sessionRepository: FashionSessionRepository,
    private val simulatorEngine: StyleSimulatorEngine,
    private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _uiState = MutableStateFlow(StyleSimulatorUiState())
    val uiState: StateFlow<StyleSimulatorUiState> = _uiState.asStateFlow()

    private val _effect = kotlinx.coroutines.channels.Channel<SimulatorEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        checkRoutineStatus()
        observePortrait()
    }

    private fun observePortrait() {
        viewModelScope.launch {
            sessionRepository.faceUri.collect { uri ->
                _uiState.update { it.copy(userPortraitUri = uri) }
            }
        }
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
            SimulatorEvent.CapturePortrait -> {
                viewModelScope.launch {
                    _effect.send(SimulatorEffect.NavigateToCamera("face_simulator"))
                }
            }
        }
    }

    private fun runSimulation() {
        viewModelScope.launch {
            val apiKey = aiSettings.getGeminiApiKey()
            val userIntent = uiState.value.userMessage
            
            // 1. Manifest Pre-Filtering
            val filteredWardrobe = wardrobeRepository.getShortlistByIntent(userIntent).first()

            // 2. Biological Skin Anchoring
            val profile = fashionRepository.getProfile().first()
            val skinContext = profile?.let { 
                "Undertone: ${it.undertone}, Seasonal Type: ${it.seasonalType}"
            } ?: "Unknown"

            // 3. User Portrait Retrieval (Multimodal Anchor)
            val portraitUri = sessionRepository.faceUri.value
            val userPortrait = portraitUri?.let { uri ->
                loadBitmapFromUri(Uri.parse(uri))
            }

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
                userPortrait = userPortrait,
                apiKey = apiKey
            )
            
            val isLocal = apiKey.isNullOrBlank() || blueprint.rationale.startsWith("Local Architect")
            val selectedItems = filteredWardrobe.filter { it.id in blueprint.selectedItemIds }

            // Recycle portrait bitmap after AI analysis to save memory
            userPortrait?.recycle()

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

    private fun loadBitmapFromUri(uri: Uri): android.graphics.Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            android.graphics.BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) { null }
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
                seasonalType = (fashionRepository.getProfile().first()?.seasonalType) ?: SeasonalType.WINTER,
                undertone = (fashionRepository.getProfile().first()?.undertone) ?: Undertone.COOL,
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
