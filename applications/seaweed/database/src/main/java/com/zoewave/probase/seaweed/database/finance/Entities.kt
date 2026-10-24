package com.zoewave.probase.seaweed.database.finance

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "financial_documents")
data class FinancialDocumentEntity(
    @PrimaryKey val documentId: String,
    val documentFamily: String,
    val sourceSha256: String,
    val captureTimestampEpochMs: Long,
    val rawEvidencePurged: Boolean,
    val processingStatus: String // PENDING, NEEDS_REVIEW, VERIFIED
)

@Entity(
    tableName = "document_pages",
    primaryKeys = ["documentId", "pageNumber"],
    foreignKeys = [
        ForeignKey(
            entity = FinancialDocumentEntity::class,
            parentColumns = ["documentId"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DocumentPageEntity(
    val documentId: String,
    val pageNumber: Int,
    val rawDisplayEncryptedPath: String?,
    val cleanOcrEncryptedPath: String?,
    val widthPx: Int,
    val heightPx: Int,
    val normalizedHomographyCsv: String, // 9 floats for H_norm
    val blurVariance: Float,
    val glareRatio: Float,
    val dominantHeaderLabCsv: String?
)

@Entity(
    tableName = "ocr_elements",
    primaryKeys = ["elementId"],
    foreignKeys = [
        ForeignKey(
            entity = DocumentPageEntity::class,
            parentColumns = ["documentId", "pageNumber"],
            childColumns = ["documentId", "pageNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId", "pageNumber")]
)
data class OcrElementEntity(
    val elementId: String, // "P01_T0014"
    val documentId: String,
    val pageNumber: Int,
    val rawText: String,
    val normalizedText: String,
    val provenance: String,
    val boxNormalizedOcrJson: String,
    val boxNormalizedRawQuadJson: String?,
    val ocrConfidence: Float?,
    val rotationDegrees: Float,
    val lineId: String,
    val region: String
)

@Entity(
    tableName = "evidence_manifests",
    primaryKeys = ["documentId", "evidenceId"],
    foreignKeys = [
        ForeignKey(
            entity = FinancialDocumentEntity::class,
            parentColumns = ["documentId"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class EvidenceManifestEntity(
    val documentId: String,
    val evidenceId: String, // e.g., "P01_T0014"
    val pageNumber: Int,
    val semanticRole: String,
    val valueHmacSha256Hex: String, // Keyed HMAC-SHA256 of "$documentId:$evidenceId:$normalizedValue"
    val quadRawDisplayNormalizedJson: String?,
    val sourcePurged: Boolean,
    val verificationStatus: String // "DETERMINISTIC_LOCKED", "MODEL_VERIFIED", "USER_CORRECTED"
)

@Entity(
    tableName = "statement_summaries",
    foreignKeys = [
        ForeignKey(
            entity = FinancialDocumentEntity::class,
            parentColumns = ["documentId"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["documentId"], unique = true)]
)
data class StatementSummaryEntity(
    @PrimaryKey val summaryId: String,
    val documentId: String,
    val statementFamily: String, // "BANK_STATEMENT" or "CREDIT_CARD_STATEMENT"
    val maskedAccountIdentifier: String?,
    val periodStartIso: String?,
    val periodEndIso: String?,
    val openingBalanceMinor: Long?,
    val closingBalanceMinor: Long?,
    val minimumPaymentDueMinor: Long?,
    val currencyCode: String,
    val currencyScale: Int,
    val isReconciled: Boolean,
    val discrepancyMinorUnits: Long
)

@Entity(
    tableName = "financial_events",
    foreignKeys = [
        ForeignKey(
            entity = FinancialDocumentEntity::class,
            parentColumns = ["documentId"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["dedupFingerprint"], unique = false),
        Index(value = ["documentId"])
    ]
)
data class FinancialEventEntity(
    @PrimaryKey val eventId: String,
    val documentId: String?, // Nullable if user deletes source document while keeping verified events
    val eventKind: String, // FinancialEventKind.name
    val persistenceState: String, // EventPersistenceState.name
    val merchantOrPayee: String,
    val eventDateIso: String?, // Non-null required when persistenceState == VERIFIED_FINAL
    val dueDateIso: String?,
    val billPaymentStatus: String?, // BillPaymentStatus.name
    val amountMinor: Long?, // Non-null required when persistenceState == VERIFIED_FINAL
    val currencyCode: String?,
    val currencyScale: Int?,
    val draftRawAmountToken: String?, // Populated when ExcessPrecisionFlagged
    val draftCurrencyHypothesis: String?,
    val draftParseIssue: String?,
    val categoryId: String,
    val maskedAccountFingerprint: String?,
    val sourceRowId: String?,
    val dedupFingerprint: String,
    val evidenceIdsCsv: String,
    val userReviewed: Boolean
) {
    init {
        if (persistenceState == "VERIFIED_FINAL") {
            require(eventDateIso != null) { "VERIFIED_FINAL event requires non-null eventDateIso" }
            require(amountMinor != null && currencyCode != null && currencyScale != null) {
                "VERIFIED_FINAL event requires non-null amountMinor, currencyCode, and currencyScale"
            }
            require(draftParseIssue == null) { "VERIFIED_FINAL event cannot have unresolved draftParseIssue" }
        }
    }
}
