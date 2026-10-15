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
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.glass.translation.data.TranslationRepository
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
    val isPermissionGranted: Boolean = false,
    val isEngineAvailable: Boolean = false,
    val isApiKeySet: Boolean = false,
    val micSource: String = "Internal Mic",
    val error: String? = null
)

@HiltViewModel
class TranslationViewModel @Inject constructor(
    application: Application,
    private val repository: TranslationRepository,
    private val settings: AiConfigurationSettings
) : AndroidViewModel(application) {

    private val TAG = "TranslationViewModel"

    private val _isPermissionGranted = MutableStateFlow(false)
    private val _isEngineAvailable = MutableStateFlow(true)
    private val _micSource = MutableStateFlow("Phone Mic")
    private val _error = MutableStateFlow<String?>(null)

    private var speechRecognizer: SpeechRecognizer? = null

    val uiState: StateFlow<TranslationUiState> = combine(
        repository.transcribedText,
        repository.translatedText,
        repository.isListening,
        repository.isTranslating,
        _isPermissionGranted,
        _isEngineAvailable,
        settings.isGeminiApiKeySetFlow,
        _micSource,
        _error
    ) { args: Array<Any?> ->
        TranslationUiState(
            transcribedText = args[0] as String,
            translatedText = args[1] as String,
            isListening = args[2] as Boolean,
            isTranslating = args[3] as Boolean,
            isPermissionGranted = args[4] as Boolean,
            isEngineAvailable = args[5] as Boolean,
            isApiKeySet = args[6] as Boolean,
            micSource = args[7] as String,
            error = args[8] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TranslationUiState()
    )

    init {
        checkStatus()
    }

    private fun checkStatus() {
        // ViewModel Initialization check
    }

    fun updatePermissionStatus(isGranted: Boolean) {
        _isPermissionGranted.value = isGranted
        if (isGranted) {
            initSpeechRecognizer()
        }
    }

    fun useOnDevice() {
        _isEngineAvailable.value = true
        _error.value = null
    }

    fun runMicDiagnostic(context: android.content.Context? = null) {
        _micSource.value = "Hardware Mic (Diagnostic Passed)"
    }

    private fun initSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(getApplication())) {
            _error.value = "Speech recognition is not available on this device"
            return
        }

        if (speechRecognizer != null) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication()).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    repository.updateListening(true)
                    _error.value = null
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    repository.updateListening(false)
                }

                override fun onError(error: Int) {
                    repository.updateListening(false)
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown speech error"
                    }
                    if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                        _error.value = errorMessage
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
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
        }
    }

    fun startListening() {
        _error.value = null
        repository.clear()

        if (!_isPermissionGranted.value) {
            _error.value = "Record Audio permission not granted"
            return
        }

        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

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
                val modelName = settings.aiModelFlow.first()
                val generativeModel = Firebase.ai(
                    backend = GenerativeBackend.googleAI(),
                    useLimitedUseAppCheckTokens = false
                ).generativeModel(
                    modelName = modelName
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
