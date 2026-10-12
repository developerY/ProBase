package com.zoewave.probase.features.ai.firebase

import android.util.Log
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
        maxInputTokens = 8192,
        maxOutputTokens = 1024,
        timeoutMillis = 15000L,
        maxCandidateAdditions = 25,
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
        Log.d("FirebaseAiProvider", "Executing Cloud AI Request. Type: ${input.javaClass.simpleName}")
        
        // Defense-in-Depth Privacy Enforcement
        if (input is AiInput.Multimodal) {
            val err = IllegalArgumentException("Cloud providers strictly reject Multimodal image inputs.")
            Log.e("FirebaseAiProvider", "Validation failed", err)
            return Result.failure(
                AiExecutionFailure.ExecutionError(err).let { Exception(it.throwable) }
            )
        }

        return try {
            Log.d("FirebaseAiProvider", "Authenticating Anonymously via Firebase Auth...")
            authManager.signInAnonymously()
            
            Log.d("FirebaseAiProvider", "Delegating prompt to FirebaseAiClient.generateContent...")
            val response = client.generateContent(input.promptString, input.temperatureOverride)
            
            Log.d("FirebaseAiProvider", "Cloud AI Request completed successfully. Parsed text length: ${response.text.length}")
            Result.success(response.text)
        } catch (e: Exception) {
            Log.e("FirebaseAiProvider", "FATAL EXECUTION ERROR in Cloud Provider", e)
            Result.failure(e)
        }
    }
}
