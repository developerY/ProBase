package com.zoewave.probase.kocolor.features.seasonal_trends.domain

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
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
        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey
        )

        val response = generativeModel.generateContent(content { text(prompt) })
        return response.text ?: throw IllegalStateException("Empty response from AI")
    }
}
