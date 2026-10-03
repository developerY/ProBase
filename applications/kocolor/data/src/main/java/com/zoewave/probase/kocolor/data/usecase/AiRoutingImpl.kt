package com.zoewave.probase.kocolor.data.usecase

import kotlinx.coroutines.flow.firstOrNull

import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.ByokAiProvider
import com.zoewave.probase.features.ai.firebase.FirebaseAiProvider
import com.zoewave.probase.features.ai.local.data.LocalNanoAiProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityRouterImpl @Inject constructor(
    private val localProvider: LocalNanoAiProvider,
    private val firebaseProvider: FirebaseAiProvider,
    private val byokProvider: ByokAiProvider,
    private val settings: com.zoewave.probase.core.data.repository.AiConfigurationSettings
) : CapabilityRouter {

    override suspend fun getRankedAvailableProviders(): List<AiProvider> {
        val isEnabled = settings.isAiEnabledFlow.firstOrNull() ?: true
        if (!isEnabled) {
            return emptyList()
        }

        val providers = mutableListOf<AiProvider>()
        
        // Priority 1: BYOK with Gemini API Key
        val apiKey = settings.getGeminiApiKey()
        byokProvider.setApiKey(apiKey)
        if (byokProvider.isAvailable()) {
            providers.add(byokProvider)
        }
        
        // Priority 2: Local Nano (On-Device AI)
        if (localProvider.isAvailable()) {
            providers.add(localProvider)
        }
        
        // Priority 3: Firebase AI Logic (Managed Cloud Fallback)
        val useFirebase = settings.useFirebaseVertexAi.firstOrNull() ?: true
        if (useFirebase && firebaseProvider.isAvailable()) {
            providers.add(firebaseProvider)
        }
        
        return providers
    }
}
