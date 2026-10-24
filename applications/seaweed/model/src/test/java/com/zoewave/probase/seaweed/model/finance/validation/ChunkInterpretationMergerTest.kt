package com.zoewave.probase.seaweed.model.finance.validation

import com.zoewave.probase.seaweed.model.finance.CurrencyAmount
import org.junit.Assert.*
import org.junit.Test

class ChunkInterpretationMergerTest {

    @Test
    fun `mergeValidatedChunks flags documentTotal conflict and requires review`() {
        val chunk1 = DocumentInterpretation(
            documentFamily = DocumentFamily.RECEIPT,
            documentTotal = ResolvedAmountField(CurrencyAmount(1000, "USD", 2), SemanticRole.TOTAL_AMOUNT, listOf("1")),
            reviewRequired = false
        )

        val chunk2 = DocumentInterpretation(
            documentFamily = DocumentFamily.RECEIPT,
            documentTotal = ResolvedAmountField(CurrencyAmount(2000, "USD", 2), SemanticRole.TOTAL_AMOUNT, listOf("2")),
            reviewRequired = false
        )

        val merged = ChunkInterpretationMerger.mergeValidatedChunks(
            listOf(chunk1, chunk2),
            LockedDeterministicFields(lockedDocumentFamily = DocumentFamily.RECEIPT)
        )

        assertNull(merged.documentTotal)
        assertTrue(merged.unresolvedFields.contains("conflict:documentTotal"))
        assertTrue(merged.reviewRequired)
    }

    @Test
    fun `mergeValidatedChunks deduplicates events properly`() {
        val event1 = ResolvedFinancialEvent(
            eventKind = FinancialEventKind.PURCHASE_EXPENSE,
            description = "Store",
            descriptionEvidenceIds = listOf("desc1"),
            amount = CurrencyAmount(1000, "USD", 2),
            amountEvidenceIds = listOf("amt1"),
            isoDate = "2026-10-15",
            categoryId = "cat1",
            sourceRowId = "R1",
            persistenceState = EventPersistenceState.VERIFIED_FINAL
        )

        // Exact duplicate
        val event2 = event1.copy()

        // Different event on same row (e.g. fee)
        val event3 = event1.copy(
            amount = CurrencyAmount(10, "USD", 2),
            amountEvidenceIds = listOf("fee1")
        )

        val chunk1 = DocumentInterpretation(DocumentFamily.RECEIPT, financialEvents = listOf(event1), reviewRequired = false)
        val chunk2 = DocumentInterpretation(DocumentFamily.RECEIPT, financialEvents = listOf(event2, event3), reviewRequired = false)

        val merged = ChunkInterpretationMerger.mergeValidatedChunks(
            listOf(chunk1, chunk2),
            LockedDeterministicFields(lockedDocumentFamily = DocumentFamily.RECEIPT, lockedDocumentTotal = ResolvedAmountField(CurrencyAmount(1010, "USD", 2), SemanticRole.TOTAL_AMOUNT, listOf("1")), lockedPrimaryDate = ResolvedDateField("2026-10-15", SemanticRole.TRANSACTION_DATE, listOf("2")))
        )

        assertEquals(2, merged.financialEvents.size)
    }
}
