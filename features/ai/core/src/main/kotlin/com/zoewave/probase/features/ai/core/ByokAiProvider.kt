package com.zoewave.probase.features.ai.core

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ByokAiProvider @Inject constructor() : AiProvider {

    override val capability = AiProviderCapability(
        id = "byok_cloud",
        displayName = "BYOK Cloud (Deep Fallback)",
        maxInputTokens = 4096,
        maxOutputTokens = 1024,
        timeoutMillis = 5000L,
        initialTopK = 16,
        minTopK = 8,
        isLocal = false
    )

    override suspend fun isAvailable(): Boolean {
        // Only available if user provides their own API key
        return false // Implementation pending
    }

    override suspend fun countTokens(request: StylePromptRequest): Int {
        return request.exactPromptString.length / 4
    }

    override suspend fun execute(request: StylePromptRequest): Result<String> {
        return Result.failure(Exception("BYOK Provider not configured"))
    }
}
