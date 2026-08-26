package com.zoewave.probase.features.ai.firebase

import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import com.zoewave.probase.features.ai.core.StylePromptRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAiProvider @Inject constructor(
    private val client: FirebaseAiClient
) : AiProvider {

    override val capability = AiProviderCapability(
        id = "firebase_ai_logic",
        displayName = "Firebase AI Logic (Cloud)",
        maxInputTokens = 1536,
        maxOutputTokens = 512,
        timeoutMillis = 3000L,
        initialTopK = 12,
        minTopK = 6,
        isLocal = false
    )

    override suspend fun isAvailable(): Boolean {
        // In a real app, check network status
        return true 
    }

    override suspend fun countTokens(request: StylePromptRequest): Int {
        return client.countTokens(request.exactPromptString)
    }

    override suspend fun execute(request: StylePromptRequest): Result<String> {
        return try {
            val response = client.generateContent(request.exactPromptString)
            Result.success(response.text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
