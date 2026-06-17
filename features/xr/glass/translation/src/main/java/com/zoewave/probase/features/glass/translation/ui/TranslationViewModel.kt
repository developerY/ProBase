package com.zoewave.probase.features.glass.translation.ui

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TranslationUiState(
    val transcribedText: String = "",
    val translatedText: String = "",
    val isListening: Boolean = false,
    val isTranslating: Boolean = false,
    val isApiKeySet: Boolean = false,
    val isEngineAvailable: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TranslationViewModel @Inject constructor(
    application: Application,
    private val settings: AiConfigurationSettings
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TranslationUiState())
    val uiState: StateFlow<TranslationUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        checkStatus()
        initSpeechRecognizer()
    }

    private fun checkStatus() {
        viewModelScope.launch {
            settings.isGeminiApiKeySetFlow.collect { isSet ->
                _uiState.value = _uiState.value.copy(isApiKeySet = isSet)
            }
        }
    }

    private fun initSpeechRecognizer() {
        val context = getApplication<Application>()
        val available = SpeechRecognizer.isRecognitionAvailable(context)
        _uiState.value = _uiState.value.copy(isEngineAvailable = available)
        
        if (!available) {
            _uiState.value = _uiState.value.copy(error = "DEBUG: Speech Engine Missing! Ensure Google Speech Services are updated.")
            return
        }

        speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _uiState.value = _uiState.value.copy(isListening = true, error = null)
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                _uiState.value = _uiState.value.copy(isListening = false)
            }

            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                    else -> "Speech recognition error: $error"
                }
                _uiState.value = _uiState.value.copy(isListening = false, error = message)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    _uiState.value = _uiState.value.copy(transcribedText = text)
                    translateText(text)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(transcribedText = matches[0])
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    private fun translateText(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTranslating = true)
            try {
                val apiKey = settings.getGeminiApiKey()
                if (apiKey.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isTranslating = false,
                        error = "Gemini API Key not configured. Please set it in Settings."
                    )
                    return@launch
                }

                val modelName = settings.aiModelFlow.first()
                val generativeModel = GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey
                )

                val prompt = "Translate the following speech to Spanish. Return ONLY the translation: $text"
                val response = generativeModel.generateContent(prompt)
                
                _uiState.value = _uiState.value.copy(
                    translatedText = response.text ?: "Translation failed",
                    isTranslating = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    error = "Translation error: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
