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
    private val initializationMessage: String,
) : DefaultLifecycleObserver {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private val pendingMessages = mutableListOf<String>()

    init {
        Log.d(TAG, "Initializing SeaweedAudioInterface with: $initializationMessage")
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                Log.d(TAG, "TTS initialized successfully")
                // On some devices, even after SUCCESS, it needs a moment
                processPendingMessages()
            } else {
                Log.e(TAG, "TTS Initialization failed with status: $status")
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "onStart: Triggering initial message")
        speak(initializationMessage)
    }

    /**
     * Speaks the given text.
     * @param flush If true, clears the queue and speaks immediately.
     */
    fun speak(textToSpeak: String, flush: Boolean = false) {
        if (textToSpeak.isBlank()) return

        synchronized(pendingMessages) {
            val currentTts = tts
            if (currentTts != null && isTtsReady) {
                Log.d(TAG, "Speaking: $textToSpeak (flush=$flush)")
                val mode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                val utteranceId = "msg_${System.nanoTime()}"
                currentTts.speak(
                    textToSpeak,
                    mode,
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
                Log.d(TAG, "Processing ${pendingMessages.size} pending messages")
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
