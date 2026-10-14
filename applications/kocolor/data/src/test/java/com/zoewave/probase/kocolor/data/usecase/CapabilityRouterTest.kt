package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.core.AiProviderCapability
import com.zoewave.probase.features.ai.core.ByokAiProvider
import com.zoewave.probase.features.ai.firebase.FirebaseAiProvider
import com.zoewave.probase.features.ai.local.data.LocalNanoAiProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CapabilityRouterTest {

    private val localProvider: LocalNanoAiProvider = mockk()
    private val firebaseProvider: FirebaseAiProvider = mockk()
    private val byokProvider: ByokAiProvider = mockk(relaxed = true)
    private val settings: AiConfigurationSettings = mockk(relaxed = true)

    private lateinit var router: CapabilityRouterImpl

    @Before
    fun setUp() {
        router = CapabilityRouterImpl(localProvider, firebaseProvider, byokProvider, settings)

        every { byokProvider.capability } returns AiProviderCapability(
            id = "byok", displayName = "BYOK Cloud", maxInputTokens = 8192, maxOutputTokens = 1024, timeoutMillis = 5000, isLocal = false
        )
        every { localProvider.capability } returns AiProviderCapability(
            id = "local_nano", displayName = "Local Nano", maxInputTokens = 2048, maxOutputTokens = 512, timeoutMillis = 3000, isLocal = true
        )
        every { firebaseProvider.capability } returns AiProviderCapability(
            id = "firebase_cloud", displayName = "Firebase Cloud", maxInputTokens = 8192, maxOutputTokens = 1024, timeoutMillis = 10000, isLocal = false
        )
    }

    @Test
    fun testByokPriorityWhenAvailable() = runTest {
        coEvery { settings.useByokKey } returns flowOf(true)
        coEvery { settings.getGeminiApiKey() } returns "valid-test-key"
        coEvery { settings.aiModelFlow } returns flowOf("gemini-3.8-flash")
        coEvery { byokProvider.isAvailable() } returns true
        coEvery { localProvider.isAvailable() } returns true
        coEvery { firebaseProvider.isAvailable() } returns true

        val ranked = router.getRankedAvailableProviders()

        assertThat(ranked).isNotEmpty()
        assertThat(ranked.first().capability.id).isEqualTo("byok")
    }

    @Test
    fun testFirebaseFallbackWhenByokDisabled() = runTest {
        coEvery { settings.useByokKey } returns flowOf(false)
        coEvery { settings.useFirebaseVertexAi } returns flowOf(true)
        coEvery { byokProvider.isAvailable() } returns false
        coEvery { localProvider.isAvailable() } returns false
        coEvery { firebaseProvider.isAvailable() } returns true

        val activeName = router.getActiveProviderName()

        assertThat(activeName).isEqualTo("Firebase Cloud")
    }

    @Test
    fun testDeterministicFallbackWhenAllUnavailable() = runTest {
        coEvery { settings.useByokKey } returns flowOf(false)
        coEvery { settings.useFirebaseVertexAi } returns flowOf(false)
        coEvery { localProvider.isAvailable() } returns false

        val activeName = router.getActiveProviderName()

        assertThat(activeName).isEqualTo("Math/Deterministic (No AI)")
    }
}
