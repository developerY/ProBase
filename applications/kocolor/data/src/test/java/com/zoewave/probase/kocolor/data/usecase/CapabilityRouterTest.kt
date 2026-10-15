package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.core.AiProviderCapability
import com.zoewave.probase.features.ai.core.ByokAiProvider
import com.zoewave.probase.features.ai.firebase.FirebaseAiProvider
import com.zoewave.probase.features.ai.firebase.GemmaRestFallbackProvider
import com.zoewave.probase.features.ai.local.data.LocalNanoAiProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CapabilityRouterTest {

    private val localProvider: LocalNanoAiProvider = mockk(relaxed = true)
    private val byokProvider: ByokAiProvider = mockk(relaxed = true)
    private val firebaseProvider: FirebaseAiProvider = mockk(relaxed = true)
    private val gemmaProvider: GemmaRestFallbackProvider = mockk(relaxed = true)
    private val settings: AiConfigurationSettings = mockk(relaxed = true)

    private lateinit var router: CapabilityRouterImpl

    @Before
    fun setUp() {
        router = CapabilityRouterImpl(localProvider, byokProvider, firebaseProvider, gemmaProvider, settings)

        every { localProvider.capability } returns AiProviderCapability(
            id = "local_nano", displayName = "Local Nano", maxInputTokens = 2048, maxOutputTokens = 512, timeoutMillis = 3000, isLocal = true
        )
        every { byokProvider.capability } returns AiProviderCapability(
            id = "byok", displayName = "BYOK Cloud", maxInputTokens = 8192, maxOutputTokens = 1024, timeoutMillis = 5000, isLocal = false
        )
        every { firebaseProvider.capability } returns AiProviderCapability(
            id = "firebase_cloud", displayName = "Firebase Cloud", maxInputTokens = 8192, maxOutputTokens = 1024, timeoutMillis = 10000, isLocal = false
        )
        every { gemmaProvider.capability } returns AiProviderCapability(
            id = "gemma_4_cloud", displayName = "Gemma 4 Cloud (Free Tier)", maxInputTokens = 8192, maxOutputTokens = 2048, timeoutMillis = 15000, isLocal = false
        )
    }

    @Test
    fun testLocalNanoPriorityWhenAvailable() = runTest {
        coEvery { localProvider.isAvailable() } returns true
        coEvery { byokProvider.isAvailable() } returns true
        coEvery { firebaseProvider.isAvailable() } returns true
        coEvery { gemmaProvider.isAvailable() } returns true

        val ranked = router.getRankedAvailableProviders()

        assertThat(ranked).isNotEmpty()
        assertThat(ranked.first().capability.id).isEqualTo("local_nano")
    }

    @Test
    fun testByokPriorityWhenLocalNanoUnavailable() = runTest {
        coEvery { localProvider.isAvailable() } returns false
        coEvery { settings.useByokKey } returns flowOf(true)
        coEvery { settings.getGeminiApiKey() } returns "valid-test-key"
        coEvery { settings.aiModelFlow } returns flowOf("gemini-2.5-flash")
        coEvery { byokProvider.isAvailable() } returns true

        val ranked = router.getRankedAvailableProviders()

        assertThat(ranked.first().capability.id).isEqualTo("byok")
    }

    @Test
    fun testGemmaFallbackWhenCloudProvidersUnavailable() = runTest {
        coEvery { localProvider.isAvailable() } returns false
        coEvery { byokProvider.isAvailable() } returns false
        coEvery { settings.useFirebaseVertexAi } returns flowOf(false)
        coEvery { gemmaProvider.isAvailable() } returns true

        val activeName = router.getActiveProviderName()

        assertThat(activeName).isEqualTo("Gemma 4 Cloud (Free Tier)")
    }

    @Test
    fun testDeterministicFallbackWhenAllUnavailable() = runTest {
        coEvery { localProvider.isAvailable() } returns false
        coEvery { byokProvider.isAvailable() } returns false
        coEvery { settings.useFirebaseVertexAi } returns flowOf(false)
        coEvery { gemmaProvider.isAvailable() } returns false

        val activeName = router.getActiveProviderName()

        assertThat(activeName).isEqualTo("Math/Deterministic (No AI)")
    }
}
