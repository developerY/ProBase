package com.zoewave.probase.seaweed.model.finance.validation

import com.zoewave.probase.seaweed.model.finance.CurrencyAmount
import org.junit.Assert.*
import org.junit.Test

class ValidatorProvenanceAndRoleTest {

    @Test
    fun `validateEvent rejects mixed evidence lists`() {
        val bundle = LlmAnalysisBundle(
            documentId = "doc1", chunkIndex = 0, totalChunks = 1,
            detectedFamilyCandidates = listOf(DocumentFamily.RECEIPT),
            lockedDeterministicFields = LockedDeterministicFields(lockedDocumentFamily = DocumentFamily.RECEIPT),
            textCandidates = listOf(
                TextCandidate(
                    candidateId = "c1", role = SemanticRole.MERCHANT_NAME,
                    rawText = "Store A", normalizedValue = "Store A",
                    confidence = 0.9f, evidenceIds = listOf("P01_T0014")
                )
            ),
            dateCandidates = listOf(
                DateCandidate("c2", SemanticRole.TRANSACTION_DATE, "2026-10-15", 0.9f, listOf("P01_T0015"))
            ),
            amountCandidates = listOf(
                AmountCandidate("c3", SemanticRole.TOTAL_AMOUNT, CurrencyAmount(1500, "USD", 2), null, null, 0.9f, listOf("P01_T0016"))
            ),
            tableRows = emptyList(),
            arithmeticValidation = ArithmeticValidationSummary("", true, null, 0, emptyList()),
            allowedCategoryIds = listOf("cat1")
        )

        val rawEvent = ResolvedFinancialEvent(
            eventKind = FinancialEventKind.PURCHASE_EXPENSE,
            description = "Store A",
            descriptionEvidenceIds = listOf("P01_T0014", "P01_T9999"), // MIXED IDS!
            amount = CurrencyAmount(1500, "USD", 2),
            amountEvidenceIds = listOf("P01_T0016"),
            isoDate = "2026-10-15",
            dateEvidenceIds = listOf("P01_T0015"),
            categoryId = "cat1",
            persistenceState = EventPersistenceState.VERIFIED_FINAL
        )

        val interpretation = DocumentInterpretation(
            documentFamily = DocumentFamily.RECEIPT,
            financialEvents = listOf(rawEvent),
            reviewRequired = false
        )

        val validOcrIds = setOf("P01_T0014", "P01_T0015", "P01_T0016", "P01_T9999")

        val sanitized = PostLlmSemanticValidator.validateAndSanitize(interpretation, bundle, validOcrIds)

        // Event should be rejected (dropped) because the mixed description evidence doesn't match the candidate
        assertTrue(sanitized.financialEvents.isEmpty())
        assertTrue(sanitized.unresolvedFields.contains("event.description"))
        assertTrue(sanitized.reviewRequired)
    }

    @Test
    fun `validateEvent rejects table-cell date if evidenceIds do not match`() {
        val cellDate = ReconstructedTableCell("cell1", 0, null, SemanticRole.TRANSACTION_DATE, "10/15/2026", null, null, "2026-10-15", listOf("P01_T0015"))
        val cellDesc = ReconstructedTableCell("cell2", 1, null, SemanticRole.LINE_ITEM_DESCRIPTION, "Item A", null, null, null, listOf("P01_T0014"))
        val cellAmt = ReconstructedTableCell("cell3", 2, null, SemanticRole.LINE_ITEM_AMOUNT, "15.00", CurrencyAmount(1500, "USD", 2), null, null, listOf("P01_T0016"))

        val row = ReconstructedTableRow("row1", 1, 0, listOf(cellDate, cellDesc, cellAmt))

        val bundle = LlmAnalysisBundle(
            documentId = "doc1", chunkIndex = 0, totalChunks = 1,
            detectedFamilyCandidates = listOf(DocumentFamily.RECEIPT),
            lockedDeterministicFields = LockedDeterministicFields(lockedDocumentFamily = DocumentFamily.RECEIPT),
            textCandidates = emptyList(),
            dateCandidates = emptyList(),
            amountCandidates = emptyList(),
            tableRows = listOf(row),
            arithmeticValidation = ArithmeticValidationSummary("", true, null, 0, emptyList()),
            allowedCategoryIds = listOf("cat1")
        )

        val rawEvent = ResolvedFinancialEvent(
            eventKind = FinancialEventKind.PURCHASE_EXPENSE,
            description = "Item A",
            descriptionEvidenceIds = listOf("P01_T0014"),
            amount = CurrencyAmount(1500, "USD", 2),
            amountEvidenceIds = listOf("P01_T0016"),
            isoDate = "2026-10-15",
            dateEvidenceIds = listOf("P01_T0099"), // MISMATCHED IDS!
            categoryId = "cat1",
            sourceRowId = "row1",
            persistenceState = EventPersistenceState.VERIFIED_FINAL
        )

        val interpretation = DocumentInterpretation(
            documentFamily = DocumentFamily.RECEIPT,
            financialEvents = listOf(rawEvent),
            reviewRequired = false
        )

        val validOcrIds = setOf("P01_T0014", "P01_T0015", "P01_T0016", "P01_T0099")

        val sanitized = PostLlmSemanticValidator.validateAndSanitize(interpretation, bundle, validOcrIds)

        // Event should NOT be dropped entirely, but its date should be nulled out and flagged for review
        assertEquals(1, sanitized.financialEvents.size)
        assertNull(sanitized.financialEvents[0].isoDate)
        assertTrue(sanitized.unresolvedFields.contains("event.isoDate"))
        assertTrue(sanitized.reviewRequired)
        assertEquals(EventPersistenceState.NEEDS_USER_REVIEW_DRAFT, sanitized.financialEvents[0].persistenceState)
    }

    @Test
    fun `validateEvent rejects billPaymentStatus if candidate text is contradictory`() {
        val bundle = LlmAnalysisBundle(
            documentId = "doc1", chunkIndex = 0, totalChunks = 1,
            detectedFamilyCandidates = listOf(DocumentFamily.INVOICE_OR_UTILITY_BILL),
            lockedDeterministicFields = LockedDeterministicFields(
                lockedDocumentFamily = DocumentFamily.INVOICE_OR_UTILITY_BILL,
                lockedPrimaryDate = ResolvedDateField("2026-10-15", SemanticRole.TRANSACTION_DATE, listOf("d1"))
            ),
            textCandidates = listOf(
                TextCandidate("c1", SemanticRole.MERCHANT_NAME, "Store", "Store", 0.9f, listOf("t1")),
                TextCandidate("c2", SemanticRole.PAYMENT_STATUS_LABEL, "AMOUNT DUE", "AMOUNT DUE", 0.9f, listOf("t2"))
            ),
            dateCandidates = emptyList(),
            amountCandidates = listOf(
                AmountCandidate("c3", SemanticRole.TOTAL_AMOUNT, CurrencyAmount(1500, "USD", 2), null, null, 0.9f, listOf("a1"))
            ),
            tableRows = emptyList(),
            arithmeticValidation = ArithmeticValidationSummary("", true, null, 0, emptyList()),
            allowedCategoryIds = listOf("cat1")
        )

        val rawEvent = ResolvedFinancialEvent(
            eventKind = FinancialEventKind.UNPAID_BILL_LIABILITY,
            description = "Store",
            descriptionEvidenceIds = listOf("t1"),
            amount = CurrencyAmount(1500, "USD", 2),
            amountEvidenceIds = listOf("a1"),
            isoDate = "2026-10-15",
            dateEvidenceIds = listOf("d1"),
            billPaymentStatus = BillPaymentStatus.PAID_IN_FULL, // LIED BY LLM! Text is AMOUNT DUE
            paymentStatusEvidenceIds = listOf("t2"),
            categoryId = "cat1",
            persistenceState = EventPersistenceState.VERIFIED_FINAL
        )

        val interpretation = DocumentInterpretation(
            documentFamily = DocumentFamily.INVOICE_OR_UTILITY_BILL,
            financialEvents = listOf(rawEvent),
            reviewRequired = false
        )

        val validOcrIds = setOf("t1", "t2", "a1", "d1")

        val sanitized = PostLlmSemanticValidator.validateAndSanitize(interpretation, bundle, validOcrIds)

        assertEquals(1, sanitized.financialEvents.size)
        assertEquals(BillPaymentStatus.UNKNOWN, sanitized.financialEvents[0].billPaymentStatus)
        assertTrue(sanitized.unresolvedFields.contains("event.billPaymentStatus"))
        assertTrue(sanitized.reviewRequired)
        assertEquals(EventPersistenceState.NEEDS_USER_REVIEW_DRAFT, sanitized.financialEvents[0].persistenceState)
    }
}
