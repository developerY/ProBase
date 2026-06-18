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
import com.zoewave.probase.features.glass.translation.data.TranslationRepository
import androidx.xr.projected.ProjectedContext
import androidx.xr.projected.experimental.ExperimentalProjectedApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
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
    val micSource: String = "Phone",
    val error: String? = null
)

@OptIn(ExperimentalProjectedApi::class)
@HiltViewModel
class TranslationViewModel @Inject constructor(
    application: Application,
    private val settings: AiConfigurationSettings,
    private val repository: TranslationRepository
) : AndroidViewModel(application) {

    private val TAG = "TranslationVM"
    
    private val _isApiKeySet = MutableStateFlow(false)
    private val _isEngineAvailable = MutableStateFlow(false)
    private val _isPermissionGranted = MutableStateFlow(false)
    private val _micSource = MutableStateFlow("Phone")
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<TranslationUiState> = combine(
        repository.transcribedText,
        repository.translatedText,
        repository.isListening,
        repository.isTranslating,
        _isApiKeySet,
        _isEngineAvailable,
        _isPermissionGranted,
        _micSource,
        _error
    ) { args: Array<Any?> ->
        TranslationUiState(
            transcribedText = args[0] as String,
            translatedText = args[1] as String,
            isListening = args[2] as Boolean,
            isTranslating = args[3] as Boolean,
            isApiKeySet = args[4] as Boolean,
            isEngineAvailable = args[5] as Boolean,
            isPermissionGranted = args[6] as Boolean,
            micSource = args[7] as String,
            error = args[8] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TranslationUiState()
    )

    private var speechRecognizer: SpeechRecognizer? = null
    private var useOnDevice: Boolean = true

    init {
        checkStatus()
    }

    private fun checkStatus() {
        viewModelScope.launch {
            settings.isGeminiApiKeySetFlow.collect { isSet ->
                _isApiKeySet.value = isSet
            }
        }
    }

    fun updatePermissionStatus(granted: Boolean) {
        _isPermissionGranted.value = granted
    }

    @OptIn(ExperimentalProjectedApi::class)
    private fun initSpeechRecognizer() {
        Log.d(TAG, "Initializing SpeechRecognizer (useOnDevice=$useOnDevice)")
        val context = getApplication<Application>()

        // Try to use Glasses context to target glasses hardware mic
        val (finalContext, source) = try {
            ProjectedContext.createProjectedDeviceContext(context) to "Glasses"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create projected context: ${e.message}. Falling back to Phone.")
            context to "Phone"
        }

        _micSource.value = source

        val available = SpeechRecognizer.isRecognitionAvailable(finalContext)
        Log.d(TAG, "Speech recognition available: $available on $source")
        _isEngineAvailable.value = available
        
        if (!available) {
            _error.value = "Speech Engine Missing! Ensure Google Speech Services are updated."
            return
        }

        try {
            speechRecognizer?.destroy()
            
            val canDoOnDevice = SpeechRecognizer.isOnDeviceRecognitionAvailable(finalContext)
            
            speechRecognizer = if (useOnDevice && canDoOnDevice) {
                Log.d(TAG, "Creating On-Device Recognizer on $source")
                SpeechRecognizer.createOnDeviceSpeechRecognizer(finalContext)
            } else {
                Log.d(TAG, "Creating Cloud Recognizer (Fallback) on $source")
                SpeechRecognizer.createSpeechRecognizer(finalContext)
            }
            
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "onReadyForSpeech")
                    repository.updateListening(true)
                    _error.value = null
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "onBeginningOfSpeech")
                }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    Log.d(TAG, "onEndOfSpeech")
                    repository.updateListening(false)
                }

                override fun onError(error: Int) {
                    Log.e(TAG, "Speech error: $error")
                    
                    if (error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE && useOnDevice) {
                        Log.w(TAG, "Error 13 detected. Retrying with Cloud Recognizer...")
                        useOnDevice = false
                        speechRecognizer?.destroy()
                        speechRecognizer = null
                        startListening() 
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
                    repository.updateListening(false)
                    _error.value = message
                    
                    if (error == SpeechRecognizer.ERROR_CLIENT) {
                        speechRecognizer?.cancel()
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.d(TAG, "onResults: ${matches?.getOrNull(0)}")
                    if (!matches.isNullOrEmpty()) {
                        val text = matches[0]
                        repository.updateTranscription(text)
                        translateText(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        repository.updateTranscription(matches[0])
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } catch (e: Exception) {
            Log.e(TAG, "Engine Init Failed: ${e.message}")
            _error.value = "Engine Init Failed: ${e.message}"
        }
    }

    fun startListening() {
        Log.d(TAG, "startListening requested")
        
        repository.clear()
        _error.value = null

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
            repository.updateTranslating(true)
            try {
                val apiKey = settings.getGeminiApiKey()
                if (apiKey.isNullOrBlank()) {
                    _error.value = "Gemini API Key missing. Check Settings."
                    repository.updateTranslating(false)
                    return@launch
                }

                val modelName = settings.aiModelFlow.first()
                val generativeModel = GenerativeModel(
                    modelName = modelName,
                    apiKey = apiKey
                )

                val prompt = "Translate to Spanish: $text"
                val response = generativeModel.generateContent(prompt)
                
                repository.updateTranslation(response.text ?: "Translation failed")
                repository.updateTranslating(false)
            } catch (e: Exception) {
                _error.value = "AI Error: ${e.message}"
                repository.updateTranslating(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
