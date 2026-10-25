package com.zoewave.probase.seaweed.model.finance.validation

import com.zoewave.probase.seaweed.model.finance.CurrencyAmount
import com.zoewave.probase.seaweed.model.finance.common.PipelineGear
import com.zoewave.probase.seaweed.model.finance.common.PipelineGearLogger
import kotlinx.serialization.json.Json

object PostLlmSemanticValidator {

    private val strictJson = Json {
        ignoreUnknownKeys = false
        isLenient = false
    }

    private val PERMITTED_EVENT_DATE_ROLES = setOf(
        SemanticRole.TRANSACTION_DATE,
        SemanticRole.POSTING_DATE
    )

    fun parseStrictJson(rawOutput: String): DocumentInterpretation? {
        val trimmed = rawOutput.trim()
        val unwrapped = if (trimmed.startsWith("```")) {
            trimmed
                .removePrefix("```json")
                .removePrefix("```JSON")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
        } else {
            trimmed
        }
        return runCatching {
            strictJson.decodeFromString<DocumentInterpretation>(unwrapped)
        }.getOrNull()
    }

    fun validateAndSanitize(
        raw: DocumentInterpretation,
        bundle: LlmAnalysisBundle,
        validOcrIds: Set<String>
    ): DocumentInterpretation {
        val locked = bundle.lockedDeterministicFields
        val unresolved = raw.unresolvedFields.toMutableList()

        val validFamily = locked.lockedDocumentFamily
            ?: if (raw.documentFamily in bundle.detectedFamilyCandidates &&
                raw.documentFamily != DocumentFamily.UNKNOWN
            ) {
                raw.documentFamily
            } else {
                unresolved += "documentFamily"
                bundle.detectedFamilyCandidates.firstOrNull() ?: DocumentFamily.UNKNOWN
            }

        val cleanMerchant = locked.lockedMerchant
            ?: validateStringField(
                field = raw.merchantOrIssuer,
                allowedRoles = setOf(SemanticRole.MERCHANT_NAME),
                bundle = bundle,
                validOcrIds = validOcrIds
            ).also { if (raw.merchantOrIssuer != null && it == null) unresolved += "merchantOrIssuer" }

        val cleanAddress = locked.lockedIssuerAddress
            ?: validateStringField(
                field = raw.issuerAddress,
                allowedRoles = setOf(SemanticRole.ISSUER_ADDRESS),
                bundle = bundle,
                validOcrIds = validOcrIds
            ).also { if (raw.issuerAddress != null && it == null) unresolved += "issuerAddress" }

        val cleanPrimaryDate = locked.lockedPrimaryDate
            ?: validateDateField(
                field = raw.primaryDate,
                allowedRoles = setOf(SemanticRole.TRANSACTION_DATE, SemanticRole.POSTING_DATE),
                bundle = bundle,
                validOcrIds = validOcrIds
            ).also { if (raw.primaryDate != null && it == null) unresolved += "primaryDate" }

        val cleanDueDate = locked.lockedDueDate
            ?: validateDateField(
                field = raw.dueDate,
                allowedRoles = setOf(SemanticRole.DUE_DATE),
                bundle = bundle,
                validOcrIds = validOcrIds
            ).also { if (raw.dueDate != null && it == null) unresolved += "dueDate" }

        val cleanTotal = locked.lockedDocumentTotal
            ?: validateAmountField(
                field = raw.documentTotal,
                allowedRoles = setOf(SemanticRole.TOTAL_AMOUNT, SemanticRole.AMOUNT_DUE),
                bundle = bundle,
                validOcrIds = validOcrIds
            ).also { if (raw.documentTotal != null && it == null) unresolved += "documentTotal" }

        val cleanTax = locked.lockedTaxTotal
            ?: validateAmountField(
                field = raw.taxTotal,
                allowedRoles = setOf(SemanticRole.TAX_AMOUNT),
                bundle = bundle,
                validOcrIds = validOcrIds
            ).also { if (raw.taxTotal != null && it == null) unresolved += "taxTotal" }

        val cleanStatementSummary = raw.statementSummary?.let { summary ->
            validateStatementSummary(summary, bundle, validOcrIds, unresolved)
        }

        val cleanEvents = raw.financialEvents.mapNotNull { event ->
            validateEvent(
                event = event,
                documentFamily = validFamily,
                cleanPrimaryDate = cleanPrimaryDate,
                bundle = bundle,
                validOcrIds = validOcrIds,
                unresolved = unresolved
            )
        }

        val needsReview = raw.reviewRequired ||
            validFamily == DocumentFamily.UNKNOWN ||
            cleanTotal == null ||
            cleanPrimaryDate == null ||
            cleanEvents.any { it.persistenceState == EventPersistenceState.NEEDS_USER_REVIEW_DRAFT } ||
            unresolved.isNotEmpty()

        PipelineGearLogger.d(PipelineGear.GEAR_7_AI_VALIDATOR, bundle.documentId) {
            "Validated family=$validFamily eventsCount=${cleanEvents.size} reviewRequired=$needsReview unresolved=$unresolved"
        }

        return raw.copy(
            documentFamily = validFamily,
            merchantOrIssuer = cleanMerchant,
            issuerAddress = cleanAddress,
            primaryDate = cleanPrimaryDate,
            dueDate = cleanDueDate,
            documentTotal = cleanTotal,
            taxTotal = cleanTax,
            statementSummary = cleanStatementSummary,
            financialEvents = cleanEvents,
            reviewRequired = needsReview,
            unresolvedFields = unresolved.distinct()
        )
    }

    private fun allIdsOwnedBy(citedIds: List<String>, ownerIds: Collection<String>): Boolean =
        citedIds.isNotEmpty() && ownerIds.containsAll(citedIds)

    private fun normalizedTextEquals(a: String, b: String): Boolean =
        a.trim().replace(Regex("""\s+"""), " ").equals(
            b.trim().replace(Regex("""\s+"""), " "),
            ignoreCase = true
        )

    fun parsePaymentStatusFromText(rawText: String): BillPaymentStatus {
        val upper = rawText.trim().uppercase()
        return when {
            upper.contains("UNPAID") || upper.contains("NOT PAID") || upper.contains("AMOUNT DUE") ->
                BillPaymentStatus.UNPAID
            upper.contains("PAST DUE") || upper.contains("OVERDUE") ->
                BillPaymentStatus.OVERDUE
            upper.contains("PARTIAL") || upper.contains("PARTIALLY PAID") ->
                BillPaymentStatus.PARTIALLY_PAID
            upper.contains("PAID IN FULL") || upper == "PAID" || upper.contains("STATUS: PAID") ->
                BillPaymentStatus.PAID_IN_FULL
            else -> BillPaymentStatus.UNKNOWN
        }
    }

    private fun validateStringField(
        field: ResolvedStringField?,
        allowedRoles: Set<SemanticRole>,
        bundle: LlmAnalysisBundle,
        validOcrIds: Set<String>
    ): ResolvedStringField? {
        if (field == null || field.role !in allowedRoles) return null
        if (!allIdsOwnedBy(field.evidenceIds, validOcrIds)) return null
        val matched = bundle.textCandidates.any { candidate ->
            candidate.role == field.role &&
                (normalizedTextEquals(candidate.normalizedValue, field.value) ||
                    normalizedTextEquals(candidate.rawText, field.value)) &&
                allIdsOwnedBy(field.evidenceIds, candidate.evidenceIds)
        }
        return if (matched) field else null
    }

    private fun validateDateField(
        field: ResolvedDateField?,
        allowedRoles: Set<SemanticRole>,
        bundle: LlmAnalysisBundle,
        validOcrIds: Set<String>
    ): ResolvedDateField? {
        if (field == null || field.role !in allowedRoles) return null
        if (!allIdsOwnedBy(field.evidenceIds, validOcrIds)) return null
        val matched = bundle.dateCandidates.any { candidate ->
            candidate.role == field.role &&
                candidate.isoDate == field.isoDate &&
                allIdsOwnedBy(field.evidenceIds, candidate.evidenceIds)
        }
        return if (matched) field else null
    }

    private fun validateAmountField(
        field: ResolvedAmountField?,
        allowedRoles: Set<SemanticRole>,
        bundle: LlmAnalysisBundle,
        validOcrIds: Set<String>
    ): ResolvedAmountField? {
        if (field == null || field.role !in allowedRoles) return null
        if (!allIdsOwnedBy(field.evidenceIds, validOcrIds)) return null
        val matched = bundle.amountCandidates.any { candidate ->
            candidate.role == field.role &&
                candidate.amount != null &&
                candidate.amount == field.amount &&
                allIdsOwnedBy(field.evidenceIds, candidate.evidenceIds)
        }
        return if (matched) field else null
    }

    private fun validateStatementSummary(
        summary: ResolvedStatementSummary,
        bundle: LlmAnalysisBundle,
        validOcrIds: Set<String>,
        unresolved: MutableList<String>
    ): ResolvedStatementSummary? {
        val open = validateAmountField(
            summary.openingBalance,
            setOf(SemanticRole.OPENING_BALANCE),
            bundle,
            validOcrIds
        ).also { if (summary.openingBalance != null && it == null) unresolved += "statementSummary.openingBalance" }

        val close = validateAmountField(
            summary.closingBalance,
            setOf(SemanticRole.CLOSING_BALANCE, SemanticRole.TOTAL_AMOUNT),
            bundle,
            validOcrIds
        ).also { if (summary.closingBalance != null && it == null) unresolved += "statementSummary.closingBalance" }

        val minPay = validateAmountField(
            summary.minimumPaymentDue,
            setOf(SemanticRole.MINIMUM_PAYMENT_DUE),
            bundle,
            validOcrIds
        ).also { if (summary.minimumPaymentDue != null && it == null) unresolved += "statementSummary.minimumPaymentDue" }

        val start = validateDateField(
            summary.periodStartDate,
            setOf(SemanticRole.STATEMENT_PERIOD_START),
            bundle,
            validOcrIds
        ).also { if (summary.periodStartDate != null && it == null) unresolved += "statementSummary.periodStartDate" }

        val end = validateDateField(
            summary.periodEndDate,
            setOf(SemanticRole.STATEMENT_PERIOD_END),
            bundle,
            validOcrIds
        ).also { if (summary.periodEndDate != null && it == null) unresolved += "statementSummary.periodEndDate" }

        return if (open == null && close == null && minPay == null && start == null && end == null) {
            null
        } else {
            ResolvedStatementSummary(open, close, minPay, start, end)
        }
    }

    private fun validateEvent(
        event: ResolvedFinancialEvent,
        documentFamily: DocumentFamily,
        cleanPrimaryDate: ResolvedDateField?,
        bundle: LlmAnalysisBundle,
        validOcrIds: Set<String>,
        unresolved: MutableList<String>
    ): ResolvedFinancialEvent? {
        if (event.eventKind == FinancialEventKind.STATEMENT_BALANCE_IGNORE) return null
        if (!allIdsOwnedBy(event.amountEvidenceIds, validOcrIds)) {
            unresolved += "event.invalid_amount_evidence_ids"
            return null
        }
        if (!allIdsOwnedBy(event.descriptionEvidenceIds, validOcrIds)) {
            unresolved += "event.invalid_description_evidence_ids"
            return null
        }
        if (event.description.isBlank()) {
            unresolved += "event.blank_description"
            return null
        }

        val matchedRow = event.sourceRowId?.let { rowId ->
            bundle.tableRows.find { it.rowId == rowId }
        }
        if (event.sourceRowId != null && matchedRow == null) {
            unresolved += "event.invalid_source_row_id"
            return null
        }

        // 1. Validate description provenance against candidate or matched row cell
        val descriptionRoles = setOf(SemanticRole.LINE_ITEM_DESCRIPTION, SemanticRole.MERCHANT_NAME)
        val matchedDescCandidate = bundle.textCandidates.any { candidate ->
            candidate.role in descriptionRoles &&
                (normalizedTextEquals(candidate.rawText, event.description) ||
                    normalizedTextEquals(candidate.normalizedValue, event.description)) &&
                allIdsOwnedBy(event.descriptionEvidenceIds, candidate.evidenceIds)
        }
        val matchedDescCell = matchedRow?.cells?.any { cell ->
            cell.role in descriptionRoles &&
                normalizedTextEquals(cell.rawText, event.description) &&
                allIdsOwnedBy(event.descriptionEvidenceIds, cell.evidenceIds) &&
                allIdsOwnedBy(cell.evidenceIds, validOcrIds)
        } == true

        if (!matchedDescCandidate && !matchedDescCell) {
            unresolved += "event.description"
            return null
        }

        // 2. Validate amount or excess-precision draft token
        val permittedAmountRoles = permittedRolesForEventKind(event.eventKind)
        var verifiedAmount: CurrencyAmount? = null
        var draftToken: String? = null
        var draftIssue: String? = null

        if (event.amount != null) {
            val matchedCandidate = bundle.amountCandidates.any { candidate ->
                candidate.role in permittedAmountRoles &&
                    candidate.amount == event.amount &&
                    allIdsOwnedBy(event.amountEvidenceIds, candidate.evidenceIds)
            }
            val matchedCell = matchedRow?.cells?.any { cell ->
                cell.role in permittedAmountRoles &&
                    cell.parsedAmount == event.amount &&
                    allIdsOwnedBy(event.amountEvidenceIds, cell.evidenceIds) &&
                    allIdsOwnedBy(cell.evidenceIds, validOcrIds)
            } == true

            if (!matchedCandidate && !matchedCell) return null
            verifiedAmount = event.amount
        } else if (event.draftRawAmountToken != null) {
            val matchedDraftCandidate = bundle.amountCandidates.find { candidate ->
                candidate.role in permittedAmountRoles &&
                    candidate.draftRawToken == event.draftRawAmountToken &&
                    allIdsOwnedBy(event.amountEvidenceIds, candidate.evidenceIds)
            }
            val matchedDraftCell = matchedRow?.cells?.find { cell ->
                cell.role in permittedAmountRoles &&
                    cell.draftRawAmountToken == event.draftRawAmountToken &&
                    allIdsOwnedBy(event.amountEvidenceIds, cell.evidenceIds) &&
                    allIdsOwnedBy(cell.evidenceIds, validOcrIds)
            }
            if (matchedDraftCandidate == null && matchedDraftCell == null) return null
            draftToken = event.draftRawAmountToken
            draftIssue = matchedDraftCandidate?.draftParseIssue ?: "EXCESS_PRECISION_OR_AMBIGUOUS"
        } else {
            return null
        }

        // 3. Validate event date (table cell branch strictly enforces event.dateEvidenceIds ownership)
        val resolvedIsoDate: String?
        val resolvedDateIds: List<String>

        if (event.isoDate != null) {
            val matchedDateCandidate = bundle.dateCandidates.any { candidate ->
                candidate.role in PERMITTED_EVENT_DATE_ROLES &&
                    candidate.isoDate == event.isoDate &&
                    allIdsOwnedBy(event.dateEvidenceIds, candidate.evidenceIds) &&
                    allIdsOwnedBy(candidate.evidenceIds, validOcrIds)
            }
            val matchedRowCellDate = matchedRow?.cells?.any { cell ->
                cell.role in PERMITTED_EVENT_DATE_ROLES &&
                    cell.parsedIsoDate == event.isoDate &&
                    allIdsOwnedBy(event.dateEvidenceIds, cell.evidenceIds) &&
                    allIdsOwnedBy(cell.evidenceIds, validOcrIds)
            } == true

            if (matchedDateCandidate || matchedRowCellDate) {
                resolvedIsoDate = event.isoDate
                resolvedDateIds = event.dateEvidenceIds
            } else {
                // Never overwrite an invalid supplied row date with primaryDate
                resolvedIsoDate = null
                resolvedDateIds = emptyList()
                unresolved += "event.isoDate"
            }
        } else if (documentFamily in setOf(
                DocumentFamily.RECEIPT,
                DocumentFamily.INVOICE_OR_UTILITY_BILL,
                DocumentFamily.REFUND_OR_CREDIT_NOTE
            ) && cleanPrimaryDate != null
        ) {
            resolvedIsoDate = cleanPrimaryDate.isoDate
            resolvedDateIds = cleanPrimaryDate.evidenceIds
        } else {
            resolvedIsoDate = null
            resolvedDateIds = emptyList()
            unresolved += "event.isoDate"
        }

        // 4. Validate optional dueDateIso
        val validDueDateIso = event.dueDateIso?.takeIf { dueIso ->
            bundle.dateCandidates.any { candidate ->
                candidate.role == SemanticRole.DUE_DATE &&
                    candidate.isoDate == dueIso &&
                    allIdsOwnedBy(event.dueDateEvidenceIds, candidate.evidenceIds) &&
                    allIdsOwnedBy(candidate.evidenceIds, validOcrIds)
            }
        }

        // 5. Validate billPaymentStatus against actual PAYMENT_STATUS_LABEL text
        val validPaymentStatus = if (event.billPaymentStatus != BillPaymentStatus.UNKNOWN) {
            val statusCandidateMatch = bundle.textCandidates.any { candidate ->
                candidate.role == SemanticRole.PAYMENT_STATUS_LABEL &&
                    parsePaymentStatusFromText(candidate.rawText) == event.billPaymentStatus &&
                    allIdsOwnedBy(event.paymentStatusEvidenceIds, candidate.evidenceIds) &&
                    allIdsOwnedBy(candidate.evidenceIds, validOcrIds)
            }
            val statusCellMatch = matchedRow?.cells?.any { cell ->
                cell.role == SemanticRole.PAYMENT_STATUS_LABEL &&
                    parsePaymentStatusFromText(cell.rawText) == event.billPaymentStatus &&
                    allIdsOwnedBy(event.paymentStatusEvidenceIds, cell.evidenceIds) &&
                    allIdsOwnedBy(cell.evidenceIds, validOcrIds)
            } == true

            if (statusCandidateMatch || statusCellMatch) {
                event.billPaymentStatus
            } else {
                unresolved += "event.billPaymentStatus"
                BillPaymentStatus.UNKNOWN
            }
        } else {
            BillPaymentStatus.UNKNOWN
        }

        val cleanCategory = if (event.categoryId in bundle.allowedCategoryIds) {
            event.categoryId
        } else {
            "uncategorized"
        }

        val state = if (
            verifiedAmount != null &&
            resolvedIsoDate != null &&
            cleanCategory != "uncategorized" &&
            (event.billPaymentStatus == BillPaymentStatus.UNKNOWN || validPaymentStatus != BillPaymentStatus.UNKNOWN)
        ) {
            EventPersistenceState.VERIFIED_FINAL
        } else {
            EventPersistenceState.NEEDS_USER_REVIEW_DRAFT
        }

        return event.copy(
            amount = verifiedAmount,
            draftRawAmountToken = draftToken,
            draftParseIssue = draftIssue,
            isoDate = resolvedIsoDate,
            dateEvidenceIds = resolvedDateIds,
            dueDateIso = validDueDateIso,
            dueDateEvidenceIds = if (validDueDateIso != null) event.dueDateEvidenceIds else emptyList(),
            billPaymentStatus = validPaymentStatus,
            paymentStatusEvidenceIds = if (validPaymentStatus != BillPaymentStatus.UNKNOWN) {
                event.paymentStatusEvidenceIds
            } else {
                emptyList()
            },
            categoryId = cleanCategory,
            persistenceState = state
        )
    }

    private fun permittedRolesForEventKind(kind: FinancialEventKind): Set<SemanticRole> = when (kind) {
        FinancialEventKind.PURCHASE_EXPENSE -> setOf(
            SemanticRole.TOTAL_AMOUNT,
            SemanticRole.LINE_ITEM_AMOUNT,
            SemanticRole.DEBIT_AMOUNT
        )
        FinancialEventKind.UNPAID_BILL_LIABILITY -> setOf(
            SemanticRole.AMOUNT_DUE,
            SemanticRole.TOTAL_AMOUNT
        )
        FinancialEventKind.BILL_PAYMENT_SETTLEMENT,
        FinancialEventKind.INTERNAL_OR_CC_TRANSFER -> setOf(
            SemanticRole.TOTAL_AMOUNT,
            SemanticRole.DEBIT_AMOUNT,
            SemanticRole.CREDIT_AMOUNT
        )
        FinancialEventKind.REFUND_OR_CREDIT,
        FinancialEventKind.INCOME_DEPOSIT -> setOf(
            SemanticRole.TOTAL_AMOUNT,
            SemanticRole.CREDIT_AMOUNT
        )
        FinancialEventKind.FEE_OR_INTEREST -> setOf(
            SemanticRole.SURCHARGE_OR_FEE_AMOUNT,
            SemanticRole.DEBIT_AMOUNT,
            SemanticRole.LINE_ITEM_AMOUNT
        )
        FinancialEventKind.STATEMENT_BALANCE_IGNORE -> emptySet()
    }
}
