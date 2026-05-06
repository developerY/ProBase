package com.zoewave.probase.kocolor.features.analyzer.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
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
    val nailUri: String? = null,
    val clothesUri: String? = null
)

sealed class AnalyzerEvent {
    data class OnFaceCaptured(val uri: String) : AnalyzerEvent()
    data class OnHairCaptured(val uri: String) : AnalyzerEvent()
    data class OnNailCaptured(val uri: String) : AnalyzerEvent()
    data class OnClothesCaptured(val uri: String) : AnalyzerEvent()
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
    private val sessionRepository: FashionSessionRepository
) : ViewModel() {

    private val _analyzerState = MutableStateFlow<AnalyzerUiState>(AnalyzerUiState.Idle)

    val uiState: StateFlow<AnalyzerScreenUiState> = combine(
        _analyzerState,
        sessionRepository.faceUri,
        sessionRepository.hairUri,
        sessionRepository.nailUri,
        sessionRepository.clothesUri
    ) { analyzerState, faceUri, hairUri, nailUri, clothesUri ->
        AnalyzerScreenUiState(
            analyzerState = analyzerState,
            faceUri = faceUri,
            hairUri = hairUri,
            nailUri = nailUri,
            clothesUri = clothesUri
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
            is AnalyzerEvent.OnNailCaptured -> {
                sessionRepository.setNailUri(event.uri)
            }
            is AnalyzerEvent.OnClothesCaptured -> {
                sessionRepository.setClothesUri(event.uri)
            }
            is AnalyzerEvent.OnAnalyzeClicked -> {
                analyzePhotos()
            }
            is AnalyzerEvent.OnSaveClicked -> {
                saveAnalysis(event.advice)
            }
            is AnalyzerEvent.OnResetClicked -> {
                _analyzerState.value = AnalyzerUiState.Idle
                sessionRepository.reset()
            }
        }
    }

    private fun analyzePhotos() {
        viewModelScope.launch {
            val fUri = uiState.value.faceUri
            val hUri = uiState.value.hairUri
            val nUri = uiState.value.nailUri
            val cUri = uiState.value.clothesUri

            if (fUri == null && hUri == null && nUri == null && cUri == null) {
                _analyzerState.value = AnalyzerUiState.Error("Please capture at least one image.")
                return@launch
            }

            _analyzerState.value = AnalyzerUiState.Loading(listOf("Initializing analysis..."))
            
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
                val nailBitmap = nUri?.let { loadBitmapFromUri(Uri.parse(it)) }
                val clothesBitmap = cUri?.let { loadBitmapFromUri(Uri.parse(it)) }

                val result = analyzerEngine.analyzeStyle(
                    face = faceBitmap,
                    hair = hairBitmap,
                    nail = nailBitmap,
                    clothes = clothesBitmap,
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
                nailUri = sessionRepository.nailUri.value,
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
