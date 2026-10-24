package com.zoewave.probase.seaweed.model.finance.validation

import com.zoewave.probase.seaweed.model.finance.CurrencyAmount

object ChunkInterpretationMerger {

    private data class EventDedupKey(
        val sourceRowId: String?,
        val eventKind: FinancialEventKind,
        val amount: CurrencyAmount?,
        val draftRawAmountToken: String?,
        val isoDate: String?,
        val normalizedDescription: String,
        val sortedAmountEvidenceIds: List<String>
    )

    fun mergeValidatedChunks(
        validatedChunks: List<DocumentInterpretation>,
        lockedFields: LockedDeterministicFields
    ): DocumentInterpretation {
        require(validatedChunks.isNotEmpty()) { "validatedChunks must not be empty" }
        if (validatedChunks.size == 1) return validatedChunks.first()

        val unresolved = validatedChunks.flatMap { it.unresolvedFields }.toMutableList()
        var conflictDetected = false

        fun <T> reconcileField(
            fieldName: String,
            lockedValue: T?,
            selector: (DocumentInterpretation) -> T?
        ): T? {
            if (lockedValue != null) return lockedValue
            val distinctValues = validatedChunks.mapNotNull(selector).distinct()
            return when {
                distinctValues.isEmpty() -> null
                distinctValues.size == 1 -> distinctValues.first()
                else -> {
                    conflictDetected = true
                    unresolved += "conflict:$fieldName"
                    null
                }
            }
        }

        val distinctFamilies = validatedChunks
            .map { it.documentFamily }
            .filter { it != DocumentFamily.UNKNOWN }
            .distinct()

        val mergedFamily = lockedFields.lockedDocumentFamily ?: when {
            distinctFamilies.size == 1 -> distinctFamilies.first()
            distinctFamilies.size > 1 -> {
                conflictDetected = true
                unresolved += "conflict:documentFamily"
                DocumentFamily.UNKNOWN
            }
            else -> DocumentFamily.UNKNOWN
        }

        val mergedMerchant = reconcileField("merchantOrIssuer", lockedFields.lockedMerchant) { it.merchantOrIssuer }
        val mergedAddress = reconcileField("issuerAddress", lockedFields.lockedIssuerAddress) { it.issuerAddress }
        val mergedPrimaryDate = reconcileField("primaryDate", lockedFields.lockedPrimaryDate) { it.primaryDate }
        val mergedDueDate = reconcileField("dueDate", lockedFields.lockedDueDate) { it.dueDate }
        val mergedTotal = reconcileField("documentTotal", lockedFields.lockedDocumentTotal) { it.documentTotal }
        val mergedTax = reconcileField("taxTotal", lockedFields.lockedTaxTotal) { it.taxTotal }

        val mergedOpening = reconcileField("statementSummary.openingBalance", null) { it.statementSummary?.openingBalance }
        val mergedClosing = reconcileField("statementSummary.closingBalance", null) { it.statementSummary?.closingBalance }
        val mergedMinPay = reconcileField("statementSummary.minimumPaymentDue", null) { it.statementSummary?.minimumPaymentDue }
        val mergedPeriodStart = reconcileField("statementSummary.periodStartDate", null) { it.statementSummary?.periodStartDate }
        val mergedPeriodEnd = reconcileField("statementSummary.periodEndDate", null) { it.statementSummary?.periodEndDate }

        val mergedSummary = if (
            mergedOpening != null || mergedClosing != null || mergedMinPay != null ||
            mergedPeriodStart != null || mergedPeriodEnd != null
        ) {
            ResolvedStatementSummary(mergedOpening, mergedClosing, mergedMinPay, mergedPeriodStart, mergedPeriodEnd)
        } else null

        val mergedEvents = validatedChunks
            .flatMap { it.financialEvents }
            .distinctBy { ev ->
                EventDedupKey(
                    sourceRowId = ev.sourceRowId,
                    eventKind = ev.eventKind,
                    amount = ev.amount,
                    draftRawAmountToken = ev.draftRawAmountToken,
                    isoDate = ev.isoDate,
                    normalizedDescription = ev.description.trim().lowercase(),
                    sortedAmountEvidenceIds = ev.amountEvidenceIds.sorted()
                )
            }

        val reviewRequired = conflictDetected ||
            mergedFamily == DocumentFamily.UNKNOWN ||
            mergedTotal == null ||
            mergedPrimaryDate == null ||
            validatedChunks.any { it.reviewRequired } ||
            mergedEvents.any { it.persistenceState == EventPersistenceState.NEEDS_USER_REVIEW_DRAFT } ||
            unresolved.isNotEmpty()

        return DocumentInterpretation(
            documentFamily = mergedFamily,
            merchantOrIssuer = mergedMerchant,
            issuerAddress = mergedAddress,
            primaryDate = mergedPrimaryDate,
            dueDate = mergedDueDate,
            documentTotal = mergedTotal,
            taxTotal = mergedTax,
            statementSummary = mergedSummary,
            financialEvents = mergedEvents,
            reviewRequired = reviewRequired,
            unresolvedFields = unresolved.distinct()
        )
    }
}
