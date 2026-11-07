package com.zoewave.probase.features.ai.core

import android.graphics.Bitmap
import android.util.Log
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ByokAiProviderTest {

    private lateinit var provider: ByokAiProvider

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns false

        provider = ByokAiProvider()
    }

    @Test
    fun execute_multimodalInput_throwsUnsupportedOperationException() = runTest {
        val mockBitmap = mockk<Bitmap>(relaxed = true)
        val input = AiInput.Multimodal(promptString = "Test prompt", localImage = mockBitmap)

        val result = provider.execute(input)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(UnsupportedOperationException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("BYOK Provider does not support multimodal inputs")
    }

    @Test
    fun countTokens_heuristic_returnsCharLengthDividedByFour() = runTest {
        val input = AiInput.TextOnly("1234567890123456") // 16 chars -> 4 tokens
        val count = provider.countTokens(input)
        assertThat(count).isEqualTo(4)
    }

    @Test
    fun execute_unconfiguredApiKey_returnsFailure() = runTest {
        val input = AiInput.TextOnly("Hello")
        val result = provider.execute(input)
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).contains("BYOK Provider not configured")
    }
}
