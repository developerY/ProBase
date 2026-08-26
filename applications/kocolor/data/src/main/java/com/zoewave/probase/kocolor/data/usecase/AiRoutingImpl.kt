package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.firebase.FirebaseAiProvider
import com.zoewave.probase.features.ai.local.data.LocalNanoAiProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CapabilityRouterImpl @Inject constructor(
    private val localProvider: LocalNanoAiProvider,
    private val firebaseProvider: FirebaseAiProvider
) : CapabilityRouter {

    override suspend fun getRankedAvailableProviders(): List<AiProvider> {
        val providers = mutableListOf<AiProvider>()
        
        // Priority 1: Local Nano (Free, Private, Fast)
        if (localProvider.isAvailable()) {
            providers.add(localProvider)
        }
        
        // Priority 2: Firebase AI Logic (Managed Cloud)
        if (firebaseProvider.isAvailable()) {
            providers.add(firebaseProvider)
        }
        
        return providers
    }
}
