package com.zoewave.probase.features.ai.firebase

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAiClient @Inject constructor() {
    
    // Utilizing a current, supported Gemini model (Gemini 3.5 Flash-Lite)
    fun getModel(modelName: String = "gemini-3.5-flash-lite"): GenerativeModel {
        
        // Explicitly route to the Gemini Developer API and enable limited-use tokens for replay protection
        return Firebase.ai(
            backend = GenerativeBackend.googleAI(),
            useLimitedUseAppCheckTokens = true
        ).generativeModel(modelName = modelName)
    }
}
