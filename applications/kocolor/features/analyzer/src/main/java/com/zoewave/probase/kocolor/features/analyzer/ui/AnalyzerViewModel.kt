package com.zoewave.probase.kocolor.features.analyzer.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoewave.probase.features.ai.configuration.domain.AiConfigurationSettings
import com.zoewave.probase.kocolor.data.FashionRepository
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
    val capturedUri: String? = null
)

sealed class AnalyzerEvent {
    data class OnPhotoCaptured(val uri: String) : AnalyzerEvent()
    data object OnAnalyzeClicked : AnalyzerEvent()
    data class OnSaveClicked(val advice: FashionAdvice) : AnalyzerEvent()
    data object OnResetClicked : AnalyzerEvent()
}

@HiltViewModel
class AnalyzerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyzerEngine: AnalyzerEngine,
    private val fashionRepository: FashionRepository,
    private val aiSettings: AiConfigurationSettings
) : ViewModel() {

    private val _analyzerState = MutableStateFlow<AnalyzerUiState>(AnalyzerUiState.Idle)
    private val _capturedUri = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AnalyzerScreenUiState> = combine(
        _analyzerState,
        _capturedUri
    ) { analyzerState, capturedUri ->
        AnalyzerScreenUiState(
            analyzerState = analyzerState,
            capturedUri = capturedUri
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyzerScreenUiState()
    )

    fun onEvent(event: AnalyzerEvent) {
        when (event) {
            is AnalyzerEvent.OnPhotoCaptured -> {
                _capturedUri.value = event.uri
                _analyzerState.value = AnalyzerUiState.Idle
            }
            is AnalyzerEvent.OnAnalyzeClicked -> {
                val uri = _capturedUri.value
                if (uri != null) {
                    analyzePhoto(uri)
                }
            }
            is AnalyzerEvent.OnSaveClicked -> {
                saveAnalysis(event.advice)
            }
            is AnalyzerEvent.OnResetClicked -> {
                _analyzerState.value = AnalyzerUiState.Idle
                _capturedUri.value = null
            }
        }
    }

    private fun analyzePhoto(uri: String) {
        viewModelScope.launch {
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
                val bitmap = loadBitmapFromUri(Uri.parse(uri))
                if (bitmap == null) {
                    _analyzerState.value = AnalyzerUiState.Error("Failed to load image.")
                    return@launch
                }

                val result = analyzerEngine.analyzeSelfie(bitmap, apiKey, modelName)
                _analyzerState.value = AnalyzerUiState.Success(result)
            } catch (e: Exception) {
                _analyzerState.value = AnalyzerUiState.Error("Analysis failed: ${e.localizedMessage}")
            }
        }
    }

    private fun saveAnalysis(advice: FashionAdvice) {
        viewModelScope.launch {
            val profile = FashionProfile(
                seasonalType = advice.seasonalType,
                undertone = advice.undertone,
                notes = advice.summary
            )
            fashionRepository.saveProfile(profile)
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
