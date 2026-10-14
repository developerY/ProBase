package com.zoewave.probase.kocolor.data.usecase

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import com.zoewave.probase.features.ai.local.data.PromptCacheRepository
import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.telemetry.StyleAuditLogger
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaEvaluator
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaScore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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
    private val validator = RecommendationValidator()
    private val fashionistaEvaluator = mockk<FashionistaEvaluator>()
    private val intentFulfillmentEvaluator = mockk<IntentFulfillmentEvaluator>()

    private lateinit var engine: StyleSimulatorEngine

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any<Throwable>()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any<Throwable>()) } returns 0

        every { cache.generateFingerprint(any(), any(), any(), any(), any(), any(), any(), any()) } returns "fp123"
        coEvery { cache.get(any()) } returns null
        coEvery { cache.put(any(), any()) } returns Unit

        every { fashionistaEvaluator.evaluate(any(), any()) } returns FashionistaScore(
            colorHarmonyScore = 80f,
            silhouetteScore = 80f,
            contrastScore = 80f,
            totalScore = 80f,
            isApproved = true
        )
        every { intentFulfillmentEvaluator.evaluate(any(), any(), any()) } returns IntentFulfillment()

        engine = StyleSimulatorEngine(
            contextEngine,
            candidateFilter,
            serializer,
            promptAssembler,
            capabilityRouter,
            cache,
            auditLogger,
            fallbackEngine,
            validator,
            fashionistaEvaluator,
            intentFulfillmentEvaluator
        )
    }

    @Test
    fun `generateBlueprint should return blueprint from primary provider when successful`() = runTest {
        val context = StyleRequestContext(intent = "work", weather = "sunny")
        val provider = mockk<AiProvider>()
        val capability = AiProviderCapability(
            id = "p1", displayName = "P1", maxInputTokens = 1000, maxOutputTokens = 10, timeoutMillis = 1000,
            isLocal = true
        )
        val item = ClothingItem(internalId = 1, name = "Shirt", category = ClothingCategory.TOPS, colorHex = "#000")

        every { provider.capability } returns capability
        coEvery { provider.countTokens(any()) } returns 100
        coEvery { provider.execute(any()) } returns Result.success("{\"rationale\": \"Primary AI\", \"selectedClothingIds\": [\"w_1\"], \"selectedCosmeticIds\": [], \"recommendedPalette\": []}")
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns listOf(provider)
        coEvery { contextEngine.generateSelectionState(any(), any(), any()) } returns StyleSelectionState(fullRankedCandidatePool = listOf(CandidateProvenance(clothingItem = item)))
        coEvery { candidateFilter.getCosmeticCandidateProvenance(any(), any(), any()) } returns emptyList()

        val result = engine.generateBlueprint(listOf(item), emptyList(), context)

        assertThat(result.rationale).isEqualTo("Primary AI")
    }

    @Test
    fun `generateBlueprint should fall back to deterministic engine when all providers fail`() = runTest {
        val context = StyleRequestContext(intent = "work", weather = "sunny")
        val provider = mockk<AiProvider>()
        val capability = AiProviderCapability(
            id = "p1", displayName = "P1", maxInputTokens = 1000, maxOutputTokens = 10, timeoutMillis = 1000,
            isLocal = true
        )
        val item = ClothingItem(internalId = 1, name = "Shirt", category = ClothingCategory.TOPS, colorHex = "#000")

        every { provider.capability } returns capability
        coEvery { provider.countTokens(any()) } returns 100
        coEvery { provider.execute(any()) } returns Result.failure(RuntimeException("Network error"))
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns listOf(provider)
        coEvery { contextEngine.generateSelectionState(any(), any(), any()) } returns StyleSelectionState(fullRankedCandidatePool = listOf(CandidateProvenance(clothingItem = item)))
        coEvery { candidateFilter.getCosmeticCandidateProvenance(any(), any(), any()) } returns emptyList()
        every { fallbackEngine.generate(any()) } returns StyleBlueprint("Fallback Rationale", emptyList(), emptyList(), emptyList())

        val result = engine.generateBlueprint(listOf(item), emptyList(), context)

        assertThat(result.rationale).isEqualTo("Fallback Rationale")
    }

    @Test
    fun `generateBlueprint should decode nested composition JSON structures seamlessly`() = runTest {
        val context = StyleRequestContext(intent = "dinner", weather = "cool")
        val provider = mockk<AiProvider>()
        val capability = AiProviderCapability(
            id = "p1", displayName = "P1", maxInputTokens = 2000, maxOutputTokens = 50, timeoutMillis = 1000, isLocal = true
        )
        val clothingItems = listOf(
            ClothingItem(internalId = 38L, name = "Top", category = ClothingCategory.TOPS, colorHex = "#000000"),
            ClothingItem(internalId = 32L, name = "Bottom", category = ClothingCategory.BOTTOMS, colorHex = "#000000"),
            ClothingItem(internalId = 48L, name = "Shoes", category = ClothingCategory.SHOES, colorHex = "#000000")
        )
        val cosmeticItems = listOf(
            CosmeticItem(internalId = 61L, name = "Eye Shadow", brand = "KoColor", macroCategory = MacroCategory.EYES, microCategory = MicroCategory.EYESHADOW, colorHex = "#000000"),
            CosmeticItem(internalId = 12L, name = "Lipstick", brand = "KoColor", macroCategory = MacroCategory.LIPS, microCategory = MicroCategory.LIPSTICK, colorHex = "#000000")
        )
        val provList = clothingItems.map { CandidateProvenance(clothingItem = it) }
        val cosmeticProvList = cosmeticItems.map { CandidateProvenance(cosmeticItem = it) }

        every { provider.capability } returns capability
        coEvery { provider.countTokens(any()) } returns 200
        
        // Simulating the nested JSON structure generated by LLM when using English headings
        val nestedJson = """
            {
              "explanation": "Harmonic autumn style",
              "clothing_composition": {
                "top": "w_38",
                "bottom": "w_32",
                "shoes": "w_48"
              },
              "cosmetic_composition": {
                "eye": "c_61",
                "lip": "c_12"
              },
              "palette": ["#1E88E5", "#D4AF37"]
            }
        """.trimIndent()

        coEvery { provider.execute(any()) } returns Result.success(nestedJson)
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns listOf(provider)
        coEvery { contextEngine.generateSelectionState(any(), any(), any()) } returns StyleSelectionState(fullRankedCandidatePool = provList)
        coEvery { candidateFilter.getCosmeticCandidateProvenance(any(), any(), any()) } returns cosmeticProvList

        val blueprint = engine.generateBlueprint(clothingItems, cosmeticItems, context)

        assertThat(blueprint.rationale).isEqualTo("Harmonic autumn style")
        assertThat(blueprint.selectedClothingIds).containsExactly("w_38", "w_32", "w_48")
        assertThat(blueprint.selectedCosmeticIds).containsExactly("c_61", "c_12")
        assertThat(blueprint.recommendedPalette).containsExactly("#1E88E5", "#D4AF37")
    }
}
