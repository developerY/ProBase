package com.zoewave.probase.kocolor.data.usecase

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.features.ai.core.AiProviderCapability
import com.zoewave.probase.features.ai.core.StylePromptRequest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StyleSimulatorEngineTest {

    private val candidateFilter = mockk<WardrobeCandidateFilter>()
    private val serializer = CompactManifestSerializer()
    private val promptAssembler = PromptAssembler()
    private val capabilityRouter = mockk<CapabilityRouter>()
    private val fallbackEngine = mockk<DeterministicStyleEngine>()
    
    private lateinit var engine: StyleSimulatorEngine

    @Before
    fun setup() {
        engine = StyleSimulatorEngine(
            candidateFilter,
            serializer,
            promptAssembler,
            capabilityRouter,
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
            // Simulate that expanded manifest with 10 items is > 50 tokens, but minimal/fewer items is < 50
            if (prompt.contains("Cotton")) 100 else if (prompt.lines().size > 5) 60 else 30
        }
        coEvery { provider.execute(any()) } returns Result.success("{\"rationale\": \"Success\", \"selectedClothingIds\": [], \"selectedCosmeticIds\": [], \"recommendedPalette\": []}")
        
        coEvery { capabilityRouter.getRankedAvailableProviders() } returns listOf(provider)
        coEvery { candidateFilter.getCandidates(any(), any()) } answers { items.take(it.invocation.args[1] as Int) }

        val result = engine.generateBlueprint(context)

        assertThat(result.rationale).isEqualTo("Success")
    }
}
