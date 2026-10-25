package com.zoewave.probase.seaweed.data.finance.pipeline

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.zoewave.probase.core.data.repository.AiConfigurationSettings
import com.zoewave.probase.seaweed.model.finance.ai.AiInterpretationRouter
import com.zoewave.probase.seaweed.model.finance.ai.RouterInterpretationOutcome
import com.zoewave.probase.seaweed.model.finance.validation.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceholderRouter @Inject constructor(
    private val aiSettings: AiConfigurationSettings
) : AiInterpretationRouter {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun interpretDocumentChunks(
        chunks: List<LlmAnalysisBundle>,
        validOcrIds: Set<String>,
        fallbackPartialInterpretation: DocumentInterpretation
    ): RouterInterpretationOutcome {
        if (chunks.isEmpty()) {
            return RouterInterpretationOutcome.DeterministicFallbackOnly(
                partialInterpretation = fallbackPartialInterpretation,
                fallbackReason = "Empty bundle chunks"
            )
        }

        val startTime = System.currentTimeMillis()
        val configuredModelName = aiSettings.aiModelFlow.firstOrNull()?.ifBlank { "gemini-3.8-flash" } ?: "gemini-3.8-flash"

        return try {
            val generativeModel = Firebase.ai(
                backend = GenerativeBackend.googleAI(),
                useLimitedUseAppCheckTokens = false
            ).generativeModel(
                modelName = configuredModelName,
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                }
            )

            val validatedChunkInterpretations = mutableListOf<DocumentInterpretation>()

            for (chunk in chunks) {
                val bundleJson = json.encodeToString(chunk)
                val prompt = content {
                    text("""
                        You are an expert financial document interpretation AI.
                        Analyze this redacted, candidate-bound financial document analysis bundle:
                        
                        $bundleJson
                        
                        STRICT RULES:
                        1. Every merchant, date, amount, or line item description you extract MUST cite the exact 'evidenceIds' present in the candidate/table-row input.
                        2. Do NOT invent fields or evidence IDs that do not exist in the candidates.
                        3. Respond ONLY with a valid JSON object matching the DocumentInterpretation schema:
                        {
                          "documentFamily": "RECEIPT" | "INVOICE_OR_UTILITY_BILL" | "BANK_STATEMENT" | "CREDIT_CARD_STATEMENT" | "REFUND_OR_CREDIT_NOTE" | "UNKNOWN",
                          "merchantOrIssuer": { "value": string, "role": "MERCHANT_NAME", "evidenceIds": [string] } or null,
                          "primaryDate": { "isoDate": "YYYY-MM-DD", "role": "TRANSACTION_DATE", "evidenceIds": [string] } or null,
                          "documentTotal": { "amount": { "amountMinor": number, "currencyCode": string, "currencyScale": number }, "role": "TOTAL_AMOUNT", "evidenceIds": [string] } or null,
                          "financialEvents": [
                             {
                               "eventKind": "PURCHASE_EXPENSE" | "UNPAID_BILL_LIABILITY" | "REFUND_OR_CREDIT",
                               "description": string,
                               "descriptionEvidenceIds": [string],
                               "amount": { "amountMinor": number, "currencyCode": string, "currencyScale": number },
                               "amountEvidenceIds": [string],
                               "isoDate": "YYYY-MM-DD",
                               "dateEvidenceIds": [string],
                               "categoryId": string,
                               "persistenceState": "VERIFIED_FINAL" | "NEEDS_USER_REVIEW_DRAFT"
                             }
                          ],
                          "reviewRequired": boolean,
                          "unresolvedFields": [string]
                        }
                    """.trimIndent())
                }

                val response = generativeModel.generateContent(prompt)
                val responseText = response.text ?: continue

                val parsedRaw = PostLlmSemanticValidator.parseStrictJson(responseText) ?: continue
                val sanitized = PostLlmSemanticValidator.validateAndSanitize(parsedRaw, chunk, validOcrIds)
                validatedChunkInterpretations += sanitized
            }

            if (validatedChunkInterpretations.isEmpty()) {
                return RouterInterpretationOutcome.DeterministicFallbackOnly(
                    partialInterpretation = fallbackPartialInterpretation,
                    fallbackReason = "AI returned invalid or non-validated JSON structure"
                )
            }

            val lockedFields = chunks.first().lockedDeterministicFields
            val mergedInterpretation = ChunkInterpretationMerger.mergeValidatedChunks(
                validatedChunks = validatedChunkInterpretations,
                lockedFields = lockedFields,
                docId = chunks.first().documentId
            )

            val latency = System.currentTimeMillis() - startTime
            RouterInterpretationOutcome.ValidatedSuccess(
                interpretation = mergedInterpretation,
                providerUsed = "Firebase AI Logic",
                modelIdUsed = configuredModelName,
                totalLatencyMs = latency
            )
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            RouterInterpretationOutcome.DeterministicFallbackOnly(
                partialInterpretation = fallbackPartialInterpretation,
                fallbackReason = "AI execution failed: ${e.localizedMessage}"
            )
        }
    }
}
