package com.zoewave.probase.seaweed.model.finance.validation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed class BundleTrimResult {
    data class Success(val chunks: List<LlmAnalysisBundle>) : BundleTrimResult()
    data class BundleCannotFit(val reason: String, val offendingRowId: String?) : BundleTrimResult()
}

object BundleTrimmer {
    const val MAX_UTF8_BYTES = 12_288
    private const val INITIAL_ROW_CHUNK_SIZE = 15

    private val EXCLUDED_PII_ROLES = setOf(
        SemanticRole.CUSTOMER_ADDRESS,
        SemanticRole.ACCOUNT_HOLDER_NAME,
        SemanticRole.CUSTOMER_PHONE,
        SemanticRole.CUSTOMER_EMAIL
    )

    private val EMAIL_REGEX = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val PHONE_REGEX = Regex("""(?<!\d)(?:\+?\d{1,3}[\s.-]?)?\(?\d{3}\)?[\s.-]\d{3}[\s.-]\d{4}(?!\d)""")
    private val ACCOUNT_SEQUENCE_REGEX = Regex("""(?<!\d)(?:\d[\s-]?){7,18}\d(?!\d)""")

    private val compactJson = Json { encodeDefaults = false }

    fun redactFreeText(input: String): String =
        input
            .replace(EMAIL_REGEX, "[REDACTED_EMAIL]")
            .replace(PHONE_REGEX, "[REDACTED_PHONE]")
            .replace(ACCOUNT_SEQUENCE_REGEX) { match ->
                val digitsOnly = match.value.filter { it.isDigit() }
                if (digitsOnly.length >= 8) "****${digitsOnly.takeLast(4)}" else match.value
            }

    private fun redactLockedFields(locked: LockedDeterministicFields): LockedDeterministicFields =
        locked.copy(
            lockedMerchant = locked.lockedMerchant?.let {
                it.copy(value = redactFreeText(it.value))
            },
            lockedIssuerAddress = locked.lockedIssuerAddress?.let {
                it.copy(value = redactFreeText(it.value))
            }
        )

    private fun redactArithmeticSummary(summary: ArithmeticValidationSummary): ArithmeticValidationSummary =
        summary.copy(
            reconciliationRuleUsed = redactFreeText(summary.reconciliationRuleUsed),
            notes = summary.notes.map { redactFreeText(it) }
        )

    fun prepareChunks(fullBundle: LlmAnalysisBundle): BundleTrimResult {
        val redactedLocked = redactLockedFields(fullBundle.lockedDeterministicFields)
        val redactedArithmetic = redactArithmeticSummary(fullBundle.arithmeticValidation)

        val stratifiedText = fullBundle.textCandidates
            .filter { it.role !in EXCLUDED_PII_ROLES }
            .groupBy { it.role }
            .values
            .flatMap { list -> list.sortedByDescending { it.confidence }.take(3) }
            .map {
                it.copy(
                    rawText = redactFreeText(it.rawText),
                    normalizedValue = redactFreeText(it.normalizedValue)
                )
            }

        val stratifiedDates = fullBundle.dateCandidates
            .groupBy { it.role }
            .values
            .flatMap { list -> list.sortedByDescending { it.confidence }.take(3) }

        val stratifiedAmounts = fullBundle.amountCandidates
            .groupBy { it.role }
            .values
            .flatMap { list -> list.sortedByDescending { it.confidence }.take(4) }

        val redactedRows = fullBundle.tableRows.map { row ->
            row.copy(
                cells = row.cells
                    .filter { it.role !in EXCLUDED_PII_ROLES }
                    .map { cell ->
                        cell.copy(
                            headerLabel = cell.headerLabel?.let { redactFreeText(it) },
                            rawText = redactFreeText(cell.rawText)
                        )
                    }
            )
        }

        if (redactedRows.isEmpty()) {
            val single = fullBundle.copy(
                chunkIndex = 0,
                totalChunks = 1,
                lockedDeterministicFields = redactedLocked,
                textCandidates = stratifiedText,
                dateCandidates = stratifiedDates,
                amountCandidates = stratifiedAmounts,
                tableRows = emptyList(),
                arithmeticValidation = redactedArithmetic
            )
            val fitted = fitSingleChunkToBudget(single)
                ?: return BundleTrimResult.BundleCannotFit("Header candidates exceed $MAX_UTF8_BYTES bytes", null)
            return BundleTrimResult.Success(listOf(fitted))
        }

        val builtChunks = mutableListOf<LlmAnalysisBundle>()
        var cursor = 0
        while (cursor < redactedRows.size) {
            var window = INITIAL_ROW_CHUNK_SIZE.coerceAtMost(redactedRows.size - cursor)
            var fittedChunk: LlmAnalysisBundle? = null

            while (window >= 1) {
                val candidateChunk = fullBundle.copy(
                    lockedDeterministicFields = redactedLocked,
                    textCandidates = stratifiedText,
                    dateCandidates = stratifiedDates,
                    amountCandidates = stratifiedAmounts,
                    tableRows = redactedRows.subList(cursor, cursor + window),
                    arithmeticValidation = redactedArithmetic
                )
                val fitted = fitSingleChunkToBudget(candidateChunk)
                if (fitted != null) {
                    fittedChunk = fitted
                    break
                }
                window /= 2
            }

            if (fittedChunk == null) {
                return BundleTrimResult.BundleCannotFit(
                    reason = "Single table row exceeds $MAX_UTF8_BYTES bytes even after context stripping",
                    offendingRowId = redactedRows[cursor].rowId
                )
            }

            builtChunks += fittedChunk
            cursor += window
        }

        val total = builtChunks.size
        return BundleTrimResult.Success(
            builtChunks.mapIndexed { idx, chunk ->
                chunk.copy(chunkIndex = idx, totalChunks = total)
            }
        )
    }

    private fun fitSingleChunkToBudget(bundle: LlmAnalysisBundle): LlmAnalysisBundle? {
        if (measureUtf8Bytes(bundle) <= MAX_UTF8_BYTES) return bundle

        val withoutGeneric = bundle.copy(
            textCandidates = bundle.textCandidates.filter { it.role != SemanticRole.GENERIC_LINE }
        )
        if (measureUtf8Bytes(withoutGeneric) <= MAX_UTF8_BYTES) return withoutGeneric

        val minimalContext = withoutGeneric.copy(
            textCandidates = withoutGeneric.textCandidates.take(6),
            amountCandidates = withoutGeneric.amountCandidates.take(8)
        )
        return if (measureUtf8Bytes(minimalContext) <= MAX_UTF8_BYTES) minimalContext else null
    }

    fun measureUtf8Bytes(bundle: LlmAnalysisBundle): Int =
        compactJson.encodeToString(bundle).encodeToByteArray().size
}
