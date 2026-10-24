package com.zoewave.probase.seaweed.model.finance.validation

import org.junit.Assert.*
import org.junit.Test

class BundleTrimmerRedactionAndBudgetTest {

    @Test
    fun `redactFreeText removes PII correctly`() {
        val input = "Contact john.doe123@example.co.uk or call (555) 123-4567. Account 1234-5678-9012-3456 charged."
        val result = BundleTrimmer.redactFreeText(input)

        assertFalse(result.contains("john.doe123@example.co.uk"))
        assertTrue(result.contains("[REDACTED_EMAIL]"))

        assertFalse(result.contains("(555) 123-4567"))
        assertTrue(result.contains("[REDACTED_PHONE]"))

        assertFalse(result.contains("1234-5678-9012-3456"))
        assertTrue(result.contains("****3456"))
    }

    @Test
    fun `prepareChunks fits oversized row into budget or rejects`() {
        // Create an artificially large row
        val hugeText = "A".repeat(20000)

        val hugeRow = ReconstructedTableRow(
            rowId = "R1", pageNumber = 1, rowIndex = 0,
            cells = listOf(
                ReconstructedTableCell("C1", 0, null, SemanticRole.LINE_ITEM_DESCRIPTION, hugeText, null, null, null, listOf("id1"))
            )
        )

        val bundle = LlmAnalysisBundle(
            documentId = "doc1", chunkIndex = 0, totalChunks = 1,
            detectedFamilyCandidates = emptyList(),
            lockedDeterministicFields = LockedDeterministicFields(),
            textCandidates = emptyList(),
            dateCandidates = emptyList(),
            amountCandidates = emptyList(),
            tableRows = listOf(hugeRow),
            arithmeticValidation = ArithmeticValidationSummary("", true, null, 0, emptyList()),
            allowedCategoryIds = emptyList()
        )

        val result = BundleTrimmer.prepareChunks(bundle)
        assertTrue(result is BundleTrimResult.BundleCannotFit)
        val error = result as BundleTrimResult.BundleCannotFit
        assertTrue(error.reason.contains("exceeds"))
    }
}
