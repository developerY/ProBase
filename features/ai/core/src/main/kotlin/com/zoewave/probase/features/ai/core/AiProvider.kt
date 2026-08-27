package com.zoewave.probase.features.ai.core

import android.graphics.Bitmap

/**
 * Capability description for an AI Provider.
 */
data class AiProviderCapability(
    val id: String,
    val displayName: String,
    val maxInputTokens: Int,
    val maxOutputTokens: Int,
    val timeoutMillis: Long,
    val initialTopK: Int = 16,
    val minTopK: Int = 6,
    val isLocal: Boolean,
    val supportsLocalImageIngestion: Boolean = false
)

/**
 * Unified failure model for AI execution.
 */
sealed interface AiExecutionFailure {
    data class ContextLimitExceeded(val details: String) : AiExecutionFailure
    data class QuotaExceeded(val retryAfterMillis: Long? = null) : AiExecutionFailure
    data class Timeout(val elapsedMillis: Long) : AiExecutionFailure
    data class NetworkUnavailable(val reason: String) : AiExecutionFailure
    data class ProviderUnavailable(val reason: String) : AiExecutionFailure
    data class Unknown(val throwable: Throwable) : AiExecutionFailure
}

/**
 * Wrapper for the assembled prompt.
 */
data class StylePromptRequest(
    val exactPromptString: String,
    val localImageBitmap: Bitmap? = null
)

/**
 * Common interface for all AI Providers (Local Nano, Firebase AI, BYOK Cloud, etc.)
 */
interface AiProvider {
    val capability: AiProviderCapability
    suspend fun isAvailable(): Boolean
    suspend fun countTokens(request: StylePromptRequest): Int
    
    // Using String for the result to remain generic at the core level, 
    // but the engine will decode it to StyleBlueprint.
    suspend fun execute(request: StylePromptRequest): Result<String>
}
