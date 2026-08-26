package com.zoewave.probase.kocolor.data.usecase

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import com.zoewave.probase.features.ai.local.data.PromptCacheRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StyleSimulatorIntegrationTest {

    private val candidateFilter = mockk<WardrobeCandidateFilter>()
    private val serializer = CompactManifestSerializer()
    private val promptAssembler = PromptAssembler()
    private val capabilityRouter = mockk<CapabilityRouter>()
    private val cache = mockk<PromptCacheRepository>()
    private val fallbackEngine = mockk<DeterministicStyleEngine>()
    
    private lateinit var engine: StyleSimulatorEngine

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        every { cache.generateFingerprint(any(), any(), any(), any(), any(), any(), any(), any()) } returns "test_hash"
        every { cache.get(any()) } returns null
        every { cache.put(any(), any()) } just Runs

        engine = StyleSimulatorEngine(
            candidateFilter,
            serializer,
            promptAssembler,
            capabilityRouter,
            cache,
            fallbackEngine
        )
    }

    @Test
    fun `full pipeline test with successful provider`() = runTest {
        val context = StyleRequestContext(intent = "party", weather = "warm", appearanceTelemetry = "warm")
        val provider = mockk<AiProvider>()
        val capability = AiProviderCapability(
            id = "test_ai", displayName = "Test AI", maxInputTokens = 1000, maxOutputTokens = 500, timeoutMillis = 2000,
            isLocal = true
        )
        val items = listOf(
            ClothingItem(internalId = 1, name = "Silk Top", category = ClothingCategory.TOPS, colorHex = "#FF0000")
        )

        every { provider.capability } returns capability
        coEvery { provider.countTokens(any()) } returns 200
        coEvery { provider.execute(any()) } returns Result.success("{\"rationale\": \"Harmonic look\", \"selectedClothingIds\": [\"w_1\"], \"selectedCosmeticIds\": [], \"recommendedPalette\": [\"#FF0000\"]}")
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns listOf(provider)
        coEvery { candidateFilter.getCandidates(any(), any()) } returns items

        val result = engine.generateBlueprint(context)

        assertThat(result.rationale).isEqualTo("Harmonic look")
        assertThat(result.selectedClothingIds).contains("w_1")
    }

    @Test
    fun `full pipeline test fallback logic`() = runTest {
        val context = StyleRequestContext(intent = "party", weather = "warm", appearanceTelemetry = "warm")
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns emptyList()
        every { fallbackEngine.generate(context) } returns StyleBlueprint("Fallback Rationale", emptyList(), emptyList(), emptyList())

        val result = engine.generateBlueprint(context)

        assertThat(result.rationale).isEqualTo("Fallback Rationale")
    }
}
