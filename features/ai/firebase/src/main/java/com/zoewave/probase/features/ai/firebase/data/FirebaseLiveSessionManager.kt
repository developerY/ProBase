package com.zoewave.probase.features.ai.firebase.data

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.ai.LiveSession
import com.zoewave.probase.features.ai.firebase.domain.GeminiFirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles the lifecycle and connection of a Gemini Live session.
 */
class FirebaseLiveSessionManager @Inject constructor(
    private val geminiFirebaseManager: GeminiFirebaseManager
) : DefaultLifecycleObserver {

    private var session: LiveSession? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    /**
     * Starts the audio conversation session.
     */
    fun startConversation() {
        scope.launch {
            try {
                val liveModel = geminiFirebaseManager.createLiveModel()
                session = liveModel.connect()
                
                Log.d(TAG, "Connected to Gemini Live session")
                
                // Starts the bidirectional audio conversation
                session?.startAudioConversation()
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start Gemini Live conversation: ${e.message}")
            }
        }
    }

    /**
     * Stops the current conversation and closes the session.
     */
    fun stopConversation() {
        scope.launch {
            session?.close()
            session = null
            Log.d(TAG, "Gemini Live session closed")
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        stopConversation()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        scope.cancel()
    }

    companion object {
        private const val TAG = "FirebaseLiveSession"
    }
}
