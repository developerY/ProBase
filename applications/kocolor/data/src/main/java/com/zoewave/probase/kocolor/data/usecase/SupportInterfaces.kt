package com.zoewave.probase.kocolor.data.usecase

import com.zoewave.probase.features.ai.core.AiProvider

interface CapabilityRouter {
    suspend fun getRankedAvailableProviders(): List<AiProvider>
}

interface DeterministicStyleEngine {
    fun generate(context: StyleRequestContext): StyleBlueprint
}
