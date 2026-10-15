package com.zoewave.probase.kocolor.features.seasonal_trends.domain

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val aiSettings: AiConfigurationSettings
) {
    suspend fun generateContent(prompt: String): String {
        val apiKey = aiSettings.isGeminiApiKeySetFlow.first().let { isSet ->
            if (isSet) aiSettings.getGeminiApiKey() ?: "" else ""
        }

        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini API Key is not configured.")
        }

        val modelName = aiSettings.aiModelFlow.first()
        val generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI(),
            useLimitedUseAppCheckTokens = false
        ).generativeModel(
            modelName = modelName
        )

        val response = generativeModel.generateContent(content { text(prompt) })
        return response.text ?: throw IllegalStateException("Empty response from AI")
    }
}
