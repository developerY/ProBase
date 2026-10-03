package com.zoewave.probase.features.ai.core

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ByokAiProvider @Inject constructor() : AiProvider {

    private var currentApiKey: String? = null

    fun setApiKey(key: String?) {
        this.currentApiKey = key
    }

    override val capability = AiProviderCapability(
        id = "byok_cloud",
        displayName = "BYOK Cloud (Gemini API Key)",
        maxInputTokens = 8192,
        maxOutputTokens = 1024,
        timeoutMillis = 8000L,
        maxCandidateAdditions = 25,
        minCandidateAdditions = 6,
        isLocal = false
    )

    override suspend fun isAvailable(): Boolean {
        return !currentApiKey.isNullOrBlank()
    }

    override suspend fun countTokens(input: AiInput): Int {
        return input.promptString.length / 4
    }

    override suspend fun execute(input: AiInput): Result<String> {
        return Result.failure(Exception("BYOK Provider not configured"))
    }
}
