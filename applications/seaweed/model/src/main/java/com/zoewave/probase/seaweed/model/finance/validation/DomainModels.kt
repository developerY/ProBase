package com.zoewave.probase.seaweed.model.finance.validation

import com.zoewave.probase.seaweed.model.finance.CurrencyAmount
import com.zoewave.probase.seaweed.model.finance.vision.NormalizedQuad
import com.zoewave.probase.seaweed.model.finance.vision.NormalizedRect
import kotlinx.serialization.Serializable

@Serializable
enum class ExtractionProvenance {
    MLKIT_OCR_V2,
    PDF_TEXT_LAYER
}

@Serializable
enum class DocumentFamily {
    RECEIPT,
    INVOICE_OR_UTILITY_BILL,
    BANK_STATEMENT,
    CREDIT_CARD_STATEMENT,
    REFUND_OR_CREDIT_NOTE,
    UNKNOWN
}

@Serializable
enum class FinancialEventKind {
    PURCHASE_EXPENSE,
    UNPAID_BILL_LIABILITY,
    BILL_PAYMENT_SETTLEMENT,
    INTERNAL_OR_CC_TRANSFER,
    REFUND_OR_CREDIT,
    INCOME_DEPOSIT,
    FEE_OR_INTEREST,
    STATEMENT_BALANCE_IGNORE
}

@Serializable
enum class BillPaymentStatus {
    UNPAID,
    PAID_IN_FULL,
    PARTIALLY_PAID,
    OVERDUE,
    UNKNOWN
}

@Serializable
enum class EventPersistenceState {
    VERIFIED_FINAL,
    NEEDS_USER_REVIEW_DRAFT
}

@Serializable
enum class SemanticRole {
    MERCHANT_NAME,
    ISSUER_ADDRESS,
    CUSTOMER_ADDRESS,
    ACCOUNT_HOLDER_NAME,
    CUSTOMER_PHONE,
    CUSTOMER_EMAIL,
    TRANSACTION_DATE,
    POSTING_DATE,
    DUE_DATE,
    STATEMENT_PERIOD_START,
    STATEMENT_PERIOD_END,
    PRINT_DATE,
    SUBTOTAL_AMOUNT,
    TAX_AMOUNT,
    TIP_AMOUNT,
    DISCOUNT_AMOUNT,
    SURCHARGE_OR_FEE_AMOUNT,
    TOTAL_AMOUNT,
    AMOUNT_DUE,
    MINIMUM_PAYMENT_DUE,
    OPENING_BALANCE,
    CLOSING_BALANCE,
    LINE_ITEM_DESCRIPTION,
    LINE_ITEM_AMOUNT,
    DEBIT_AMOUNT,
    CREDIT_AMOUNT,
    RUNNING_BALANCE_AMOUNT,
    PAYMENT_STATUS_LABEL,
    GENERIC_LINE
}

@Serializable
data class OcrElement(
    val id: String, // Page-scoped, e.g. "P01_T0014"
    val pageNumber: Int,
    val rawText: String,
    val normalizedText: String,
    val provenance: ExtractionProvenance,
    val boxNormalizedOcr: NormalizedRect,
    val boxNormalizedRawQuad: NormalizedQuad?, // Null if homography was singular
    val ocrConfidence: Float,
    val rotationDegrees: Float,
    val lineId: String,
    val region: String
)

@Serializable
data class TextCandidate(
    val candidateId: String,
    val role: SemanticRole,
    val rawText: String,
    val normalizedValue: String,
    val confidence: Float,
    val evidenceIds: List<String>
)

@Serializable
data class DateCandidate(
    val candidateId: String,
    val role: SemanticRole,
    val isoDate: String, // YYYY-MM-DD
    val confidence: Float,
    val evidenceIds: List<String>
)

@Serializable
data class AmountCandidate(
    val candidateId: String,
    val role: SemanticRole,
    val amount: CurrencyAmount? = null, // Null when ExcessPrecisionFlagged
    val draftRawToken: String? = null,
    val draftParseIssue: String? = null,
    val confidence: Float,
    val evidenceIds: List<String>
)

@Serializable
data class ReconstructedTableCell(
    val cellId: String,
    val columnIndex: Int,
    val headerLabel: String?,
    val role: SemanticRole,
    val rawText: String,
    val parsedAmount: CurrencyAmount? = null,
    val draftRawAmountToken: String? = null,
    val parsedIsoDate: String? = null,
    val evidenceIds: List<String>
)

@Serializable
data class ReconstructedTableRow(
    val rowId: String,
    val pageNumber: Int,
    val rowIndex: Int,
    val cells: List<ReconstructedTableCell>
)

@Serializable
data class ArithmeticValidationSummary(
    val reconciliationRuleUsed: String,
    val receiptTotalReconciled: Boolean?,
    val statementBalanceReconciled: Boolean?,
    val discrepancyMinorUnits: Long,
    val notes: List<String>
)

@Serializable
data class ResolvedStringField(
    val value: String,
    val role: SemanticRole,
    val evidenceIds: List<String>
)

@Serializable
data class ResolvedDateField(
    val isoDate: String,
    val role: SemanticRole,
    val evidenceIds: List<String>
)

@Serializable
data class ResolvedAmountField(
    val amount: CurrencyAmount,
    val role: SemanticRole,
    val evidenceIds: List<String>
)

@Serializable
data class LockedDeterministicFields(
    val lockedDocumentFamily: DocumentFamily? = null,
    val lockedMerchant: ResolvedStringField? = null,
    val lockedIssuerAddress: ResolvedStringField? = null,
    val lockedPrimaryDate: ResolvedDateField? = null,
    val lockedDueDate: ResolvedDateField? = null,
    val lockedDocumentTotal: ResolvedAmountField? = null,
    val lockedTaxTotal: ResolvedAmountField? = null
)

@Serializable
data class LlmAnalysisBundle(
    val documentId: String,
    val chunkIndex: Int,
    val totalChunks: Int,
    val detectedFamilyCandidates: List<DocumentFamily>,
    val lockedDeterministicFields: LockedDeterministicFields,
    val textCandidates: List<TextCandidate>,
    val dateCandidates: List<DateCandidate>,
    val amountCandidates: List<AmountCandidate>,
    val tableRows: List<ReconstructedTableRow>,
    val arithmeticValidation: ArithmeticValidationSummary,
    val allowedCategoryIds: List<String>
)

@Serializable
data class ResolvedStatementSummary(
    val openingBalance: ResolvedAmountField? = null,
    val closingBalance: ResolvedAmountField? = null,
    val minimumPaymentDue: ResolvedAmountField? = null,
    val periodStartDate: ResolvedDateField? = null,
    val periodEndDate: ResolvedDateField? = null
)

@Serializable
data class ResolvedFinancialEvent(
    val eventKind: FinancialEventKind,
    val description: String,
    val descriptionEvidenceIds: List<String>,
    val amount: CurrencyAmount? = null, // Non-null for VERIFIED_FINAL
    val draftRawAmountToken: String? = null, // Populated when ExcessPrecisionFlagged
    val draftParseIssue: String? = null,
    val amountEvidenceIds: List<String>,
    val isoDate: String? = null, // Non-null for VERIFIED_FINAL
    val dateEvidenceIds: List<String> = emptyList(),
    val dueDateIso: String? = null,
    val dueDateEvidenceIds: List<String> = emptyList(),
    val billPaymentStatus: BillPaymentStatus = BillPaymentStatus.UNKNOWN,
    val paymentStatusEvidenceIds: List<String> = emptyList(),
    val categoryId: String,
    val sourceRowId: String? = null,
    val persistenceState: EventPersistenceState
)

@Serializable
data class DocumentInterpretation(
    val documentFamily: DocumentFamily,
    val merchantOrIssuer: ResolvedStringField? = null,
    val issuerAddress: ResolvedStringField? = null,
    val primaryDate: ResolvedDateField? = null,
    val dueDate: ResolvedDateField? = null,
    val documentTotal: ResolvedAmountField? = null,
    val taxTotal: ResolvedAmountField? = null,
    val statementSummary: ResolvedStatementSummary? = null,
    val financialEvents: List<ResolvedFinancialEvent> = emptyList(),
    val reviewRequired: Boolean,
    val unresolvedFields: List<String> = emptyList()
)
