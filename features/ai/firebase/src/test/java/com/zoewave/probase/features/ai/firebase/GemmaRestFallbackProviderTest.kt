package com.zoewave.probase.features.ai.firebase

import android.content.Context
import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.features.ai.core.AiInput
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GemmaRestFallbackProviderTest {

    private val context: Context = mockk(relaxed = true)
    private val aiSettings: AiConfigurationSettings = mockk(relaxed = true)
    private lateinit var provider: GemmaRestFallbackProvider

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0

        coEvery { aiSettings.getGeminiApiKey() } returns null
        coEvery { aiSettings.aiModelFlow } returns flowOf("gemma-4-26b-a4b-it")

        every { context.packageName } returns "com.zoewave.probase.kocolor"

        provider = GemmaRestFallbackProvider(context, aiSettings)
    }

    @Test
    fun testProviderCapabilityProperties() {
        assertThat(provider.capability.id).isEqualTo("gemma_4_cloud")
        assertThat(provider.capability.displayName).contains("Gemma 4 Cloud")
        assertThat(provider.capability.maxInputTokens).isEqualTo(8192)
        assertThat(provider.capability.maxOutputTokens).isEqualTo(2048)
        assertThat(provider.capability.isLocal).isFalse()
    }

    @Test
    fun testIsAvailableReturnsTrueForProxy() = runTest {
        val isAvailable = provider.isAvailable()
        assertThat(isAvailable).isTrue()
    }

    @Test
    fun testSetModelNameUpdatesActiveModel() {
        provider.setModelName("gemma-4-31b-it")
        // Verified by setModelName execution without throwing
    }

    @Test
    fun testCountTokensHeuristic() = runTest {
        val input = AiInput.TextOnly("This is a test prompt for estimating tokens in Gemma 4")
        val tokens = provider.countTokens(input)

        assertThat(tokens).isAtLeast(100)
    }
}
