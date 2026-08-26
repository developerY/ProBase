package com.zoewave.probase.kocolor.data.usecase

import android.util.Log
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.StylePromptRequest
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StyleSimulatorEngine @Inject constructor(
    private val candidateFilter: WardrobeCandidateFilter,
    private val serializer: CompactManifestSerializer,
    private val promptAssembler: PromptAssembler,
    private val capabilityRouter: CapabilityRouter,
    private val fallbackEngine: DeterministicStyleEngine
) {

    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    /**
     * Entry point for generating a style blueprint using the best available AI provider.
     */
    suspend fun generateBlueprint(requestContext: StyleRequestContext): StyleBlueprint {
        val providers = capabilityRouter.getRankedAvailableProviders()
        
        for (provider in providers) {
            Log.d("StyleSimulatorEngine", "Attempting provider: ${provider.capability.displayName}")
            val fitResult = adaptContextToProvider(provider, requestContext)
            
            if (fitResult != null) {
                val executionResult = provider.execute(fitResult)
                if (executionResult.isSuccess) {
                    val rawResult = executionResult.getOrThrow()
                    return try {
                        decodeBlueprint(rawResult)
                    } catch (e: Exception) {
                        Log.e("StyleSimulatorEngine", "Failed to decode result from ${provider.capability.id}", e)
                        continue // Try next provider
                    }
                } else {
                    Log.w("StyleSimulatorEngine", "Provider ${provider.capability.id} failed: ${executionResult.exceptionOrNull()?.message}")
                }
            } else {
                Log.w("StyleSimulatorEngine", "Could not fit context into provider ${provider.capability.id} budget")
            }
        }
        
        // Indestructible baseline
        Log.i("StyleSimulatorEngine", "All providers failed. Falling back to deterministic engine.")
        return fallbackEngine.generate(requestContext)
    }

    /**
     * Step-down strategy to fit the request into a provider's token budget.
     */
    private suspend fun adaptContextToProvider(
        provider: AiProvider,
        context: StyleRequestContext
    ): StylePromptRequest? {
        val cap = provider.capability
        var currentK = cap.initialTopK
        var detailLevel = SerializationDetailLevel.EXPANDED

        while (currentK >= cap.minTopK) {
            val candidates = candidateFilter.getCandidates(context, limit = currentK)
            val manifest = serializer.serialize(candidates, detailLevel)
            
            val candidatePrompt = promptAssembler.buildExactRequest(
                context = context,
                compactManifest = manifest
            )
            
            val tokenCount = provider.countTokens(candidatePrompt)
            if (tokenCount <= cap.maxInputTokens) {
                Log.d("StyleSimulatorEngine", "Adapted context: K=$currentK, Detail=$detailLevel, Tokens=$tokenCount")
                return candidatePrompt
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

    private fun decodeBlueprint(jsonText: String): StyleBlueprint {
        val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
        val finalJson = "{$cleanedJson}"
        return json.decodeFromString<StyleBlueprint>(finalJson)
    }
}
