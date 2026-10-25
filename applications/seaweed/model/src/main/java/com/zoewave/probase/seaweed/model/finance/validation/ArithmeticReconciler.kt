package com.zoewave.probase.seaweed.model.finance.validation

object ArithmeticReconciler {

    fun reconcile(
        documentId: String,
        family: DocumentFamily,
        amountCandidates: List<AmountCandidate>,
        tableRows: List<ReconstructedTableRow>
    ): ArithmeticValidationSummary {
        val totalCandidate = amountCandidates.firstOrNull { it.role == SemanticRole.TOTAL_AMOUNT && it.amount != null }
        val taxCandidate = amountCandidates.firstOrNull { it.role == SemanticRole.TAX_AMOUNT && it.amount != null }

        if (totalCandidate == null) {
            return ArithmeticValidationSummary(
                reconciliationRuleUsed = "NO_TOTAL_CANDIDATE",
                receiptTotalReconciled = false,
                statementBalanceReconciled = null,
                discrepancyMinorUnits = -1L,
                notes = listOf("No TOTAL_AMOUNT candidate extracted")
            )
        }

        val totalMinor = totalCandidate.amount!!.amountMinor
        val taxMinor = taxCandidate?.amount?.amountMinor ?: 0L

        val lineItemAmounts = amountCandidates.filter { it.role == SemanticRole.LINE_ITEM_AMOUNT && it.amount != null }
            .map { it.amount!!.amountMinor }

        if (lineItemAmounts.isNotEmpty()) {
            val sumLineItems = lineItemAmounts.sum()

            // Tax-Exclusive Check
            if (sumLineItems + taxMinor == totalMinor) {
                return ArithmeticValidationSummary(
                    reconciliationRuleUsed = "TAX_EXCLUSIVE",
                    receiptTotalReconciled = true,
                    statementBalanceReconciled = null,
                    discrepancyMinorUnits = 0L,
                    notes = listOf("LineItems ($sumLineItems) + Tax ($taxMinor) == Total ($totalMinor)")
                )
            }

            // Tax-Inclusive Check (VAT/GST)
            if (sumLineItems == totalMinor) {
                return ArithmeticValidationSummary(
                    reconciliationRuleUsed = "TAX_INCLUSIVE",
                    receiptTotalReconciled = true,
                    statementBalanceReconciled = null,
                    discrepancyMinorUnits = 0L,
                    notes = listOf("LineItems ($sumLineItems) == Total ($totalMinor) (Tax inclusive)")
                )
            }

            val diff = kotlin.math.abs(totalMinor - (sumLineItems + taxMinor))
            return ArithmeticValidationSummary(
                reconciliationRuleUsed = "TAX_EXCLUSIVE_UNRECONCILED",
                receiptTotalReconciled = false,
                statementBalanceReconciled = null,
                discrepancyMinorUnits = diff,
                notes = listOf("Discrepancy of $diff minor units")
            )
        }

        // Subtotal / Direct total match
        return ArithmeticValidationSummary(
            reconciliationRuleUsed = "DIRECT_TOTAL_MATCH",
            receiptTotalReconciled = true,
            statementBalanceReconciled = null,
            discrepancyMinorUnits = 0L,
            notes = listOf("Single total candidate matched")
        )
    }
}
