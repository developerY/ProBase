package com.zoewave.probase.kocolor.features.seasonal_trends.domain

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class GeminiServiceTest {

    private val aiSettings: AiConfigurationSettings = mockk(relaxed = true)
    private lateinit var geminiService: GeminiService

    @Before
    fun setUp() {
        geminiService = GeminiService(aiSettings)
    }

    @Test
    fun testGenerateContentThrowsWhenApiKeyIsMissing() = runTest {
        coEvery { aiSettings.isGeminiApiKeySetFlow } returns flowOf(false)
        coEvery { aiSettings.getGeminiApiKey() } returns null

        val exception = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                geminiService.generateContent("Test seasonal trends prompt")
            }
        }

        assertThat(exception.message).contains("Gemini API Key is not configured")
    }

    @Test
    fun testGenerateContentThrowsWhenApiKeyIsBlank() = runTest {
        coEvery { aiSettings.isGeminiApiKeySetFlow } returns flowOf(true)
        coEvery { aiSettings.getGeminiApiKey() } returns "   "

        val exception = assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking {
                geminiService.generateContent("Test seasonal trends prompt")
            }
        }

        assertThat(exception.message).contains("Gemini API Key is not configured")
    }
}
