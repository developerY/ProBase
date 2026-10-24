# Master Engineering Specification: Local-Processing-First, Cloud-Assisted AI Financial Document Engine for Android

## 0. Architectural Mandate & Core System Boundaries

You are tasked with implementing a production-grade Android financial document intelligence engine in **Kotlin** using **Jetpack Compose (Material 3)**, **Coroutines/Flow**, **Hilt**, **Room 2.8.5 (with SQLCipher 4.19.1)**, **Jetpack DataStore + Google Tink (`Aead` and `StreamingAead`)**, **Firebase AI Logic (BoM 35.1.0 with Play Integrity App Check)**, and **Google ML Kit**.

### The Three Explicit System Boundaries

1. **Local-First Document Preparation:** Raw camera images, gallery imports, and PDFs never leave the device. Image quality assessment, perspective/deskew transformations, OCR, perceptual color extraction, spatial table reconstruction, locale-aware candidate extraction, and arithmetic validation always execute locally on the phone.
2. **Cloud-Assisted Semantic Interpretation (with Field-Level & Document-Level Deterministic Gate):** When the deterministic engine resolves the entire document with high confidence, no LLM is invoked. When partial fields (such as a mathematically reconciled total and date) are locked deterministically, those fields are marked `DETERMINISTIC_LOCKED` and the LLM is invoked only to resolve remaining semantic ambiguities (such as category classification or event kind). The system defaults to **Hosted Gemma (`GEMMA_4_CLOUD_FREE`)** via an App Check–authenticated backend proxy, with **On-Device Gemini Nano** treated as an optional runtime optimization rather than a hardware requirement.
3. **Granular Per-Provider Consent, Redaction & Safe Diagnostics:** Because a text-only `LlmAnalysisBundle` still contains financial metadata, the system enforces **per-provider cloud consent** (`consentedCloudProviders`), **explicit fallback authorization** (`authorizedFallbackProviders`), a **PII Redaction & Minimization Pass** prior to serialization, a strict **Local-Only Mode**, and **safe-by-default diagnostics** that omit raw financial text unless explicitly opted in for a time-bounded troubleshooting session.

---

## 1. Audited Late-2026 Dependency Baseline & Multi-Module Architecture

### 1.1 Gradle Version Catalog (`gradle/libs.versions.toml`)

Before generating domain code in Phase 1, the coding agent must validate that the Kotlin compiler, KSP, Compose, Room, SQLite, SQLCipher, and Hilt toolchain compiles cleanly via `./gradlew check`.

```toml
[versions]
kotlin = "2.4.21"
ksp = "2.4.21-1.0.32"
coroutines = "1.11.0"
serialization = "1.11.0"
composeBom = "2026.09.00"
room = "2.8.5"
sqliteKtx = "2.7.1"
sqlcipher = "4.19.1"
hilt = "2.60.1"
datastore = "1.1.4"
tink = "1.16.0"
mlkitTextRec = "16.0.1"
mlkitDocScanner = "16.0.0"
firebaseBom = "35.1.0"
okhttp = "5.1.0"
workManager = "2.10.1"

[libraries]
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-sqlite-ktx = { group = "androidx.sqlite", name = "sqlite-ktx", version.ref = "sqliteKtx" }
sqlcipher-android = { group = "net.zetetic", name = "sqlcipher-android", version.ref = "sqlcipher" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
google-tink-android = { group = "com.google.crypto.tink", name = "tink-android", version.ref = "tink" }
mlkit-text-recognition = { group = "com.google.mlkit", name = "text-recognition", version.ref = "mlkitTextRec" }
mlkit-document-scanner = { group = "com.google.android.gms", name = "play-services-mlkit-document-scanner", version.ref = "mlkitDocScanner" }
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }
firebase-appcheck-playintegrity = { group = "com.google.firebase", name = "firebase-appcheck-playintegrity" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
```

*Isolation Mandate for AICore:* The on-device Gemini Nano dependency (`com.google.ai.edge.aicore:aicore`) must be isolated strictly inside `:core:ai:nanoadapter` so experimental SDK changes can never destabilize the primary cloud or deterministic paths.

### 1.2 Gradle Module Topology

| Module | Core Responsibilities |
| :--- | :--- |
| `:feature:documentcapture` | ML Kit Document Scanner (`16.0.0`), gallery picker, PDF import, Tink `StreamingAead` file encryption. |
| `:core:ocr` | Native PDF text-layer fast path, ML Kit Text Recognition v2 (`16.0.1`), page-scoped ID assignment (`P01_T0001`). |
| `:core:document:vision` | Blur/glare detection, perspective/deskew homography capture, normalized coordinate mapping, CIELAB color extraction. |
| `:core:document:layout` | Rotation-aware vector line grouping, 1D k-means column clustering, table reconstruction, header/body/footer zoning. |
| `:core:finance:extract` | Strict ISO 4217 currency registry, locale-aware date/amount candidate extraction, entity role scoring. |
| `:core:finance:validation` | Document-family classifier, account-type-aware arithmetic reconciliation, field-level deterministic gate, post-LLM validator. |
| `:core:ai` | Capability registry, per-provider consent router, PII-redacting `BundleTrimmer`, cloud/local adapters, AI Config UI. |
| `:core:database` | SQLCipher-encrypted Room 2.8.5 database, `StatementSummaryEntity`, `EvidenceManifestEntity`, Tink credential vault, retention worker. |
| `:feature:review` | Split-pane Compose review screen, 4-corner quadrilateral `Canvas` evidence overlay, draft resolution, rule learning. |

---

## 2. Local Capture, Authenticated File Encryption, Vision & Coordinate Mapping

### 2.1 Native Digital PDF Fast-Path vs. Camera Capture
1. **Native Digital PDF Check:** When a PDF is imported, inspect its embedded text layer before rasterizing. If extractable text blocks with character bounding boxes exist, extract them directly into `OcrElement` records with `confidence = 1.0f` and an identity homography matrix $I_3$, bypassing optical OCR.
2. **Camera / Scanned Image Path:** Use `GmsDocumentScanner` (`16.0.0`) or `CameraX`. Retain two images per page in app-private encrypted storage:
   * `raw_display_page_{n}.enc`: Original color bitmap with dimensions $W_r \times H_r$ for CIELAB color extraction and the Review UI.
   * `clean_ocr_page_{n}.enc`: Deskewed, shadow-removed, contrast-normalized bitmap with dimensions $W_o \times H_o$ for ML Kit Text Recognition v2.

### 2.2 Explicit Coordinate Spaces & Normalized Homography Mapping
The pipeline maintains a tested coordinate mapping for every affine or projective transformation it performs. Define three explicit coordinate spaces for each page:
* **Raw Display Pixel Space:** $(x_r, y_r) \in [0, W_r] \times [0, H_r]$
* **Cleaned OCR Pixel Space:** $(x_o, y_o) \in [0, W_o] \times [0, H_o]$
* **Normalized Unit Space:** $(\hat{x}, \hat{y}) \in [0, 1]^2$

Let $H_{\text{px}}$ be the $3 \times 3$ forward projective matrix mapping raw display pixels to cleaned OCR pixels during preprocessing, and $H_{\text{px}}^{-1}$ be its inverse. Define the scaling matrices:

$$N_{\text{raw}} = \begin{bmatrix} \frac{1}{W_r} & 0 & 0 \\ 0 & \frac{1}{H_r} & 0 \\ 0 & 0 & 1 \end{bmatrix}, \quad N_{\text{ocr}}^{-1} = \begin{bmatrix} W_o & 0 & 0 \\ 0 & H_o & 0 \\ 0 & 0 & 1 \end{bmatrix}$$

We compose and store the **Normalized Inverse Homography Matrix** $H_{\text{norm}}$ (9 floats in row-major order) whose inputs and outputs are both in normalized $[0, 1]^2$ coordinates:

$$H_{\text{norm}} = N_{\text{raw}} \cdot H_{\text{px}}^{-1} \cdot N_{\text{ocr}}^{-1}$$

For any normalized corner $(\hat{x}_o, \hat{y}_o) \in [0, 1]^2$ of an OCR bounding box in `clean_ocr_page`, its corresponding normalized position $(\hat{x}_r, \hat{y}_r) \in [0, 1]^2$ on `raw_display_page` is:

$$\begin{bmatrix} u \\ v \\ w \end{bmatrix} = H_{\text{norm}} \begin{bmatrix} \hat{x}_o \\ \hat{y}_o \\ 1 \end{bmatrix}, \quad \hat{x}_r = \frac{u}{w}, \quad \hat{y}_r = \frac{v}{w}$$

* **4-Corner Quadrilateral Requirement:** Because perspective transformations turn an axis-aligned rectangle into a non-rectangular quadrilateral, the mapper transforms **all four corners** $(TL, TR, BR, BL)$ of every OCR bounding box into a `NormalizedQuad` and also computes a conservative enclosing `NormalizedRect`.
* **Nonlinear Dewarping Rule:** If a nonlinear cylindrical/polynomial dewarping stage is ever introduced for severely curled receipts, a single $3 \times 3$ matrix cannot invert it; that stage must persist a dense $16 \times 16$ bilinear displacement lookup grid alongside $H_{\text{norm}}$.

```kotlin
package com.finance.engine.core.document.vision

import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

@Serializable
data class NormalizedPoint(val x: Float, val y: Float)

@Serializable
data class NormalizedRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
) {
    fun corners(): List<NormalizedPoint> = listOf(
        NormalizedPoint(x, y),
        NormalizedPoint(x + width, y),
        NormalizedPoint(x + width, y + height),
        NormalizedPoint(x, y + height)
    )
}

@Serializable
data class NormalizedQuad(
    val topLeft: NormalizedPoint,
    val topRight: NormalizedPoint,
    val bottomRight: NormalizedPoint,
    val bottomLeft: NormalizedPoint
) {
    fun enclosingRect(): NormalizedRect {
        val xs = listOf(topLeft.x, topRight.x, bottomRight.x, bottomLeft.x)
        val ys = listOf(topLeft.y, topRight.y, bottomRight.y, bottomLeft.y)
        val minX = xs.minOrNull()?.coerceIn(0f, 1f) ?: 0f
        val minY = ys.minOrNull()?.coerceIn(0f, 1f) ?: 0f
        val maxX = xs.maxOrNull()?.coerceIn(0f, 1f) ?: 1f
        val maxY = ys.maxOrNull()?.coerceIn(0f, 1f) ?: 1f
        return NormalizedRect(minX, minY, max(0f, maxX - minX), max(0f, maxY - minY))
    }
}

object CoordinateTransformMapper {
    fun mapOcrRectToRawQuad(
        ocrRect: NormalizedRect,
        hNorm: FloatArray // 9 elements, row-major
    ): NormalizedQuad {
        require(hNorm.size == 9) { "H_norm must contain 9 floats." }
        val mapped = ocrRect.corners().map { pt -> projectPoint(pt, hNorm) }
        return NormalizedQuad(
            topLeft = mapped[0],
            topRight = mapped[1],
            bottomRight = mapped[2],
            bottomLeft = mapped[3]
        )
    }

    private fun projectPoint(pt: NormalizedPoint, h: FloatArray): NormalizedPoint {
        val u = h[0] * pt.x + h[1] * pt.y + h[2]
        val v = h[3] * pt.x + h[4] * pt.y + h[5]
        val w = h[6] * pt.x + h[7] * pt.y + h[8]
        if (kotlin.math.abs(w) < 1e-6f) return pt
        return NormalizedPoint(
            x = (u / w).coerceIn(0f, 1f),
            y = (v / w).coerceIn(0f, 1f)
        )
    }
}
```

### 2.3 Pre-OCR Quality Metrics & Perceptual Color Extraction
* **Laplacian Blur Variance:** Compute $\sigma^2(\nabla^2 I)$ on a $1024\text{px}$ downscaled grayscale copy. Flag `isBlurry = true` if $\sigma^2 < 110.0$.
* **Glare Ratio:** Flag `hasGlare = true` if $> 4.5\%$ of document interior pixels have luminance $Y > 250$.
* **CIELAB Color Distance:** Extract dominant background and header colors in CIELAB $(L^*, a^*, b^*)$. Compare against stored merchant templates using $\Delta E_{ab}^* = \sqrt{(\Delta L^*)^2 + (\Delta a^*)^2 + (\Delta b^*)^2}$ (treating $\Delta E_{ab}^* < 12.0$ as a supporting template signal).

---

## 3. Spatial Layout Engine & Strict Currency/Candidate Extraction

### 3.1 Page-Scoped Evidence IDs & Rotation-Aware Line Grouping
Every OCR element receives a globally unambiguous within-document ID formatted as `P{page:02d}_T{index:04d}` (e.g., `P01_T0014`).
To handle curved thermal receipts where $Y$-coordinates drift across a row, two elements $A$ and $B$ ($x_A < x_B$) are grouped into the same `LayoutLine` (`P01_L0009`) if and only if projecting $A$'s center at angle $\theta_A$ to $x_B$ satisfies:

$$y_{\text{proj}} = y_A + (x_B - x_A) \tan(\theta_A), \quad |y_B - y_{\text{proj}}| \le 0.45 \cdot \max(h_A, h_B)$$

### 3.2 Controlled ISO 4217 Currency Registry & Strict Non-Rounding Money Model
* **No Silent Rounding Mandate:** Financial parsers must **never** silently round excess fractional digits (e.g., `RoundingMode.HALF_UP` is strictly forbidden during candidate extraction). If OCR reads `12.345` for a 2-decimal currency (`USD`), the parser must return `CurrencyParseResult.ExcessPrecisionFlagged` so the candidate is either recognized as a fractional unit price (e.g., fuel price per gallon) or flagged for user review rather than silently mutating `12.345` into `12.35`.

```kotlin
package com.finance.engine.core.finance.extract

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

object Iso4217CurrencyRegistry {
    const val REGISTRY_VERSION = "2026.10"

    private val scaleByCurrency: Map<String, Int> = mapOf(
        // 0-decimal currencies
        "BIF" to 0, "CLP" to 0, "DJF" to 0, "GNF" to 0, "ISK" to 0,
        "JPY" to 0, "KMF" to 0, "KRW" to 0, "PYG" to 0, "RWF" to 0,
        "UGX" to 0, "VND" to 0, "VUV" to 0, "XAF" to 0, "XOF" to 0, "XPF" to 0,
        // 2-decimal currencies
        "USD" to 2, "EUR" to 2, "GBP" to 2, "CAD" to 2, "AUD" to 2,
        "NZD" to 2, "CHF" to 2, "CNY" to 2, "HKD" to 2, "SGD" to 2,
        "INR" to 2, "MXN" to 2, "BRL" to 2, "ZAR" to 2, "SEK" to 2,
        "NOK" to 2, "DKK" to 2, "PLN" to 2, "TWD" to 2, "THB" to 2,
        "MYR" to 2, "PHP" to 2, "IDR" to 2, "TRY" to 2, "AED" to 2, "SAR" to 2,
        // 3-decimal currencies
        "BHD" to 3, "IQD" to 3, "JOD" to 3, "KWD" to 3, "LYD" to 3,
        "OMR" to 3, "TND" to 3
    )

    fun getExpectedScale(currencyCode: String): Int? =
        scaleByCurrency[currencyCode.uppercase().trim()]
}

@Serializable
data class CurrencyAmount(
    val amountMinor: Long,
    val currencyCode: String,
    val currencyScale: Int
) {
    init {
        val expected = Iso4217CurrencyRegistry.getExpectedScale(currencyCode)
        require(expected != null) { "Unsupported ISO 4217 currency code: $currencyCode" }
        require(currencyScale == expected) {
            "Currency scale mismatch for $currencyCode: expected $expected, got $currencyScale"
        }
    }

    fun toBigDecimal(): BigDecimal =
        BigDecimal.valueOf(amountMinor, currencyScale)
}

sealed interface CurrencyParseResult {
    data class Exact(val amount: CurrencyAmount) : CurrencyParseResult
    data class ExcessPrecisionFlagged(
        val rawDecimal: String,
        val currencyCode: String,
        val expectedScale: Int,
        val actualScale: Int,
        val evidenceId: String
    ) : CurrencyParseResult
    data class InvalidInput(val reason: String) : CurrencyParseResult
}

object StrictMoneyParser {
    fun parseExact(
        rawNumericString: String,
        currencyCode: String,
        evidenceId: String
    ): CurrencyParseResult {
        val code = currencyCode.uppercase().trim()
        val expectedScale = Iso4217CurrencyRegistry.getExpectedScale(code)
            ?: return CurrencyParseResult.InvalidInput("Unknown currency code: $code")
        val bd = rawNumericString.trim().toBigDecimalOrNull()
            ?: return CurrencyParseResult.InvalidInput("Malformed decimal: $rawNumericString")

        val stripped = bd.stripTrailingZeros()
        val effectiveScale = stripped.scale().coerceAtLeast(0)
        if (effectiveScale > expectedScale) {
            return CurrencyParseResult.ExcessPrecisionFlagged(
                rawDecimal = rawNumericString,
                currencyCode = code,
                expectedScale = expectedScale,
                actualScale = effectiveScale,
                evidenceId = evidenceId
            )
        }

        return try {
            val minor = bd.setScale(expectedScale, RoundingMode.UNNECESSARY)
                .movePointRight(expectedScale)
                .longValueExact()
            CurrencyParseResult.Exact(CurrencyAmount(minor, code, expectedScale))
        } catch (e: ArithmeticException) {
            CurrencyParseResult.InvalidInput("Arithmetic overflow or scale violation: ${e.message}")
        }
    }
}
```

---

## 4. Financial Domain Model, Document Families & Data Contracts

### 4.1 Canonical Serializable Domain Contracts

```kotlin
package com.finance.engine.core.model

import com.finance.engine.core.document.vision.NormalizedQuad
import com.finance.engine.core.document.vision.NormalizedRect
import com.finance.engine.core.finance.extract.CurrencyAmount
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DocumentFamily {
    @SerialName("receipt") RECEIPT,
    @SerialName("invoice_or_utility_bill") INVOICE_OR_UTILITY_BILL,
    @SerialName("bank_statement") BANK_STATEMENT,
    @SerialName("credit_card_statement") CREDIT_CARD_STATEMENT,
    @SerialName("refund_or_credit_note") REFUND_OR_CREDIT_NOTE,
    @SerialName("unknown") UNKNOWN
}

@Serializable
enum class FinancialEventKind {
    @SerialName("purchase_expense") PURCHASE_EXPENSE,
    @SerialName("unpaid_bill_liability") UNPAID_BILL_LIABILITY,
    @SerialName("bill_payment_settlement") BILL_PAYMENT_SETTLEMENT,
    @SerialName("internal_or_cc_transfer") INTERNAL_OR_CC_TRANSFER,
    @SerialName("refund_or_credit") REFUND_OR_CREDIT,
    @SerialName("income_or_deposit") INCOME_OR_DEPOSIT,
    @SerialName("fee_or_interest_charge") FEE_OR_INTEREST_CHARGE,
    @SerialName("statement_balance_ignore") STATEMENT_BALANCE_IGNORE
}

@Serializable
enum class BillPaymentStatus {
    @SerialName("unpaid_due") UNPAID_DUE,
    @SerialName("paid_in_full") PAID_IN_FULL,
    @SerialName("autopay_scheduled") AUTOPAY_SCHEDULED,
    @SerialName("unknown") UNKNOWN
}

@Serializable
enum class SemanticRole {
    MERCHANT_NAME, ISSUER_NAME, ISSUER_ADDRESS, CUSTOMER_ADDRESS, REMITTANCE_ADDRESS,
    PURCHASE_DATE, INVOICE_DATE, DUE_DATE, STATEMENT_PERIOD_START, STATEMENT_PERIOD_END, PRINT_DATE,
    SUBTOTAL_AMOUNT, TAX_AMOUNT, TIP_AMOUNT, DISCOUNT_AMOUNT, SHIPPING_OR_FEE_AMOUNT,
    TOTAL_AMOUNT, AMOUNT_PAID, AMOUNT_DUE, MINIMUM_PAYMENT_DUE,
    STATEMENT_OPENING_BALANCE, STATEMENT_CLOSING_BALANCE,
    LINE_ITEM_DESCRIPTION, LINE_ITEM_QUANTITY, LINE_ITEM_UNIT_PRICE, LINE_ITEM_TOTAL,
    BANK_TXN_DEBIT, BANK_TXN_CREDIT, BANK_RUNNING_BALANCE, ACCOUNT_NUMBER_MASKED, UNKNOWN
}

@Serializable
data class OcrElement(
    val id: String, // e.g., "P01_T0014"
    val pageNumber: Int,
    val rawText: String,
    val normalizedText: String,
    val lineId: String,
    val boxOcrNormalized: NormalizedRect,
    val quadRawDisplayNormalized: NormalizedQuad,
    val rectRawDisplayEnclosing: NormalizedRect,
    val confidence: Float,
    val rotationDegrees: Float,
    val region: String, // "header", "body", "footer"
    val columnRole: String // "left", "center", "right", "full"
)

@Serializable
data class FieldCandidate(
    val candidateId: String,
    val role: SemanticRole,
    val normalizedValue: String,
    val evidenceIds: List<String>,
    val confidence: Float
)

@Serializable
data class AmountCandidate(
    val candidateId: String,
    val role: SemanticRole,
    val amount: CurrencyAmount,
    val evidenceIds: List<String>,
    val confidence: Float
)

@Serializable
data class DateCandidate(
    val candidateId: String,
    val role: SemanticRole,
    val isoDate: String, // YYYY-MM-DD
    val evidenceIds: List<String>,
    val confidence: Float
)

@Serializable
data class ReconstructedTableCell(
    val columnIndex: Int,
    val headerLabel: String?,
    val role: SemanticRole,
    val text: String,
    val parsedAmount: CurrencyAmount? = null,
    val parsedIsoDate: String? = null,
    val evidenceIds: List<String>
)

@Serializable
data class ReconstructedTableRow(
    val rowId: String, // e.g., "P01_R0004"
    val pageNumber: Int,
    val cells: List<ReconstructedTableCell>,
    val rowReconciled: Boolean
)

@Serializable
data class ArithmeticValidationSummary(
    val formulaApplied: String,
    val isReconciled: Boolean,
    val discrepancyMinorUnits: Long,
    val currencyCode: String?,
    val notes: String? = null
)

@Serializable
data class StatementChunkContext(
    val chunkIndex: Int,
    val totalChunks: Int,
    val startRowId: String?,
    val endRowId: String?,
    val maskedAccountContext: String?,
    val statementPeriodStartIso: String?,
    val statementPeriodEndIso: String?
)

/**
 * Redacted, token-bounded text-only payload sent to an LLM when semantic ambiguity remains.
 * Customer addresses, personal phone numbers, and unmasked account numbers are stripped before creation.
 */
@Serializable
data class LlmAnalysisBundle(
    val documentId: String,
    val detectedFamilyCandidates: List<DocumentFamily>,
    val lockedDeterministicFields: Map<String, String> = emptyMap(),
    val textCandidates: List<FieldCandidate>,
    val dateCandidates: List<DateCandidate>,
    val amountCandidates: List<AmountCandidate>,
    val tableRows: List<ReconstructedTableRow>,
    val arithmeticValidation: ArithmeticValidationSummary,
    val allowedCategories: List<String>,
    val chunkContext: StatementChunkContext? = null
)

@Serializable
data class ResolvedStringField(
    val value: String,
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
data class ResolvedFinancialEvent(
    val eventId: String,
    val eventKind: FinancialEventKind,
    val description: String,
    val isoDate: String?, // Nullable if unresolved; persists as NEEDS_USER_REVIEW_DRAFT
    val dueDateIso: String? = null,
    val billPaymentStatus: BillPaymentStatus? = null,
    val amount: CurrencyAmount,
    val categoryId: String,
    val sourceRowId: String? = null,
    val evidenceIds: List<String>
)

@Serializable
data class StatementSummaryInterpretation(
    val openingBalance: ResolvedAmountField? = null,
    val closingBalance: ResolvedAmountField? = null,
    val minimumPaymentDue: ResolvedAmountField? = null,
    val periodStartDate: ResolvedStringField? = null,
    val periodEndDate: ResolvedStringField? = null
)

@Serializable
data class DocumentInterpretation(
    val documentFamily: DocumentFamily,
    val merchantOrIssuer: ResolvedStringField? = null,
    val issuerAddress: ResolvedStringField? = null,
    val primaryDate: ResolvedStringField? = null,
    val dueDate: ResolvedStringField? = null,
    val documentTotal: ResolvedAmountField? = null,
    val taxTotal: ResolvedAmountField? = null,
    val statementSummary: StatementSummaryInterpretation? = null,
    val financialEvents: List<ResolvedFinancialEvent> = emptyList(),
    val reviewRequired: Boolean,
    val unresolvedFields: List<String> = emptyList()
)
```

---

## 5. Document-Family-Aware Reconciliation, Hybrid Deterministic Gate & Post-LLM Validator

### 5.1 Document-Family-Aware Arithmetic Reconciliation Formulas

The local validator in `:core:finance:validation` chooses its reconciliation equation based on the detected `DocumentFamily` and the components present on the document:

1. **Receipts & Invoices (`RECEIPT`, `INVOICE_OR_UTILITY_BILL`):**
   * **Tax-Exclusive Composition (when separate `TAX_AMOUNT` is present and not labeled "Tax Included"):**
     $$\sum_{i=1}^{n} \text{LineItem}_i + \sum \text{Tax} + \text{Tip} + \text{ShippingOrFee} - \sum \text{DocumentLevelDiscount} = \text{Total}$$
   * **Tax-Inclusive Composition (VAT/GST receipts where line items already include tax):**
     $$\sum_{i=1}^{n} \text{LineItem}_i + \text{Tip} + \text{ShippingOrFee} - \sum \text{DocumentLevelDiscount} = \text{Total}$$
   * **Subtotal-to-Total Check (when no line items are parsed or line-item discounts are embedded):**
     $$\text{Subtotal} + \sum \text{Tax} + \text{Tip} + \text{ShippingOrFee} - \sum \text{PostSubtotalDiscount} = \text{Total}$$

2. **Bank Asset Statements (`BANK_STATEMENT`):**
   Deposits/credits increase the asset balance; withdrawals/debits and fees decrease it:
   $$\text{OpeningBalance} + \sum \text{Credits/Deposits} - \sum \text{Debits/Withdrawals} - \sum \text{Fees} = \text{ClosingBalance}$$

3. **Credit Card Liability Statements (`CREDIT_CARD_STATEMENT`):**
   Purchases, fees, and interest increase the liability balance; payments and refunds/credits decrease it:
   $$\text{OpeningBalance} - \sum \text{Payments} - \sum \text{RefundCredits} + \sum \text{Purchases} + \sum \text{FeesAndInterest} = \text{ClosingBalance}$$

### 5.2 Hybrid Field-Level & Document-Level Deterministic Gate

Rather than an all-or-nothing gate, `DeterministicGateEvaluator` evaluates each core field independently and produces a `DeterministicEvaluation`:
* **Full Document Bypass (`CompleteBypass`):** Returned when all 5 criteria pass:
  1. `DocumentFamily` classified with confidence $\ge 0.90$.
  2. `merchantOrIssuer` has confidence $\ge 0.92$ or matches a saved `MerchantRuleEntity`.
  3. Primary date is unambiguous with confidence $\ge 0.95$.
  4. Document-family arithmetic reconciliation passes with $\text{discrepancyMinorUnits} == 0$.
  5. Every derived `ResolvedFinancialEvent` maps to a known category via `MerchantRuleEntity` or `CategoryRuleEntity`.
* **Hybrid Field-Level Lock (`PartialLockRequiresLlm`):** If arithmetic reconciliation passes ($\text{discrepancyMinorUnits} == 0$) and the primary date is unambiguous ($\ge 0.95$), the gate locks `documentTotal`, `taxTotal`, and `primaryDate` in `lockedDeterministicFields`. The LLM is then invoked strictly to classify the merchant/category or disambiguate `FinancialEventKind`, and `PostLlmSemanticValidator` enforces that the LLM cannot overwrite any locked field.

### 5.3 Candidate-Bound Post-LLM Semantic Validator (`PostLlmSemanticValidator`)

Every field and every `ResolvedFinancialEvent` returned by an LLM must satisfy an unbroken candidate-bound provenance chain. An event cannot cite a valid `evidenceId` from Row A while taking an amount from Row B.

```kotlin
package com.finance.engine.core.finance.validation

import com.finance.engine.core.model.*
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostLlmSemanticValidator @Inject constructor(
    private val json: Json
) {
    private val fencedJsonRegex = Regex("""^```(?:json)?\s*\r?\n([\s\S]*?)\r?\n```$""")

    /**
     * Strict JSON extraction: tries direct parse first, then strips a single outer Markdown fence.
     * Never guesses substrings using first '{' and last '}'.
     */
    fun parseUntrustedJson(rawResponse: String): Result<DocumentInterpretation> = runCatching {
        val trimmed = rawResponse.trim()
        try {
            json.decodeFromString<DocumentInterpretation>(trimmed)
        } catch (directError: Exception) {
            val match = fencedJsonRegex.matchEntire(trimmed)
                ?: throw IllegalArgumentException("Response is neither pure JSON nor single fenced JSON block", directError)
            json.decodeFromString<DocumentInterpretation>(match.groupValues[1].trim())
        }
    }

    fun validateAndSanitize(
        raw: DocumentInterpretation,
        bundle: LlmAnalysisBundle,
        validDocumentEvidenceIds: Set<String>,
        lockedInterpretation: DocumentInterpretation? = null
    ): DocumentInterpretation {
        val unresolved = raw.unresolvedFields.toMutableSet()

        fun hasValidEvidenceIds(ids: List<String>): Boolean =
            ids.isNotEmpty() && ids.all { it in validDocumentEvidenceIds }

        fun validateTextField(
            field: ResolvedStringField?,
            fieldName: String,
            allowedRoles: Set<SemanticRole>
        ): ResolvedStringField? {
            if (field == null) return null
            val matchingCandidate = bundle.textCandidates.find { cand ->
                cand.role in allowedRoles &&
                    cand.role == field.role &&
                    cand.normalizedValue.equals(field.value.trim(), ignoreCase = true) &&
                    field.evidenceIds.isNotEmpty() &&
                    field.evidenceIds.all { it in cand.evidenceIds && it in validDocumentEvidenceIds }
            }
            return if (matchingCandidate != null) {
                field.copy(value = matchingCandidate.normalizedValue)
            } else {
                unresolved.add(fieldName)
                null
            }
        }

        fun validateDateField(
            field: ResolvedStringField?,
            fieldName: String,
            allowedRoles: Set<SemanticRole>
        ): ResolvedStringField? {
            if (field == null) return null
            val matchingCandidate = bundle.dateCandidates.find { cand ->
                cand.role in allowedRoles &&
                    cand.role == field.role &&
                    cand.isoDate == field.value.trim() &&
                    field.evidenceIds.isNotEmpty() &&
                    field.evidenceIds.all { it in cand.evidenceIds && it in validDocumentEvidenceIds }
            }
            return if (matchingCandidate != null) {
                field
            } else {
                unresolved.add(fieldName)
                null
            }
        }

        fun validateAmountField(
            field: ResolvedAmountField?,
            fieldName: String,
            allowedRoles: Set<SemanticRole>
        ): ResolvedAmountField? {
            if (field == null) return null
            val matchingCandidate = bundle.amountCandidates.find { cand ->
                cand.role in allowedRoles &&
                    cand.role == field.role &&
                    cand.amount == field.amount &&
                    field.evidenceIds.isNotEmpty() &&
                    field.evidenceIds.all { it in cand.evidenceIds && it in validDocumentEvidenceIds }
            }
            return if (matchingCandidate != null) {
                field
            } else {
                unresolved.add(fieldName)
                null
            }
        }

        // Preserve deterministically locked fields if present; otherwise validate LLM output
        val validMerchant = lockedInterpretation?.merchantOrIssuer ?: validateTextField(
            raw.merchantOrIssuer,
            "merchantOrIssuer",
            setOf(SemanticRole.MERCHANT_NAME, SemanticRole.ISSUER_NAME)
        )

        val validAddress = lockedInterpretation?.issuerAddress ?: validateTextField(
            raw.issuerAddress,
            "issuerAddress",
            setOf(SemanticRole.ISSUER_ADDRESS, SemanticRole.REMITTANCE_ADDRESS)
        )

        // Primary date strictly forbids DUE_DATE, STATEMENT_PERIOD_START/END, and PRINT_DATE
        val validPrimaryDate = lockedInterpretation?.primaryDate ?: validateDateField(
            raw.primaryDate,
            "primaryDate",
            setOf(SemanticRole.PURCHASE_DATE, SemanticRole.INVOICE_DATE)
        )

        val validDueDate = lockedInterpretation?.dueDate ?: validateDateField(
            raw.dueDate,
            "dueDate",
            setOf(SemanticRole.DUE_DATE)
        )

        // Document total strictly forbids SUBTOTAL_AMOUNT
        val validTotal = lockedInterpretation?.documentTotal ?: validateAmountField(
            raw.documentTotal,
            "documentTotal",
            setOf(SemanticRole.TOTAL_AMOUNT, SemanticRole.AMOUNT_DUE, SemanticRole.AMOUNT_PAID)
        )

        val validTax = lockedInterpretation?.taxTotal ?: validateAmountField(
            raw.taxTotal,
            "taxTotal",
            setOf(SemanticRole.TAX_AMOUNT)
        )

        // Validate each financial event through an unbroken candidate or row/cell chain
        val validEvents = raw.financialEvents.mapNotNull { event ->
            if (event.eventKind == FinancialEventKind.STATEMENT_BALANCE_IGNORE) {
                return@mapNotNull null // Never create a transaction from a statement balance
            }
            if (!hasValidEvidenceIds(event.evidenceIds)) {
                unresolved.add("event.${event.eventId}.evidence")
                return@mapNotNull null
            }

            // Document-family invariant check: an unpaid invoice cannot produce a PURCHASE_EXPENSE directly
            if (raw.documentFamily == DocumentFamily.INVOICE_OR_UTILITY_BILL &&
                event.eventKind == FinancialEventKind.PURCHASE_EXPENSE &&
                event.billPaymentStatus == BillPaymentStatus.UNPAID_DUE
            ) {
                unresolved.add("event.${event.eventId}.unpaid_invoice_as_expense")
                return@mapNotNull null
            }

            val allowedAmountRolesForEvent = when (event.eventKind) {
                FinancialEventKind.PURCHASE_EXPENSE -> setOf(
                    SemanticRole.TOTAL_AMOUNT, SemanticRole.AMOUNT_PAID,
                    SemanticRole.LINE_ITEM_TOTAL, SemanticRole.BANK_TXN_DEBIT
                )
                FinancialEventKind.UNPAID_BILL_LIABILITY -> setOf(
                    SemanticRole.AMOUNT_DUE, SemanticRole.TOTAL_AMOUNT, SemanticRole.MINIMUM_PAYMENT_DUE
                )
                FinancialEventKind.BILL_PAYMENT_SETTLEMENT,
                FinancialEventKind.INTERNAL_OR_CC_TRANSFER -> setOf(
                    SemanticRole.AMOUNT_PAID, SemanticRole.BANK_TXN_DEBIT, SemanticRole.BANK_TXN_CREDIT
                )
                FinancialEventKind.REFUND_OR_CREDIT,
                FinancialEventKind.INCOME_OR_DEPOSIT -> setOf(
                    SemanticRole.BANK_TXN_CREDIT, SemanticRole.TOTAL_AMOUNT
                )
                FinancialEventKind.FEE_OR_INTEREST_CHARGE -> setOf(
                    SemanticRole.SHIPPING_OR_FEE_AMOUNT, SemanticRole.BANK_TXN_DEBIT
                )
                FinancialEventKind.STATEMENT_BALANCE_IGNORE -> emptySet()
            }

            // Verify that the exact amount and evidence IDs come from the same candidate OR the same table row cell
            val matchedByCandidate = bundle.amountCandidates.any { cand ->
                cand.amount == event.amount &&
                    cand.role in allowedAmountRolesForEvent &&
                    event.evidenceIds.any { it in cand.evidenceIds }
            }
            val matchedByRowCell = bundle.tableRows.any { row ->
                (event.sourceRowId == null || event.sourceRowId == row.rowId) &&
                    row.cells.any { cell ->
                        cell.parsedAmount == event.amount &&
                            cell.role in allowedAmountRolesForEvent &&
                            event.evidenceIds.any { it in cell.evidenceIds }
                    }
            }

            if (!matchedByCandidate && !matchedByRowCell) {
                unresolved.add("event.${event.eventId}.amount_provenance_mismatch")
                return@mapNotNull null
            }

            // Validate event date if non-null against date candidates or table row date cell
            val validatedIsoDate = event.isoDate?.let { dateStr ->
                val dateValid = bundle.dateCandidates.any {
                    it.isoDate == dateStr && it.role != SemanticRole.PRINT_DATE
                } || bundle.tableRows.any { row ->
                    row.cells.any { it.parsedIsoDate == dateStr }
                }
                if (dateValid) dateStr else {
                    unresolved.add("event.${event.eventId}.date")
                    null
                }
            } ?: validPrimaryDate?.value

            if (validatedIsoDate == null) {
                unresolved.add("event.${event.eventId}.missing_date")
            }

            val safeCategory = if (event.categoryId in bundle.allowedCategories) {
                event.categoryId
            } else {
                unresolved.add("event.${event.eventId}.category")
                "Uncategorized"
            }

            event.copy(
                isoDate = validatedIsoDate,
                categoryId = safeCategory
            )
        }

        val requiresReview = raw.reviewRequired ||
            unresolved.isNotEmpty() ||
            !bundle.arithmeticValidation.isReconciled ||
            validEvents.isEmpty()

        return raw.copy(
            merchantOrIssuer = validMerchant,
            issuerAddress = validAddress,
            primaryDate = validPrimaryDate,
            dueDate = validDueDate,
            documentTotal = validTotal,
            taxTotal = validTax,
            financialEvents = validEvents,
            reviewRequired = requiresReview,
            unresolvedFields = unresolved.toList()
        )
    }
}
```

---

## 6. AI Provider Capabilities, Per-Provider Consent Router & `BundleTrimmer`

### 6.1 Provider Enumeration, Readiness & Audited October 2026 Model Registry

```kotlin
package com.finance.engine.core.ai

import kotlinx.serialization.Serializable

@Serializable
enum class AiEngineProvider(
    val displayName: String,
    val badgeLabel: String,
    val isCloudProvider: Boolean
) {
    GEMMA_4_CLOUD_FREE(
        displayName = "Gemma 4 Cloud (Free Tier) - Default",
        badgeLabel = "Gemma",
        isCloudProvider = true
    ),
    FIREBASE_AI_LOGIC(
        displayName = "Firebase AI Logic (Cloud)",
        badgeLabel = "Firebase",
        isCloudProvider = true
    ),
    BYOK_GEMINI(
        displayName = "Bring Your Own Key (BYOK)",
        badgeLabel = "BYOK",
        isCloudProvider = true
    ),
    ON_DEVICE_GEMINI_NANO(
        displayName = "On-Device Local AI (Gemini Nano)",
        badgeLabel = "Local AI",
        isCloudProvider = false
    )
}

@Serializable
enum class ProviderReadiness {
    UNCONFIGURED,
    READY,
    DEGRADED,
    QUOTA_LIMITED,
    OFFLINE,
    UNSUPPORTED
}

@Serializable
data class ModelDescriptor(
    val modelId: String,
    val displayName: String,
    val provider: AiEngineProvider,
    val supportsNativeJsonSchema: Boolean,
    val maxInputBytesUtf8: Int = 12_288,
    val maxInputTokens: Int = 8_192,
    val maxOutputTokens: Int = 2_048
)

object ModelCapabilityRegistry {
    const val CURRENT_DISCLOSURE_VERSION = 2

    val defaultModelsByProvider: Map<AiEngineProvider, List<ModelDescriptor>> = mapOf(
        AiEngineProvider.GEMMA_4_CLOUD_FREE to listOf(
            ModelDescriptor(
                modelId = "gemma-4-31b-it",
                displayName = "Gemma 4 31B Instruct (Hosted Default)",
                provider = AiEngineProvider.GEMMA_4_CLOUD_FREE,
                supportsNativeJsonSchema = false
            ),
            ModelDescriptor(
                modelId = "gemma-4-26b-a4b-it",
                displayName = "Gemma 4 26B A4B MoE Instruct",
                provider = AiEngineProvider.GEMMA_4_CLOUD_FREE,
                supportsNativeJsonSchema = false
            )
        ),
        AiEngineProvider.FIREBASE_AI_LOGIC to listOf(
            ModelDescriptor(
                modelId = "gemini-3.8-flash",
                displayName = "Gemini 3.8 Flash (Firebase App Check)",
                provider = AiEngineProvider.FIREBASE_AI_LOGIC,
                supportsNativeJsonSchema = true
            ),
            ModelDescriptor(
                modelId = "gemini-3.5-flash-lite",
                displayName = "Gemini 3.5 Flash-Lite (High Efficiency)",
                provider = AiEngineProvider.FIREBASE_AI_LOGIC,
                supportsNativeJsonSchema = true
            )
        ),
        AiEngineProvider.ON_DEVICE_GEMINI_NANO to listOf(
            ModelDescriptor(
                modelId = "gemini-nano-aicore",
                displayName = "Gemini Nano (On-Device AICore)",
                provider = AiEngineProvider.ON_DEVICE_GEMINI_NANO,
                supportsNativeJsonSchema = false,
                maxInputBytesUtf8 = 8_192,
                maxInputTokens = 3_072,
                maxOutputTokens = 1_024
            )
        )
    )
}
```

### 6.2 Per-Provider Consent & Explicit Fallback Policy (`AiRoutingPolicy`)

To prevent silent cross-provider cloud switching, `AiRoutingPolicy` separates **cloud data-transfer consent per provider** (`consentedCloudProviders`) from **fallback authorization** (`authorizedFallbackProviders`).
* **Strict Rule:** A readiness check, connection test, or quota failure must **never** grant consent. Before calling any cloud provider (whether as primary or fallback), the router verifies that `provider in policy.consentedCloudProviders` and `policy.acceptedDisclosureVersion == CURRENT_DISCLOSURE_VERSION`.

```kotlin
package com.finance.engine.core.ai

import kotlinx.serialization.Serializable

@Serializable
data class AiRoutingPolicy(
    val primaryProvider: AiEngineProvider = AiEngineProvider.GEMMA_4_CLOUD_FREE,
    val preferredModelIdByProvider: Map<AiEngineProvider, String> = mapOf(
        AiEngineProvider.GEMMA_4_CLOUD_FREE to "gemma-4-31b-it",
        AiEngineProvider.FIREBASE_AI_LOGIC to "gemini-3.8-flash",
        AiEngineProvider.ON_DEVICE_GEMINI_NANO to "gemini-nano-aicore"
    ),
    val strictLocalOnlyMode: Boolean = false,
    val consentedCloudProviders: Set<AiEngineProvider> = emptySet(),
    val authorizedFallbackProviders: List<AiEngineProvider> = listOf(
        AiEngineProvider.ON_DEVICE_GEMINI_NANO
    ),
    val acceptedDisclosureVersion: Int = 0,
    val consentAcceptedEpochMs: Long? = null
) {
    fun isProviderAuthorizedForTransfer(provider: AiEngineProvider): Boolean {
        if (!provider.isCloudProvider) return true
        if (strictLocalOnlyMode) return false
        return provider in consentedCloudProviders &&
            acceptedDisclosureVersion >= ModelCapabilityRegistry.CURRENT_DISCLOSURE_VERSION &&
            consentAcceptedEpochMs != null
    }
}
```

### 6.3 Role-Stratified, PII-Redacting `BundleTrimmer` with Measured Byte/Token Enforcement

`BundleTrimmer` guarantees that no mandatory financial candidate is dropped by a global top-$N$ cutoff, strips unnecessary personal identifiers before cloud serialization, and enforces the 12,288-byte UTF-8 ceiling dynamically.

```kotlin
package com.finance.engine.core.ai

import com.finance.engine.core.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundleTrimmer @Inject constructor(
    private val json: Json
) {
    companion object {
        const val DEFAULT_MAX_UTF8_BYTES = 12_288
        const val INITIAL_ROW_CHUNK_SIZE = 15
    }

    private val redactedRoles = setOf(SemanticRole.CUSTOMER_ADDRESS)
    private val emailRegex = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val rawAccountRegex = Regex("""\b\d{8,19}\b""")

    fun buildRedactedAndBoundedChunks(
        rawBundle: LlmAnalysisBundle,
        maxUtf8Bytes: Int = DEFAULT_MAX_UTF8_BYTES
    ): List<LlmAnalysisBundle> {
        // 1. PII Redaction & Role-Stratified Candidate Selection
        // Retain top candidates *within each SemanticRole* so mandatory totals/dates/balances are never dropped
        val stratifiedAmounts = rawBundle.amountCandidates
            .groupBy { it.role }
            .values
            .flatMap { list -> list.sortedByDescending { it.confidence }.take(3) }

        val stratifiedDates = rawBundle.dateCandidates
            .groupBy { it.role }
            .values
            .flatMap { list -> list.sortedByDescending { it.confidence }.take(2) }

        val stratifiedTexts = rawBundle.textCandidates
            .filter { it.role !in redactedRoles }
            .groupBy { it.role }
            .values
            .flatMap { list ->
                list.sortedByDescending { it.confidence }.take(2).map { cand ->
                    cand.copy(normalizedValue = redactPiiText(cand.normalizedValue))
                }
            }

        val redactedRows = rawBundle.tableRows.map { row ->
            row.copy(
                cells = row.cells.map { cell ->
                    cell.copy(text = redactPiiText(cell.text))
                }
            )
        }

        val baseBundle = rawBundle.copy(
            textCandidates = stratifiedTexts,
            dateCandidates = stratifiedDates,
            amountCandidates = stratifiedAmounts,
            tableRows = emptyList()
        )

        if (redactedRows.isEmpty()) {
            return listOf(enforceByteBudget(baseBundle, maxUtf8Bytes))
        }

        // 2. Adaptive Row Chunking with Measured UTF-8 Byte Verification
        val resultChunks = mutableListOf<LlmAnalysisBundle>()
        var cursor = 0
        val maskedAccount = stratifiedTexts.find { it.role == SemanticRole.ACCOUNT_NUMBER_MASKED }?.normalizedValue
        val periodStart = stratifiedDates.find { it.role == SemanticRole.STATEMENT_PERIOD_START }?.isoDate
        val periodEnd = stratifiedDates.find { it.role == SemanticRole.STATEMENT_PERIOD_END }?.isoDate

        while (cursor < redactedRows.size) {
            var step = minOf(INITIAL_ROW_CHUNK_SIZE, redactedRows.size - cursor)
            var candidateSlice = redactedRows.subList(cursor, cursor + step)
            var candidateChunk = baseBundle.copy(tableRows = candidateSlice)

            while (measureUtf8Bytes(candidateChunk) > maxUtf8Bytes && step > 1) {
                step /= 2
                candidateSlice = redactedRows.subList(cursor, cursor + step)
                candidateChunk = baseBundle.copy(tableRows = candidateSlice)
            }

            resultChunks.add(
                enforceByteBudget(candidateChunk, maxUtf8Bytes).copy(
                    chunkContext = StatementChunkContext(
                        chunkIndex = resultChunks.size,
                        totalChunks = -1, // Backfilled below
                        startRowId = candidateSlice.first().rowId,
                        endRowId = candidateSlice.last().rowId,
                        maskedAccountContext = maskedAccount,
                        statementPeriodStartIso = periodStart,
                        statementPeriodEndIso = periodEnd
                    )
                )
            )
            cursor += step
        }

        val totalChunks = resultChunks.size
        return resultChunks.map { chunk ->
            chunk.copy(chunkContext = chunk.chunkContext?.copy(totalChunks = totalChunks))
        }
    }

    private fun enforceByteBudget(bundle: LlmAnalysisBundle, maxUtf8Bytes: Int): LlmAnalysisBundle {
        if (measureUtf8Bytes(bundle) <= maxUtf8Bytes) return bundle
        // Drop non-mandatory issuer address candidates first if still over budget
        val trimmedTexts = bundle.textCandidates.filter {
            it.role == SemanticRole.MERCHANT_NAME || it.role == SemanticRole.ISSUER_NAME
        }
        return bundle.copy(textCandidates = trimmedTexts)
    }

    private fun redactPiiText(input: String): String =
        input.replace(emailRegex, "[REDACTED_EMAIL]")
            .replace(rawAccountRegex) { match ->
                val v = match.value
                "****" + v.takeLast(4)
            }

    fun measureUtf8Bytes(bundle: LlmAnalysisBundle): Int =
        json.encodeToString(bundle).encodeToByteArray().size
}
```

### 6.4 Policy-Driven AI Router (`PolicyDrivenAiRouter`)

```kotlin
package com.finance.engine.core.ai

import com.finance.engine.core.finance.validation.PostLlmSemanticValidator
import com.finance.engine.core.model.DocumentInterpretation
import com.finance.engine.core.model.LlmAnalysisBundle
import javax.inject.Inject
import javax.inject.Singleton

sealed interface RoutingOutcome {
    data class ResolvedByDeterministicGate(val interpretation: DocumentInterpretation) : RoutingOutcome
    data class ResolvedByModel(
        val interpretation: DocumentInterpretation,
        val providerUsed: AiEngineProvider,
        val modelIdUsed: String,
        val fallbackReason: String?
    ) : RoutingOutcome
    data class ConsentRequired(val provider: AiEngineProvider) : RoutingOutcome
    data class FallbackToDeterministicReview(
        val partialInterpretation: DocumentInterpretation,
        val failureReason: String
    ) : RoutingOutcome
}

@Singleton
class PolicyDrivenAiRouter @Inject constructor(
    private val adapters: Map<AiEngineProvider, @JvmSuppressWildcards DocumentInterpreterAdapter>,
    private val bundleTrimmer: BundleTrimmer,
    private val validator: PostLlmSemanticValidator
) {
    suspend fun routeAndInterpret(
        rawBundle: LlmAnalysisBundle,
        validEvidenceIds: Set<String>,
        deterministicCandidate: DocumentInterpretation,
        deterministicGatePassedCompletely: Boolean,
        policy: AiRoutingPolicy
    ): RoutingOutcome {
        // Step 1: Full Deterministic Gate Bypass
        if (deterministicGatePassedCompletely) {
            return RoutingOutcome.ResolvedByDeterministicGate(deterministicCandidate)
        }

        // Step 2: Strict Local-Only Mode Check
        if (policy.strictLocalOnlyMode) {
            val localAdapter = adapters[AiEngineProvider.ON_DEVICE_GEMINI_NANO]
            if (localAdapter != null && localAdapter.checkReadiness() == ProviderReadiness.READY) {
                val res = executeChunkedPipeline(
                    localAdapter, "gemini-nano-aicore", rawBundle, validEvidenceIds, deterministicCandidate
                )
                if (res.isSuccess) {
                    return RoutingOutcome.ResolvedByModel(
                        res.getOrThrow(), AiEngineProvider.ON_DEVICE_GEMINI_NANO, "gemini-nano-aicore", null
                    )
                }
            }
            return RoutingOutcome.FallbackToDeterministicReview(
                deterministicCandidate.copy(reviewRequired = true),
                "STRICT_LOCAL_ONLY_MODE_ACTIVE"
            )
        }

        // Step 3: Verify Primary Provider Consent before making any cloud call
        val primary = policy.primaryProvider
        if (primary.isCloudProvider && !policy.isProviderAuthorizedForTransfer(primary)) {
            return RoutingOutcome.ConsentRequired(primary)
        }

        // Step 4: Build ordered execution chain of consented & authorized providers
        val candidateChain = buildList {
            add(primary)
            policy.authorizedFallbackProviders.forEach { fallback ->
                if (fallback != primary && policy.isProviderAuthorizedForTransfer(fallback)) {
                    add(fallback)
                }
            }
        }

        var firstFailureReason: String? = null
        for (provider in candidateChain) {
            val adapter = adapters[provider] ?: continue
            val readiness = adapter.checkReadiness()
            if (readiness != ProviderReadiness.READY) {
                if (firstFailureReason == null) firstFailureReason = "${provider.name}_$readiness"
                continue
            }
            val modelId = policy.preferredModelIdByProvider[provider]
                ?: ModelCapabilityRegistry.defaultModelsByProvider[provider]?.firstOrNull()?.modelId
                ?: continue

            val result = executeChunkedPipeline(
                adapter, modelId, rawBundle, validEvidenceIds, deterministicCandidate
            )
            if (result.isSuccess) {
                return RoutingOutcome.ResolvedByModel(
                    interpretation = result.getOrThrow(),
                    providerUsed = provider,
                    modelIdUsed = modelId,
                    fallbackReason = if (provider == primary) null else firstFailureReason
                )
            } else if (firstFailureReason == null) {
                firstFailureReason = "${provider.name}_${result.exceptionOrNull()?.javaClass?.simpleName}"
            }
        }

        // Step 5: Graceful Deterministic Partial Result & Review
        return RoutingOutcome.FallbackToDeterministicReview(
            partialInterpretation = deterministicCandidate.copy(reviewRequired = true),
            failureReason = firstFailureReason ?: "NO_AUTHORIZED_PROVIDER_AVAILABLE"
        )
    }

    private suspend fun executeChunkedPipeline(
        adapter: DocumentInterpreterAdapter,
        modelId: String,
        rawBundle: LlmAnalysisBundle,
        validEvidenceIds: Set<String>,
        lockedInterpretation: DocumentInterpretation
    ): Result<DocumentInterpretation> = runCatching {
        val maxBytes = ModelCapabilityRegistry.defaultModelsByProvider[adapter.provider]
            ?.find { it.modelId == modelId }?.maxInputBytesUtf8 ?: BundleTrimmer.DEFAULT_MAX_UTF8_BYTES
        val chunks = bundleTrimmer.buildRedactedAndBoundedChunks(rawBundle, maxBytes)

        val validatedChunks = chunks.map { chunk ->
            val rawInterpretation = adapter.interpret(chunk, modelId).getOrThrow()
            validator.validateAndSanitize(rawInterpretation, chunk, validEvidenceIds, lockedInterpretation)
        }

        // Aggregate chunks and verify row order and uniqueness
        val first = validatedChunks.first()
        val seenRowIds = mutableSetOf<String>()
        val mergedEvents = validatedChunks.flatMap { it.financialEvents }.filter { event ->
            val rowId = event.sourceRowId
            if (rowId == null) true else seenRowIds.add(rowId)
        }
        val mergedUnresolved = validatedChunks.flatMap { it.unresolvedFields }.distinct()

        first.copy(
            financialEvents = mergedEvents,
            reviewRequired = validatedChunks.any { it.reviewRequired },
            unresolvedFields = mergedUnresolved
        )
    }
}
```

### 6.5 AI Configuration UI & Per-Provider Cloud Consent Dialog (`AiConfigurationCard.kt`)

Matches the visual layout of `image_7e5505.jpg` while enforcing per-provider consent transitions and safe BYOK clearing.

```kotlin
package com.finance.engine.core.ai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finance.engine.core.ai.AiEngineProvider
import com.finance.engine.core.ai.ModelDescriptor
import com.finance.engine.core.ai.ProviderReadiness

data class AiConfigUiState(
    val isCardExpanded: Boolean = true,
    val selectedEngine: AiEngineProvider = AiEngineProvider.GEMMA_4_CLOUD_FREE,
    val readinessByProvider: Map<AiEngineProvider, ProviderReadiness> = mapOf(
        AiEngineProvider.BYOK_GEMINI to ProviderReadiness.UNCONFIGURED,
        AiEngineProvider.ON_DEVICE_GEMINI_NANO to ProviderReadiness.UNCONFIGURED,
        AiEngineProvider.FIREBASE_AI_LOGIC to ProviderReadiness.UNCONFIGURED,
        AiEngineProvider.GEMMA_4_CLOUD_FREE to ProviderReadiness.UNCONFIGURED
    ),
    val consentedCloudProviders: Set<AiEngineProvider> = emptySet(),
    val pendingConsentProvider: AiEngineProvider? = null,
    val hasSavedByokKey: Boolean = false,
    val transientByokKeyInput: String = "",
    val isConnectionVerified: Boolean = false,
    val isTestingConnection: Boolean = false,
    val unlockedModels: List<ModelDescriptor> = emptyList(),
    val selectedModelId: String? = null,
    val strictLocalOnlyMode: Boolean = false,
    val showDiagnosticsSheet: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConfigurationCard(
    state: AiConfigUiState,
    onToggleExpand: () -> Unit,
    onSelectEngineRequest: (AiEngineProvider) -> Unit,
    onConfirmProviderConsent: (AiEngineProvider) -> Unit,
    onDismissProviderConsent: () -> Unit,
    onByokKeyInputChanged: (String) -> Unit,
    onSaveAndClearByokKey: () -> Unit,
    onTestConnection: () -> Unit,
    onSelectModel: (String) -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var engineDropdownExpanded by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    // Per-Provider Cloud Consent Modal Transition
    state.pendingConsentProvider?.let { provider ->
        AlertDialog(
            onDismissRequest = onDismissProviderConsent,
            title = { Text("Authorize Cloud Financial Metadata Transfer") },
            text = {
                Text(
                    "To interpret ambiguous fields using ${provider.displayName}, a compact text-only JSON bundle " +
                        "(merchant candidates, dates, amounts, and line descriptions) is transmitted. " +
                        "Camera images, customer addresses, and full account numbers are never sent. " +
                        "Each cloud provider is governed by its own service terms."
                )
            },
            confirmButton = {
                Button(onClick = { onConfirmProviderConsent(provider) }) {
                    Text("Authorize ${provider.badgeLabel}")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissProviderConsent) {
                    Text("Stay Local-Only")
                }
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE4FA))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Configuration",
                    tint = Color(0xFFD99B26),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Configuration",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ProviderStatusChainRow(state.readinessByProvider)
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (state.isCardExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand or Collapse"
                    )
                }
            }

            AnimatedVisibility(visible = state.isCardExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Primary AI Engine",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5E478C)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = engineDropdownExpanded,
                        onExpandedChange = { engineDropdownExpanded = !engineDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = state.selectedEngine.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = engineDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF5E478C),
                                unfocusedBorderColor = Color(0xFF5E478C)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = engineDropdownExpanded,
                            onDismissRequest = { engineDropdownExpanded = false }
                        ) {
                            AiEngineProvider.entries.forEach { provider ->
                                DropdownMenuItem(
                                    text = { Text(provider.displayName) },
                                    onClick = {
                                        engineDropdownExpanded = false
                                        onSelectEngineRequest(provider)
                                    }
                                )
                            }
                        }
                    }

                    if (state.selectedEngine == AiEngineProvider.BYOK_GEMINI) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.transientByokKeyInput,
                            onValueChange = onByokKeyInputChanged,
                            label = {
                                Text(if (state.hasSavedByokKey) "Replace Gemini API Key (Encrypted in Tink)" else "Gemini API Key")
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            trailingIcon = {
                                if (state.transientByokKeyInput.isNotBlank()) {
                                    TextButton(onClick = onSaveAndClearByokKey) {
                                        Text("Save")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Preferred AI Model",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5E478C)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = modelDropdownExpanded && state.isConnectionVerified,
                        onExpandedChange = {
                            if (state.isConnectionVerified) modelDropdownExpanded = !modelDropdownExpanded
                            else onTestConnection()
                        }
                    ) {
                        OutlinedTextField(
                            value = if (state.isConnectionVerified) {
                                state.unlockedModels.find { it.modelId == state.selectedModelId }?.displayName
                                    ?: "Select verified model"
                            } else {
                                "Test connection to see models"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gemini Model") },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = modelDropdownExpanded && state.isConnectionVerified,
                            onDismissRequest = { modelDropdownExpanded = false }
                        ) {
                            state.unlockedModels.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model.displayName) },
                                    onClick = {
                                        onSelectModel(model.modelId)
                                        modelDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (state.isConnectionVerified) {
                            "Connection and model capability verified."
                        } else {
                            "Test connection to unlock available models."
                        },
                        fontSize = 13.sp,
                        color = Color(0xFF5A5A5A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onOpenDiagnostics) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Open AI Pipeline Diagnostics",
                                tint = Color(0xFF6E6E6E)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderStatusChainRow(statusMap: Map<AiEngineProvider, ProviderReadiness>) {
    val chainOrder = listOf(
        AiEngineProvider.BYOK_GEMINI,
        AiEngineProvider.ON_DEVICE_GEMINI_NANO,
        AiEngineProvider.FIREBASE_AI_LOGIC,
        AiEngineProvider.GEMMA_4_CLOUD_FREE
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        chainOrder.forEachIndexed { index, provider ->
            val readiness = statusMap[provider] ?: ProviderReadiness.UNCONFIGURED
            val isReady = readiness == ProviderReadiness.READY
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(if (isReady) Color(0xFF2EA043) else Color(0xFFD9383A))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = provider.badgeLabel,
                fontSize = 12.sp,
                fontWeight = if (isReady) FontWeight.Bold else FontWeight.Normal,
                color = if (isReady) Color(0xFF1A1A1A) else Color(0xFF7A7A7A)
            )
            if (index < chainOrder.lastIndex) {
                Text(
                    text = " \u2192 ",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}
```

---

## 7. Encrypted Persistence, Statement Summaries, Post-Purge Provenance & Backup Exclusions

### 7.1 Aligned Room 2.8.5 + SQLCipher 4.19.1 Schema (`Entities.kt`)

```kotlin
package com.finance.engine.core.database

import androidx.room.*

enum class EventPersistenceState {
    VERIFIED_FINAL,
    NEEDS_USER_REVIEW_DRAFT
}

enum class EvidenceRetentionState {
    RAW_SOURCE_AVAILABLE,
    RAW_SOURCE_PURGED_MANIFEST_RETAINED
}

@Entity(
    tableName = "financial_documents",
    indices = [Index(value = ["sha256SourceHash"], unique = true)]
)
data class FinancialDocumentEntity(
    @PrimaryKey val documentId: String,
    val sha256SourceHash: String,
    val documentFamily: String,
    val issuerName: String?,
    val captureTimestampEpochMs: Long,
    val processingStatus: String, // "VERIFIED", "NEEDS_REVIEW", "FAILED"
    val evidenceRetentionState: String // EvidenceRetentionState.name
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
    val dominantHeaderLabCsv: String
)

/**
 * Lightweight, non-PII provenance manifest that SURVIVES the 30-day raw OCR/bitmap purge.
 * Ensures verified FinancialEventEntity records never point to broken evidence references.
 */
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
    val valueSha256Digest: String, // HmacSHA256 or salted SHA-256 of normalized value
    val quadRawDisplayNormalizedJson: String,
    val sourcePurged: Boolean,
    val verificationStatus: String // "DETERMINISTIC_LOCKED", "MODEL_VERIFIED", "USER_CORRECTED"
)

/**
 * Separate aggregate table for statement-level opening/closing balances and billing periods.
 * Never mixed into FinancialEventEntity expense rows.
 */
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
    val eventDateIso: String?, // Nullable ONLY when persistenceState == NEEDS_USER_REVIEW_DRAFT
    val dueDateIso: String?,
    val billPaymentStatus: String?, // BillPaymentStatus.name
    val amountMinor: Long,
    val currencyCode: String,
    val currencyScale: Int,
    val categoryId: String,
    val maskedAccountFingerprint: String?,
    val sourceRowId: String?,
    val dedupFingerprint: String,
    val evidenceIdsCsv: String,
    val userReviewed: Boolean
)

@Entity(tableName = "interpretation_runs")
data class InterpretationRunEntity(
    @PrimaryKey val runId: String,
    val documentId: String,
    val selectedProvider: String,
    val actualProviderUsed: String,
    val modelId: String,
    val fallbackReason: String?,
    val latencyMs: Long,
    val payloadSizeBytes: Int,
    val validationPassed: Boolean,
    val reconciliationStatus: String,
    val redactedDiagnosticJson: String? // Null unless time-bounded developer troubleshooting is enabled
)
```

### 7.2 Authenticated Image Encryption (`TinkStreamingFileEncryptor`) & Backup Exclusions
1. **Per-File Authenticated Encryption:** All cached `.enc` page bitmaps are encrypted using Google Tink's `StreamingAead` (`AES256_GCM_HKDF_4KB`) backed by a master key in `AndroidKeyStore`.
   * **Associated Data (AAD):** Bind each file's encryption stream to `associatedData = "$documentId:page_$pageNumber:$variant".encodeToByteArray()` so ciphertext files cannot be swapped across documents or pages.
   * **Atomic Writes:** Write to a temporary `.tmp` file, call `fsync`, and atomically rename to `.enc`.
2. **Retention & Cascade Policy (`DocumentRetentionWorker`):**
   * Runs daily via `WorkManager`. For documents older than the user's retention window (default 30 days) with `processingStatus == "VERIFIED"`, it deletes `raw_display_page_{n}.enc`, `clean_ocr_page_{n}.enc`, and raw `OcrElement` rows, updates `EvidenceManifestEntity.sourcePurged = true`, and sets `FinancialDocumentEntity.evidenceRetentionState = "RAW_SOURCE_PURGED_MANIFEST_RETAINED"`.
   * When a user manually deletes a document, the UI offers two explicit options: **"Delete Source Scan Only (Keep Verified Transactions & Manifest)"** or **"Delete Scan and All Derived Transactions"**.
3. **Android Backup Exclusions (`res/xml/data_extraction_rules.xml`):**
   * Explicitly exclude the SQLCipher `.db` file, the encrypted image directory, the Tink keyset preferences, and the AI routing consent DataStore from both cloud backup and device-to-device transfer so key-wrapped blobs are never restored onto a device lacking the hardware Keystore key.

---

## 8. Review & Correction UI (`DocumentReviewScreen`)

1. **Split-Pane Interactive Canvas Overlay:**
   * Top/left pane renders `raw_display_page_{n}.enc` (when `RAW_SOURCE_AVAILABLE`) with an interactive Jetpack Compose `Canvas` overlay.
   * When the user taps any resolved field or `FinancialEventEntity`, the UI looks up its `evidenceIds` in `EvidenceManifestEntity` and draws the 4-corner `NormalizedQuad` polygon using `Path` (`moveTo(topLeft)`, `lineTo(topRight)`, `lineTo(bottomRight)`, `lineTo(bottomLeft)`, `close()`).
   * If `sourcePurged == true`, the UI displays a badge: *"Verified via Evidence Manifest (Raw scan purged per 30-day privacy policy)"*.
2. **Unresolved Draft Resolution & Rule Learning:**
   * Events in `NEEDS_USER_REVIEW_DRAFT` state (such as an event missing `eventDateIso` or flagged for excess decimal precision) must have their highlighted fields confirmed before transitioning to `VERIFIED_FINAL`.
   * Tapping any OCR bounding box on the image assigns that element's value and `evidenceId` directly to the active field and offers to save a deterministic `MerchantRuleEntity`.

---

## 9. Phased Implementation Plan & Measurable Acceptance Criteria

The coding agent must execute and verify this system in **five sequential phases**, running `./gradlew test` after each phase before proceeding.

### Phase 1: Toolchain Lock, Strict Money, SQLCipher & Tink Storage
* **Deliverables:** `:core:database` and `:core:finance:extract` currency foundation. Validate the late-2026 Gradle catalog (`Kotlin 2.4.21`, `Room 2.8.5`, `SQLCipher 4.19.1`, `Hilt 2.60.1`, `Compose BOM 2026.09.00`), `Iso4217CurrencyRegistry`, `StrictMoneyParser`, `TinkStreamingFileEncryptor`, and Room entities (`FinancialDocumentEntity`, `StatementSummaryEntity`, `EvidenceManifestEntity`, `FinancialEventEntity`).
* **Exact Invariant Gate (100% Pass Required):**
  * `StrictMoneyParser` accurately converts `0`, `2`, and `3` decimal currencies (`JPY`, `USD`, `KWD`) and returns `ExcessPrecisionFlagged` (never rounding silently) when given `12.345 USD`.
  * `TinkStreamingFileEncryptor` rejects decryption if Associated Data (`documentId + pageNumber`) is modified.

### Phase 2: Vision, Normalized Homography Quadrilaterals & Spatial Layout
* **Deliverables:** `:core:ocr`, `:core:document:vision`, and `:core:document:layout`. Implement the PDF text-layer fast path, `CoordinateTransformMapper` (mapping all 4 corners via $H_{\text{norm}}$ into `NormalizedQuad`), Laplacian blur/glare detection, CIELAB extraction, and rotation-aware line/table reconstruction.
* **Exact Invariant Gate (100% Pass Required):**
  * Unit tests verify that `CoordinateTransformMapper.mapOcrRectToRawQuad()` under $90^\circ$ rotation, $12^\circ$ skew, and perspective projection maps all 4 corners within $\le 10^{-4}$ normalized error.

### Phase 3: Document-Family Reconciliation, Hybrid Gate & Candidate-Bound Validator
* **Deliverables:** `:core:finance:validation`. Implement receipt (tax-inclusive and tax-exclusive), bank asset statement, and credit-card liability statement reconciliation formulas, the hybrid field-level deterministic gate, and `PostLlmSemanticValidator`.
* **Exact Invariant Gate (100% Pass Required):**
  * `PostLlmSemanticValidator` rejects: (a) any hallucinated `evidenceId`, (b) any event whose `amount` matches Candidate A while its `evidenceIds` point to Candidate B, (c) `SUBTOTAL_AMOUNT` mapped to `documentTotal`, (d) `DUE_DATE` or `PRINT_DATE` mapped to `primaryDate`, and (e) `STATEMENT_BALANCE_IGNORE` events.

### Phase 4: AI Capability Registry, Per-Provider Consent Router, `BundleTrimmer` & UI
* **Deliverables:** `:core:ai`. Implement `ModelCapabilityRegistry` (`gemma-4-31b-it`, `gemma-4-26b-a4b-it`, `gemini-3.8-flash`, `gemini-3.5-flash-lite`), `BundleTrimmer`, `PolicyDrivenAiRouter`, provider adapters, and `AiConfigurationCard` with the per-provider consent modal.
* **Exact Invariant Gate (100% Pass Required):**
  * Router contract tests verify that: (a) a readiness check or quota error never grants consent, (b) `FIREBASE_AI_LOGIC` is skipped on fallback if the user only consented to `GEMMA_4_CLOUD_FREE`, (c) `BundleTrimmer` strips `CUSTOMER_ADDRESS` and keeps serialized chunks $\le 12,288\text{ bytes}$ without dropping mandatory role candidates, and (d) multi-chunk statement aggregation preserves exact row order without duplicates.

### Phase 5: End-to-End Review Screen, Retention Worker & Corpus Benchmark
* **Deliverables:** `:feature:documentcapture`, `:feature:review`, `DocumentRetentionWorker`, and the split evaluation benchmark suite.
* **Held-Out Corpus Evaluation Gate (Separate Calibration vs. Held-Out Test Split):**
  * Evaluate on a 150-document corpus split into **50 calibration documents** and **100 held-out test documents** with disjoint merchant templates across 5 document families (`25 retail/restaurant receipts`, `20 utility bills/invoices`, `25 bank statements`, `20 credit-card statements`, `10 refund notes/skewed low-light captures`):
    * **0% Silent Hallucination Rate:** Zero persisted `VERIFIED_FINAL` financial events with unverified amounts or mismatched candidate evidence IDs.
    * **$\ge 99.0\%$ Precision on Deterministic-Gate-Locked Fields:** Measured across all held-out documents where the deterministic gate locks `documentTotal` and `primaryDate`.
    * **$\ge 94.0\%$ Row-to-Amount Association Recall & Precision** on held-out single- and multi-page statements.