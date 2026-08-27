package com.zoewave.probase.features.ai.firebase

import com.zoewave.probase.features.ai.core.AiExecutionFailure
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAiProvider @Inject constructor(
    private val client: FirebaseAiClient,
    private val authManager: FirebaseAiAuthManager
) : AiProvider {

    override val capability = AiProviderCapability(
        id = "firebase_ai_logic",
        displayName = "Firebase AI Logic (Cloud)",
        maxInputTokens = 1536,
        maxOutputTokens = 512,
        timeoutMillis = 3000L,
        maxCandidateAdditions = 12,
        minCandidateAdditions = 6,
        isLocal = false
    )

    override suspend fun isAvailable(): Boolean {
        // In a real app, check network status
        return true 
    }

    override suspend fun countTokens(input: AiInput): Int {
        return client.countTokens(input.promptString)
    }

    override suspend fun execute(input: AiInput): Result<String> {
        // Defense-in-Depth Privacy Enforcement
        if (input is AiInput.Multimodal) {
            return Result.failure(
                AiExecutionFailure.ExecutionError(
                    IllegalArgumentException("Cloud providers strictly reject Multimodal image inputs.")
                ).let { Exception(it.throwable) }
            )
        }

        return try {
            authManager.signInAnonymously()
            val response = client.generateContent(input.promptString)
            Result.success(response.text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
