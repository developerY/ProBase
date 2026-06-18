package com.zoewave.probase.features.glass.translation.ui

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class TranslationUiState(
    val transcribedText: String = "",
    val translatedText: String = "",
    val isListening: Boolean = false,
    val isTranslating: Boolean = false,
    val isApiKeySet: Boolean = false,
    val isEngineAvailable: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalProjectedApi::class)
@HiltViewModel
class TranslationViewModel @Inject constructor(
    application: Application,
    private val settings: AiConfigurationSettings
) : AndroidViewModel(application) {

    private val TAG = "TranslationVM"
    private val _uiState = MutableStateFlow(TranslationUiState())
    val uiState: StateFlow<TranslationUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var useOnDevice: Boolean = true

    init {
        checkStatus()
    }

    private fun checkStatus() {
        viewModelScope.launch {
            settings.isGeminiApiKeySetFlow.collect { isSet ->
                _uiState.value = _uiState.value.copy(isApiKeySet = isSet)
            }
        }
    }

    fun updatePermissionStatus(granted: Boolean) {
        _uiState.value = _uiState.value.copy(isPermissionGranted = granted)
    }

    @OptIn(ExperimentalProjectedApi::class)
    private fun initSpeechRecognizer() {
        Log.d(TAG, "Initializing SpeechRecognizer (useOnDevice=$useOnDevice)")
        val context = getApplication<Application>()
        
        val hostContext = try { 
            ProjectedContext.createHostDeviceContext(context) 
        } catch (e: Exception) { 
            Log.e(TAG, "Failed to create host context: ${e.message}")
            context 
        }

        val available = SpeechRecognizer.isRecognitionAvailable(hostContext)
        Log.d(TAG, "Speech recognition available: $available")
        _uiState.value = _uiState.value.copy(isEngineAvailable = available)
        
        if (!available) {
            _uiState.value = _uiState.value.copy(error = "Speech Engine Missing! Ensure Google Speech Services are updated.")
            return
        }

        try {
            speechRecognizer?.destroy()
            
            // On emulators, on-device recognition often fails with Error 13 (Language Unavailable)
            // because models aren't downloaded. We check support first.
            val canDoOnDevice = android.os.Build.VERSION.SDK_INT >= 31 && 
                               SpeechRecognizer.isOnDeviceRecognitionAvailable(hostContext)
            
            speechRecognizer = if (useOnDevice && canDoOnDevice) {
                Log.d(TAG, "Creating On-Device Recognizer")
                SpeechRecognizer.createOnDeviceSpeechRecognizer(hostContext)
            } else {
                Log.d(TAG, "Creating Cloud Recognizer (Fallback)")
                SpeechRecognizer.createSpeechRecognizer(hostContext)
            }
            
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "onReadyForSpeech")
                    _uiState.value = _uiState.value.copy(isListening = true, error = null)
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "onBeginningOfSpeech")
                }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    Log.d(TAG, "onEndOfSpeech")
                    _uiState.value = _uiState.value.copy(isListening = false)
                }

                override fun onError(error: Int) {
                    Log.e(TAG, "Speech error: $error")
                    
                    if (error == 13 && useOnDevice) {
                        Log.w(TAG, "Error 13 detected. Retrying with Cloud Recognizer...")
                        useOnDevice = false
                        speechRecognizer?.destroy()
                        speechRecognizer = null
                        startListening() // Recursive call will trigger cloud init
                        return
                    }

                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                            speechRecognizer?.destroy()
                            speechRecognizer = null
                            "Permission sync delay. Please tap STOP then START again to refresh."
                        }
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                        SpeechRecognizer.ERROR_CLIENT -> "Client busy (Error 5). Resetting..."
                        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Language model unavailable on device. Retrying with cloud..."
                        else -> "Speech error: $error"
                    }
                    _uiState.value = _uiState.value.copy(isListening = false, error = message)
                    
                    if (error == SpeechRecognizer.ERROR_CLIENT) {
                        speechRecognizer?.cancel()
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.d(TAG, "onResults: ${matches?.getOrNull(0)}")
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
        } catch (e: Exception) {
            Log.e(TAG, "Engine Init Failed: ${e.message}")
            _uiState.value = _uiState.value.copy(error = "Engine Init Failed: ${e.message}")
        }
    }

    fun startListening() {
        Log.d(TAG, "startListening requested")
        
        _uiState.value = _uiState.value.copy(transcribedText = "", translatedText = "", error = null)

        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        
        speechRecognizer?.cancel()
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        Log.d(TAG, "stopListening requested")
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
                        error = "Gemini API Key missing. Check Settings."
                    )
                    return@launch
                }

                val modelName = settings.aiModelFlow.first()
                val generativeModel = GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey
                )

                val prompt = "Translate to Spanish: $text"
                val response = generativeModel.generateContent(prompt)
                
                _uiState.value = _uiState.value.copy(
                    translatedText = response.text ?: "Translation failed",
                    isTranslating = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    error = "AI Error: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
