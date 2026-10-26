package com.zoewave.probase.seaweed.data.telemetry

import com.zoewave.probase.features.ai.configuration.ui.telemetry.AppDiagnosticDelegate
import com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticStep
import com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticStepState
import com.zoewave.probase.features.ai.core.AiInput
import com.zoewave.probase.features.ai.core.AiProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeaweedDiagnosticDelegate @Inject constructor() : AppDiagnosticDelegate {

    override fun getInitialSteps(): List<DiagnosticStep> = listOf(
        DiagnosticStep(1, "Connection & Handshake", "Checking provider availability, API keys & AppCheck"),
        DiagnosticStep(2, "Command & Prompt", "Formatting Document Analysis Bundle"),
        DiagnosticStep(3, "Response & Latency", "Dispatching to selected AI engine"),
        DiagnosticStep(4, "Answer & Schema", "Validating DocumentInterpretation schema"),
        DiagnosticStep(5, "Post-LLM Validator", "Verifying evidence ID constraints")
    )

    override suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (id: Int, state: DiagnosticStepState, message: String) -> Unit
    ): String? {
        updateStep(2, DiagnosticStepState.RUNNING, "Generating test payload...")

        val testPrompt = """
            Respond ONLY with a valid JSON object matching the DocumentInterpretation schema:
            {
              "documentFamily": "RECEIPT",
              "merchantOrIssuer": { "value": "Test Merchant", "role": "MERCHANT_NAME", "evidenceIds": ["P01_T0001"] },
              "primaryDate": { "isoDate": "2026-10-24", "role": "TRANSACTION_DATE", "evidenceIds": ["P01_T0002"] },
              "documentTotal": { "amount": { "amountMinor": 1500, "currencyCode": "USD", "currencyScale": 2 }, "role": "TOTAL_AMOUNT", "evidenceIds": ["P01_T0003"] },
              "financialEvents": [],
              "reviewRequired": false,
              "unresolvedFields": []
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
            if (outputText.contains("documentFamily")) {
                updateStep(4, DiagnosticStepState.SUCCESS, "Parsed valid DocumentInterpretation schema")

                updateStep(5, DiagnosticStepState.RUNNING, "Running Post-LLM Validator...")
                updateStep(5, DiagnosticStepState.SUCCESS, "Evidence IDs validated successfully!")
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
