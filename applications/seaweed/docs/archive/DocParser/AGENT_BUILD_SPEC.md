Here is the updated, self-contained **`ImagePipeline.md`** specification.

Per your scope boundaries, Gradle dependency catalogs (`libs.versions.toml`) and the internal multi-provider AI router implementation have been removed as out-of-scope pre-existing components. The specification retains only the minimal router integration contract needed to show how the document pipeline submits redacted bundles, merges chunked responses, and validates results, while resolving all remaining data-integrity, provenance, redaction, chunk-merging, and Room persistence issues from the review.

---

```markdown
# Android Financial Document Intelligence Engine — Engineering Specification

## 1. System Architecture & Scope Boundaries

### 1.1 Core Architectural Contract
1. **Local-First Document Preparation:** Image capture, perspective/dewarping transforms, dual-bitmap preprocessing, OCR, native PDF text extraction, CIELAB perceptual color analysis, spatial table reconstruction, locale-aware candidate extraction, and integer-minor-unit arithmetic validation execute **100% locally on the device**.
2. **Explicit Field-Level Deterministic Locking:** Whenever the local rules engine resolves specific fields (such as reconciled totals, taxes, or dates) with high mathematical certainty, it locks *only* those verified fields inside a typed `LockedDeterministicFields` structure. Unverified low-confidence guesses are never locked.
3. **Minimal, PII-Redacted Cloud Bundle:** When semantic ambiguity remains, the pipeline constructs a role-stratified, PII-redacted text-only JSON bundle (`LlmAnalysisBundle`, strictly $\le 12,288\text{ bytes}$ per chunk) and passes it to the existing AI Router interface. Raw images, full OCR dumps, customer addresses, emails, phone numbers, and unmasked account numbers never leave the device.
4. **Candidate-Bound Post-LLM Validation:** Every field and financial event returned by the model is treated as untrusted text until verified by `PostLlmSemanticValidator`. Every resolved value must prove an unbroken provenance chain back to a specific extracted candidate or table cell that owns the exact cited page-scoped `evidence_ids`, matches the parsed value, and carries a permitted `SemanticRole`.

### 1.2 Scope Boundaries
* **In Scope (This Specification):**
  * Local image capture, dual-bitmap retention, native digital PDF extraction, and coordinate space mapping ($3 \times 3$ normalized homography and 4-corner quadrilateral projection).
  * Spatial layout reconstruction (rotation-aware vector projection for curved receipts, column clustering, and CIELAB perceptual color extraction).
  * Complete ISO 4217 currency scale registry and strict non-rounding money parsing.
  * Document-family signed-ledger reconciliation, field-level deterministic locking, candidate-bound semantic validation, PII-redacting bundle trimming, and conflict-aware multi-chunk aggregation.
  * Encrypted Room + SQLCipher persistence, foreign-key lifecycle, Tink `StreamingAead` image encryption, Tink `Mac` HMAC-SHA256 post-purge provenance manifests, and the Jetpack Compose review overlay UI.
  * Calibration/evaluation corpus split and exact unit/integration acceptance tests.
* **Out of Scope (Pre-Existing Project Infrastructure):**
  * **Dependency Management (`libs.versions.toml`):** Managed directly by the IDE and project build system.
  * **AI Router Internals & Provider Settings UI:** The multi-provider router (Hosted Gemma proxy, Firebase AI Logic, BYOK, On-Device Gemini Nano), credential vault, and AI Configuration UI are existing components. This specification defines only the reference integration interface (`AiInterpretationRouter`) required for the document pipeline to invoke interpretation and validate results.

### 1.3 Module Responsibilities
* `:feature:documentcapture` — ML Kit Document Scanner integration, PDF import, and dual-bitmap generation.
* `:core:ocr` — Native PDF text-layer extractor and ML Kit Text Recognition v2 wrapper with page-scoped ID assignment (`P01_T0001`).
* `:core:document:vision` — Image quality metrics, CIELAB perceptual color histogram extraction, and normalized homography matrix ($H_{\text{norm}}$) computation.
* `:core:document:layout` — Radian-converted vector line grouping, column clustering, reading order, and table cell reconstruction.
* `:core:finance:extract` — Complete ISO 4217 currency registry, strict non-rounding money parser, and regex/heuristic candidate detectors.
* `:core:finance:validation` — Signed-ledger arithmetic reconciler, `DeterministicConfidenceGate`, `BundleTrimmer`, `ChunkInterpretationMerger`, and `PostLlmSemanticValidator`.
* `:core:database` — Room + SQLCipher persistence, Tink `StreamingAead` file encryption, Tink `Mac` HMAC-SHA256 provenance digests, and retention workers.
* `:feature:review` — Split-pane Jetpack Compose review UI with 4-corner quadrilateral evidence overlays and draft resolution workflows.

---

## 2. Capture, Preprocessing, OCR & Coordinate Transforms

### 2.1 Dual-Bitmap Retention & Native Digital PDF Extraction
1. **Native Digital PDF Check (`:core:ocr`):**
   * When a user imports a PDF, inspect each page's embedded text layer before rasterizing.
   * Transform PDF user-space coordinates (origin at bottom-left, affected by page `/Rotate` and `/CropBox`) into top-left normalized page coordinates $[0, 1]^2$.
   * Populate `OcrElement` records with `provenance = ExtractionProvenance.PDF_TEXT_LAYER`, `ocrConfidence = 0.99f` (reflecting deterministic character decoding without assuming semantic correctness), and an identity homography matrix $I_3$ once mapped into normalized display space.
2. **Camera / Rasterized Image Capture:**
   * **Raw Display Bitmap (`raw_display_page_{n}.enc`):** Downsampled to max $2048\text{ px}$ on the longest edge while preserving native aspect ratio and RGB colors. Used for CIELAB color analysis and the Review UI.
   * **Cleaned OCR Bitmap (`clean_ocr_page_{n}.enc`):** Deskewed, perspective-corrected, shadow-suppressed, and contrast-normalized for ML Kit Text Recognition v2 (`provenance = ExtractionProvenance.MLKIT_OCR_V2`).

### 2.2 Normalized Homography ($H_{\text{norm}}$) & Failure-Safe 4-Corner Projection
1. **Explicit Coordinate Spaces:**
   * **Raw Display Pixel Space:** $W_r \times H_r$
   * **Cleaned OCR Pixel Space:** $W_o \times H_o$
   * **Normalized Space:** $[0, 1]^2$
2. **Normalized Homography Composition:**
   Let $H_{\text{px}}^{-1}$ be the $3 \times 3$ pixel-space inverse perspective matrix mapping a pixel $(x_o, y_o)$ on the cleaned OCR bitmap back to $(x_r, y_r)$ on the raw display bitmap. Define scaling matrices:
   $$N_{\text{ocr}} = \begin{bmatrix} 1/W_o & 0 & 0 \\ 0 & 1/H_o & 0 \\ 0 & 0 & 1 \end{bmatrix}, \quad N_{\text{raw}} = \begin{bmatrix} 1/W_r & 0 & 0 \\ 0 & 1/H_r & 0 \\ 0 & 0 & 1 \end{bmatrix}$$
   Store the composed normalized matrix $H_{\text{norm}}$ in `DocumentPage`:
   $$H_{\text{norm}} = N_{\text{raw}} \cdot H_{\text{px}}^{-1} \cdot N_{\text{ocr}}^{-1}$$
   *(Note: $H_{\text{norm}}$ models global affine/projective transforms. If a future pipeline stage introduces nonlinear mesh dewarping, a 2D displacement grid must replace $H_{\text{norm}}$.)*
3. **Failure-Safe 4-Corner Projection (`CoordinateTransformMapper.kt`):**
   Perspective transformations turn axis-aligned OCR rectangles into 4-corner quadrilaterals. The mapper transforms all four vertices and explicitly flags singular or non-finite matrices rather than silently returning untransformed coordinates:

```kotlin
package com.example.financeengine.core.vision

import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
data class NormalizedPoint(val x: Float, val y: Float)

@Serializable
data class NormalizedRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

@Serializable
data class NormalizedQuad(
    val topLeft: NormalizedPoint,
    val topRight: NormalizedPoint,
    val bottomRight: NormalizedPoint,
    val bottomLeft: NormalizedPoint
)

sealed class ProjectionResult {
    data class Success(
        val quad: NormalizedQuad,
        val requiresViewportClipping: Boolean
    ) : ProjectionResult()

    data class SingularOrInvalidTransform(val reason: String) : ProjectionResult()
}

object CoordinateTransformMapper {
    fun projectOcrRectToRawQuad(
        ocrRect: NormalizedRect,
        hNorm: FloatArray // 9 elements, row-major
    ): ProjectionResult {
        if (hNorm.size != 9 || hNorm.any { !it.isFinite() }) {
            return ProjectionResult.SingularOrInvalidTransform("Invalid homography array")
        }
        val det = hNorm[0] * (hNorm[4] * hNorm[8] - hNorm[5] * hNorm[7]) -
            hNorm[1] * (hNorm[3] * hNorm[8] - hNorm[5] * hNorm[6]) +
            hNorm[2] * (hNorm[3] * hNorm[7] - hNorm[4] * hNorm[6])
        if (!det.isFinite() || abs(det) < 1e-7f) {
            return ProjectionResult.SingularOrInvalidTransform("Singular homography determinant: $det")
        }

        val tl = mapPoint(ocrRect.x, ocrRect.y, hNorm)
            ?: return ProjectionResult.SingularOrInvalidTransform("TL vertex projection failed")
        val tr = mapPoint(ocrRect.x + ocrRect.width, ocrRect.y, hNorm)
            ?: return ProjectionResult.SingularOrInvalidTransform("TR vertex projection failed")
        val br = mapPoint(ocrRect.x + ocrRect.width, ocrRect.y + ocrRect.height, hNorm)
            ?: return ProjectionResult.SingularOrInvalidTransform("BR vertex projection failed")
        val bl = mapPoint(ocrRect.x, ocrRect.y + ocrRect.height, hNorm)
            ?: return ProjectionResult.SingularOrInvalidTransform("BL vertex projection failed")

        val points = listOf(tl, tr, br, bl)
        val outOfBounds = points.any { it.x < 0f || it.x > 1f || it.y < 0f || it.y > 1f }

        return ProjectionResult.Success(
            quad = NormalizedQuad(tl, tr, br, bl),
            requiresViewportClipping = outOfBounds
        )
    }

    private fun mapPoint(x: Float, y: Float, h: FloatArray): NormalizedPoint? {
        val w = h[6] * x + h[7] * y + h[8]
        if (!w.isFinite() || abs(w) < 1e-6f) return null
        val px = (h[0] * x + h[1] * y + h[2]) / w
        val py = (h[3] * x + h[4] * y + h[5]) / w
        if (!px.isFinite() || !py.isFinite()) return null
        return NormalizedPoint(px, py)
    }
}
```

### 2.3 Spatial Layout Reconstruction & Perceptual Color Extraction
1. **Radian-Converted Vector Line Grouping (`:core:document:layout`):**
    * Thermal receipts frequently curl, causing horizontal $Y$-coordinates to drift across a single line.
    * For each OCR element $i$ with center $(x_i, y_i)$, height $h_i$, and rotation angle $\theta_i^{\circ}$ (`rotationDegrees`), convert to radians first:
      $$\theta_i = \theta_i^{\circ} \times \frac{\pi}{180}$$
    * Project a baseline ray to target horizontal coordinate $x_j$:
      $$\hat{y}_{i \rightarrow j} = y_i + (x_j - x_i) \cdot \tan(\theta_i)$$
    * Merge elements $i$ and $j$ into the same `ReconstructedLine` if and only if:
      $$|\hat{y}_{i \rightarrow j} - y_j| \le 0.45 \cdot \max(h_i, h_j) \quad \text{and} \quad |\theta_i^{\circ} - \theta_j^{\circ}| \le 8.0^{\circ}$$
2. **Column Clustering & Table Cell Assignment:**
    * Cluster horizontal $X$-edges across consecutive lines using 1D density clustering ($\epsilon = 0.035$ normalized page width) to detect table columns (`Date`, `Description`, `Qty`, `UnitPrice`, `Debit`, `Credit`, `Balance`, `Amount`).
    * Assign each cell a stable `cellId` (`P01_R004_C02`), `headerLabel`, `role`, and `evidenceIds`.
3. **CIELAB Perceptual Color Extraction (`:core:document:vision`):**
    * Sample the `raw_display` bitmap across `HEADER` ($y \in [0.0, 0.20]$), `BODY` ($y \in [0.20, 0.85]$), and `FOOTER` ($y \in [0.85, 1.0]$).
    * Convert RGB pixels to CIE $L^*a^*b^*$ (D65 illuminant) and run $k$-means ($k = 3$) to extract dominant regional colors. Compare against stored merchant templates using CIEDE2000 ($\Delta E_{00} < 12.0$) as a supplementary scoring signal.

---

## 3. Deterministic Candidate Extraction & Strict Currency Model

### 3.1 Complete ISO 4217 Currency Metadata Registry
`Iso4217CurrencyRegistry` covers all active ISO 4217 currencies by combining explicit scale overrides for zero-, three-, and four-decimal currencies with the JVM/Android ISO 4217 table:

```kotlin
package com.example.financeengine.core.finance

import java.util.Currency

object Iso4217CurrencyRegistry {
    const val REGISTRY_VERSION = "2026.10-ISO4217"

    private val EXPLICIT_SCALES: Map<String, Int> = mapOf(
        // 0-decimal currencies
        "BIF" to 0, "CLP" to 0, "DJF" to 0, "GNF" to 0, "ISK" to 0,
        "JPY" to 0, "KMF" to 0, "KRW" to 0, "PYG" to 0, "RWF" to 0,
        "UGX" to 0, "VND" to 0, "VUV" to 0, "XAF" to 0, "XOF" to 0, "XPF" to 0,
        // 3-decimal currencies
        "BHD" to 3, "IQD" to 3, "JOD" to 3, "KWD" to 3, "LYD" to 3, "OMR" to 3, "TND" to 3,
        // 4-decimal currencies
        "CLF" to 4, "UYW" to 4
    )

    private val ALL_VALID_CODES: Map<String, Int> by lazy {
        val map = mutableMapOf<String, Int>()
        for (currency in Currency.getAvailableCurrencies()) {
            val code = currency.currencyCode.uppercase()
            val defaultDigits = currency.defaultFractionDigits
            if (code in EXPLICIT_SCALES) {
                map[code] = EXPLICIT_SCALES.getValue(code)
            } else if (defaultDigits in 0..4) {
                map[code] = defaultDigits
            }
        }
        map.putAll(EXPLICIT_SCALES)
        map
    }

    fun getScaleOrNull(currencyCode: String): Int? =
        ALL_VALID_CODES[currencyCode.uppercase()]
}
```

### 3.2 Strict Non-Rounding Money Parser (`StrictMoneyParser.kt`)
Monetary values are stored as integer minor units (`amountMinor: Long`) paired with an explicit `currencyCode` and `currencyScale`. **Silent rounding is strictly prohibited.** If OCR reads `"12.345"` for `USD` (scale 2), `StrictMoneyParser` returns `ExcessPrecisionFlagged` so the pipeline creates a user-review draft rather than silently altering the amount to `12.35`:

```kotlin
package com.example.financeengine.core.finance

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

@Serializable
data class CurrencyAmount(
    val amountMinor: Long,
    val currencyCode: String,
    val currencyScale: Int
) {
    init {
        val expectedScale = Iso4217CurrencyRegistry.getScaleOrNull(currencyCode)
        require(expectedScale != null) { "Unsupported ISO 4217 code: $currencyCode" }
        require(currencyScale == expectedScale) {
            "Scale mismatch for $currencyCode: expected $expectedScale, got$currencyScale"
        }
    }

    fun toBigDecimal(): BigDecimal =
        BigDecimal.valueOf(amountMinor, currencyScale)
}

sealed class MoneyParseResult {
    data class Exact(val amount: CurrencyAmount) : MoneyParseResult()
    data class ExcessPrecisionFlagged(
        val rawToken: String,
        val currencyCode: String,
        val observedScale: Int,
        val expectedScale: Int
    ) : MoneyParseResult()
    data class UnknownCurrencyCode(val currencyCode: String) : MoneyParseResult()
    data class MalformedNumber(val rawToken: String) : MoneyParseResult()
}

object StrictMoneyParser {
    fun parse(rawDecimalToken: String, currencyCode: String): MoneyParseResult {
        val normalizedCode = currencyCode.trim().uppercase()
        val expectedScale = Iso4217CurrencyRegistry.getScaleOrNull(normalizedCode)
            ?: return MoneyParseResult.UnknownCurrencyCode(normalizedCode)

        val cleaned = rawDecimalToken.trim().replace(" ", "")
        val bd = cleaned.toBigDecimalOrNull()?.stripTrailingZeros()
            ?: return MoneyParseResult.MalformedNumber(rawDecimalToken)

        val observedScale = bd.scale().coerceAtLeast(0)
        if (observedScale > expectedScale) {
            return MoneyParseResult.ExcessPrecisionFlagged(
                rawToken = rawDecimalToken,
                currencyCode = normalizedCode,
                observedScale = observedScale,
                expectedScale = expectedScale
            )
        }

        return try {
            val minor = bd.setScale(expectedScale, RoundingMode.UNNECESSARY)
                .movePointRight(expectedScale)
                .longValueExact()
            MoneyParseResult.Exact(
                CurrencyAmount(
                    amountMinor = minor,
                    currencyCode = normalizedCode,
                    currencyScale = expectedScale
                )
            )
        } catch (e: ArithmeticException) {
            MoneyParseResult.MalformedNumber(rawDecimalToken)
        }
    }
}
```

---

## 4. Core Domain Models & Locked Field Contracts

### 4.1 Document Families, Semantic Roles & Event Kinds

```kotlin
package com.example.financeengine.core.model

import com.example.financeengine.core.finance.CurrencyAmount
import com.example.financeengine.core.vision.NormalizedQuad
import com.example.financeengine.core.vision.NormalizedRect
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
```

### 4.2 Evidence Candidates, `LockedDeterministicFields` & `LlmAnalysisBundle`
Only fields that pass `DeterministicConfidenceGate` are placed inside `LockedDeterministicFields`. Unlocked low-confidence guesses remain inside the candidate lists and never override LLM outputs in the validator:

```kotlin
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
```

### 4.3 `DocumentInterpretation` & Draft-Capable `ResolvedFinancialEvent`
Every event binds its description, amount, date, due date, and payment status to explicit `evidenceIds`:

```kotlin
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
```

---

## 5. Signed-Ledger Reconciliation, Hybrid Field Gate & Semantic Validator

### 5.1 Document-Family Signed-Ledger Reconciliation (`ArithmeticReconciler.kt`)
All amounts must share the same `currencyCode` and `currencyScale` before arithmetic comparison. Each table row is classified into a single mutually exclusive signed-ledger delta $\Delta_r$ so fees, refunds, or payments are never double-counted:

1. **Receipts & Invoices (`RECEIPT`, `INVOICE_OR_UTILITY_BILL`, `REFUND_OR_CREDIT_NOTE`):**
    * **Tax-Exclusive Rule:**
      $$\sum \text{LineItems} + \sum \text{Taxes} + \sum \text{Tips} + \sum \text{Surcharges} - \sum |\text{Discounts}| = \text{Total}$$
    * **Tax-Inclusive Rule (VAT/GST):**
      $$\sum \text{LineItems} + \sum \text{Tips} + \sum \text{Surcharges} - \sum |\text{Discounts}| = \text{Total} \quad \left(\text{with } 0 \le \sum \text{Taxes} < \text{Total}\right)$$
    * **Subtotal-Only Rule (No Line Items):**
      $$\text{Subtotal} + \sum \text{Taxes} + \sum \text{Tips} + \sum \text{Surcharges} - \sum |\text{Discounts}| = \text{Total}$$
2. **Bank Asset Statements (`BANK_STATEMENT`):**
   Each row $r$ is assigned once to $\Delta_r = +\text{Credit}_r$ (deposits/refunds) or $\Delta_r = -\text{Debit}_r$ (withdrawals/purchases/fees):
   $$\text{OpeningBalance} + \sum_{r=1}^{N} \Delta_r = \text{ClosingBalance}$$
3. **Credit-Card Liability Statements (`CREDIT_CARD_STATEMENT`):**
   Each row $r$ is assigned once to $\Delta_r = +\text{Charge}_r$ (purchases, cash advances, fees, interest) or $\Delta_r = -\text{Reduction}_r$ (payments, merchant credits/refunds):
   $$\text{PreviousBalance} + \sum_{r=1}^{N} \Delta_r = \text{NewBalance}$$

### 5.2 Hybrid Field-Level Deterministic Gate (`DeterministicConfidenceGate.kt`)
The gate locks individual fields into `LockedDeterministicFields` **only** when their deterministic confidence and reconciliation invariants are satisfied:

```kotlin
package com.example.financeengine.core.finance

import com.example.financeengine.core.model.*

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
        matchedCategoryRuleId: String?
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

        val lockedTotal = if (reconciled && totalCandidate != null && totalCandidate.confidence >= 0.96f) {
            ResolvedAmountField(totalCandidate.amount!!, totalCandidate.role, totalCandidate.evidenceIds)
        } else null

        val taxCandidate = amountCandidates
            .filter { it.role == SemanticRole.TAX_AMOUNT && it.amount != null }
            .maxByOrNull { it.confidence }

        val lockedTax = if (reconciled && taxCandidate != null && taxCandidate.confidence >= 0.96f) {
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
                documentFamily = lockedFamily!!,
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
                        amount = lockedTotal!!.amount,
                        amountEvidenceIds = lockedTotal.evidenceIds,
                        isoDate = lockedPrimaryDate!!.isoDate,
                        dateEvidenceIds = lockedPrimaryDate.evidenceIds,
                        categoryId = matchedCategoryRuleId!!,
                        persistenceState = EventPersistenceState.VERIFIED_FINAL
                    )
                ),
                reviewRequired = false,
                unresolvedFields = emptyList()
            )
        } else null

        return GateDecision(
            lockedFields = locked,
            fullDocumentResolvedWithoutAi = canBypassLlm,
            resolvedInterpretation = completeInterpretation
        )
    }
}
```

### 5.3 Candidate-Bound `PostLlmSemanticValidator` (`PostLlmSemanticValidator.kt`)
This validator enforces:
1. Only fields explicitly present in `LockedDeterministicFields` override LLM output.
2. Every evidence ID list must be a non-empty subset (`allIdsOwnedBy`) of a single authoritative candidate or table cell.
3. Table-cell transaction dates must own `event.dateEvidenceIds`.
4. `billPaymentStatus` is verified against the actual text of the matched `PAYMENT_STATUS_LABEL` candidate (`parsePaymentStatusFromText`).
5. `event.description` must match a `LINE_ITEM_DESCRIPTION` or `MERCHANT_NAME` candidate or row cell that owns `event.descriptionEvidenceIds`.

```kotlin
package com.example.financeengine.core.validation

import com.example.financeengine.core.model.*
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
        if (!allIdsOwnedBy(event.amountEvidenceIds, validOcrIds)) return null
        if (!allIdsOwnedBy(event.descriptionEvidenceIds, validOcrIds)) return null
        if (event.description.isBlank()) return null

        val matchedRow = event.sourceRowId?.let { rowId ->
            bundle.tableRows.find { it.rowId == rowId }
        }
        if (event.sourceRowId != null && matchedRow == null) return null

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
```

---

## 6. PII-Redacting Bundle Trimmer, Multi-Chunk Merger & Router Interface

### 6.1 Comprehensive Role & Free-Text PII Redaction (`BundleTrimmer.kt`)
`BundleTrimmer` strips PII roles from candidates and table cells, applies regex redaction to **every** free-text field in `LlmAnalysisBundle` (including `lockedDeterministicFields`, `arithmeticValidation.notes`, `ReconstructedTableCell.headerLabel`, `ReconstructedTableCell.rawText`, and `TextCandidate`), and verifies that every serialized chunk is $\le 12,288\text{ bytes}$:

```kotlin
package com.example.financeengine.core.validation

import com.example.financeengine.core.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed class BundleTrimResult {
    data class Success(val chunks: List<LlmAnalysisBundle>) : BundleTrimResult()
    data class BundleCannotFit(val reason: String, val offendingRowId: String?) : BundleTrimResult()
}

object BundleTrimmer {
    const val MAX_UTF8_BYTES = 12_288
    private const val INITIAL_ROW_CHUNK_SIZE = 15

    private val EXCLUDED_PII_ROLES = setOf(
        SemanticRole.CUSTOMER_ADDRESS,
        SemanticRole.ACCOUNT_HOLDER_NAME,
        SemanticRole.CUSTOMER_PHONE,
        SemanticRole.CUSTOMER_EMAIL
    )

    private val EMAIL_REGEX = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val PHONE_REGEX = Regex("""(?<!\d)(?:\+?\d{1,3}[\s.-]?)?\(?\d{3}\)?[\s.-]\d{3}[\s.-]\d{4}(?!\d)""")
    private val ACCOUNT_SEQUENCE_REGEX = Regex("""(?<!\d)(?:\d[\s-]?){7,18}\d(?!\d)""")

    private val compactJson = Json { encodeDefaults = false }

    fun redactFreeText(input: String): String =
        input
            .replace(EMAIL_REGEX, "[REDACTED_EMAIL]")
            .replace(PHONE_REGEX, "[REDACTED_PHONE]")
            .replace(ACCOUNT_SEQUENCE_REGEX) { match ->
                val digitsOnly = match.value.filter { it.isDigit() }
                if (digitsOnly.length >= 8) "****${digitsOnly.takeLast(4)}" else match.value
            }

    private fun redactLockedFields(locked: LockedDeterministicFields): LockedDeterministicFields =
        locked.copy(
            lockedMerchant = locked.lockedMerchant?.let {
                it.copy(value = redactFreeText(it.value))
            },
            lockedIssuerAddress = locked.lockedIssuerAddress?.let {
                it.copy(value = redactFreeText(it.value))
            }
        )

    private fun redactArithmeticSummary(summary: ArithmeticValidationSummary): ArithmeticValidationSummary =
        summary.copy(
            reconciliationRuleUsed = redactFreeText(summary.reconciliationRuleUsed),
            notes = summary.notes.map { redactFreeText(it) }
        )

    fun prepareChunks(fullBundle: LlmAnalysisBundle): BundleTrimResult {
        val redactedLocked = redactLockedFields(fullBundle.lockedDeterministicFields)
        val redactedArithmetic = redactArithmeticSummary(fullBundle.arithmeticValidation)

        val stratifiedText = fullBundle.textCandidates
            .filter { it.role !in EXCLUDED_PII_ROLES }
            .groupBy { it.role }
            .values
            .flatMap { list -> list.sortedByDescending { it.confidence }.take(3) }
            .map {
                it.copy(
                    rawText = redactFreeText(it.rawText),
                    normalizedValue = redactFreeText(it.normalizedValue)
                )
            }

        val stratifiedDates = fullBundle.dateCandidates
            .groupBy { it.role }
            .values
            .flatMap { list -> list.sortedByDescending { it.confidence }.take(3) }

        val stratifiedAmounts = fullBundle.amountCandidates
            .groupBy { it.role }
            .values
            .flatMap { list -> list.sortedByDescending { it.confidence }.take(4) }

        val redactedRows = fullBundle.tableRows.map { row ->
            row.copy(
                cells = row.cells
                    .filter { it.role !in EXCLUDED_PII_ROLES }
                    .map { cell ->
                        cell.copy(
                            headerLabel = cell.headerLabel?.let { redactFreeText(it) },
                            rawText = redactFreeText(cell.rawText)
                        )
                    }
            )
        }

        if (redactedRows.isEmpty()) {
            val single = fullBundle.copy(
                chunkIndex = 0,
                totalChunks = 1,
                lockedDeterministicFields = redactedLocked,
                textCandidates = stratifiedText,
                dateCandidates = stratifiedDates,
                amountCandidates = stratifiedAmounts,
                tableRows = emptyList(),
                arithmeticValidation = redactedArithmetic
            )
            val fitted = fitSingleChunkToBudget(single)
                ?: return BundleTrimResult.BundleCannotFit("Header candidates exceed $MAX_UTF8_BYTES bytes", null)
            return BundleTrimResult.Success(listOf(fitted))
        }

        val builtChunks = mutableListOf<LlmAnalysisBundle>()
        var cursor = 0
        while (cursor < redactedRows.size) {
            var window = INITIAL_ROW_CHUNK_SIZE.coerceAtMost(redactedRows.size - cursor)
            var fittedChunk: LlmAnalysisBundle? = null

            while (window >= 1) {
                val candidateChunk = fullBundle.copy(
                    lockedDeterministicFields = redactedLocked,
                    textCandidates = stratifiedText,
                    dateCandidates = stratifiedDates,
                    amountCandidates = stratifiedAmounts,
                    tableRows = redactedRows.subList(cursor, cursor + window),
                    arithmeticValidation = redactedArithmetic
                )
                val fitted = fitSingleChunkToBudget(candidateChunk)
                if (fitted != null) {
                    fittedChunk = fitted
                    break
                }
                window /= 2
            }

            if (fittedChunk == null) {
                return BundleTrimResult.BundleCannotFit(
                    reason = "Single table row exceeds $MAX_UTF8_BYTES bytes even after context stripping",
                    offendingRowId = redactedRows[cursor].rowId
                )
            }

            builtChunks += fittedChunk
            cursor += window
        }

        val total = builtChunks.size
        return BundleTrimResult.Success(
            builtChunks.mapIndexed { idx, chunk ->
                chunk.copy(chunkIndex = idx, totalChunks = total)
            }
        )
    }

    private fun fitSingleChunkToBudget(bundle: LlmAnalysisBundle): LlmAnalysisBundle? {
        if (measureUtf8Bytes(bundle) <= MAX_UTF8_BYTES) return bundle

        val withoutGeneric = bundle.copy(
            textCandidates = bundle.textCandidates.filter { it.role != SemanticRole.GENERIC_LINE }
        )
        if (measureUtf8Bytes(withoutGeneric) <= MAX_UTF8_BYTES) return withoutGeneric

        val minimalContext = withoutGeneric.copy(
            textCandidates = withoutGeneric.textCandidates.take(6),
            amountCandidates = withoutGeneric.amountCandidates.take(8)
        )
        return if (measureUtf8Bytes(minimalContext) <= MAX_UTF8_BYTES) minimalContext else null
    }

    fun measureUtf8Bytes(bundle: LlmAnalysisBundle): Int =
        compactJson.encodeToString(bundle).encodeToByteArray().size
}
```

### 6.2 Reference AI Router Integration Interface (`AiInterpretationRouter.kt`)
The document pipeline communicates with the existing AI Router exclusively through this contract. Implementations must rethrow `CancellationException` on coroutine cancellation:

```kotlin
package com.example.financeengine.core.ai

import com.example.financeengine.core.model.DocumentInterpretation
import com.example.financeengine.core.model.LlmAnalysisBundle

sealed class RouterInterpretationOutcome {
    data class ValidatedSuccess(
        val interpretation: DocumentInterpretation,
        val providerUsed: String,
        val modelIdUsed: String,
        val totalLatencyMs: Long
    ) : RouterInterpretationOutcome()

    data class DeterministicFallbackOnly(
        val partialInterpretation: DocumentInterpretation,
        val fallbackReason: String
    ) : RouterInterpretationOutcome()
}

interface AiInterpretationRouter {
    /**
     * Submits redacted, byte-bounded [LlmAnalysisBundle] chunks to the active AI provider,
     * validates each chunk response via [PostLlmSemanticValidator], and merges chunk results
     * via [ChunkInterpretationMerger]. Must rethrow [kotlinx.coroutines.CancellationException].
     */
    suspend fun interpretDocumentChunks(
        chunks: List<LlmAnalysisBundle>,
        validOcrIds: Set<String>,
        fallbackPartialInterpretation: DocumentInterpretation
    ): RouterInterpretationOutcome
}
```

### 6.3 Safe Multi-Chunk Aggregation & Conflict Resolution (`ChunkInterpretationMerger.kt`)
When a multi-page statement is processed across $K$ chunks:
1. **Composite Event Deduplication:** Events are deduplicated by `EventDedupKey` (combining `sourceRowId`, `eventKind`, `amount`, `draftRawAmountToken`, `isoDate`, normalized `description`, and sorted `amountEvidenceIds`). This preserves legitimate multiple events originating from the same row (e.g., a purchase and a separate inline foreign-transaction fee with distinct amounts/evidence IDs) while eliminating duplicate events across chunk boundaries.
2. **Cross-Chunk Document-Field Conflict Detection:** If two chunks return non-null validated values for the same document-level field (`documentFamily`, `merchantOrIssuer`, `issuerAddress`, `primaryDate`, `dueDate`, `documentTotal`, `taxTotal`, or `statementSummary` subfields) that disagree, the merger flags `"conflict:<fieldName>"` in `unresolvedFields`, nulls out the conflicting unlocked field, and sets `reviewRequired = true` rather than blindly trusting `chunks.first()`.

```kotlin
package com.example.financeengine.core.validation

import com.example.financeengine.core.finance.CurrencyAmount
import com.example.financeengine.core.model.*

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
```

---

## 7. Encrypted Persistence, Retention Lifecycle & Review UI

### 7.1 Room + SQLCipher Entities & Foreign-Key Contracts (`Entities.kt`)
* `OcrElementEntity` is explicitly persisted and cascades on page deletion (`onDelete = ForeignKey.CASCADE`), and is purged after 30 days by `PurgeExpiredEvidenceWorker`.
* `FinancialEventEntity` declares `@ForeignKey` to `FinancialDocumentEntity` with `onDelete = ForeignKey.SET_NULL` so verified financial transactions survive source-document deletion.
* `EvidenceManifestEntity` stores a keyed **HMAC-SHA256** digest (`normalizedValueHmac`) of each verified field so post-purge provenance remains verifiable without retaining raw OCR text.

```kotlin
package com.example.financeengine.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    foreignKeys = [
        ForeignKey(
            entity = FinancialDocumentEntity::class,
            parentColumns = ["documentId"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("documentId")]
)
data class DocumentPageEntity(
    @PrimaryKey val pageId: String,
    val documentId: String,
    val pageNumber: Int,
    val encryptedRawImagePath: String?,
    val encryptedCleanImagePath: String?,
    val homographyNormJson: String, // 9 floats in normalized [0,1]^2 space
    val dominantHeaderHex: String?,
    val dominantBackgroundHex: String?
)

@Entity(
    tableName = "ocr_elements",
    foreignKeys = [
        ForeignKey(
            entity = DocumentPageEntity::class,
            parentColumns = ["pageId"],
            childColumns = ["pageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pageId"), Index("documentId")]
)
data class OcrElementEntity(
    @PrimaryKey val elementId: String, // "P01_T0014"
    val documentId: String,
    val pageId: String,
    val pageNumber: Int,
    val rawText: String,
    val normalizedText: String,
    val provenance: String,
    val boxNormalizedOcrJson: String,
    val boxNormalizedRawQuadJson: String?,
    val ocrConfidence: Float,
    val rotationDegrees: Float,
    val lineId: String,
    val region: String
)

@Entity(
    tableName = "evidence_manifests",
    indices = [Index("documentId")]
)
data class EvidenceManifestEntity(
    @PrimaryKey val evidenceId: String, // "P01_T0014"
    val documentId: String,
    val pageNumber: Int,
    val semanticRole: String,
    val normalizedValueHmac: String, // Keyed HMAC-SHA256 hex digest (Tink Mac)
    val hmacKeyVersion: Int,
    val rawQuadNormalizedJson: String?, // Null once raw image is purged
    val verifiedByUser: Boolean
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
    indices = [Index("documentId")]
)
data class StatementSummaryEntity(
    @PrimaryKey val summaryId: String,
    val documentId: String,
    val currencyCode: String,
    val currencyScale: Int,
    val openingBalanceMinor: Long?,
    val closingBalanceMinor: Long?,
    val minimumPaymentDueMinor: Long?,
    val periodStartIso: String?,
    val periodEndIso: String?,
    val reconciledExact: Boolean
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
        Index(value = ["dedupFingerprint"], unique = true),
        Index(value = ["documentId"])
    ]
)
data class FinancialEventEntity(
    @PrimaryKey val eventId: String,
    val documentId: String?, // Set to null via ForeignKey.SET_NULL if user deletes source document
    val eventKind: String,
    val persistenceState: String, // VERIFIED_FINAL or NEEDS_USER_REVIEW_DRAFT
    val eventDateIso: String?, // Non-null required for VERIFIED_FINAL
    val dueDateIso: String?,
    val billPaymentStatus: String,
    val amountMinor: Long?, // Non-null required for VERIFIED_FINAL
    val draftRawAmountToken: String?, // Populated when ExcessPrecisionFlagged
    val draftParseIssue: String?,
    val currencyCode: String,
    val currencyScale: Int,
    val merchantNormalized: String,
    val description: String,
    val categoryId: String,
    val accountIdentifierMasked: String?,
    val sourceRowId: String?,
    val evidenceIdsCsv: String,
    val dedupFingerprint: String,
    val verifiedByUser: Boolean
) {
    init {
        if (persistenceState == "VERIFIED_FINAL") {
            require(amountMinor != null && eventDateIso != null) {
                "VERIFIED_FINAL events require non-null amountMinor and eventDateIso"
            }
        }
    }
}
```

### 7.2 Cryptographic Key Management & Backup Exclusions
1. **SQLCipher Database Key Lifecycle:**
    * Generate a 256-bit random passphrase on first launch and encrypt it using a dedicated Tink `Aead` keyset backed by the Android Keystore (`android-keystore://finance_db_master_key`).
    * Pass the decrypted byte array to `SupportOpenHelperFactory(passphraseBytes)` and immediately zero the in-memory `ByteArray` after database opening.
2. **Per-File Image Encryption (`Tink StreamingAead`):**
    * Encrypt `raw_display_page_{n}.enc` and `clean_ocr_page_{n}.enc` using Tink `StreamingAead` (`AES256_GCM_HKDF_4KB`) backed by `android-keystore://finance_image_master_key`.
    * Bind Associated Data `AD = "$documentId:$pageNumber:$variant"` to prevent cross-document file swapping. Write to a `.tmp` file first and atomically rename on completion.
3. **Provenance Digest Key (`Tink Mac` HMAC-SHA256):**
    * Generate a dedicated Tink `Mac` (`HMAC_SHA256_256BITTAG`) keyset backed by `android-keystore://finance_manifest_hmac_key`.
    * Compute `normalizedValueHmac = hmacSha256("$documentId|$evidenceId|$semanticRole|$normalizedValue")` when creating `EvidenceManifestEntity`.
4. **Android Backup Exclusions (`data_extraction_rules.xml` & `backup_rules.xml`):**
    * Exclude `databases/`, `files/evidence_images/`, and `datastore/` containing encryption keysets from both cloud backup and device-to-device transfer so ciphertext never separates from hardware-bound Android Keystore keys.

### 7.3 Retention & Deletion Workflows (`FinancialDocumentDao.kt`)

```kotlin
package com.example.financeengine.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FinancialDocumentDao {

    @Query("DELETE FROM ocr_elements WHERE documentId = :documentId")
    suspend fun deleteOcrElementsForDocument(documentId: String)

    @Query("UPDATE document_pages SET encryptedRawImagePath = NULL, encryptedCleanImagePath = NULL WHERE documentId = :documentId")
    suspend fun clearPageImagePaths(documentId: String)

    @Query("UPDATE evidence_manifests SET rawQuadNormalizedJson = NULL WHERE documentId = :documentId")
    suspend fun clearManifestQuads(documentId: String)

    @Query("UPDATE financial_documents SET rawEvidencePurged = 1 WHERE documentId = :documentId")
    suspend fun markDocumentRawEvidencePurged(documentId: String)

    @Query("DELETE FROM financial_events WHERE documentId = :documentId AND persistenceState = 'NEEDS_USER_REVIEW_DRAFT'")
    suspend fun deleteUnverifiedDraftEventsForDocument(documentId: String)

    @Query("DELETE FROM financial_events WHERE documentId = :documentId")
    suspend fun deleteAllEventsForDocument(documentId: String)

    @Query("DELETE FROM financial_documents WHERE documentId = :documentId")
    suspend fun deleteFinancialDocumentRow(documentId: String)

    /**
     * Invoked by PurgeExpiredEvidenceWorker 30 days after document verification
     * (after encrypted bitmap files on disk have been securely deleted).
     */
    @Transaction
    suspend fun purgeRawEvidenceMetadata(documentId: String) {
        deleteOcrElementsForDocument(documentId)
        clearPageImagePaths(documentId)
        clearManifestQuads(documentId)
        markDocumentRawEvidencePurged(documentId)
    }

    /**
     * Option 1 in User Delete Dialog: Deletes the document, pages, OCR rows, and unverified drafts,
     * while verified FinancialEventEntity rows survive with documentId = NULL via ForeignKey.SET_NULL.
     */
    @Transaction
    suspend fun deleteSourceDocumentPreservingVerifiedEvents(documentId: String) {
        deleteUnverifiedDraftEventsForDocument(documentId)
        deleteFinancialDocumentRow(documentId)
    }

    /**
     * Option 2 in User Delete Dialog: Deletes the document AND all derived financial events.
     */
    @Transaction
    suspend fun deleteDocumentAndAllDerivedEvents(documentId: String) {
        deleteAllEventsForDocument(documentId)
        deleteFinancialDocumentRow(documentId)
    }
}
```

### 7.4 Jetpack Compose Review Screen & 4-Corner Polygon Overlay
* **4-Corner Polygon Highlight:** When the user taps any resolved field or event, the UI looks up its `evidenceIds` in `EvidenceManifestEntity`, deserializes `rawQuadNormalizedJson` (`NormalizedQuad`), and draws a closed 4-vertex `Path` (`moveTo(tl) -> lineTo(tr) -> lineTo(br) -> lineTo(bl) -> close()`) over the decrypted `raw_display` bitmap.
* **Honest Post-Purge Provenance Status Badge:**
    * While `rawEvidencePurged == false` and `rawQuadNormalizedJson != null`: display **"Tap to inspect source highlight on scan"**.
    * Once `rawEvidencePurged == true` or `documentId == null`: display **"Source Scan Purged (Retention Policy) — HMAC Provenance Digest Recorded"** so the UI never implies the user can visually re-verify purged pixels.

---

## 8. Acceptance Corpus, Evaluation Metrics & Exact Invariant Tests

### 8.1 150-Document Corpus Split & Evaluation Metrics
1. **Strict Dataset Partition (Zero Merchant-Template Overlap):**
    * **Calibration Set ($N = 50$):** Used to tune layout clustering and confidence thresholds (18 retail/dining receipts, 10 utility bills/invoices, 10 bank statements, 7 credit-card statements, 5 refund notes).
    * **Held-Out Evaluation Set ($N = 100$):** Used strictly for release gating (35 retail/dining receipts including curled thermal paper, 20 utility bills/invoices, 20 multi-page bank statements, 15 credit-card statements, 10 refund notes).
2. **Deterministic Gate Evaluation Metrics:**
    * **Precision on Locked Fields:** $\ge 99.0\%$ exact match against ground truth.
    * **False-Lock Rate:** $\le 1.0\%$ across the 100 held-out documents.
    * **Field Lock Coverage:** Reported separately per field (`documentTotal`, `primaryDate`, `merchantOrIssuer`) so high precision cannot mask a gate that abstains on all documents.

### 8.2 Mandatory Unit & Integration Test Suite (`./gradlew test`)
1. `StrictMoneyParserTest`:
    * Verifies `JPY 1500` $\rightarrow$ `amountMinor = 1500, scale = 0`; `USD 20.07` $\rightarrow$ `amountMinor = 2007, scale = 2`; `KWD 4.125` $\rightarrow$ `amountMinor = 4125, scale = 3`; `PLN 49.99` $\rightarrow$ `amountMinor = 4999, scale = 2`.
    * Verifies `"12.345"` for `USD` returns `MoneyParseResult.ExcessPrecisionFlagged` and **never** rounds to `1235`.
2. `CoordinateTransformMapperTest`:
    * Verifies $H_{\text{norm}}$ maps normalized OCR boxes to 4-corner `NormalizedQuad` coordinates across $90^\circ$ rotation and $15^\circ$ perspective skew within $\le 1.5\text{ px}$ error.
    * Verifies a singular matrix ($\det(H_{\text{norm}}) = 0$) returns `ProjectionResult.SingularOrInvalidTransform` rather than returning untransformed points.
3. `LockedDeterministicFieldsValidatorTest`:
    * Verifies that when `DeterministicConfidenceGate` locks only `lockedDocumentTotal` and leaves `lockedMerchant = null`, `PostLlmSemanticValidator` preserves `lockedDocumentTotal` while allowing a valid LLM `merchantOrIssuer` to be accepted (never overwriting with an unlocked deterministic guess).
4. `ValidatorProvenanceAndRoleTest`:
    * Verifies `PostLlmSemanticValidator` rejects mixed evidence lists (`["P01_T0014", "P01_T9999"]`) when `"P01_T9999"` is not owned by the matched candidate.
    * Verifies a table-cell date is rejected if `event.dateEvidenceIds` does not match the date cell's `evidenceIds`.
    * Verifies `billPaymentStatus = PAID_IN_FULL` is rejected when the cited `PAYMENT_STATUS_LABEL` candidate text reads `"UNPAID"` or `"AMOUNT DUE"`.
    * Verifies an event with an LLM-invented `description` that does not match any `LINE_ITEM_DESCRIPTION` or `MERCHANT_NAME` candidate/cell is rejected (`null`).
5. `BundleTrimmerRedactionAndBudgetTest`:
    * Seeds synthetic customer addresses, emails, formatted phone numbers (`(555) 234-5678`), and hyphenated account numbers (`1234-5678-9012-3456`) into `lockedDeterministicFields`, `arithmeticValidation.notes`, `ReconstructedTableCell.headerLabel`, `ReconstructedTableCell.rawText`, and `textCandidates`, and asserts zero raw PII tokens exist in the serialized JSON string.
    * Verifies a synthetic 20 KB single table row returns `BundleTrimResult.BundleCannotFit` and never emits a chunk $> 12,288\text{ bytes}$.
6. `ChunkInterpretationMergerTest`:
    * Verifies two distinct events on the same statement row (e.g., purchase + foreign transaction fee with distinct `amountEvidenceIds`) are both preserved, while duplicate overlapping boundary events are deduplicated.
    * Verifies that if Chunk 0 and Chunk 1 return conflicting unlocked `documentTotal` values, `mergeValidatedChunks()` sets `documentTotal = null`, adds `"conflict:documentTotal"` to `unresolvedFields`, and forces `reviewRequired = true`.
7. `PersistenceForeignKeyAndRetentionTest`:
    * Verifies `deleteSourceDocumentPreservingVerifiedEvents()` deletes `FinancialDocumentEntity`, `DocumentPageEntity`, `OcrElementEntity`, and draft events while keeping `VERIFIED_FINAL` `FinancialEventEntity` rows with `documentId = null`.

```