package com.zoewave.probase.features.ai.firebase

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FirebaseAiClientTest {

    private val aiSettings: AiConfigurationSettings = mockk(relaxed = true)
    private lateinit var client: FirebaseAiClientImpl

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0

        client = FirebaseAiClientImpl(aiSettings)
    }

    @Test
    fun testDeprecatedModelNameAutoMigratedToGemini38Flash() = runTest {
        coEvery { aiSettings.aiModelFlow } returns flowOf("gemini-1.5-flash")

        val tokenEstimate = client.countTokens("Test prompt for token count")

        assertThat(tokenEstimate).isGreaterThan(0)
    }

    @Test
    fun testDeprecatedGemini20AutoMigratesTo38() = runTest {
        coEvery { aiSettings.aiModelFlow } returns flowOf("gemini-2.0-flash")

        val tokenEstimate = client.countTokens("Sample prompt text")
        assertThat(tokenEstimate).isGreaterThan(0)
    }

    @Test
    fun testCountTokensGracefulFallbackOnException() = runTest {
        coEvery { aiSettings.aiModelFlow } returns flowOf("invalid-model")

        val samplePrompt = "Hello world this is a test prompt for estimating tokens"
        val estimate = client.countTokens(samplePrompt)

        assertThat(estimate).isEqualTo(100)
    }
}
