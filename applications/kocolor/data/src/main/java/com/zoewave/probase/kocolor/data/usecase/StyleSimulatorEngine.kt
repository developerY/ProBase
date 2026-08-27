package com.zoewave.probase.kocolor.data.usecase

import android.util.Log
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.StylePromptRequest
import com.zoewave.probase.features.ai.local.data.PromptCacheRepository
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditLogger
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleSimulatorEngine @Inject constructor(
    private val contextEngine: DeterministicContextEngine,
    private val candidateFilter: WardrobeCandidateFilter,
    private val serializer: CompactManifestSerializer,
    private val promptAssembler: PromptAssembler,
    private val capabilityRouter: CapabilityRouter,
    private val cache: PromptCacheRepository,
    private val auditLogger: StyleAuditLogger,
    private val fallbackEngine: DeterministicStyleEngine
) {

    companion object {
        private const val RETRIEVAL_POLICY_VERSION = "3.0" // Anchor-Driven Pipeline
        private const val PROMPT_VERSION = "3.0"
    }

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    /**
     * Entry point for generating a style blueprint using the best available AI provider.
     */
    suspend fun generateBlueprint(
        wardrobe: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        requestContext: StyleRequestContext
    ): StyleBlueprint {
        auditLogger.startRequest(requestContext.requestId)
        val providers = capabilityRouter.getRankedAvailableProviders()
        
        for (provider in providers) {
            val providerStartTime = System.currentTimeMillis()
            Log.d("StyleSimulatorEngine", "Attempting provider: ${provider.capability.displayName}")
            
            val fitResult = adaptContextToProvider(provider, wardrobe, cosmetics, requestContext)
            
            if (fitResult != null) {
                // Phase 3: Deterministic Cache Check
                val fingerprint = cache.generateFingerprint(
                    executionTier = provider.capability.id,
                    promptVersion = PROMPT_VERSION,
                    modelVersion = provider.capability.id,
                    retrievalPolicyVersion = RETRIEVAL_POLICY_VERSION,
                    appearanceTelemetry = requestContext.appearanceTelemetry.toString(),
                    weatherState = requestContext.weather,
                    userIntent = requestContext.intent,
                    minifiedManifest = fitResult.request.exactPromptString
                )

                val cachedResponse = cache.get(fingerprint)
                if (cachedResponse != null) {
                    return try {
                        val blueprint = decodeBlueprint(cachedResponse)
                        auditLogger.logAiExecution(
                            requestId = requestContext.requestId,
                            providerId = "CACHE_${provider.capability.id}",
                            tokens = 0,
                            blueprint = blueprint
                        )
                        auditLogger.printAuditTrail(requestContext.requestId)
                        logTelemetry(
                            tier = "CACHE_${provider.capability.id}",
                            kLimit = fitResult.kLimit,
                            detail = fitResult.detailLevel,
                            tokens = 0,
                            latency = System.currentTimeMillis() - providerStartTime,
                            success = true
                        )
                        blueprint
                    } catch (e: Exception) {
                        Log.e("StyleSimulatorEngine", "Failed to decode cached result", e)
                        // Continue to execute if cache is corrupt
                        executeAndCache(provider, fitResult, fingerprint, providerStartTime, requestContext.requestId) ?: continue
                    }
                }

                val blueprint = executeAndCache(provider, fitResult, fingerprint, providerStartTime, requestContext.requestId)
                if (blueprint != null) {
                    auditLogger.printAuditTrail(requestContext.requestId)
                    return blueprint
                }
            } else {
                Log.w("StyleSimulatorEngine", "Could not fit context into provider ${provider.capability.id} budget")
                logTelemetry(
                    tier = provider.capability.id,
                    kLimit = provider.capability.initialTopK,
                    detail = SerializationDetailLevel.EXPANDED,
                    tokens = 0,
                    latency = System.currentTimeMillis() - providerStartTime,
                    success = false,
                    reason = "CONTEXT_OVERFLOW"
                )
            }
        }
        
        // Indestructible baseline
        val fallbackStartTime = System.currentTimeMillis()
        Log.i("StyleSimulatorEngine", "All providers failed. Falling back to deterministic engine.")
        val blueprint = fallbackEngine.generate(requestContext)
        
        auditLogger.logAiExecution(
            requestId = requestContext.requestId,
            providerId = "DETERMINISTIC_FALLBACK",
            tokens = 0,
            blueprint = blueprint
        )
        auditLogger.printAuditTrail(requestContext.requestId)
        
        logTelemetry(
            tier = "DETERMINISTIC_FALLBACK",
            kLimit = 0,
            detail = SerializationDetailLevel.MINIMAL,
            tokens = 0,
            latency = System.currentTimeMillis() - fallbackStartTime,
            success = true
        )
        
        return blueprint
    }

    private suspend fun executeAndCache(
        provider: AiProvider,
        fitResult: AdaptiveFitResult,
        fingerprint: String,
        startTime: Long,
        requestId: String
    ): StyleBlueprint? {
        val executionResult = provider.execute(fitResult.request)
        val latency = System.currentTimeMillis() - startTime

        return if (executionResult.isSuccess) {
            val rawResult = executionResult.getOrThrow()
            try {
                val blueprint = decodeBlueprint(rawResult)
                cache.put(fingerprint, rawResult)
                auditLogger.logAiExecution(
                    requestId = requestId,
                    providerId = provider.capability.id,
                    tokens = fitResult.tokenCount,
                    blueprint = blueprint
                )
                // auditLogger.printAuditTrail(...)
                logTelemetry(
                    tier = provider.capability.id,
                    kLimit = fitResult.kLimit,
                    detail = fitResult.detailLevel,
                    tokens = fitResult.tokenCount,
                    latency = latency,
                    success = true
                )
                blueprint
            } catch (e: Exception) {
                Log.e("StyleSimulatorEngine", "Failed to decode result from ${provider.capability.id}", e)
                logTelemetry(
                    tier = provider.capability.id,
                    kLimit = fitResult.kLimit,
                    detail = fitResult.detailLevel,
                    tokens = fitResult.tokenCount,
                    latency = latency,
                    success = false,
                    reason = "DECODE_ERROR"
                )
                null
            }
        } else {
            val failure = executionResult.exceptionOrNull()
            val reason = failure?.let { it::class.simpleName } ?: "UNKNOWN_FAILURE"
            Log.w("StyleSimulatorEngine", "Provider ${provider.capability.id} failed: ${failure?.message}")
            
            logTelemetry(
                tier = provider.capability.id,
                kLimit = fitResult.kLimit,
                detail = fitResult.detailLevel,
                tokens = fitResult.tokenCount,
                latency = latency,
                success = false,
                reason = reason
            )
            null
        }
    }

    private data class AdaptiveFitResult(
        val request: StylePromptRequest,
        val kLimit: Int,
        val detailLevel: SerializationDetailLevel,
        val tokenCount: Int
    )

    /**
     * Step-down strategy to fit the request into a provider's token budget.
     */
    private suspend fun adaptContextToProvider(
        provider: AiProvider,
        wardrobe: List<ClothingItem>,
        cosmetics: List<CosmeticItem>,
        context: StyleRequestContext
    ): AdaptiveFitResult? {
        val cap = provider.capability
        var currentK = cap.initialTopK
        var detailLevel = SerializationDetailLevel.EXPANDED

        while (currentK >= cap.minTopK) {
            // Phase 2: Anchor-Driven Retrieval
            val wCandidates = contextEngine.buildReasoningSet(wardrobe, context, limit = currentK)
            val cItems = candidateFilter.getCosmeticCandidates(cosmetics, context, limit = currentK)
            
            // Map cosmetics to provenance for audit logging
            val cCandidates = cItems.map { item ->
                CandidateProvenance(
                    cosmeticItem = item,
                    contextScore = 0.5f, // Simplified for now
                    colorScore = 0.8f,
                    appearanceScore = 0.8f,
                    freshnessScore = 1.0f,
                    retrievalReason = if (item.isSignature) "[Signature Item] Rotation bypassed." else "Role diversity match"
                )
            }
            
            // Log combined reasoning set
            auditLogger.logReasoningSet(context.requestId, wCandidates + cCandidates)

            val manifest = serializer.serialize(wCandidates, cItems, detailLevel)
            
            val candidatePrompt = promptAssembler.buildExactRequest(
                context = context,
                compactManifest = manifest,
                providerCapability = cap
            )
            
            val tokenCount = provider.countTokens(candidatePrompt)
            if (tokenCount <= cap.maxInputTokens) {
                Log.d("StyleSimulatorEngine", "Adapted context: K=$currentK, Detail=$detailLevel, Tokens=$tokenCount")
                return AdaptiveFitResult(candidatePrompt, currentK, detailLevel, tokenCount)
            }

            // Step-down strategy:
            // 1. If EXPANDED -> downgrade to BALANCED.
            // 2. Else if BALANCED -> downgrade to MINIMAL.
            // 3. Else -> reduce currentK by 2 and reset detail level to BALANCED.
            when (detailLevel) {
                SerializationDetailLevel.EXPANDED -> {
                    detailLevel = SerializationDetailLevel.BALANCED
                }
                SerializationDetailLevel.BALANCED -> {
                    detailLevel = SerializationDetailLevel.MINIMAL
                }
                SerializationDetailLevel.MINIMAL -> {
                    currentK -= 2
                    detailLevel = SerializationDetailLevel.BALANCED
                }
            }
        }
        return null // Cannot fit within provider budget
    }

    private fun logTelemetry(
        tier: String,
        kLimit: Int,
        detail: SerializationDetailLevel,
        tokens: Int,
        latency: Long,
        success: Boolean,
        reason: String? = null
    ) {
        Log.d("KoColor_Telemetry", """
            - execution_tier_used: $tier
            - retrieval_k_limit: $kLimit
            - serialization_strategy: ${detail.name}
            - tokens_used: $tokens
            - latency_ms: $latency
            - success: $success
            - fallback_reason: $reason
        """.trimIndent())
    }

    private fun decodeBlueprint(jsonText: String): StyleBlueprint {
        val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
        val finalJson = "{$cleanedJson}"
        return json.decodeFromString<StyleBlueprint>(finalJson)
    }
}
