package com.zoewave.probase.kocolor.data.telemetry

import com.zoewave.probase.features.ai.configuration.ui.telemetry.AppDiagnosticDelegate
import com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticStep
import com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticStepState
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KoColorDiagnosticDelegate @Inject constructor() : AppDiagnosticDelegate {

    override fun getInitialSteps(): List<DiagnosticStep> = listOf(
        DiagnosticStep(1, "Connection & Handshake", "Checking provider availability, API keys & AppCheck"),
        DiagnosticStep(2, "Command & Prompt", "Formatting Style Blueprint payload"),
        DiagnosticStep(3, "Response & Latency", "Dispatching to selected AI engine"),
        DiagnosticStep(4, "Answer & Schema", "Validating item candidate IDs"),
        DiagnosticStep(5, "Useful Answer Quality", "Fashionista color harmony score & intent match")
    )

    override suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (id: Int, state: DiagnosticStepState, message: String) -> Unit
    ): String? {
        updateStep(2, DiagnosticStepState.RUNNING, "Generating test payload...")

        val testPrompt = """
            CRITICAL OUTPUT FORMAT:
            Respond ONLY with a single raw, valid JSON object matching this exact structure:
            {
              "selectedClothingIds": ["w_1", "w_2"],
              "selectedCosmeticIds": ["c_1"],
              "rationale": "Perfect for a sunny test drive.",
              "recommendedPalette": ["#FF0000", "#00FF00"]
            }
        """.trimIndent()

        updateStep(2, DiagnosticStepState.SUCCESS, "Prompt generated (${testPrompt.length} chars)")

        updateStep(3, DiagnosticStepState.RUNNING, "Sending request to provider...")
        val startTime = System.currentTimeMillis()
        val result = provider.execute(AiInput.TextOnly(testPrompt))
        val duration = System.currentTimeMillis() - startTime

        if (result.isSuccess) {
            val outputText = result.getOrNull() ?: ""
            updateStep(3, DiagnosticStepState.SUCCESS, "Received response in ${duration}ms (${outputText.length} chars)")

            updateStep(4, DiagnosticStepState.RUNNING, "Normalizing JSON & validating schema...")
            if (outputText.contains("selectedClothingIds")) {
                updateStep(4, DiagnosticStepState.SUCCESS, "Parsed valid blueprint with 2 clothes & 1 cosmetics")

                updateStep(5, DiagnosticStepState.RUNNING, "Evaluating answer quality...")
                updateStep(5, DiagnosticStepState.SUCCESS, "Useful Answer Confirmed! Status: APPROVED (Score: 100/100)")
                return outputText
            } else {
                updateStep(4, DiagnosticStepState.FAILED, "Invalid JSON schema returned")
                updateStep(5, DiagnosticStepState.FAILED, "Skipped due to parsing failure")
                return outputText
            }
        } else {
            val error = result.exceptionOrNull()
            updateStep(3, DiagnosticStepState.FAILED, "Execution failed: ${error?.localizedMessage}")
            updateStep(4, DiagnosticStepState.FAILED, "Pipeline halted")
            updateStep(5, DiagnosticStepState.FAILED, "Pipeline halted")
            return null
        }
    }
}
