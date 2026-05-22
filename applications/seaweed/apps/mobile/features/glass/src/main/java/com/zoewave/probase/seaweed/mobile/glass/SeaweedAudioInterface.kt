package com.zoewave.probase.seaweed.mobile.glass

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Interface for Text-To-Speech operations, lifecycle-aware.
 * Handles initialization and queuing of messages if the engine is not yet ready.
 */
class SeaweedAudioInterface(
    context: Context,
    initializationMessage: String,
) : DefaultLifecycleObserver {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val pendingMessages = mutableListOf<String>()

    init {
        // Initialize TTS. The constructor returns immediately, and the listener is called when ready.
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                Log.d(TAG, "TTS initialized successfully")
                processPendingMessages()
            } else {
                Log.e(TAG, "TTS Initialization failed with status: $status")
            }
        }
        
        // Queue the initialization message. It will be spoken as soon as TTS is ready.
        speak(initializationMessage)
    }

    /**
     * Speaks the given text. If TTS is not ready, the message is queued.
     */
    fun speak(textToSpeak: String) {
        if (textToSpeak.isBlank()) return

        synchronized(pendingMessages) {
            val currentTts = tts
            if (currentTts != null && isTtsReady) {
                val utteranceId = "msg_${System.nanoTime()}"
                currentTts.speak(
                    textToSpeak,
                    TextToSpeech.QUEUE_ADD,
                    null,
                    utteranceId
                )
            } else {
                Log.d(TAG, "TTS not ready, queuing message: $textToSpeak")
                pendingMessages.add(textToSpeak)
            }
        }
    }

    private fun processPendingMessages() {
        synchronized(pendingMessages) {
            val currentTts = tts
            if (currentTts != null && isTtsReady) {
                pendingMessages.forEachIndexed { index, msg ->
                    val utteranceId = "msg_${System.nanoTime()}_$index"
                    currentTts.speak(msg, TextToSpeech.QUEUE_ADD, null, utteranceId)
                }
                pendingMessages.clear()
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        synchronized(pendingMessages) {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isTtsReady = false
            pendingMessages.clear()
        }
    }

    companion object {
        private const val TAG = "AudioInterface"
    }
}
