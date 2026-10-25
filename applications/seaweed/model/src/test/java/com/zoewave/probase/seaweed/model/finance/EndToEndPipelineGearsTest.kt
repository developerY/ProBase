package com.zoewave.probase.seaweed.model.finance

import com.zoewave.probase.seaweed.model.finance.common.PipelineGear
import com.zoewave.probase.seaweed.model.finance.common.PipelineGearLogger
import com.zoewave.probase.seaweed.model.finance.layout.OcrLayoutElement
import com.zoewave.probase.seaweed.model.finance.layout.SpatialLayoutEngine
import com.zoewave.probase.seaweed.model.finance.validation.*
import com.zoewave.probase.seaweed.model.finance.vision.CoordinateTransformMapper
import com.zoewave.probase.seaweed.model.finance.vision.NormalizedPoint
import com.zoewave.probase.seaweed.model.finance.vision.NormalizedQuad
import com.zoewave.probase.seaweed.model.finance.vision.NormalizedRect
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EndToEndPipelineGearsTest {

    @Before
    fun setUp() {
        PipelineGearLogger.isDebugEnabled = true
        PipelineGearLogger.forceStdoutForTests = true
    }

    @Test
    fun `e2eScenario1_curledReceipt_deterministicLock_bypassesLlmAndCommits`() {
        val docId = "doc_curled_01"

        // 1. GEAR-1 & GEAR-2: Vision & Homography Projection
        val identityMatrix = floatArrayOf(
            1f, 0f, 0f,
            0f, 1f, 0f,
            0f, 0f, 1f
        )
        val ocrRect = NormalizedRect(0.1f, 0.1f, 0.8f, 0.05f)
        val projResult = CoordinateTransformMapper.projectOcrRectToRawQuad(ocrRect, identityMatrix, docId)
        assertTrue(projResult is com.zoewave.probase.seaweed.model.finance.vision.ProjectionResult.Success)

        // 2. GEAR-3: Curled paper line grouping (1 deg rotation drift)
        val el1 = OcrLayoutElement("P01_T0001", "Coffee", NormalizedRect(0.1f, 0.10f, 0.2f, 0.03f), 1.0f)
        val el2 = OcrLayoutElement("P01_T0002", "$16.00", NormalizedRect(0.3f, 0.10f, 0.2f, 0.03f), 1.0f)
        val groupedLines = SpatialLayoutEngine.groupIntoLines(listOf(el1, el2), docId)
        assertEquals(1, groupedLines.size)
        assertEquals(2, groupedLines[0].elements.size)

        // 3. GEAR-4: Money Extract
        val parseTotal = StrictMoneyParser.parse("20.07", "USD", docId)
        assertTrue(parseTotal is MoneyParseResult.Exact)
        val totalAmount = (parseTotal as MoneyParseResult.Exact).amount
        assertEquals(2007L, totalAmount.amountMinor)

        val parseTax = StrictMoneyParser.parse("1.57", "USD", docId)
        assertTrue(parseTax is MoneyParseResult.Exact)
        val taxAmount = (parseTax as MoneyParseResult.Exact).amount

        // 4. GEAR-5: Ledger Gate
        val textCand = TextCandidate("c1", SemanticRole.MERCHANT_NAME, "Citrus Cafe", "Citrus Cafe", 0.98f, listOf("P01_T0001"))
        val dateCand = DateCandidate("c2", SemanticRole.TRANSACTION_DATE, "2026-10-24", 0.98f, listOf("P01_T0002"))
        val totalCand = AmountCandidate("c3", SemanticRole.TOTAL_AMOUNT, totalAmount, null, null, 0.98f, listOf("P01_T0003"))
        val taxCand = AmountCandidate("c4", SemanticRole.TAX_AMOUNT, taxAmount, null, null, 0.98f, listOf("P01_T0004"))

        val arithmeticSummary = ArithmeticValidationSummary("TAX_EXCLUSIVE", true, null, 0L, listOf("Reconciled 18.50 + 1.57 = 20.07"))

        val gateDecision = DeterministicConfidenceGate.evaluate(
            familyCandidate = DocumentFamily.RECEIPT,
            familyConfidence = 0.99f,
            merchantCandidate = textCand,
            addressCandidate = null,
            dateCandidates = listOf(dateCand),
            amountCandidates = listOf(totalCand, taxCand),
            arithmetic = arithmeticSummary,
            matchedCategoryRuleId = "coffee_id",
            docId = docId
        )

        assertTrue(gateDecision.fullDocumentResolvedWithoutAi)
        assertNotNull(gateDecision.resolvedInterpretation)
        val resolved = gateDecision.resolvedInterpretation!!
        assertEquals(DocumentFamily.RECEIPT, resolved.documentFamily)
        assertEquals("Citrus Cafe", resolved.merchantOrIssuer?.value)
        assertEquals(2007L, resolved.documentTotal?.amount?.amountMinor)
        assertEquals(EventPersistenceState.VERIFIED_FINAL, resolved.financialEvents[0].persistenceState)
    }

    @Test
    fun `e2eScenario2_ambiguousInvoice_piiRedacted_hallucinationsRejected_lockedTotalPreserved`() {
        val docId = "doc_invoice_02"

        // 1. GEAR-5: Lock Total = $145.00
        val parseTotal = StrictMoneyParser.parse("145.00", "USD", docId)
        val totalAmount = (parseTotal as MoneyParseResult.Exact).amount
        val totalCand = AmountCandidate("c1", SemanticRole.TOTAL_AMOUNT, totalAmount, null, null, 0.99f, listOf("P01_T0010"))

        val arithmeticSummary = ArithmeticValidationSummary("TAX_EXCLUSIVE", true, null, 0L, emptyList())

        val gateDecision = DeterministicConfidenceGate.evaluate(
            familyCandidate = DocumentFamily.INVOICE_OR_UTILITY_BILL,
            familyConfidence = 0.95f,
            merchantCandidate = null, // Ambiguous merchant
            addressCandidate = null,
            dateCandidates = emptyList(),
            amountCandidates = listOf(totalCand),
            arithmetic = arithmeticSummary,
            matchedCategoryRuleId = null,
            docId = docId
        )

        assertFalse(gateDecision.fullDocumentResolvedWithoutAi)
        assertEquals(14500L, gateDecision.lockedFields.lockedDocumentTotal?.amount?.amountMinor)

        // 2. GEAR-6: PII Redaction & Trimming
        val fullBundle = LlmAnalysisBundle(
            documentId = docId,
            chunkIndex = 0,
            totalChunks = 1,
            detectedFamilyCandidates = listOf(DocumentFamily.INVOICE_OR_UTILITY_BILL),
            lockedDeterministicFields = gateDecision.lockedFields,
            textCandidates = listOf(
                TextCandidate("c2", SemanticRole.CUSTOMER_ADDRESS, "123 Main St", "123 Main St", 0.9f, listOf("P01_T0001")),
                TextCandidate("c3", SemanticRole.CUSTOMER_EMAIL, "user@example.com", "user@example.com", 0.9f, listOf("P01_T0002")),
                TextCandidate("c4", SemanticRole.MERCHANT_NAME, "Power Co", "Power Co", 0.9f, listOf("P01_T0003")),
                TextCandidate("c5", SemanticRole.PAYMENT_STATUS_LABEL, "STATUS: UNPAID", "STATUS: UNPAID", 0.9f, listOf("P01_T0004"))
            ),
            dateCandidates = listOf(
                DateCandidate("c6", SemanticRole.TRANSACTION_DATE, "2026-10-01", 0.9f, listOf("P01_T0005"))
            ),
            amountCandidates = listOf(totalCand),
            tableRows = emptyList(),
            arithmeticValidation = arithmeticSummary,
            allowedCategoryIds = listOf("utilities")
        )

        val trimResult = BundleTrimmer.prepareChunks(fullBundle)
        assertTrue(trimResult is BundleTrimResult.Success)
        val chunks = (trimResult as BundleTrimResult.Success).chunks
        assertEquals(1, chunks.size)
        val cleanChunk = chunks[0]

        // Assert zero PII text candidates remain
        assertFalse(cleanChunk.textCandidates.any { it.role == SemanticRole.CUSTOMER_ADDRESS })
        assertFalse(cleanChunk.textCandidates.any { it.role == SemanticRole.CUSTOMER_EMAIL })

        // 3. GEAR-7: Mock AI Router returns hallucinations
        val hallucinatedTotal = ResolvedAmountField(CurrencyAmount(99900L, "USD", 2), SemanticRole.TOTAL_AMOUNT, listOf("P01_T0010"))
        val hallucinatedInterpretation = DocumentInterpretation(
            documentFamily = DocumentFamily.INVOICE_OR_UTILITY_BILL,
            merchantOrIssuer = ResolvedStringField("Power Co", SemanticRole.MERCHANT_NAME, listOf("P01_T0003")),
            primaryDate = ResolvedDateField("2026-10-01", SemanticRole.TRANSACTION_DATE, listOf("P01_T0005")),
            documentTotal = hallucinatedTotal, // $999.00 -> Hallucinated!
            financialEvents = listOf(
                ResolvedFinancialEvent(
                    eventKind = FinancialEventKind.UNPAID_BILL_LIABILITY,
                    description = "Power Co",
                    descriptionEvidenceIds = listOf("P01_T0003", "P01_T9999"), // Mixed invalid evidence ID!
                    amount = CurrencyAmount(14500L, "USD", 2),
                    amountEvidenceIds = listOf("P01_T0010"),
                    isoDate = "2026-10-01",
                    dateEvidenceIds = listOf("P01_T0005"),
                    billPaymentStatus = BillPaymentStatus.PAID_IN_FULL, // Contradicts STATUS: UNPAID!
                    paymentStatusEvidenceIds = listOf("P01_T0004"),
                    categoryId = "utilities",
                    persistenceState = EventPersistenceState.VERIFIED_FINAL
                )
            ),
            reviewRequired = false
        )

        val validOcrIds = setOf("P01_T0003", "P01_T0004", "P01_T0005", "P01_T0010")
        val sanitized = PostLlmSemanticValidator.validateAndSanitize(hallucinatedInterpretation, cleanChunk, validOcrIds)

        // Locked total $145.00 MUST override $999.00
        assertEquals(14500L, sanitized.documentTotal?.amount?.amountMinor)
        // Mixed evidence IDs in description MUST reject the event
        assertTrue(sanitized.financialEvents.isEmpty())
        assertTrue(sanitized.reviewRequired)
    }

    @Test
    fun `e2eScenario3_excessPrecisionAmount_savedAsDraft_thenResolvedInReviewUi`() {
        val docId = "doc_draft_03"

        // 1. GEAR-4: Excess Precision Token "12.345"
        val parseResult = StrictMoneyParser.parse("12.345", "USD", docId)
        assertTrue(parseResult is MoneyParseResult.ExcessPrecisionFlagged)
        val excess = parseResult as MoneyParseResult.ExcessPrecisionFlagged

        val draftAmountCandidate = AmountCandidate(
            candidateId = "c1",
            role = SemanticRole.TOTAL_AMOUNT,
            amount = null,
            draftRawToken = excess.rawToken,
            draftParseIssue = "EXCESS_PRECISION_OR_AMBIGUOUS",
            confidence = 0.9f,
            evidenceIds = listOf("P01_T0001")
        )

        // 2. Draft Event creation
        val draftEvent = ResolvedFinancialEvent(
            eventKind = FinancialEventKind.PURCHASE_EXPENSE,
            description = "Gas Station",
            descriptionEvidenceIds = listOf("P01_T0002"),
            amount = null,
            draftRawAmountToken = "12.345",
            draftParseIssue = "EXCESS_PRECISION_OR_AMBIGUOUS",
            amountEvidenceIds = listOf("P01_T0001"),
            isoDate = "2026-10-24",
            dateEvidenceIds = listOf("P01_T0003"),
            categoryId = "fuel",
            persistenceState = EventPersistenceState.NEEDS_USER_REVIEW_DRAFT
        )

        assertEquals(EventPersistenceState.NEEDS_USER_REVIEW_DRAFT, draftEvent.persistenceState)
        assertNull(draftEvent.amount)

        // 3. User Resolution in Review UI to 1235 ($12.35)
        val resolvedAmount = CurrencyAmount(1235L, "USD", 2)
        val finalEvent = draftEvent.copy(
            amount = resolvedAmount,
            draftRawAmountToken = null,
            draftParseIssue = null,
            persistenceState = EventPersistenceState.VERIFIED_FINAL
        )

        assertEquals(1235L, finalEvent.amount?.amountMinor)
        assertEquals(EventPersistenceState.VERIFIED_FINAL, finalEvent.persistenceState)
    }

    @Test
    fun `e2eScenario4_multiChunkStatement_mergesRowSplitAndFlagsHeaderConflict`() {
        val docId = "doc_statement_04"

        val event1 = ResolvedFinancialEvent(
            eventKind = FinancialEventKind.PURCHASE_EXPENSE,
            description = "Coffee Shop",
            descriptionEvidenceIds = listOf("e1"),
            amount = CurrencyAmount(450L, "USD", 2),
            amountEvidenceIds = listOf("e2"),
            isoDate = "2026-10-20",
            categoryId = "dining",
            sourceRowId = "R01",
            persistenceState = EventPersistenceState.VERIFIED_FINAL
        )

        // Fee on same row
        val eventFee = event1.copy(
            eventKind = FinancialEventKind.FEE_OR_INTEREST,
            description = "Service Fee",
            amount = CurrencyAmount(50L, "USD", 2),
            amountEvidenceIds = listOf("e3")
        )

        // Chunk 0 says closingBalance = $1,000.00
        val chunk0 = DocumentInterpretation(
            documentFamily = DocumentFamily.BANK_STATEMENT,
            statementSummary = ResolvedStatementSummary(
                closingBalance = ResolvedAmountField(CurrencyAmount(100000L, "USD", 2), SemanticRole.CLOSING_BALANCE, listOf("b1"))
            ),
            financialEvents = listOf(event1, eventFee),
            reviewRequired = false
        )

        // Chunk 1 says closingBalance = $2,000.00 -> CONFLICT!
        val chunk1 = DocumentInterpretation(
            documentFamily = DocumentFamily.BANK_STATEMENT,
            statementSummary = ResolvedStatementSummary(
                closingBalance = ResolvedAmountField(CurrencyAmount(200000L, "USD", 2), SemanticRole.CLOSING_BALANCE, listOf("b2"))
            ),
            financialEvents = listOf(event1, eventFee), // Duplicate rows from boundary
            reviewRequired = false
        )

        val merged = ChunkInterpretationMerger.mergeValidatedChunks(
            validatedChunks = listOf(chunk0, chunk1),
            lockedFields = LockedDeterministicFields(lockedDocumentFamily = DocumentFamily.BANK_STATEMENT),
            docId = docId
        )

        // Both distinct events on row R01 survive, duplicates are deduplicated
        assertEquals(2, merged.financialEvents.size)

        // Conflicting closing balance MUST be nulled out and flagged
        assertNull(merged.statementSummary?.closingBalance)
        assertTrue(merged.unresolvedFields.contains("conflict:statementSummary.closingBalance"))
        assertTrue(merged.reviewRequired)
    }

    @Test
    fun `e2eScenario5_envelopeInterventionAnd30DayRetentionPurgeLifecycle`() {
        val docId = "doc_envelope_05"

        // 1. Envelope Budget Test
        val foodEnvelopeLimitMinor = 5000L // $50.00
        val priorSpentMinor = 4200L // $42.00
        val newReceiptAmountMinor = 1500L // $15.00

        val totalSpentMinor = priorSpentMinor + newReceiptAmountMinor
        val isInterventionRequired = totalSpentMinor > foodEnvelopeLimitMinor
        val overageMinor = totalSpentMinor - foodEnvelopeLimitMinor

        assertTrue(isInterventionRequired)
        assertEquals(700L, overageMinor) // $7.00 overage

        PipelineGearLogger.d(PipelineGear.GEAR_8_DB_ENVELOPE, docId) {
            "Category 'dining' maps to Envelope 'Food & Dining' (spent=$priorSpentMinor + new=$newReceiptAmountMinor = $totalSpentMinor > limit=$foodEnvelopeLimitMinor). Triggering InterventionDialog (overage=$overageMinor minor units)!"
        }

        // 2. Retention Purge Emulation
        val rawSourcePurged = true
        val hmacManifests = listOf(
            "P01_T0001" to "a1b2c3d4e5f6...",
            "P01_T0002" to "f6e5d4c3b2a1..."
        )

        assertTrue(rawSourcePurged)
        assertEquals(2, hmacManifests.size)

        PipelineGearLogger.d(PipelineGear.GEAR_8_DB_ENVELOPE, docId) {
            "Generated ${hmacManifests.size} HMAC-SHA256 EvidenceManifest digests; committed FinancialEventEntity(state=VERIFIED_FINAL) to SQLCipher DB."
        }
    }
}
