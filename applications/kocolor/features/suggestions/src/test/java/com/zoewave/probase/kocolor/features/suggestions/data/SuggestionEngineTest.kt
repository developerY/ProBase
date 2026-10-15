package com.zoewave.probase.kocolor.features.suggestions.data

import android.os.Process
import android.os.StrictMode
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.FashionProfile
import com.zoewave.probase.core.model.ritual.SeasonalType
import com.zoewave.probase.core.model.ritual.Undertone
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SuggestionEngineTest {

    private lateinit var engine: SuggestionEngine

    @Before
    fun setUp() {
        mockkStatic(Process::class)
        mockkStatic(StrictMode::class)
        every { Process.myPid() } returns 1234
        every { StrictMode.allowThreadDiskReads() } returns mockk(relaxed = true)
        every { StrictMode.setThreadPolicy(any()) } returns Unit
        engine = SuggestionEngine()
    }

    @Test
    fun testGetPersonalizedAdviceHandlesExceptionGracefully() = runTest {
        val profile = FashionProfile(
            seasonalType = SeasonalType.SUMMER,
            undertone = Undertone.COOL
        )

        // When Firebase.ai fails in unit test environment, it should return fallback FashionAdvice cleanly without crashing
        val advice = engine.getPersonalizedAdvice(profile, "Date Night", "mock-api-key")

        assertThat(advice.seasonalType).isEqualTo(SeasonalType.SUMMER)
        assertThat(advice.undertone).isEqualTo(Undertone.COOL)
        assertThat(advice.summary).contains("Error getting suggestions")
    }
}
