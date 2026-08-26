package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import com.zoewave.probase.features.ai.core.StylePromptRequest
import com.zoewave.probase.features.ai.local.data.PromptCacheRepository
import io.mockk.*
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import android.util.Log

class StyleSimulatorEngineTest {

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
    fun `generateBlueprint should return result from first successful provider`() = runTest {
        val context = StyleRequestContext(intent = "party", weather = "warm", appearanceTelemetry = "warm")
        val provider1 = mockk<AiProvider>()
        val provider2 = mockk<AiProvider>()
        
        val capability1 = AiProviderCapability(
            id = "p1", displayName = "P1", maxInputTokens = 100, maxOutputTokens = 10, timeoutMillis = 1000,
            isLocal = true
        )
        
        every { provider1.capability } returns capability1
        coEvery { provider1.countTokens(any()) } returns 50
        coEvery { provider1.execute(any()) } returns Result.success("{\"rationale\": \"P1 result\", \"selectedClothingIds\": [], \"selectedCosmeticIds\": [], \"recommendedPalette\": []}")
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns listOf(provider1, provider2)
        coEvery { candidateFilter.getCandidates(any(), any()) } returns emptyList()

        val result = engine.generateBlueprint(context)

        assertThat(result.rationale).isEqualTo("P1 result")
    }

    @Test
    fun `generateBlueprint should fallback to deterministic engine if all providers fail`() = runTest {
        val context = StyleRequestContext(intent = "party", weather = "warm", appearanceTelemetry = "warm")
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns emptyList()
        every { fallbackEngine.generate(context) } returns StyleBlueprint("Fallback", emptyList(), emptyList(), emptyList())

        val result = engine.generateBlueprint(context)

        assertThat(result.rationale).isEqualTo("Fallback")
    }

    @Test
    fun `generateBlueprint should step-down context if provider budget exceeded`() = runTest {
        val context = StyleRequestContext(intent = "party", weather = "warm", appearanceTelemetry = "warm")
        val provider = mockk<AiProvider>()
        val capability = AiProviderCapability(
            id = "p1", displayName = "P1", maxInputTokens = 50, maxOutputTokens = 10, timeoutMillis = 1000,
            initialTopK = 10, minTopK = 2, isLocal = true
        )
        
        val items = List(10) { 
            ClothingItem(internalId = it.toLong(), name = "Item $it", category = ClothingCategory.TOPS, colorHex = "#000000")
        }

        every { provider.capability } returns capability
        coEvery { provider.countTokens(any()) } answers {
            val prompt = (it.invocation.args[0] as StylePromptRequest).exactPromptString
            // Simulate:
            // 1. Expanded (with Cotton) is 100 tokens
            // 2. Balanced (more than 4 bars '|') is 60 tokens
            // 3. Minimal (exactly 3 bars '|') is 30 tokens
            when {
                prompt.contains("Cotton") -> 100
                prompt.count { it == '|' } > 40 -> 60 // 10 items * 5 or 6 bars
                else -> 30
            }
        }
        coEvery { provider.execute(any()) } returns Result.success("{\"rationale\": \"Success\", \"selectedClothingIds\": [], \"selectedCosmeticIds\": [], \"recommendedPalette\": []}")
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns listOf(provider)
        coEvery { candidateFilter.getCandidates(any(), any()) } answers { items.take(it.invocation.args[1] as Int) }
        every { fallbackEngine.generate(any()) } returns StyleBlueprint("Fallback", emptyList(), emptyList(), emptyList())

        val result = engine.generateBlueprint(context)

        assertThat(result.rationale).isEqualTo("Success")
    }
}
