package com.zoewave.probase.features.microphone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SpeechEngine(private val context: Context) {
    private var recognizer: SpeechRecognizer? = null
    
    private val _textState = MutableStateFlow("Tap 'Start' to test mic...")
    val textState: StateFlow<String> = _textState

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel

    private val _isServiceReady = MutableStateFlow(false)
    val isServiceReady: StateFlow<Boolean> = _isServiceReady

    private val _hasDetectedSignal = MutableStateFlow(false)
    val hasDetectedSignal: StateFlow<Boolean> = _hasDetectedSignal

    val isRecognitionAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    private fun addLog(message: String) {
        Log.d("SpeechEngine", message)
        _logs.update { it + message }
    }

    fun startListening() {
        addLog("Action: startListening()")
        _rmsLevel.value = 0f
        _isServiceReady.value = false
        _hasDetectedSignal.value = false

        if (!isRecognitionAvailable) {
            val error = "ERROR: SpeechRecognizer not available on this device."
            _textState.value = error
            addLog(error)
            return
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _textState.value = "Listening..."
                    _isServiceReady.value = true
                    addLog("Event: onReadyForSpeech")
                }
                override fun onBeginningOfSpeech() {
                    addLog("Event: onBeginningOfSpeech")
                }
                override fun onRmsChanged(rmsdB: Float) {
                    _rmsLevel.value = rmsdB
                    if (rmsdB > -2f) { // Threshold for "signal detected"
                        _hasDetectedSignal.value = true
                    }
                }
                override fun onBufferReceived(buffer: ByteArray?) {
                    _hasDetectedSignal.value = true
                    addLog("Event: onBufferReceived (size: ${buffer?.size ?: 0})")
                }
                override fun onEndOfSpeech() {
                    addLog("Event: onEndOfSpeech")
                }
                override fun onError(error: Int) {
                    val errorMessage = getErrorText(error)
                    _textState.value = "ERROR: $errorMessage"
                    _isServiceReady.value = false
                    addLog("Event: onError - Code $error ($errorMessage)")
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val resultText = matches?.firstOrNull() ?: "No results"
                    _textState.value = resultText
                    addLog("Event: onResults - \"$resultText\"")
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _textState.value = matches[0]
                        addLog("Event: onPartialResults - \"${matches[0]}\"")
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {
                    addLog("Event: onEvent - Type $eventType")
                }
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        addLog("Calling recognizer?.startListening(intent)")
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        addLog("Action: stopListening()")
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
        _textState.value = "Stopped."
        addLog("Recognizer destroyed.")
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }
}
