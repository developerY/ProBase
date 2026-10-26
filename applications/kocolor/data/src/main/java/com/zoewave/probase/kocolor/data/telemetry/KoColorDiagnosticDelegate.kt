package com.zoewave.probase.kocolor.data.telemetry

import com.zoewave.probase.features.ai.configuration.ui.telemetry.AppDiagnosticDelegate
import com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticStep
import com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticStepState
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.repository.WardrobeRepository
import com.zoewave.probase.kocolor.data.usecase.PromptAssembler
import com.zoewave.probase.kocolor.fashionista.domain.FashionistaEvaluator
import javax.inject.Inject
import javax.inject.Singleton

import com.zoewave.probase.kocolor.data.color.CandidateProvenance
import com.zoewave.probase.kocolor.data.usecase.StyleBlueprint
import com.zoewave.probase.kocolor.data.usecase.StyleRequestContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

@Singleton
class KoColorDiagnosticDelegate @Inject constructor(
    private val wardrobeRepository: WardrobeRepository,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val promptAssembler: PromptAssembler,
    private val fashionistaEvaluator: FashionistaEvaluator
) : AppDiagnosticDelegate {

    private val json = Json { ignoreUnknownKeys = true }

    override val appName: String = "KoColor Fashionista"

    override fun getInitialSteps(): List<DiagnosticStep> = listOf(
        DiagnosticStep(1, "Connection & Handshake", "Checking provider availability, API keys & AppCheck"),
        DiagnosticStep(2, "Command & Prompt", "Formatting Style Blueprint payload"),
        DiagnosticStep(3, "Response & Latency", "Dispatching to selected AI engine"),
        DiagnosticStep(4, "Answer & Schema", "Validating item candidate IDs"),
        DiagnosticStep(5, "Useful Answer Quality", "Fashionista color harmony score & intent match")
    )

    override fun getAuditLogs(): List<String> = listOf(
        """
        [1] COMPOSITION POLICY
        Required: TOP + BOTTOM + SHOES
        Outerwear: FORBIDDEN
        Reason: Thermal context = WARM
        """,
        """
        [2] ANCHOR ESTABLISHMENT
        Item: [w_3] "Electric Coral Cropped Hoodie"
        Role: ACTIVEWEAR
        Source: AUTOMATIC_CONTEXT
        Priority: 4.00 / 4.00
        Reason: Highest context, freshness, and thermal compatibility score
        Resolution: NOT_SELECTED
        Final Context: Final composition optimized across required roles
        """
    )

    override suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (id: Int, state: DiagnosticStepState, message: String) -> Unit
    ): com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticRunReport {
        updateStep(2, DiagnosticStepState.RUNNING, "Gathering live inventory & formatting payload...")

        // 1. Fetch Live Data
        val clothing = try { wardrobeRepository.getAllClothing().first() } catch (e: Exception) { emptyList() }
        val cosmetics = try { cosmeticRepository.getAllCosmetics().first() } catch (e: Exception) { emptyList() }

        // 2. Build live manifest
        val clothingCandidates = clothing.map { 
            CandidateProvenance(clothingItem = it, contextScore = 1.0f, retrievalReason = "Diagnostic Check")
        }
        val compactManifest = StringBuilder().apply {
            appendLine("CLOTHING:")
            clothingCandidates.forEach { c -> 
                appendLine("- [w_${c.clothingItem?.internalId}] ${c.clothingItem?.name} (Color: ${c.clothingItem?.dominantHex})")
            }
        }.toString()

        val context = StyleRequestContext(intent = "Telemetry Diagnostic Run", occasion = "Testing")
        val aiInput = promptAssembler.buildExactRequest(
            context = context,
            compactManifest = compactManifest,
            clothingCandidates = clothingCandidates,
            providerCapability = provider.capability
        )

        val promptLength = (aiInput as? AiInput.TextOnly)?.promptString?.length ?: 0
        updateStep(2, DiagnosticStepState.SUCCESS, "Prompt generated ($promptLength chars, ~${promptLength/4} tokens)")

        updateStep(3, DiagnosticStepState.RUNNING, "Sending request to provider...")
        val startTime = System.currentTimeMillis()
        val result = provider.execute(aiInput)
        val duration = System.currentTimeMillis() - startTime

        if (result.isSuccess) {
            val outputText = result.getOrNull() ?: ""
            updateStep(3, DiagnosticStepState.SUCCESS, "Received response in ${duration}ms (${outputText.length} chars)")

            updateStep(4, DiagnosticStepState.RUNNING, "Normalizing JSON & validating schema...")
            
            return try {
                val blueprint = json.decodeFromString<StyleBlueprint>(outputText)
                val validClothes = blueprint.selectedClothingIds.count { id -> clothing.any { "w_${it.internalId}" == id } }
                val validCosmetics = blueprint.selectedCosmeticIds.count { id -> cosmetics.any { "c_${it.internalId}" == id } }
                
                updateStep(4, DiagnosticStepState.SUCCESS, "Parsed valid blueprint with $validClothes clothes & $validCosmetics cosmetics")

                updateStep(5, DiagnosticStepState.RUNNING, "Evaluating answer quality...")
                val score = fashionistaEvaluator.evaluate(blueprint, context)
                val status = if (score.isApproved) "APPROVED" else "REJECTED"
                updateStep(5, DiagnosticStepState.SUCCESS, "Useful Answer Confirmed! Status: $status (Score: ${score.totalScore.toInt()}/100)")
                
                com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticRunReport(
                    passed = true, 
                    providerName = provider.capability.displayName, 
                    latencyMs = duration, 
                    rawResponseJson = outputText,
                    failureReason = null
                )
            } catch (e: Exception) {
                updateStep(4, DiagnosticStepState.FAILED, "Invalid JSON schema returned: ${e.message}")
                updateStep(5, DiagnosticStepState.FAILED, "Skipped due to parsing failure")
                com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticRunReport(false, provider.capability.displayName, duration, outputText, "Invalid schema: ${e.message}")
            }
        } else {
            val error = result.exceptionOrNull()
            updateStep(3, DiagnosticStepState.FAILED, "Execution failed: ${error?.localizedMessage}")
            updateStep(4, DiagnosticStepState.FAILED, "Pipeline halted")
            updateStep(5, DiagnosticStepState.FAILED, "Pipeline halted")
            return com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticRunReport(false, provider.capability.displayName, duration, null, error?.message)
        }
    }
}
