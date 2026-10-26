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

    override val appName: String = "Seaweed Finance Engine"

    override fun getAuditLogs(): List<String> = listOf(
        "[1] CAPTURE & TINK CRYPTO",
        "[2] VISION & HOMOGRAPHY",
        "[3] SPATIAL LAYOUT & OCR",
        "[4] STRICT MONEY & CANDIDATE EXTRACTION",
        "[5] SIGNED-LEDGER DETERMINISTIC GATE",
        "[6] PII REDACTION & BUNDLE TRIMMING",
        "[7] AI SEMANTIC VALIDATION",
        "[8] SQLCIPHER & ENVELOPE INTERVENTION"
    )

    override suspend fun runDiagnostic(
        provider: AiProvider,
        updateStep: (id: Int, state: DiagnosticStepState, message: String) -> Unit
    ): com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticRunReport {
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
            val parsedRaw = com.zoewave.probase.seaweed.model.finance.validation.PostLlmSemanticValidator.parseStrictJson(outputText)
            
            if (parsedRaw != null) {
                updateStep(4, DiagnosticStepState.SUCCESS, "Parsed valid DocumentInterpretation schema")

                updateStep(5, DiagnosticStepState.RUNNING, "Running Post-LLM Validator...")
                
                // Construct synthetic bundle and valid IDs
                val syntheticBundle = com.zoewave.probase.seaweed.model.finance.validation.LlmAnalysisBundle(
                    documentId = "synthetic_test_doc",
                    chunkIndex = 0,
                    totalChunks = 1,
                    detectedFamilyCandidates = listOf(com.zoewave.probase.seaweed.model.finance.validation.DocumentFamily.RECEIPT),
                    lockedDeterministicFields = com.zoewave.probase.seaweed.model.finance.validation.LockedDeterministicFields(),
                    textCandidates = listOf(
                        com.zoewave.probase.seaweed.model.finance.validation.TextCandidate(
                            candidateId = "c1", role = com.zoewave.probase.seaweed.model.finance.validation.SemanticRole.MERCHANT_NAME,
                            rawText = "Test Merchant", normalizedValue = "TEST MERCHANT",
                            confidence = 0.9f, evidenceIds = listOf("P01_T0001")
                        )
                    ),
                    dateCandidates = listOf(
                        com.zoewave.probase.seaweed.model.finance.validation.DateCandidate("c2", com.zoewave.probase.seaweed.model.finance.validation.SemanticRole.TRANSACTION_DATE, "2026-10-24", 0.9f, listOf("P01_T0002"))
                    ),
                    amountCandidates = listOf(
                        com.zoewave.probase.seaweed.model.finance.validation.AmountCandidate(
                            "c3", com.zoewave.probase.seaweed.model.finance.validation.SemanticRole.TOTAL_AMOUNT,
                            com.zoewave.probase.seaweed.model.finance.CurrencyAmount(1500, "USD", 2), null, null, 0.9f, listOf("P01_T0003")
                        )
                    ),
                    tableRows = emptyList(),
                    arithmeticValidation = com.zoewave.probase.seaweed.model.finance.validation.ArithmeticValidationSummary("", true, null, 0, emptyList()),
                    allowedCategoryIds = emptyList()
                )
                
                val validOcrIds = setOf("P01_T0001", "P01_T0002", "P01_T0003")
                
                val sanitized = com.zoewave.probase.seaweed.model.finance.validation.PostLlmSemanticValidator.validateAndSanitize(parsedRaw, syntheticBundle, validOcrIds)
                
                if (sanitized.documentTotal?.amount?.amountMinor == 1500L && sanitized.merchantOrIssuer?.value == "Test Merchant") {
                    updateStep(5, DiagnosticStepState.SUCCESS, "Evidence IDs validated successfully!")
                    return com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticRunReport(true, provider.capability.displayName, duration, outputText)
                } else {
                    updateStep(5, DiagnosticStepState.FAILED, "Post-LLM Validator rejected fields (missing evidence IDs?)")
                    return com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticRunReport(false, provider.capability.displayName, duration, outputText, "Post-LLM Validator rejected fields")
                }
                
            } else {
                updateStep(4, DiagnosticStepState.FAILED, "Invalid JSON schema returned")
                updateStep(5, DiagnosticStepState.FAILED, "Skipped due to parsing failure")
                return com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticRunReport(false, provider.capability.displayName, duration, outputText, "Invalid JSON schema returned")
            }
        } else {
            val error = result.exceptionOrNull()
            updateStep(3, DiagnosticStepState.FAILED, "Execution failed: ${error?.localizedMessage}")
            updateStep(4, DiagnosticStepState.FAILED, "Pipeline halted")
            updateStep(5, DiagnosticStepState.FAILED, "Pipeline halted")
            return com.zoewave.probase.features.ai.configuration.ui.telemetry.DiagnosticRunReport(false, provider.capability.displayName, duration, null, error?.localizedMessage)
        }
    }
}
