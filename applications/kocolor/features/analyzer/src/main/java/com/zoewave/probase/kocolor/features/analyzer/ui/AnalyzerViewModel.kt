package com.zoewave.probase.kocolor.features.analyzer.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.core.data.repository.travel.LocationRepository
import com.zoewave.probase.kocolor.data.FashionRepository
import com.zoewave.probase.kocolor.data.repository.FashionSessionRepository
import com.zoewave.probase.kocolor.features.analyzer.data.AnalyzerEngine
import com.zoewave.probase.kocolor.model.FashionAdvice
import com.zoewave.probase.kocolor.model.FashionProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

sealed class AnalyzerUiState {
    data object Idle : AnalyzerUiState()
    data class Loading(val logs: List<String> = emptyList()) : AnalyzerUiState()
    data class Success(val advice: FashionAdvice) : AnalyzerUiState()
    data class Error(val message: String) : AnalyzerUiState()
}

data class AnalyzerScreenUiState(
    val analyzerState: AnalyzerUiState = AnalyzerUiState.Idle,
    val faceUri: String? = null,
    val hairUri: String? = null,
    val shoesUri: String? = null,
    val clothesUri: String? = null,
    val selectedOccasion: String = "Work",
    val locationName: String? = null,
    val isLocating: Boolean = false
)

sealed class AnalyzerEvent {
    data class OnFaceCaptured(val uri: String) : AnalyzerEvent()
    data class OnHairCaptured(val uri: String) : AnalyzerEvent()
    data class OnShoesCaptured(val uri: String) : AnalyzerEvent()
    data class OnClothesCaptured(val uri: String) : AnalyzerEvent()
    data class OnOccasionSelected(val occasion: String) : AnalyzerEvent()
    data class OnLocationChanged(val location: String?) : AnalyzerEvent()
    data object OnDetectLocationClicked : AnalyzerEvent()
    data object OnAnalyzeClicked : AnalyzerEvent()
    data class OnSaveClicked(val advice: FashionAdvice) : AnalyzerEvent()
    data object OnResetClicked : AnalyzerEvent()
}

@HiltViewModel
class AnalyzerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyzerEngine: AnalyzerEngine,
    private val fashionRepository: FashionRepository,
    private val aiSettings: AiConfigurationSettings,
    private val sessionRepository: FashionSessionRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _analyzerState = MutableStateFlow<AnalyzerUiState>(AnalyzerUiState.Idle)
    private val _selectedOccasion = MutableStateFlow("Work")
    private val _manualLocation = MutableStateFlow<String?>(null)
    private val _isLocating = MutableStateFlow(false)

    val uiState: StateFlow<AnalyzerScreenUiState> = combine(
        _analyzerState,
        _selectedOccasion,
        _manualLocation,
        _isLocating,
        sessionRepository.faceUri,
        sessionRepository.hairUri,
        sessionRepository.shoesUri,
        sessionRepository.clothesUri
    ) { params ->
        val analyzerState = params[0] as AnalyzerUiState
        val occasion = params[1] as String
        val manualLoc = params[2] as String?
        val isLocating = params[3] as Boolean
        val faceUri = params[4] as String?
        val hairUri = params[5] as String?
        val shoesUri = params[6] as String?
        val clothesUri = params[7] as String?

        AnalyzerScreenUiState(
            analyzerState = analyzerState,
            faceUri = faceUri,
            hairUri = hairUri,
            shoesUri = shoesUri,
            clothesUri = clothesUri,
            selectedOccasion = occasion,
            locationName = manualLoc,
            isLocating = isLocating
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyzerScreenUiState()
    )

    fun onEvent(event: AnalyzerEvent) {
        when (event) {
            is AnalyzerEvent.OnFaceCaptured -> {
                sessionRepository.setFaceUri(event.uri)
            }
            is AnalyzerEvent.OnHairCaptured -> {
                sessionRepository.setHairUri(event.uri)
            }
            is AnalyzerEvent.OnShoesCaptured -> {
                sessionRepository.setShoesUri(event.uri)
            }
            is AnalyzerEvent.OnClothesCaptured -> {
                sessionRepository.setClothesUri(event.uri)
            }
            is AnalyzerEvent.OnOccasionSelected -> {
                _selectedOccasion.value = event.occasion
            }
            is AnalyzerEvent.OnLocationChanged -> {
                _manualLocation.value = event.location
            }
            is AnalyzerEvent.OnDetectLocationClicked -> {
                detectLocation()
            }
            is AnalyzerEvent.OnAnalyzeClicked -> {
                analyzePhotos()
            }
            is AnalyzerEvent.OnSaveClicked -> {
                saveAnalysis(event.advice)
            }
            is AnalyzerEvent.OnResetClicked -> {
                _analyzerState.value = AnalyzerUiState.Idle
                _selectedOccasion.value = "Work"
                _manualLocation.value = null
                sessionRepository.reset()
            }
        }
    }

    private fun detectLocation() {
        viewModelScope.launch {
            _isLocating.value = true
            locationRepository.updateLocation()
            val latLng = locationRepository.currentLocation.first()
            if (latLng != null) {
                _manualLocation.value = "GPS: ${latLng.latitude}, ${latLng.longitude}"
            }
            _isLocating.value = false
        }
    }

    private fun analyzePhotos() {
        viewModelScope.launch {
            val fUri = uiState.value.faceUri
            val hUri = uiState.value.hairUri
            val sUri = uiState.value.shoesUri
            val cUri = uiState.value.clothesUri
            val occasion = uiState.value.selectedOccasion
            val location = uiState.value.locationName

            if (fUri == null && hUri == null && sUri == null && cUri == null) {
                _analyzerState.value = AnalyzerUiState.Error("Please capture at least one image.")
                return@launch
            }

            _analyzerState.value = AnalyzerUiState.Loading(listOf("Initializing style analysis..."))
            
            val apiKey = aiSettings.isGeminiApiKeySetFlow.first().let { isSet ->
                if (isSet) aiSettings.getGeminiApiKey() ?: "" else ""
            }

            if (apiKey.isBlank()) {
                _analyzerState.value = AnalyzerUiState.Error("Gemini API Key is not configured. Please go to Settings.")
                return@launch
            }

            val modelName = aiSettings.aiModelFlow.first()

            try {
                val faceBitmap = fUri?.let { loadBitmapFromUri(Uri.parse(it)) }
                val hairBitmap = hUri?.let { loadBitmapFromUri(Uri.parse(it)) }
                val shoesBitmap = sUri?.let { loadBitmapFromUri(Uri.parse(it)) }
                val clothesBitmap = cUri?.let { loadBitmapFromUri(Uri.parse(it)) }

                val result = analyzerEngine.analyzeStyle(
                    face = faceBitmap,
                    hair = hairBitmap,
                    shoes = shoesBitmap,
                    clothes = clothesBitmap,
                    occasion = occasion,
                    location = location,
                    apiKey = apiKey,
                    modelName = modelName
                )
                _analyzerState.value = AnalyzerUiState.Success(result)
            } catch (e: Exception) {
                _analyzerState.value = AnalyzerUiState.Error("Analysis failed: ${e.localizedMessage}")
            }
        }
    }

    private fun saveAnalysis(advice: FashionAdvice) {
        viewModelScope.launch {
            val updatedAdvice = advice.copy(
                faceUri = sessionRepository.faceUri.value,
                hairUri = sessionRepository.hairUri.value,
                shoesUri = sessionRepository.shoesUri.value,
                clothesUri = sessionRepository.clothesUri.value
            )

            val profile = FashionProfile(
                seasonalType = updatedAdvice.seasonalType,
                undertone = updatedAdvice.undertone,
                notes = updatedAdvice.summary,
                recommendedPalette = updatedAdvice.recommendedPalette
            )
            fashionRepository.saveProfile(profile)
            fashionRepository.saveSuggestion(updatedAdvice)
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }
}
