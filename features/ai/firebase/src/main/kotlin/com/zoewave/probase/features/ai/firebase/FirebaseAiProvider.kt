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
        // In a real app, check network and Firebase status
        return true 
    }

    override suspend fun countTokens(request: StylePromptRequest): Int {
        // We can reuse the countTokens logic from the client if exposed
        // For now, let's assume it's exposed or we can estimate
        return (request.exactPromptString.length / 4) // Rough estimation if countTokens is not available
    }

    override suspend fun execute(request: StylePromptRequest): Result<String> {
        return try {
            // Note: FirebaseAiClient currently uses StyleTelemetry.
            // This wrapper needs to be updated if we want to use the raw prompt.
            // For now, we'll just return a failure or a dummy response to satisfy the interface.
            // Ideally, we'd refactor the client to accept the raw prompt too.
            Result.failure(Exception("FirebaseAiProvider needs refactoring to accept raw prompts"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
