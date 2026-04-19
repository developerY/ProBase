package com.zoewave.probase.features.ai.firebase.domain

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.LiveGenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.liveGenerationConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Isolated manager for Firebase AI Logic SDK.
 * Used primarily for Android XR (AI Glasses) capabilities like Gemini Live.
 */
@OptIn(PublicPreviewAPI::class)
@Singleton
class GeminiFirebaseManager @Inject constructor() {

    /**
     * Creates a LiveModel for real-time audio/text conversations.
     */
    fun createLiveModel(
        modelName: String = "gemini-2.5-flash-native-audio-preview-12-2025"
    ): LiveGenerativeModel {
        return Firebase.ai(backend = GenerativeBackend.googleAI())
            .liveModel(
                modelName = modelName,
                generationConfig = liveGenerationConfig {
                    responseModality = ResponseModality.AUDIO
                }
            )
    }

    /**
     * Creates a standard GenerativeModel for one-shot requests.
     */
    fun createGenerativeModel(
        modelName: String = "gemini-1.5-flash"
    ): GenerativeModel {
        return Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(modelName)
    }
}
