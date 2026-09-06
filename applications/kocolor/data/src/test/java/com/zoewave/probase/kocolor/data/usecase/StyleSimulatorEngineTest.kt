package com.zoewave.probase.kocolor.data.usecase

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import com.zoewave.probase.features.ai.local.data.PromptCacheRepository
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditLogger
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaEvaluator
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StyleSimulatorEngineTest {

    private val contextEngine = mockk<DeterministicContextEngine>()
    private val candidateFilter = mockk<WardrobeCandidateFilter>()
    private val serializer = CompactManifestSerializer()
    private val promptAssembler = PromptAssembler()
    private val capabilityRouter = mockk<CapabilityRouter>()
    private val cache = mockk<PromptCacheRepository>()
    private val auditLogger = mockk<StyleAuditLogger>(relaxed = true)
    private val fallbackEngine = mockk<DeterministicStyleEngine>()
    private val fashionistaEvaluator = mockk<FashionistaEvaluator>(relaxed = true)
    
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
            contextEngine,
            candidateFilter,
            serializer,
            promptAssembler,
            capabilityRouter,
            cache,
            auditLogger,
            fallbackEngine,
            RecommendationValidator(),
            fashionistaEvaluator
        )
    }

    @Test
    fun `generateBlueprint should return result from first successful provider`() = runTest {
        val context = StyleRequestContext(intent = "party", weather = "warm", appearanceTelemetry = ColorTelemetry())
        val provider1 = mockk<AiProvider>()
        val provider2 = mockk<AiProvider>()
        
        val capability1 = AiProviderCapability(
            id = "p1", displayName = "P1", maxInputTokens = 1000, maxOutputTokens = 10, timeoutMillis = 1000,
            isLocal = true
        )
        
        every { provider1.capability } returns capability1
        coEvery { provider1.countTokens(any()) } returns 50
        coEvery { provider1.execute(any()) } returns Result.success("{\"rationale\": \"P1 result\", \"selectedClothingIds\": [], \"selectedCosmeticIds\": [\"c_1\", \"c_2\", \"c_3\", \"c_4\"], \"recommendedPalette\": []}")
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns listOf(provider1, provider2)
        coEvery { contextEngine.generateSelectionState(any(), any(), any()) } returns StyleSelectionState()
        coEvery { candidateFilter.getCosmeticCandidateProvenance(any(), any(), any()) } returns emptyList()

        val result = engine.generateBlueprint(emptyList(), emptyList(), context)

        assertThat(result.rationale).isEqualTo("P1 result")
    }

    @Test
    fun `generateBlueprint should fallback to deterministic engine if all providers fail`() = runTest {
        val context = StyleRequestContext(intent = "party", weather = "warm", appearanceTelemetry = ColorTelemetry())
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns emptyList()
        every { fallbackEngine.generate(context) } returns StyleBlueprint("Fallback", emptyList(), emptyList(), emptyList())

        val result = engine.generateBlueprint(emptyList(), emptyList(), context)

        assertThat(result.rationale).isEqualTo("Fallback")
    }

    @Test
    fun `generateBlueprint should step-down context if provider budget exceeded`() = runTest {
        val context = StyleRequestContext(intent = "party", weather = "warm", appearanceTelemetry = ColorTelemetry())
        val provider = mockk<AiProvider>()
        val capability = AiProviderCapability(
            id = "p1", displayName = "P1", maxInputTokens = 500, maxOutputTokens = 10, timeoutMillis = 1000,
            maxCandidateAdditions = 10, minCandidateAdditions = 2, isLocal = true
        )
        
        val items = List(10) { 
            ClothingItem(internalId = it.toLong(), name = "Item $it", category = ClothingCategory.TOPS, colorHex = "#000000")
        }
        val provList = items.map { CandidateProvenance(clothingItem = it) }

        every { provider.capability } returns capability
        coEvery { provider.countTokens(any()) } answers {
            val input = it.invocation.args[0] as AiInput
            if (input.promptString.contains("Cotton")) 1000 else 200
        }
        coEvery { provider.execute(any()) } returns Result.success("{\"rationale\": \"Success\", \"selectedClothingIds\": [], \"selectedCosmeticIds\": [], \"recommendedPalette\": []}")
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns listOf(provider)
        coEvery { contextEngine.generateSelectionState(any(), any(), any()) } returns StyleSelectionState(fullRankedCandidatePool = provList)
        coEvery { candidateFilter.getCosmeticCandidateProvenance(any(), any(), any()) } returns emptyList()
        every { fallbackEngine.generate(any()) } returns StyleBlueprint("Fallback", emptyList(), emptyList(), emptyList())

        val result = engine.generateBlueprint(items, emptyList(), context)

        assertThat(result.rationale).isEqualTo("Success")
    }

    @Test
    fun `generateBlueprint should log non-empty reasoning set to audit logger`() = runTest {
        val context = StyleRequestContext(intent = "party", weather = "warm", appearanceTelemetry = ColorTelemetry())
        val provider = mockk<AiProvider>()
        val capability = AiProviderCapability(
            id = "p1", displayName = "P1", maxInputTokens = 1000, maxOutputTokens = 10, timeoutMillis = 1000,
            isLocal = true
        )
        
        val items = List(5) { 
            ClothingItem(internalId = it.toLong(), name = "Item $it", category = ClothingCategory.TOPS, colorHex = "#000000")
        }
        val provList = items.map { CandidateProvenance(clothingItem = it) }

        every { provider.capability } returns capability
        coEvery { provider.countTokens(any()) } returns 100
        coEvery { provider.execute(any()) } returns Result.success("{\"rationale\": \"Success\", \"selectedClothingIds\": [], \"selectedCosmeticIds\": [], \"recommendedPalette\": []}")
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns listOf(provider)
        coEvery { contextEngine.generateSelectionState(any(), any(), any()) } returns StyleSelectionState(fullRankedCandidatePool = provList)
        coEvery { candidateFilter.getCosmeticCandidateProvenance(any(), any(), any()) } returns emptyList()

        engine.generateBlueprint(items, emptyList(), context)

        verify { auditLogger.logReasoningSet(context.requestId, match { it.isNotEmpty() }) }
    }
}
