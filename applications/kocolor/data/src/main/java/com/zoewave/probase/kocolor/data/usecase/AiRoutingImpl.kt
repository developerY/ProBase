package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.ByokAiProvider
import com.zoewave.probase.features.ai.firebase.FirebaseAiProvider
import com.zoewave.probase.features.ai.firebase.GemmaRestFallbackProvider
import com.zoewave.probase.features.ai.local.data.LocalNanoAiProvider
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityRouterImpl @Inject constructor(
    private val localProvider: LocalNanoAiProvider,
    private val byokProvider: ByokAiProvider,
    private val firebaseProvider: FirebaseAiProvider,
    private val gemmaProvider: GemmaRestFallbackProvider,
    private val settings: AiConfigurationSettings
) : CapabilityRouter {

    override suspend fun getActiveProviderName(): String {
        return getRankedAvailableProviders().firstOrNull()?.capability?.displayName ?: "Math/Deterministic (No AI)"
    }

    override suspend fun getRankedAvailableProviders(): List<AiProvider> {
        val providers = mutableListOf<AiProvider>()
        
        // Priority 1: Local Nano (On-Device AI)
        if (localProvider.isAvailable()) {
            providers.add(localProvider)
        }

        // Priority 2: BYOK with Gemini API Key
        val useByok = settings.useByokKey.firstOrNull() ?: true
        val apiKey = settings.getGeminiApiKey()
        val modelName = settings.aiModelFlow.firstOrNull() ?: "gemini-2.5-flash"
        byokProvider.setApiKey(apiKey)
        byokProvider.setModelName(modelName)
        if (useByok && byokProvider.isAvailable()) {
            providers.add(byokProvider)
        }
        
        // Priority 3: Firebase AI Logic (Managed Cloud Fallback)
        val useFirebase = settings.useFirebaseVertexAi.firstOrNull() ?: true
        if (useFirebase && firebaseProvider.isAvailable()) {
            providers.add(firebaseProvider)
        }

        // Priority 4: Gemma 4 Cloud REST Fallback
        if (gemmaProvider.isAvailable()) {
            providers.add(gemmaProvider)
        }
        
        return providers
    }
}
