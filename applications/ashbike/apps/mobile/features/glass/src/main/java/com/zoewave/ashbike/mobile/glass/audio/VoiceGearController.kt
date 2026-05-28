package com.zoewave.ashbike.mobile.glass.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.zoewave.ashbike.data.repository.bike.BikeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class VoiceGearController(
    private val context: Context,
    private val repository: BikeRepository,
    private val onCommandDetected: (String) -> Unit = {}
) : DefaultLifecycleObserver {

    private var speechRecognizer: SpeechRecognizer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var isListening = false

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        // Note: RECORD_AUDIO permission must be granted before this is used
    }

    fun startListening() {
        if (isListening) return
        
        scope.launch {
            if (speechRecognizer == null) {
                // Use attribution context for XR hardware access tracking
                val attributionContext = context.createAttributionContext("xr_projected")
                
                speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(attributionContext).apply {
                    setRecognitionListener(createRecognitionListener())
                }
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
            isListening = true
            Log.d(TAG, "Started listening for voice commands")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        isListening = false
        Log.d(TAG, "Stopped listening")
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech")
            isListening = false
            // Automatically restart listening for continuous experience
            startListening()
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                SpeechRecognizer.ERROR_SERVER -> "Server sends error status"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error"
            }
            Log.e(TAG, "Speech recognition error: $message ($error)")
            isListening = false
            
            // Restart listening unless it was a fatal error or busy
            if (error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                startListening()
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.forEach { text ->
                Log.d(TAG, "Heard: $text")
                processCommand(text)
            }
            isListening = false
            startListening()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.forEach { text ->
                Log.d(TAG, "Partial: $text")
                processCommand(text)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun processCommand(text: String) {
        val normalized = text.lowercase()
        when {
            normalized.contains("gear up") || normalized.contains("shift up") || normalized.contains("higher gear") -> {
                Log.i(TAG, "Command detected: GEAR UP")
                onCommandDetected("Gear Up")
                scope.launch { repository.gearUp() }
            }
            normalized.contains("gear down") || normalized.contains("shift down") || normalized.contains("lower gear") -> {
                Log.i(TAG, "Command detected: GEAR DOWN")
                onCommandDetected("Gear Down")
                scope.launch { repository.gearDown() }
            }
            normalized.contains("hey ash") || normalized.contains("talk to ash") -> {
                Log.i(TAG, "Triggering AI Assistant")
                onCommandDetected("AI Assistant")
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    companion object {
        private const val TAG = "VoiceGearController"
    }
}
