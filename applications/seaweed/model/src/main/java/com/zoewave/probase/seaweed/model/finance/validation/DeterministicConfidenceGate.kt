package com.zoewave.probase.seaweed.model.finance.validation

import com.zoewave.probase.seaweed.model.finance.common.PipelineGear
import com.zoewave.probase.seaweed.model.finance.common.PipelineGearLogger

data class GateDecision(
    val lockedFields: LockedDeterministicFields,
    val fullDocumentResolvedWithoutAi: Boolean,
    val resolvedInterpretation: DocumentInterpretation?
)

object DeterministicConfidenceGate {
    fun evaluate(
        familyCandidate: DocumentFamily,
        familyConfidence: Float,
        merchantCandidate: TextCandidate?,
        addressCandidate: TextCandidate?,
        dateCandidates: List<DateCandidate>,
        amountCandidates: List<AmountCandidate>,
        arithmetic: ArithmeticValidationSummary,
        matchedCategoryRuleId: String?,
        docId: String = "GLOBAL"
    ): GateDecision {
        val lockedFamily = if (
            familyCandidate != DocumentFamily.UNKNOWN && familyConfidence >= 0.92f
        ) familyCandidate else null

        val lockedMerchant = merchantCandidate
            ?.takeIf { it.role == SemanticRole.MERCHANT_NAME && it.confidence >= 0.95f }
            ?.let { ResolvedStringField(it.normalizedValue, it.role, it.evidenceIds) }

        val lockedAddress = addressCandidate
            ?.takeIf { it.role == SemanticRole.ISSUER_ADDRESS && it.confidence >= 0.95f }
            ?.let { ResolvedStringField(it.normalizedValue, it.role, it.evidenceIds) }

        val txDates = dateCandidates.filter { it.role == SemanticRole.TRANSACTION_DATE }
        val lockedPrimaryDate = if (txDates.size == 1 && txDates.first().confidence >= 0.95f) {
            val d = txDates.first()
            ResolvedDateField(d.isoDate, d.role, d.evidenceIds)
        } else null

        val dueDates = dateCandidates.filter { it.role == SemanticRole.DUE_DATE }
        val lockedDueDate = if (dueDates.size == 1 && dueDates.first().confidence >= 0.95f) {
            val d = dueDates.first()
            ResolvedDateField(d.isoDate, d.role, d.evidenceIds)
        } else null

        val reconciled = arithmetic.receiptTotalReconciled == true &&
            arithmetic.discrepancyMinorUnits == 0L

        val totalCandidate = amountCandidates
            .filter { it.role == SemanticRole.TOTAL_AMOUNT && it.amount != null }
            .maxByOrNull { it.confidence }

        val lockedTotal = if (reconciled && totalCandidate != null && totalCandidate.amount != null && totalCandidate.confidence >= 0.96f) {
            ResolvedAmountField(totalCandidate.amount!!, totalCandidate.role, totalCandidate.evidenceIds)
        } else null

        val taxCandidate = amountCandidates
            .filter { it.role == SemanticRole.TAX_AMOUNT && it.amount != null }
            .maxByOrNull { it.confidence }

        val lockedTax = if (reconciled && taxCandidate != null && taxCandidate.amount != null && taxCandidate.confidence >= 0.96f) {
            ResolvedAmountField(taxCandidate.amount!!, taxCandidate.role, taxCandidate.evidenceIds)
        } else null

        val locked = LockedDeterministicFields(
            lockedDocumentFamily = lockedFamily,
            lockedMerchant = lockedMerchant,
            lockedIssuerAddress = lockedAddress,
            lockedPrimaryDate = lockedPrimaryDate,
            lockedDueDate = lockedDueDate,
            lockedDocumentTotal = lockedTotal,
            lockedTaxTotal = lockedTax
        )

        val canBypassLlm = lockedFamily == DocumentFamily.RECEIPT &&
            lockedMerchant != null &&
            lockedPrimaryDate != null &&
            lockedTotal != null &&
            matchedCategoryRuleId != null

        val completeInterpretation = if (canBypassLlm) {
            DocumentInterpretation(
                documentFamily = lockedFamily,
                merchantOrIssuer = lockedMerchant,
                issuerAddress = lockedAddress,
                primaryDate = lockedPrimaryDate,
                dueDate = lockedDueDate,
                documentTotal = lockedTotal,
                taxTotal = lockedTax,
                statementSummary = null,
                financialEvents = listOf(
                    ResolvedFinancialEvent(
                        eventKind = FinancialEventKind.PURCHASE_EXPENSE,
                        description = lockedMerchant.value,
                        descriptionEvidenceIds = lockedMerchant.evidenceIds,
                        amount = lockedTotal.amount,
                        amountEvidenceIds = lockedTotal.evidenceIds,
                        isoDate = lockedPrimaryDate.isoDate,
                        dateEvidenceIds = lockedPrimaryDate.evidenceIds,
                        categoryId = matchedCategoryRuleId,
                        persistenceState = EventPersistenceState.VERIFIED_FINAL
                    )
                ),
                reviewRequired = false,
                unresolvedFields = emptyList()
            )
        } else null

        PipelineGearLogger.d(PipelineGear.GEAR_5_LEDGER_GATE, docId) {
            "Rule=${arithmetic.reconciliationRuleUsed} reconciled=$reconciled discrepancy=${arithmetic.discrepancyMinorUnits}. " +
            "Locked=[total=${lockedTotal?.amount?.amountMinor}, date=${lockedPrimaryDate?.isoDate}] fullBypass=$canBypassLlm"
        }

        return GateDecision(
            lockedFields = locked,
            fullDocumentResolvedWithoutAi = canBypassLlm,
            resolvedInterpretation = completeInterpretation
        )
    }
}
