package com.zoewave.probase.kocolor.features.analyzer.simulator.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.core.data.repository.weather.AtmosphericRepository
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.ColorFamily
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.FashionAdvice
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MakeupSuggestion
import com.zoewave.probase.core.model.ritual.OutfitSuggestion
import com.zoewave.probase.core.model.ritual.RoutineTime
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.SuggestedPiece
import com.zoewave.probase.core.model.ritual.Undertone
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.data.repository.RotationRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.usecase.GeneratePlaylistUseCase
import com.zoewave.probase.kocolor.data.usecase.RotationScoringUseCase
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleSimulatorEngine
import com.zoewave.probase.kocolor.features.analyzer.calibration.ColorSeasonClassifier
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.model.calibration.ColorProfile
import com.zoewave.probase.kocolor.model.calibration.FacialContrastVector
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.ResultTab
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.VisualBlueprintData
import com.zoewave.probase.kocolor.features.analyzer.simulator.ui.components.graphics.mapToVisualBlueprintData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

data class StyleSimulatorUiState(
    val morningRoutineCompleted: Boolean = false,
    val circadianContext: String = "Defense & Protection",
    val wellnessScore: Double = 0.85,
    val recommendedPalette: List<String> = emptyList(),
    val recommendedClothing: List<ClothingItem> = emptyList(),
    val recommendedCosmetics: List<CosmeticItem> = emptyList(),
    val isAnalyzing: Boolean = false,
    val simulationStep: SimulationStep = SimulationStep.MESSAGING,
    val userMessage: String = "",
    val rationale: String? = null,
    val isLocalResult: Boolean = false,
    val fashionProfileLabel: String? = null,
    val userPortraitUri: String? = null,
    val fullClothingInventory: List<ClothingItem> = emptyList(),
    val fullCosmeticInventory: List<CosmeticItem> = emptyList(),
    val selectedClothingCategory: ClothingCategory = ClothingCategory.TOPS,
    val selectedCosmeticCategory: MacroCategory = MacroCategory.LIPS,
    
    // Grouped items by color family
    val clothingFamilies: Map<ColorFamily, List<ClothingItem>> = emptyMap(),
    val cosmeticFamilies: Map<ColorFamily, List<CosmeticItem>> = emptyMap(),
    
    // Family-based anchors (Constraint set by user)
    val anchoredClothingFamilies: Map<ClothingCategory, ColorFamily> = emptyMap(),
    val anchoredCosmeticFamilies: Map<MacroCategory, ColorFamily> = emptyMap(),
    
    val selectedResultTab: ResultTab = ResultTab.CLOTHES,
    val visualBlueprintData: VisualBlueprintData = VisualBlueprintData()
)

enum class SimulationStep {
    MESSAGING, BIO_MARKERS, ROUTINE, GENERATING, RESULT
}

sealed class SimulatorEvent {
    data class UpdateMessage(val message: String) : SimulatorEvent()
    data object StartSimulation : SimulatorEvent()
    data object GeneratePlaylist : SimulatorEvent()
    data object SaveToPalette : SimulatorEvent()
    data object Reset : SimulatorEvent()
    data object CapturePortrait : SimulatorEvent()
    data object PickPortrait : SimulatorEvent()
    data class OnPortraitSelected(val uri: String) : SimulatorEvent()
    data class ToggleClothingFamily(val category: ClothingCategory, val family: ColorFamily) : SimulatorEvent()
    data class ToggleCosmeticFamily(val category: MacroCategory, val family: ColorFamily) : SimulatorEvent()
    data class SelectClothingCategory(val category: ClothingCategory) : SimulatorEvent()
    data class SelectCosmeticCategory(val category: MacroCategory) : SimulatorEvent()
    data class SelectResultTab(val tab: ResultTab) : SimulatorEvent()
}

sealed class SimulatorEffect {
    data object NavigateToHistory : SimulatorEffect()
    data object NavigateToPlaylist : SimulatorEffect()
    data class NavigateToCamera(val target: String) : SimulatorEffect()
    data object OpenGalleryPicker : SimulatorEffect()
}

@HiltViewModel
class StyleSimulatorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val routineDao: RoutineDao,
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val fashionRepository: FashionRepository,
    private val sessionRepository: FashionSessionRepository,
    private val simulatorEngine: StyleSimulatorEngine,
    private val generatePlaylistUseCase: GeneratePlaylistUseCase,
    private val atmosphericRepository: AtmosphericRepository,
    private val rotationRepository: RotationRepository,
    private val rotationScoringUseCase: RotationScoringUseCase,
    private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _selectedClothingCategory = MutableStateFlow(ClothingCategory.TOPS)
    private val _selectedCosmeticCategory = MutableStateFlow(MacroCategory.LIPS)
    private val _userMessage = MutableStateFlow("")
    private val _anchoredClothingFamilies = MutableStateFlow<Map<ClothingCategory, ColorFamily>>(emptyMap())
    private val _anchoredCosmeticFamilies = MutableStateFlow<Map<MacroCategory, ColorFamily>>(emptyMap())
    private val _simulationStep = MutableStateFlow(SimulationStep.MESSAGING)
    private val _simulationResult = MutableStateFlow<StyleBlueprint?>(null)
    private val _selectedResultTab = MutableStateFlow(ResultTab.CLOTHES)

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    )
    
    private val seasonClassifier = ColorSeasonClassifier()

    val uiState: StateFlow<StyleSimulatorUiState> = combine(
        sessionRepository.faceUri,
        fashionRepository.getProfile(),
        wardrobeRepository.getAllClothing(),
        cosmeticRepository.getAllCosmetics(),
        _selectedClothingCategory,
        _selectedCosmeticCategory,
        _userMessage,
        _anchoredClothingFamilies,
        _anchoredCosmeticFamilies,
        _simulationStep,
        _simulationResult,
        _selectedResultTab
    ) { array ->
        val faceUri = array[0] as String?
        val profile = array[1] as FashionProfile?
        val allClothing = array[2] as List<ClothingItem>
        val allCosmetics = array[3] as List<CosmeticItem>
        val selectedClothingCat = array[4] as ClothingCategory
        val selectedCosmeticCat = array[5] as MacroCategory
        val userMsg = array[6] as String
        val anchoredClothing = array[7] as Map<ClothingCategory, ColorFamily>
        val anchoredCosmetics = array[8] as Map<MacroCategory, ColorFamily>
        val step = array[9] as SimulationStep
        val result = array[10] as StyleBlueprint?
        val resultTab = array[11] as ResultTab

        val clothingFamilies = allClothing.filter { it.category == selectedClothingCat }
            .groupBy { it.colorFamily }
        
        val cosmeticFamilies = allCosmetics.filter { it.macroCategory == selectedCosmeticCat }
            .groupBy { it.colorFamily }

        val recommendedClothing = allClothing.filter { item ->
            "w_${item.internalId}" in (result?.selectedClothingIds ?: emptyList())
        }
        val recommendedCosmetics = allCosmetics.filter { item ->
            "c_${item.internalId}" in (result?.selectedCosmeticIds ?: emptyList())
        }

        StyleSimulatorUiState(
            userPortraitUri = faceUri,
            fullClothingInventory = allClothing,
            fullCosmeticInventory = allCosmetics,
            selectedClothingCategory = selectedClothingCat,
            selectedCosmeticCategory = selectedCosmeticCat,
            userMessage = userMsg,
            anchoredClothingFamilies = anchoredClothing,
            anchoredCosmeticFamilies = anchoredCosmetics,
            clothingFamilies = clothingFamilies,
            cosmeticFamilies = cosmeticFamilies,
            simulationStep = step,
            rationale = result?.rationale,
            recommendedPalette = result?.recommendedPalette ?: emptyList(),
            recommendedClothing = recommendedClothing,
            recommendedCosmetics = recommendedCosmetics,
            isLocalResult = result?.rationale?.startsWith("Local Architect") ?: false,
            fashionProfileLabel = profile?.let { "${it.undertone} ${it.seasonalType}" },
            selectedResultTab = resultTab,
            visualBlueprintData = mapToVisualBlueprintData(
                cosmetics = recommendedCosmetics,
                clothing = recommendedClothing,
                palette = result?.recommendedPalette ?: emptyList()
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StyleSimulatorUiState()
    )

    private val _effect = Channel<SimulatorEffect>()
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
                }
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun onEvent(event: SimulatorEvent) {
        when (event) {
            is SimulatorEvent.UpdateMessage -> _userMessage.value = event.message
            SimulatorEvent.StartSimulation -> runSimulation()
            SimulatorEvent.GeneratePlaylist -> {
                viewModelScope.launch {
                    _simulationStep.value = SimulationStep.GENERATING
                    generatePlaylistUseCase.generateWeeklyPlaylist(LocalDate.now())
                    _effect.send(SimulatorEffect.NavigateToPlaylist)
                    _simulationStep.value = SimulationStep.MESSAGING // Reset for next time
                }
            }
            SimulatorEvent.SaveToPalette -> saveSelectionToColorTab()
            SimulatorEvent.Reset -> {
                _userMessage.value = ""
                _anchoredClothingFamilies.value = emptyMap()
                _anchoredCosmeticFamilies.value = emptyMap()
                _simulationStep.value = SimulationStep.MESSAGING
                _simulationResult.value = null
                _selectedResultTab.value = ResultTab.CLOTHES
            }
            SimulatorEvent.CapturePortrait -> {
                viewModelScope.launch {
                    _effect.send(SimulatorEffect.NavigateToCamera("face_simulator"))
                }
            }
            SimulatorEvent.PickPortrait -> {
                viewModelScope.launch {
                    _effect.send(SimulatorEffect.OpenGalleryPicker)
                }
            }
            is SimulatorEvent.OnPortraitSelected -> {
                sessionRepository.setFaceUri(event.uri)
                establishProfileFromPortrait(event.uri)
            }
            is SimulatorEvent.ToggleClothingFamily -> {
                val current = _anchoredClothingFamilies.value[event.category]
                val nextMap = _anchoredClothingFamilies.value.toMutableMap()
                if (current == event.family) {
                    nextMap.remove(event.category)
                } else {
                    nextMap[event.category] = event.family
                }
                _anchoredClothingFamilies.value = nextMap
            }
            is SimulatorEvent.ToggleCosmeticFamily -> {
                val current = _anchoredCosmeticFamilies.value[event.category]
                val nextMap = _anchoredCosmeticFamilies.value.toMutableMap()
                if (current == event.family) {
                    nextMap.remove(event.category)
                } else {
                    nextMap[event.category] = event.family
                }
                _anchoredCosmeticFamilies.value = nextMap
            }
            is SimulatorEvent.SelectClothingCategory -> {
                _selectedClothingCategory.value = event.category
            }
            is SimulatorEvent.SelectCosmeticCategory -> {
                _selectedCosmeticCategory.value = event.category
            }
            is SimulatorEvent.SelectResultTab -> {
                _selectedResultTab.value = event.tab
            }
        }
    }

    private fun runSimulation() {
        viewModelScope.launch {
            val apiKey = aiSettings.getGeminiApiKey()
            val state = uiState.value
            val userIntent = state.userMessage
            
            val filteredWardrobe = wardrobeRepository.getShortlistByIntent(userIntent).first()
            val allCosmetics = state.fullCosmeticInventory

            val profile = fashionRepository.getProfile().first()
            val skinContext = profile?.let { 
                "Undertone: ${it.undertone}, Seasonal Type: ${it.seasonalType}"
            } ?: "Unknown"
            
            val weather = atmosphericRepository.atmosphericState.value
            val weatherContext = "UV: ${weather.environmentalContext?.uvIndex ?: "Unknown"}, Temp: ${weather.weather?.main?.temp ?: "Unknown"}C"

            val anchoredClothing = state.anchoredClothingFamilies.flatMap { (cat, family) ->
                state.fullClothingInventory.filter { it.category == cat && it.colorFamily == family }
            }
            val anchoredCosmetics = state.anchoredCosmeticFamilies.flatMap { (cat, family) ->
                state.fullCosmeticInventory.filter { it.macroCategory == cat && it.colorFamily == family }
            }

            val preferredModel = aiSettings.aiModelFlow.first()

            // 1. Calculate Rotation Scores for all items in availableWardrobe
            val rotationScores = filteredWardrobe.associate { item ->
                item.remoteId!! to rotationScoringUseCase.calculateRotationPenalty(item.remoteId!!, item.category.name)
            }

            _simulationStep.value = SimulationStep.BIO_MARKERS
            delay(1000)
            _simulationStep.value = SimulationStep.ROUTINE
            delay(1000)
            _simulationStep.value = SimulationStep.GENERATING
            
            val blueprint = simulatorEngine.architectStyleBlueprint(
                userIntent = userIntent,
                circadianContext = "Defense & Protection",
                routineCompleted = false,
                wellnessScore = 0.85,
                weatherContext = weatherContext,
                availableWardrobe = filteredWardrobe,
                availableCosmetics = allCosmetics,
                rotationScores = rotationScores,
                fashionProfile = skinContext,
                anchoredClothing = anchoredClothing,
                anchoredCosmetics = anchoredCosmetics,
                apiKey = apiKey,
                modelName = preferredModel
            )
            
            // Translate Rationale: Swap <ITEM:id> tags for rich names
            val translatedRationale = translateRationale(
                blueprint.rationale,
                state.fullClothingInventory,
                state.fullCosmeticInventory
            )

            _simulationResult.value = blueprint.copy(rationale = translatedRationale)
            _simulationStep.value = SimulationStep.RESULT
        }
    }

    private fun translateRationale(
        rawRationale: String,
        clothing: List<ClothingItem>,
        cosmetics: List<CosmeticItem>
    ): String {
        // Matches <ITEM:w_16> or <ITEM:c_5>
        val tagPattern = "<ITEM:([wc])_(\\d+)>".toRegex()
        var translated = rawRationale
        
        tagPattern.findAll(rawRationale).forEach { match ->
            val fullTag = match.value
            val domain = match.groupValues[1] // 'w' or 'c'
            val id = match.groupValues[2].toLongOrNull() ?: return@forEach
            
            val richName = if (domain == "w") {
                clothing.find { it.internalId == id }?.let { 
                    "${it.brand ?: ""} ${it.name.removePrefix(it.brand ?: "")}".trim()
                }
            } else {
                cosmetics.find { it.internalId == id }?.let {
                    "${it.brand} ${it.name.removePrefix(it.brand)}".trim()
                }
            }
            
            translated = translated.replace(fullTag, richName ?: "this item")
        }
        return translated
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) { null }
    }

    private fun saveSelectionToColorTab() {
        viewModelScope.launch {
            val state = uiState.value
            
            // Fix: Wardrobe should ONLY contain clothing items
            val outfitSuggestion = OutfitSuggestion(
                occasion = state.userMessage,
                advice = state.rationale ?: "",
                keyPieces = state.recommendedClothing.map { it.name },
                colorCombinations = state.recommendedPalette,
                wardrobeItemIds = state.recommendedClothing.map { it.internalId },
                suggestedItems = state.recommendedClothing.map { item ->
                    SuggestedPiece(
                        name = item.name,
                        category = item.category.name,
                        colorHex = item.colorHex,
                        imageUrl = item.imageUrl,
                        description = item.notes ?: "AI architected selection.",
                        isOwned = true
                    )
                } + listOf(
                    SuggestedPiece(
                        name = "Atelier Silk Scarf",
                        category = "ACCESSORIES",
                        colorHex = "#8B4513",
                        description = "A limited edition pure silk scarf from the Atelier line.",
                        isOwned = false
                    )
                )
            )

            // Fix: Vanity should ONLY contain cosmetic items
            val makeupSuggestions = state.recommendedCosmetics.map { cosmetic ->
                MakeupSuggestion(
                    category = cosmetic.macroCategory.displayName,
                    advice = "Strategically selected to harmonize with your ${state.recommendedPalette.getOrNull(0) ?: "look"}.",
                    recommendedColors = cosmetic.colorHex?.let { listOf(it) } ?: emptyList(),
                    suggestedProductName = cosmetic.name,
                    suggestedProductImageUrl = cosmetic.imageUrl,
                    productId = cosmetic.internalId
                )
            }

            val advice = FashionAdvice(
                title = "The ${state.userMessage.take(15).ifBlank { "Personal" }} Collection",
                summary = state.rationale ?: "AI optimized style blueprint.",
                seasonalType = (fashionRepository.getProfile().first()?.seasonalType) ?: SeasonalType.WINTER,
                undertone = (fashionRepository.getProfile().first()?.undertone) ?: Undertone.COOL,
                makeupSuggestions = makeupSuggestions,
                outfitSuggestions = listOf(outfitSuggestion),
                recommendedPalette = state.recommendedPalette,
                clothesUri = state.recommendedClothing.firstOrNull()?.imageUrl 
                    ?: state.recommendedCosmetics.firstOrNull()?.imageUrl
            )
            fashionRepository.saveSuggestion(advice)
            
            // 3. Commit Rotation Metrics for selected items
            val committedIds = state.recommendedClothing.mapNotNull { it.remoteId }
            if (committedIds.isNotEmpty()) {
                rotationRepository.commitOutfitUsage(committedIds)
            }
            
            _effect.send(SimulatorEffect.NavigateToHistory)
        }
    }

    private fun establishProfileFromPortrait(uri: String) {
        viewModelScope.launch {
            val bitmap = loadBitmapFromUri(Uri.parse(uri)) ?: return@launch
            val image = InputImage.fromBitmap(bitmap, 0)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    if (faces.isNotEmpty()) {
                        val face = faces[0]
                        val cheekLandmark = face.getLandmark(FaceLandmark.LEFT_CHEEK) ?: face.getLandmark(FaceLandmark.RIGHT_CHEEK)
                        val skinLuminance = cheekLandmark?.let { sampleLuminance(bitmap, it.position.x.toInt(), it.position.y.toInt()) } ?: 0.5f

                        val eyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE) ?: face.getLandmark(FaceLandmark.RIGHT_EYE)
                        val eyeLuminance = eyeLandmark?.let { sampleLuminance(bitmap, it.position.x.toInt(), it.position.y.toInt()) } ?: 0.2f

                        val hairLuminance = sampleLuminance(bitmap, face.boundingBox.centerX(), (face.boundingBox.top - 20).coerceAtLeast(0))
                        val contrastDelta = abs(skinLuminance - hairLuminance)
                        val undertone = estimateUndertone(bitmap, cheekLandmark?.position?.x?.toInt() ?: face.boundingBox.centerX(), cheekLandmark?.position?.y?.toInt() ?: face.boundingBox.centerY())

                        val vector = FacialContrastVector(skinLuminance, hairLuminance, eyeLuminance, contrastDelta)
                        val season = seasonClassifier.classify(vector, undertone)
                        
                        val profile = ColorProfile(
                            season = season,
                            undertone = undertone,
                            contrastVector = vector,
                            optimalPaletteHexCodes = seasonClassifier.getOptimalPalette(season)
                        )

                        viewModelScope.launch {
                            fashionRepository.saveProfile(profile.toFashionProfile())
                        }
                    }
                }
                .addOnCompleteListener {
                    bitmap.recycle()
                }
        }
    }

    private fun sampleLuminance(bitmap: Bitmap, x: Int, y: Int): Float {
        if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) return 0.5f
        val pixel = bitmap.getPixel(x, y)
        return (0.2126f * Color.red(pixel) + 0.7152f * Color.green(pixel) + 0.0722f * Color.blue(pixel)) / 255f
    }

    private fun estimateUndertone(bitmap: Bitmap, x: Int, y: Int): Float {
        if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) return 0f
        val pixel = bitmap.getPixel(x, y)
        val r = Color.red(pixel)
        val b = Color.blue(pixel)
        return ((r - b).toFloat() / 255f).coerceIn(-1.0f, 1.0f)
    }
}
