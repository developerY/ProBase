# Master Engineering Specification: Local-Processing-First, Cloud-Assisted AI Financial Document Engine for Android

## 0. Architectural Mandate & Core System Boundaries

You are tasked with implementing a production-grade Android financial document intelligence engine in **Kotlin** using **Jetpack Compose (Material 3)**, **Coroutines/Flow**, **Hilt**, **Room 2.8.5 (with SQLCipher)**, **Jetpack DataStore + Google Tink**, and **Google ML Kit**.

### The Three Explicit System Boundaries

1. **Local-First Document Preparation:** Raw camera images, gallery imports, and PDFs never leave the device. Image quality assessment, perspective/deskew transformations, OCR, perceptual color extraction, spatial table reconstruction, locale-aware candidate extraction, and arithmetic validation always execute locally on the phone.
2. **Cloud-Assisted Semantic Interpretation (with Local Deterministic Gate):** When the deterministic engine meets the measurable high-confidence threshold, no LLM is invoked. When semantic ambiguity remains, the engine transmits only a trimmed, text-only `LlmAnalysisBundle` JSON payload to the user's configured AI provider—defaulting to **Hosted Gemma (`GEMMA_4_CLOUD_FREE`)** via an authenticated backend proxy, with **On-Device Gemini Nano** treated as an optional runtime optimization rather than a baseline hardware requirement.
3. **Explicit Privacy, Consent, and Capability Enforcement:** Because a text-only `LlmAnalysisBundle` still contains sensitive financial metadata (merchant names, dates, amounts, and line descriptions), the system enforces an explicit pre-flight **Cloud Disclosure & Consent Policy**, offers a strict **Local-Only Mode**, forbids silent cross-provider cloud switching, and keeps raw financial text out of routine diagnostic logs by default.

### Core Architectural Laws (Non-Negotiable)

1. **Zero Raw Images Sent to Any LLM:** Neither on-device nor cloud LLMs receive pixel data.
2. **Scale-Aware Integer Minor Units Only (`CurrencyAmount`):** Never use `Float` or `Double` for monetary values. Every monetary value must carry its `amountMinor: Long`, ISO 4217 `currencyCode: String`, and explicit `currencyScale: Int` (e.g., `JPY` → 0, `USD`/`EUR` → 2, `BHD`/`KWD` → 3).
3. **Page-Scoped Evidence Provenance & Semantic-Role Validation:** Every resolved field must reference page-scoped, document-unique `evidence_ids` (formatted as `P{pageNumber}_T{elementIndex}`, e.g., `["P01_T014"]`). The post-LLM validator verifies not only that the ID exists, but also that its value, currency scale, spatial region, and semantic role match the extracted candidate context.
4. **Invertible Coordinate Transforms (`ocrToDisplayMatrix`):** Because OCR runs on a deskewed/cropped bitmap while the user review UI displays the original capture, every page stores the 3 × 3 homography matrix mapping OCR bounding boxes back to display coordinates.
5. **Separation of Document Families from Financial Events:** A parsed document (`FinancialDocumentEntity`) is never blindly written as an expense. The domain layer classifies the document family first and emits distinct financial events: `ExpenseTransaction`, `LiabilityBillRecord` (unpaid invoices/utility bills), `TransferRecord` (internal transfers and credit-card bill payments), `RefundCreditRecord`, and `StatementSummaryRecord` (opening/closing balances).
6. **Safe Diagnostics & Zero Shared Secrets in the APK:** Hosted Gemma (`GEMMA_4_CLOUD_FREE`) authenticates through a developer backend proxy with app attestation and rate limiting—never via a shared Gemini API key embedded in the APK. User-supplied `BYOK` keys are encrypted via **Google Tink (`Aead`) + Android Keystore** in Jetpack DataStore. Routine diagnostics record only non-sensitive telemetry metrics.

---

## 1. Verified 2026 Dependency Baseline & Modular Architecture

### 1.1 `gradle/libs.versions.toml`

Verify and lock this compatible late-2026 dependency baseline before generating module code:

```toml
[versions]
kotlin = "2.1.20"
ksp = "2.1.20-1.0.31"
coroutines = "1.10.1"
serialization = "1.8.0"
composeBom = "2026.09.00"
hilt = "2.56"
room = "2.8.5"
sqlcipher = "4.6.1"
sqliteKtx = "2.5.0"
datastore = "1.1.4"
tink = "1.16.0"
mlkitTextRecognition = "16.0.1"
mlkitDocumentScanner = "16.0.0-beta1"
aicore = "0.0.1-exp02"
firebaseBom = "34.4.0"
okhttp = "4.12.0"
junit5 = "5.11.4"
turbine = "1.2.0"

[libraries]
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }

# Room 2.8.5 + SQLCipher
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-sqlite-ktx = { module = "androidx.sqlite:sqlite-ktx", version.ref = "sqliteKtx" }
sqlcipher-android = { module = "net.zetetic:sqlcipher-android", version.ref = "sqlcipher" }

# Modern Credential Storage (Replaces deprecated EncryptedSharedPreferences)
androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
google-tink-android = { module = "com.google.crypto.tink:tink-android", version.ref = "tink" }

# Google ML Kit & AICore
mlkit-text-recognition = { module = "com.google.mlkit:text-recognition", version.ref = "mlkitTextRecognition" }
mlkit-document-scanner = { module = "com.google.android.gms:play-services-mlkit-document-scanner", version.ref = "mlkitDocumentScanner" }
google-ai-edge-aicore = { module = "com.google.ai.edge.aicore:aicore", version.ref = "aicore" }

# Firebase AI Logic & Mandatory App Check (Play Integrity enforced Nov 2, 2026)
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebaseBom" }
firebase-ai = { module = "com.google.firebase:firebase-ai" }
firebase-appcheck-playintegrity = { module = "com.google.firebase:firebase-appcheck-playintegrity" }
firebase-appcheck-debug = { module = "com.google.firebase:firebase-appcheck-debug" }

# Networking
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
```

### 1.2 Gradle Module Topology

* `:app` — Application class, Hilt graph root, navigation host, Firebase App Check initialization.
* `:feature:documentcapture` — ML Kit Document Scanner flow, CameraX fallback, gallery & native PDF import UI.
* `:feature:aiconfig` — AI Configuration Card UI, provider readiness testing, Cloud Consent dialog, and safe Diagnostics bottom sheet.
* `:feature:review` — Split-pane Compose review screen with homography-transformed bounding-box overlays and interactive correction binding.
* `:core:ocr` — Native PDF text-layer fast path, ML Kit Text Recognition v2 wrapper, page-scoped ID generator.
* `:core:document:vision` — Dual-bitmap preprocessing, 3 × 3 homography calculation, image quality scoring, and CIELAB perceptual color extraction.
* `:core:document:layout` — Polynomial/vector-ray baseline grouping for curled receipts, column clustering, header/footer region partitioning, and table reconstruction.
* `:core:finance:extract` — Scale-aware minor-unit parser, entity/address candidate scoring, arithmetic reconciliation, and deterministic confidence gate.
* `:core:finance:domain` — Document family classifier, financial event separator (`Expense`, `LiabilityBill`, `Transfer`, `Refund`, `StatementSummary`), account-aware duplicate detector, and post-LLM semantic validator.
* `:core:ai` — `ModelCapabilityRegistry`, `PolicyDrivenDocumentRouter`, `BundleTrimmer`, and provider adapters (`HostedGemmaProxyAdapter`, `FirebaseAiGeminiAdapter`, `ByokGeminiAdapter`, `GeminiNanoOnDeviceAdapter`, `DeterministicFallbackAdapter`).
* `:core:security` — Tink + Android Keystore credential vault, SQLCipher passphrase manager, PII log scrubber.
* `:core:database` — Room 2.8.5 database, DAOs, type converters, and automated evidence retention/purge worker.

---

## 2. Capture, Dual-Bitmap Pipeline & Invertible Coordinate Transforms

### 2.1 Native Digital PDF Fast-Path vs. Camera Capture

Before rasterizing an imported PDF, `:core:ocr` inspects the PDF structure:
1. **Native PDF Fast-Path:** If the PDF page contains an embedded text stream with character bounding coordinates and covers ≥ 85% of non-whitespace content, extract the text tokens and normalize their bounding boxes directly into `OcrElement` objects (`sourceType = "PDF_NATIVE_TEXT"`). Set the page's coordinate transform matrix to the identity matrix \(I_3\).
2. **Rasterized / Camera Path:** For photographed documents or scanned image-only PDFs, use **ML Kit Document Scanner** (`GmsDocumentScannerOptions.Builder().setScannerMode(SCANNER_MODE_FULL)`) or CameraX to produce two local bitmaps per page in encrypted app-private storage:
   * `raw_display_page_{n}.enc`: Original color bitmap used for UI presentation and perceptual CIELAB color analysis.
   * `clean_ocr_page_{n}.enc`: Deskewed, perspective-corrected, shadow-normalized grayscale bitmap passed to ML Kit Text Recognition v2.

### 2.2 Invertible Coordinate Transform (`ocrToDisplayMatrix`)

When perspective correction, rotation, or cropping transforms the original capture into the cleaned OCR bitmap via transformation matrix \(M_{\text{raw}\rightarrow\text{ocr}}\), `:core:document:vision` computes and persists the inverse 3 × 3 homography matrix:

$$M_{\text{ocr}\rightarrow\text{raw}} = \left(M_{\text{raw}\rightarrow\text{ocr}}\right)^{-1} = \begin{bmatrix} h_{00} & h_{01} & h_{02} \\ h_{10} & h_{11} & h_{12} \\ h_{20} & h_{21} & h_{22} \end{bmatrix}$$

Any normalized OCR bounding box corner \((x_{\text{ocr}}, y_{\text{ocr}}) \in [0, 1]^2\) is mapped back to the displayed image coordinate \((x_{\text{disp}}, y_{\text{disp}})\) via homogeneous projection:

$$x_{\text{disp}} = \frac{h_{00} x_{\text{ocr}} + h_{01} y_{\text{ocr}} + h_{02}}{h_{20} x_{\text{ocr}} + h_{21} y_{\text{ocr}} + h_{22}}, \quad y_{\text{disp}} = \frac{h_{10} x_{\text{ocr}} + h_{11} y_{\text{ocr}} + h_{12}}{h_{20} x_{\text{ocr}} + h_{21} y_{\text{ocr}} + h_{22}}$$

This guarantees that tapping an extracted amount in `:feature:review` highlights the exact pixels on `raw_display_page_{n}.enc` even when the receipt was photographed at an angle.

### 2.3 Perceptual Color Analysis (CIELAB)

Convert sampled pixels from `raw_display_page_{n}` (header, logo neighborhood, body background, and ink foreground) from sRGB to CIE \(L^*a^*b^*\). Compare header/logo dominant colors against stored `MerchantRuleEntity` templates using perceptual color distance:

$$\Delta E^*_{ab} = \sqrt{(\Delta L^*)^2 + (\Delta a^*)^2 + (\Delta b^*)^2}$$

A \(\Delta E^*_{ab} < 12.0\) combined with layout column alignment contributes a `+0.15` boost to candidate merchant confidence, but never independently overrides OCR text evidence.

---

## 3. Deterministic Spatial Layout, Scale-Aware Money & Confidence Gate

### 3.1 Page-Scoped Globally Unique Evidence IDs

Every OCR element and table cell receives a strictly formatted, document-unique ID:
* **OCR Elements:** `P{2-digit-page}_T{4-digit-index}` (e.g., `P01_T0014`, `P02_T0108`).
* **Reconstructed Lines:** `P{2-digit-page}_L{4-digit-index}` (e.g., `P01_L0009`).
* **Table Cells:** `P{2-digit-page}_C{4-digit-index}` (e.g., `P01_C0022`).

Bare element IDs like `T014` are rejected by the schema validator to prevent multi-page collisions.

### 3.2 Rotation-Aware Vector-Ray Line Grouping (Curled Receipt Fix)

Thermal receipts frequently curl, causing the vertical coordinate \(y\) of a single line to drift from left to right. `:core:document:layout` must not group tokens purely by horizontal \(y\)-overlap. Instead, for an anchor token \(A\) with center \((x_A, y_A)\), height \(h_A\), and ML Kit baseline angle \(\theta_A\) (in radians), project a baseline ray to the horizontal center \(x_B\) of candidate token \(B\):

$$\hat{y}_B = y_A + (x_B - x_A) \cdot \tan(\theta_A)$$

Token \(B\) belongs to the same logical row as \(A\) if and only if:

$$\left| y_B - \hat{y}_B \right| \le 0.45 \cdot \max(h_A, h_B) \quad \text{and} \quad \left| \theta_A - \theta_B \right| \le 5^\circ$$

### 3.3 Scale-Aware Monetary Representation (`CurrencyAmount`)

Never hardcode two decimal places. `:core:finance:extract` resolves the currency's minor-unit scale dynamically via ISO 4217 rules:

```kotlin
package com.example.finengine.core.finance.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

@Serializable
data class CurrencyAmount(
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("currency_code") val currencyCode: String,
    @SerialName("currency_scale") val currencyScale: Int
) {
    init {
        require(currencyCode.length == 3) { "Currency code must be ISO 4217 3-letter format" }
        require(currencyScale in 0..4) { "Unsupported currency scale: $currencyScale" }
    }

    fun toBigDecimal(): BigDecimal =
        BigDecimal.valueOf(amountMinor, currencyScale)

    companion object {
        fun fromBigDecimal(value: BigDecimal, currencyCode: String): CurrencyAmount {
            val normalizedCode = currencyCode.uppercase()
            val scale = runCatching {
                Currency.getInstance(normalizedCode).defaultFractionDigits.coerceAtLeast(0)
            }.getOrDefault(2)
            val minor = value.setScale(scale, RoundingMode.HALF_UP)
                .movePointRight(scale)
                .longValueExact()
            return CurrencyAmount(
                amountMinor = minor,
                currencyCode = normalizedCode,
                currencyScale = scale
            )
        }
    }
}
```

### 3.4 Measurable Deterministic Confidence Gate

Before calling any AI provider, `DeterministicConfidenceGate` computes a composite readiness check. The pipeline **skips the LLM completely** and marks `executedProvider = DETERMINISTIC_RULES_ONLY` if and only if all five conditions hold:
1. **Document Family Certainty:** A single `DocumentFamily` scores ≥ 0.90 with at least a 0.25 margin over the second-highest candidate.
2. **Issuer/Merchant Match:** Primary merchant candidate confidence is ≥ 0.92 (backed by header position + known `MerchantRuleEntity` or unambiguous header typography).
3. **Date Unambiguity:** Exactly one plausible primary transaction/statement date exists in the expected region, or multiple dates are explicitly labeled (`Invoice Date` vs. `Due Date`).
4. **Exact Arithmetic Reconciliation:**
   * For receipts/invoices:
     \[
     \sum_i \text{LineItem}_i + \text{Tax}_{\text{minor}} + \text{Tip}_{\text{minor}} - \text{Discount}_{\text{minor}} = \text{Total}_{\text{minor}}
     \]
   * For statements:
     \[
     \text{OpeningBalance}_{\text{minor}} + \sum \text{Credits}_{\text{minor}} - \sum \text{Debits}_{\text{minor}} = \text{ClosingBalance}_{\text{minor}}
     \]
5. **Category Resolution:** The merchant maps deterministically to an active `CategoryRuleEntity` with confidence ≥ 0.90.

---

## 4. Financial Domain Semantics & Canonical Data Contracts

### 4.1 Separating Document Families from Financial Events

A document's total is not always an expense. The engine classifies the document into a `DocumentFamily` first, then emits zero, one, or many typed `FinancialEvent` records:

| Document Family | Primary Domain Output | Crucial Guardrail |
| :--- | :--- | :--- |
| `RECEIPT` | `ExpenseTransaction` (+ optional `TransactionItem` list) | Reconcile subtotal + tax + tip = total. |
| `INVOICE_OR_UTILITY_BILL` | `LiabilityBillRecord` (`UNPAID` or `PAID`) | An unpaid bill creates a liability with `amountDue` and `dueDate`. Only emit an `ExpenseTransaction` if payment confirmation evidence is present on the document. |
| `BANK_STATEMENT` | `StatementSummaryRecord` + multiple `ExpenseTransaction` / `TransferRecord` / `IncomeRecord` rows | Never create an expense from `opening_balance` or `closing_balance`. Classify payments to credit cards as `TransferRecord`. |
| `CREDIT_CARD_STATEMENT` | `StatementSummaryRecord` + `LiabilityBillRecord` (statement balance due) + individual `ExpenseTransaction` / `RefundCreditRecord` rows | Recording individual card purchases as expenses AND recording the statement balance or payment due as an expense is strictly forbidden (prevents double-counting). |
| `REFUND_OR_CREDIT_NOTE` | `RefundCreditRecord` | Stored with positive minor amount and `event_type = REFUND_CREDIT`; linked to original purchase if reference matches. |

### 4.2 Account-Aware Duplicate Detection

To avoid flagging two legitimate identical purchases on the same day (e.g., two morning coffees) while catching re-scanned receipts or overlapping statement imports, compute a two-tier fingerprint:
* **Hard Duplicate Key (Auto-Suppress):** `SHA-256(sourceFileHash)` or `SHA-256(accountIdHash + statementPeriod + rowIndex + amountMinor + dateIso)`.
* **Soft Duplicate Candidate (Flag for User Review):** `SHA-256(normalizedMerchant + dateIso + amountMinor + currencyCode + (last4AccountDigits ?: "UNKNOWN") + (referenceNumber ?: "NONE"))`.
* **Cross-Document Transfer Linker:** When a `BANK_STATEMENT` shows a debit to a credit card issuer (e.g., `"CHASE CREDIT CRD AUTOPAY"`) for amount $X$ on date $T$, and a `CREDIT_CARD_STATEMENT` shows a payment credit for amount $X$ within \(T \pm 3\text{ days}\), link both into a single reconciled `TransferRecord`.

### 4.3 Canonical Kotlin Serialization Contracts (`:core:ai` & `:core:finance:domain`)

```kotlin
package com.example.finengine.core.finance.contract

import com.example.finengine.core.finance.model.CurrencyAmount
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DocumentFamily {
    @SerialName("receipt") RECEIPT,
    @SerialName("invoice_or_bill") INVOICE_OR_UTILITY_BILL,
    @SerialName("bank_statement") BANK_STATEMENT,
    @SerialName("credit_card_statement") CREDIT_CARD_STATEMENT,
    @SerialName("refund_or_credit_note") REFUND_OR_CREDIT_NOTE,
    @SerialName("unknown") UNKNOWN
}

@Serializable
enum class FinancialEventKind {
    @SerialName("purchase_expense") PURCHASE_EXPENSE,
    @SerialName("unpaid_bill_liability") UNPAID_BILL_LIABILITY,
    @SerialName("paid_bill_expense") PAID_BILL_EXPENSE,
    @SerialName("internal_or_cc_transfer") INTERNAL_OR_CC_TRANSFER,
    @SerialName("refund_or_credit") REFUND_OR_CREDIT,
    @SerialName("income_deposit") INCOME_DEPOSIT,
    @SerialName("fee_or_interest") FEE_OR_INTEREST,
    @SerialName("statement_balance_ignore") STATEMENT_BALANCE_IGNORE
}

@Serializable
enum class CandidateSemanticRole {
    @SerialName("merchant_name") MERCHANT_NAME,
    @SerialName("issuer_address") ISSUER_ADDRESS,
    @SerialName("customer_address") CUSTOMER_ADDRESS,
    @SerialName("remittance_address") REMITTANCE_ADDRESS,
    @SerialName("transaction_date") TRANSACTION_DATE,
    @SerialName("posting_date") POSTING_DATE,
    @SerialName("due_date") DUE_DATE,
    @SerialName("statement_period_start") STATEMENT_PERIOD_START,
    @SerialName("statement_period_end") STATEMENT_PERIOD_END,
    @SerialName("subtotal_amount") SUBTOTAL_AMOUNT,
    @SerialName("tax_amount") TAX_AMOUNT,
    @SerialName("tip_amount") TIP_AMOUNT,
    @SerialName("discount_amount") DISCOUNT_AMOUNT,
    @SerialName("total_amount") TOTAL_AMOUNT,
    @SerialName("amount_due") AMOUNT_DUE,
    @SerialName("opening_balance") OPENING_BALANCE,
    @SerialName("closing_balance") CLOSING_BALANCE,
    @SerialName("line_item_amount") LINE_ITEM_AMOUNT
}

@Serializable
data class NormalizedBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

@Serializable
data class AmountCandidateDto(
    @SerialName("candidate_id") val candidateId: String,
    @SerialName("role_hint") val roleHint: CandidateSemanticRole,
    val amount: CurrencyAmount,
    @SerialName("region") val region: String, // "header", "body", "table", "footer"
    @SerialName("label_text") val labelText: String?,
    @SerialName("evidence_ids") val evidenceIds: List<String>,
    val confidence: Float
)

@Serializable
data class DateCandidateDto(
    @SerialName("candidate_id") val candidateId: String,
    @SerialName("role_hint") val roleHint: CandidateSemanticRole,
    @SerialName("iso_date") val isoDate: String, // YYYY-MM-DD
    @SerialName("region") val region: String,
    @SerialName("label_text") val labelText: String?,
    @SerialName("evidence_ids") val evidenceIds: List<String>,
    val confidence: Float
)

@Serializable
data class TextCandidateDto(
    @SerialName("candidate_id") val candidateId: String,
    @SerialName("role_hint") val roleHint: CandidateSemanticRole,
    val value: String,
    @SerialName("region") val region: String,
    @SerialName("evidence_ids") val evidenceIds: List<String>,
    val confidence: Float
)

@Serializable
data class ReconstructedRowDto(
    @SerialName("row_id") val rowId: String,
    @SerialName("page_number") val pageNumber: Int,
    val cells: List<ReconstructedCellDto>
)

@Serializable
data class ReconstructedCellDto(
    @SerialName("cell_id") val cellId: String, // e.g., "P01_C0012"
    @SerialName("column_role") val columnRole: String, // "date", "description", "qty", "unit_price", "debit", "credit", "amount", "balance"
    @SerialName("normalized_text") val normalizedText: String,
    @SerialName("parsed_amount") val parsedAmount: CurrencyAmount? = null,
    @SerialName("parsed_iso_date") val parsedIsoDate: String? = null,
    @SerialName("evidence_ids") val evidenceIds: List<String>
)

/**
 * Compact, budget-trimmed text-only bundle sent to the LLM.
 * Strict ceiling: <= 12 KB UTF-8 bytes (~3,000 tokens) per chunk.
 */
@Serializable
data class LlmAnalysisBundle(
    @SerialName("schema_version") val schemaVersion: String = "2.0",
    @SerialName("document_id") val documentId: String,
    @SerialName("chunk_index") val chunkIndex: Int = 0,
    @SerialName("total_chunks") val totalChunks: Int = 1,
    @SerialName("family_candidates") val familyCandidates: Map<DocumentFamily, Float>,
    @SerialName("text_candidates") val textCandidates: List<TextCandidateDto>,
    @SerialName("date_candidates") val dateCandidates: List<DateCandidateDto>,
    @SerialName("amount_candidates") val amountCandidates: List<AmountCandidateDto>,
    @SerialName("table_rows") val tableRows: List<ReconstructedRowDto>,
    @SerialName("arithmetic_check") val arithmeticCheck: ArithmeticCheckSummary,
    @SerialName("allowed_categories") val allowedCategories: List<String>
)

@Serializable
data class ArithmeticCheckSummary(
    @SerialName("reconciled") val reconciled: Boolean,
    @SerialName("discrepancy_minor") val discrepancyMinor: Long,
    @SerialName("notes") val notes: String
)

/**
 * Canonical output returned by any AI provider or deterministic fallback.
 */
@Serializable
data class DocumentInterpretation(
    @SerialName("document_family") val documentFamily: DocumentFamily,
    @SerialName("merchant_or_issuer") val merchantOrIssuer: ResolvedStringField? = null,
    @SerialName("issuer_address") val issuerAddress: ResolvedStringField? = null,
    @SerialName("primary_date") val primaryDate: ResolvedDateField? = null,
    @SerialName("due_date") val dueDate: ResolvedDateField? = null,
    @SerialName("document_total") val documentTotal: ResolvedAmountField? = null,
    @SerialName("tax_total") val taxTotal: ResolvedAmountField? = null,
    @SerialName("financial_events") val financialEvents: List<ResolvedFinancialEvent> = emptyList(),
    @SerialName("review_required") val reviewRequired: Boolean,
    @SerialName("unresolved_fields") val unresolvedFields: List<String> = emptyList()
)

@Serializable
data class ResolvedStringField(
    val value: String,
    @SerialName("role") val role: CandidateSemanticRole,
    @SerialName("evidence_ids") val evidenceIds: List<String>
)

@Serializable
data class ResolvedDateField(
    @SerialName("iso_date") val isoDate: String,
    @SerialName("role") val role: CandidateSemanticRole,
    @SerialName("evidence_ids") val evidenceIds: List<String>
)

@Serializable
data class ResolvedAmountField(
    val amount: CurrencyAmount,
    @SerialName("role") val role: CandidateSemanticRole,
    @SerialName("evidence_ids") val evidenceIds: List<String>
)

@Serializable
data class ResolvedFinancialEvent(
    @SerialName("event_kind") val eventKind: FinancialEventKind,
    val description: String,
    @SerialName("iso_date") val isoDate: String?,
    val amount: CurrencyAmount,
    val category: String,
    @SerialName("evidence_ids") val evidenceIds: List<String>
)
```

### 4.4 Deterministic Token & Payload Budgeter (`BundleTrimmer`)

To prevent context-window overflow and control cloud latency/cost, `BundleTrimmer` enforces strict limits before any provider call:
1. **Candidate Filtering:** Include only the top 5 `textCandidates`, top 6 `dateCandidates`, and top 10 `amountCandidates` ranked by local confidence. Strip all raw OCR lines that did not contribute to a candidate or a table row.
2. **Row Chunking for Multi-Page Statements:** If a reconstructed table contains > 15 rows, split `tableRows` into sequential chunks of 15 rows (`chunkIndex = 0..N-1`). Each chunk retains the document-level header candidates (`familyCandidates`, `textCandidates`, `dateCandidates`, `allowedCategories`) and reconciles locally after all chunks return.
3. **Hard Payload Ceiling:** Verify `json.encodeToString(bundle).encodeToByteArray().size <= 12_288` (12 KB).

---

## 5. Policy-Driven Multi-Provider AI Architecture & Security Model

### 5.1 Resolving the Provider Architecture & Default Configuration

1. **Hosted Gemma (`GEMMA_4_CLOUD_FREE`) is the Persisted Default Primary Engine:**
   * Because most Android devices in the wild do not have Gemini Nano downloaded or hardware-supported, **Gemma 4 Cloud (Free Tier) - Default** is the out-of-the-box default selection.
   * **Valid Hosted Gemma Model IDs:** Managed via `ModelCapabilityRegistry`, using Google's current Gemini API hosted Gemma IDs:
     * `gemma-4-31b-it` (Default primary model)
     * `gemma-4-26b-a4b-it` (Low-latency alternative)
   * **Backend Proxy Security Architecture:** Never ship a developer-owned Gemini API key inside the APK. `HostedGemmaProxyAdapter` sends the `LlmAnalysisBundle` to your authenticated backend proxy (`https://api.yourfinapp.example/v1/interpret/gemma`), presenting a **Play Integrity / Firebase App Check token** in the `X-Firebase-AppCheck` header. The backend verifies app attestation, enforces per-device daily free-tier quotas, attaches the server-side Gemini API key, and forwards the request to `models/gemma-4-31b-it:generateContent`.
2. **Firebase AI Logic (`FIREBASE_AI_LOGIC`) is an Explicit Gemini-Model Route:**
   * Firebase AI Logic does not serve hosted Gemma models. In `ModelCapabilityRegistry`, `FIREBASE_AI_LOGIC` is explicitly typed as a **Cloud Gemini Route** supporting `gemini-2.5-flash` and `gemini-2.5-pro`.
   * **Mandatory App Check Enforcement (Nov 2, 2026 Deadline):** In `:app`, initialize `FirebaseAppCheck.getInstance().installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())` prior to any `Firebase.ai` call.
3. **Bring Your Own Key (`BYOK_GEMINI`):**
   * Explicitly scoped to user-provided Gemini API keys (`https://generativelanguage.googleapis.com/v1beta/models`), allowing users to unlock and choose from their account's available models (`gemma-4-31b-it`, `gemma-4-26b-a4b-it`, `gemini-2.5-flash`, `gemini-2.5-pro`) after clicking **Test connection to see models**.
   * **Modern Credential Vault (`TinkCredentialVault`):** Replaces the deprecated `EncryptedSharedPreferences`. Uses Google Tink `AndroidKeysetManager` backed by `android-keystore://fin_ai_master_key` to encrypt the user's API key via `Aead` before persisting ciphertext bytes in Jetpack `DataStore<Preferences>`.
4. **On-Device Local AI (`ON_DEVICE_GEMINI_NANO`):**
   * Treated as an optional, zero-network-egress runtime capability via AICore. Checked dynamically at runtime; never assumed available.

### 5.2 Provider Capability Registry & Readiness States

```kotlin
package com.example.finengine.core.ai

import kotlinx.serialization.Serializable

@Serializable
enum class AiEngineProvider(
    val displayName: String,
    val shortBadgeLabel: String,
    val requiresCloudConsent: Boolean
) {
    GEMMA_4_CLOUD_FREE(
        displayName = "Gemma 4 Cloud (Free Tier) - Default",
        shortBadgeLabel = "Gemma",
        requiresCloudConsent = true
    ),
    FIREBASE_AI_LOGIC(
        displayName = "Firebase AI Logic (Cloud)",
        shortBadgeLabel = "Firebase",
        requiresCloudConsent = true
    ),
    BYOK_GEMINI(
        displayName = "Bring Your Own Key (BYOK)",
        shortBadgeLabel = "BYOK",
        requiresCloudConsent = true
    ),
    ON_DEVICE_GEMINI_NANO(
        displayName = "On-Device Local AI (Gemini Nano)",
        shortBadgeLabel = "Local AI",
        requiresCloudConsent = false
    )
}

enum class ProviderReadinessState {
    UNCONFIGURED,   // No key or consent yet; Initial state before verification
    CHECKING,       // Active connection / AICore probe in progress
    READY,          // Verified operational with compatible model capability
    DEGRADED,       // Reachable but high latency or partial failure
    QUOTA_LIMITED,  // HTTP 429 or daily free-tier proxy quota exhausted
    OFFLINE,        // Network unreachable for cloud provider
    UNSUPPORTED     // Device lacks AICore / Gemini Nano hardware support
}

data class ModelDescriptor(
    val modelId: String,
    val displayName: String,
    val provider: AiEngineProvider,
    val supportsNativeResponseSchema: Boolean,
    val maxInputTokens: Int,
    val maxOutputTokens: Int
)

object ModelCapabilityRegistry {
    val modelsByProvider: Map<AiEngineProvider, List<ModelDescriptor>> = mapOf(
        AiEngineProvider.GEMMA_4_CLOUD_FREE to listOf(
            ModelDescriptor(
                modelId = "gemma-4-31b-it",
                displayName = "Gemma 4 31B Instruct (Hosted)",
                provider = AiEngineProvider.GEMMA_4_CLOUD_FREE,
                supportsNativeResponseSchema = false, // Parse as untrusted JSON text
                maxInputTokens = 8192,
                maxOutputTokens = 2048
            ),
            ModelDescriptor(
                modelId = "gemma-4-26b-a4b-it",
                displayName = "Gemma 4 26B-A4B Instruct (Hosted Fast)",
                provider = AiEngineProvider.GEMMA_4_CLOUD_FREE,
                supportsNativeResponseSchema = false,
                maxInputTokens = 8192,
                maxOutputTokens = 2048
            )
        ),
        AiEngineProvider.FIREBASE_AI_LOGIC to listOf(
            ModelDescriptor(
                modelId = "gemini-2.5-flash",
                displayName = "Gemini 2.5 Flash (Firebase AI Logic)",
                provider = AiEngineProvider.FIREBASE_AI_LOGIC,
                supportsNativeResponseSchema = true,
                maxInputTokens = 32768,
                maxOutputTokens = 4096
            )
        ),
        AiEngineProvider.BYOK_GEMINI to listOf(
            ModelDescriptor(
                modelId = "gemini-2.5-flash",
                displayName = "Gemini 2.5 Flash (BYOK)",
                provider = AiEngineProvider.BYOK_GEMINI,
                supportsNativeResponseSchema = true,
                maxInputTokens = 32768,
                maxOutputTokens = 4096
            ),
            ModelDescriptor(
                modelId = "gemma-4-31b-it",
                displayName = "Gemma 4 31B Instruct (BYOK)",
                provider = AiEngineProvider.BYOK_GEMINI,
                supportsNativeResponseSchema = false,
                maxInputTokens = 8192,
                maxOutputTokens = 2048
            ),
            ModelDescriptor(
                modelId = "gemma-4-26b-a4b-it",
                displayName = "Gemma 4 26B-A4B Instruct (BYOK)",
                provider = AiEngineProvider.BYOK_GEMINI,
                supportsNativeResponseSchema = false,
                maxInputTokens = 8192,
                maxOutputTokens = 2048
            )
        ),
        AiEngineProvider.ON_DEVICE_GEMINI_NANO to listOf(
            ModelDescriptor(
                modelId = "gemini-nano-on-device",
                displayName = "Gemini Nano (AICore On-Device)",
                provider = AiEngineProvider.ON_DEVICE_GEMINI_NANO,
                supportsNativeResponseSchema = false,
                maxInputTokens = 3072,
                maxOutputTokens = 1024
            )
        )
    )
}
```

### 5.3 Policy-Driven Router (`PolicyDrivenDocumentRouter`)

Instead of a hardcoded fallback sequence that ignores the user's primary selection or privacy settings, `PolicyDrivenDocumentRouter` evaluates an explicit `AiRoutingPolicy`:

```kotlin
package com.example.finengine.core.ai.router

import com.example.finengine.core.ai.AiEngineProvider
import com.example.finengine.core.ai.ProviderReadinessState
import com.example.finengine.core.finance.contract.DocumentInterpretation
import com.example.finengine.core.finance.contract.LlmAnalysisBundle
import javax.inject.Inject
import javax.inject.Singleton

data class AiRoutingPolicy(
    val primaryProvider: AiEngineProvider = AiEngineProvider.GEMMA_4_CLOUD_FREE,
    val preferredModelIdByProvider: Map<AiEngineProvider, String> = mapOf(
        AiEngineProvider.GEMMA_4_CLOUD_FREE to "gemma-4-31b-it",
        AiEngineProvider.FIREBASE_AI_LOGIC to "gemini-2.5-flash",
        AiEngineProvider.BYOK_GEMINI to "gemini-2.5-flash",
        AiEngineProvider.ON_DEVICE_GEMINI_NANO to "gemini-nano-on-device"
    ),
    val strictLocalOnlyMode: Boolean = false,
    val cloudTransferConsentGranted: Boolean = false,
    val authorizedFallbackProviders: List<AiEngineProvider> = listOf(
        AiEngineProvider.ON_DEVICE_GEMINI_NANO,
        AiEngineProvider.FIREBASE_AI_LOGIC
    )
) {
    /**
     * Computes the ordered provider execution chain starting with the user's primary selection,
     * followed only by authorized fallbacks that respect strictLocalOnlyMode.
     */
    fun computeOrderedChain(): List<AiEngineProvider> {
        val rawOrder = listOf(primaryProvider) + authorizedFallbackProviders.filter { it != primaryProvider }
        return if (strictLocalOnlyMode) {
            rawOrder.filter { !it.requiresCloudConsent }
        } else {
            rawOrder
        }
    }
}
```

**Execution Flow inside `PolicyDrivenDocumentRouter.interpret(bundle)`:**
1. **Step 1 — Deterministic Gate:** If `DeterministicConfidenceGate.canResolveWithoutLlm(bundle)` is `true`, immediately return the deterministic `DocumentInterpretation` (`executedProvider = "DETERMINISTIC_GATE"`).
2. **Step 2 — Evaluate Ordered Provider Chain:** Iterate through `policy.computeOrderedChain()`:
   * If `provider.requiresCloudConsent && !policy.cloudTransferConsentGranted`, skip with reason `CLOUD_CONSENT_NOT_GRANTED`.
   * Check `providerAdapter.checkReadiness()`. If state is not `READY` or `DEGRADED`, skip and record the state (`UNCONFIGURED`, `QUOTA_LIMITED`, `OFFLINE`, `UNSUPPORTED`).
   * Invoke `providerAdapter.execute(bundle, modelDescriptor)` with a strict timeout (`8,000 ms` for on-device, `15,000 ms` for cloud).
   * Pass the raw response string through `UntrustedJsonExtractor` and `PostLlmSemanticValidator.validate(rawInterpretation, bundle)`.
   * If validation succeeds, return the validated `DocumentInterpretation` along with safe telemetry.
3. **Step 3 — Deterministic Fallback & Human Review:** If all eligible providers fail or are skipped, return `DeterministicFallbackAdapter.buildPartialInterpretation(bundle)` with `reviewRequired = true` and populated `unresolvedFields`.

---

## 6. Post-LLM Semantic, Role & Evidence Validator

Because Hosted Gemma (`gemma-4-31b-it` / `gemma-4-26b-a4b-it`) and Gemini Nano outputs must be treated as untrusted text, `PostLlmSemanticValidator` enforces six mandatory invariants before any field is accepted:

```kotlin
package com.example.finengine.core.finance.validation

import com.example.finengine.core.finance.contract.*
import kotlinx.serialization.json.Json

class PostLlmSemanticValidator(private val json: Json) {

    /**
     * Strips markdown code fences (```json ... ```) or conversational preambles
     * before deserializing into DocumentInterpretation.
     */
    fun parseUntrustedJson(rawText: String): Result<DocumentInterpretation> = runCatching {
        val startIdx = rawText.indexOf('{')
        val endIdx = rawText.lastIndexOf('}')
        require(startIdx != -1 && endIdx > startIdx) { "No JSON object boundaries found" }
        val cleanJson = rawText.substring(startIdx, endIdx + 1)
        json.decodeFromString<DocumentInterpretation>(cleanJson)
    }

    /**
     * Validates provenance existence, monetary value & scale equality, and semantic role coherence.
     */
    fun validateAndSanitize(
        interpretation: DocumentInterpretation,
        bundle: LlmAnalysisBundle
    ): ValidationOutcome {
        val validEvidenceIds = buildSet {
            bundle.textCandidates.flatMapTo(this) { it.evidenceIds }
            bundle.dateCandidates.flatMapTo(this) { it.evidenceIds }
            bundle.amountCandidates.flatMapTo(this) { it.evidenceIds }
            bundle.tableRows.flatMap { it.cells }.flatMapTo(this) { it.evidenceIds }
        }

        val violations = mutableListOf<String>()

        // 1. Page-scoped Evidence ID format & existence check
        val pageScopedRegex = Regex("""^P\d{2}_[TLC]\d{4}$""")
        fun verifyEvidenceIds(ids: List<String>, fieldName: String): Boolean {
            if (ids.isEmpty()) {
                violations += "$fieldName is missing evidence_ids"
                return false
            }
            val allValid = ids.all { id -> pageScopedRegex.matches(id) && id in validEvidenceIds }
            if (!allValid) violations += "$fieldName references unknown or malformed evidence_ids: $ids"
            return allValid
        }

        // 2. Document Total Value, Scale, and Semantic Role Check
        val validatedTotal = interpretation.documentTotal?.let { totalField ->
            val idsOk = verifyEvidenceIds(totalField.evidenceIds, "document_total")
            val matchingCandidate = bundle.amountCandidates.find { cand ->
                cand.amount == totalField.amount &&
                    cand.evidenceIds.intersect(totalField.evidenceIds.toSet()).isNotEmpty()
            }
            val rolePlausible = matchingCandidate != null &&
                matchingCandidate.roleHint in setOf(
                    CandidateSemanticRole.TOTAL_AMOUNT,
                    CandidateSemanticRole.AMOUNT_DUE,
                    CandidateSemanticRole.SUBTOTAL_AMOUNT
                ) &&
                !(matchingCandidate.region == "header" && bundle.amountCandidates.any {
                    it.roleHint == CandidateSemanticRole.TOTAL_AMOUNT && it.region != "header"
                })

            if (!idsOk || !rolePlausible) {
                violations += "document_total failed candidate value/role match"
                null
            } else {
                totalField
            }
        }

        // 3. Primary Date Role & Region Check (Prevent footer print dates from overriding purchase dates)
        val validatedPrimaryDate = interpretation.primaryDate?.let { dateField ->
            val idsOk = verifyEvidenceIds(dateField.evidenceIds, "primary_date")
            val matchingDate = bundle.dateCandidates.find { cand ->
                cand.isoDate == dateField.isoDate &&
                    cand.evidenceIds.intersect(dateField.evidenceIds.toSet()).isNotEmpty()
            }
            if (!idsOk || matchingDate == null || matchingDate.roleHint == CandidateSemanticRole.DUE_DATE) {
                violations += "primary_date failed candidate match or conflated due_date with transaction_date"
                null
            } else {
                dateField
            }
        }

        // 4. Financial Event Filtering (Suppress statement_balance_ignore & validate categories)
        val allowedCategorySet = bundle.allowedCategories.toSet()
        val validEvents = interpretation.financialEvents
            .filter { it.eventKind != FinancialEventKind.STATEMENT_BALANCE_IGNORE }
            .mapNotNull { event ->
                if (!verifyEvidenceIds(event.evidenceIds, "event(${event.description})")) return@mapNotNull null
                val amountExists = bundle.amountCandidates.any { it.amount == event.amount } ||
                    bundle.tableRows.flatMap { it.cells }.any { it.parsedAmount == event.amount }
                if (!amountExists) {
                    violations += "event(${event.description}) invented amount ${event.amount}"
                    return@mapNotNull null
                }
                val safeCategory = if (event.category in allowedCategorySet) event.category else "Uncategorized"
                event.copy(category = safeCategory)
            }

        val unresolved = (interpretation.unresolvedFields + violations).distinct()
        val sanitized = interpretation.copy(
            documentTotal = validatedTotal,
            primaryDate = validatedPrimaryDate,
            financialEvents = validEvents,
            reviewRequired = interpretation.reviewRequired || violations.isNotEmpty() || validatedTotal == null,
            unresolvedFields = unresolved
        )

        return ValidationOutcome(
            sanitizedInterpretation = sanitized,
            passedStrictly = violations.isEmpty(),
            violationCodes = violations
        )
    }
}

data class ValidationOutcome(
    val sanitizedInterpretation: DocumentInterpretation,
    val passedStrictly: Boolean,
    val violationCodes: List<String>
)
```

---

## 7. AI Configuration UI & Safe Diagnostics (`:feature:aiconfig`)

The UI implementation matches your design screenshot while fixing the underlying state machine:
* **True Default State:** `GEMMA_4_CLOUD_FREE` (`"Gemma 4 Cloud (Free Tier) - Default"`) is the initial persisted primary selection.
* **Dynamic Status Header Chain:** Displays the live readiness indicator dots (`READY` = Green `#2E7D32`, `CHECKING`/`DEGRADED`/`QUOTA_LIMITED` = Amber `#F57C00`, `UNCONFIGURED`/`OFFLINE`/`UNSUPPORTED` = Red `#D32F2F`) ordered dynamically according to the user's active routing chain, or showcasing the 4-provider status bar (`BYOK` → `Local AI` → `Firebase` → `Gemma`) with bold typography on ready providers.
* **Connection Test & Model Unlock Flow:** Selecting `BYOK_GEMINI` (or switching providers before verification) displays `"Test connection to see models"` in the `Gemini Model` field with the helper caption `"Test connection to unlock available models."` Only a verified connection/capability check unlocks the model selector dropdown.
* **Safe-by-Default Diagnostics Bug Icon:** Tapping the bottom-right bug icon opens the `SafeDiagnosticsBottomSheet` displaying non-sensitive metrics (provider latency, token counts, fallback reasons, validation violation codes) and an explicit, time-limited opt-in toggle for redacted debug capture.

```kotlin
package com.example.finengine.feature.aiconfig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.example.finengine.core.ai.AiEngineProvider
import com.example.finengine.core.ai.ModelDescriptor
import com.example.finengine.core.ai.ProviderReadinessState

data class AiConfigUiState(
    val isCardExpanded: Boolean = true,
    val selectedPrimaryEngine: AiEngineProvider = AiEngineProvider.GEMMA_4_CLOUD_FREE,
    val readinessByProvider: Map<AiEngineProvider, ProviderReadinessState> = mapOf(
        AiEngineProvider.BYOK_GEMINI to ProviderReadinessState.UNCONFIGURED,
        AiEngineProvider.ON_DEVICE_GEMINI_NANO to ProviderReadinessState.UNCONFIGURED,
        AiEngineProvider.FIREBASE_AI_LOGIC to ProviderReadinessState.UNCONFIGURED,
        AiEngineProvider.GEMMA_4_CLOUD_FREE to ProviderReadinessState.UNCONFIGURED
    ),
    val isConnectionVerifiedForSelected: Boolean = false,
    val unlockedModelsForSelected: List<ModelDescriptor> = emptyList(),
    val selectedModelId: String? = null,
    val byokApiKeyInput: String = "",
    val strictLocalOnlyMode: Boolean = false,
    val cloudConsentGranted: Boolean = false,
    val isTestingConnection: Boolean = false
)

@Composable
fun AiConfigurationCard(
    state: AiConfigUiState,
    onToggleExpand: () -> Unit,
    onSelectPrimaryEngine: (AiEngineProvider) -> Unit,
    onByokKeyChanged: (String) -> Unit,
    onTestConnectionClicked: () -> Unit,
    onSelectModel: (String) -> Unit,
    onOpenSafeDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var engineDropdownExpanded by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    val cardBackground = Color(0xFFEDE7F6) // Soft lavender surface matching screenshot
    val accentPurple = Color(0xFF5E35B1)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        color = cardBackground,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row: Sparkle Icon + Title + Status Chain + Expand Chevron
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
                        color = Color(0xFF1C1B1F)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ProviderStatusChainRow(readinessMap = state.readinessByProvider)
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (state.isCardExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = "Toggle AI Configuration"
                    )
                }
            }

            AnimatedVisibility(visible = state.isCardExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary AI Engine Section
                    Text(
                        text = "Primary AI Engine",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentPurple
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, accentPurple, RoundedCornerShape(6.dp))
                                .clickable { engineDropdownExpanded = true },
                            color = Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = state.selectedPrimaryEngine.displayName,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1C1B1F)
                                )
                                Icon(
                                    imageVector = if (engineDropdownExpanded) {
                                        Icons.Default.KeyboardArrowUp
                                    } else {
                                        Icons.Default.KeyboardArrowDown
                                    },
                                    contentDescription = "Select Primary AI Engine"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = engineDropdownExpanded,
                            onDismissRequest = { engineDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .background(Color(0xFFF7F2FA))
                        ) {
                            AiEngineProvider.entries.forEach { provider ->
                                val isSelected = provider == state.selectedPrimaryEngine
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = provider.displayName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF79747E) else Color(0xFF1C1B1F)
                                        )
                                    },
                                    onClick = {
                                        engineDropdownExpanded = false
                                        onSelectPrimaryEngine(provider)
                                    }
                                )
                            }
                        }
                    }

                    // Conditional BYOK Key Input + Test Connection Button
                    if (state.selectedPrimaryEngine == AiEngineProvider.BYOK_GEMINI) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = state.byokApiKeyInput,
                            onValueChange = onByokKeyChanged,
                            label = { Text("Gemini API Key (Encrypted via Tink Keystore)") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    FilledTonalButton(
                        onClick = onTestConnectionClicked,
                        enabled = !state.isTestingConnection,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (state.isTestingConnection) {
                                "Verifying Provider Readiness..."
                            } else {
                                "Test Connection & Verify Capabilities"
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Preferred AI Model Section
                    Text(
                        text = "Preferred AI Model",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentPurple
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (state.isConnectionVerifiedForSelected) {
                                state.unlockedModelsForSelected
                                    .find { it.modelId == state.selectedModelId }
                                    ?.displayName
                                    ?: "Select verified model"
                            } else {
                                "Test connection to see models"
                            },
                            onValueChange = {},
                            readOnly = true,
                            enabled = state.isConnectionVerifiedForSelected,
                            label = { Text("Gemini Model") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = state.isConnectionVerifiedForSelected) {
                                    modelDropdownExpanded = true
                                }
                        )

                        DropdownMenu(
                            expanded = modelDropdownExpanded && state.isConnectionVerifiedForSelected,
                            onDismissRequest = { modelDropdownExpanded = false }
                        ) {
                            state.unlockedModelsForSelected.forEach { descriptor ->
                                DropdownMenuItem(
                                    text = { Text("${descriptor.displayName} (${descriptor.modelId})") },
                                    onClick = {
                                        modelDropdownExpanded = false
                                        onSelectModel(descriptor.modelId)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (state.isConnectionVerifiedForSelected) {
                            "Verified models unlocked for ${state.selectedPrimaryEngine.shortBadgeLabel}."
                        } else {
                            "Test connection to unlock available models."
                        },
                        fontSize = 13.sp,
                        color = Color(0xFF49454F)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom-right Safe Diagnostics Bug Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onOpenSafeDiagnostics) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Open Safe AI Diagnostics",
                                tint = Color(0xFF79747E)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderStatusChainRow(
    readinessMap: Map<AiEngineProvider, ProviderReadinessState>
) {
    val displayOrder = listOf(
        AiEngineProvider.BYOK_GEMINI,
        AiEngineProvider.ON_DEVICE_GEMINI_NANO,
        AiEngineProvider.FIREBASE_AI_LOGIC,
        AiEngineProvider.GEMMA_4_CLOUD_FREE
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        displayOrder.forEachIndexed { index, provider ->
            val readiness = readinessMap[provider] ?: ProviderReadinessState.UNCONFIGURED
            val isReady = readiness == ProviderReadinessState.READY
            val dotColor = when (readiness) {
                ProviderReadinessState.READY -> Color(0xFF2E8B57)
                ProviderReadinessState.CHECKING,
                ProviderReadinessState.DEGRADED,
                ProviderReadinessState.QUOTA_LIMITED -> Color(0xFFF57C00)
                ProviderReadinessState.UNCONFIGURED,
                ProviderReadinessState.OFFLINE,
                ProviderReadinessState.UNSUPPORTED -> Color(0xFFD93025)
            }

            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = provider.shortBadgeLabel,
                fontSize = 13.sp,
                fontWeight = if (isReady) FontWeight.Bold else FontWeight.Normal,
                color = if (isReady) Color(0xFF1C1B1F) else Color(0xFF79747E)
            )

            if (index < displayOrder.lastIndex) {
                Text(
                    text = "  →  ",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}
```

---

## 8. Room 2.8.5 + SQLCipher Persistence Schema & Retention Policy

### 8.1 Core Room Entities (`:core:database`)

```kotlin
package com.example.finengine.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "financial_documents",
    indices = [Index(value = ["sourceSha256"], unique = true)]
)
data class FinancialDocumentEntity(
    @PrimaryKey val documentId: String,
    val documentFamily: String, // RECEIPT, INVOICE_OR_UTILITY_BILL, BANK_STATEMENT, etc.
    val sourceType: String,     // CAMERA_SCAN, GALLERY_IMAGE, PDF_NATIVE_TEXT, PDF_RASTERIZED
    val sourceSha256: String,
    val pageCount: Int,
    val primaryMerchant: String?,
    val issuerAddress: String?,
    val primaryDateIso: String?,
    val totalAmountMinor: Long?,
    val currencyCode: String?,
    val currencyScale: Int?,
    val processingStatus: String, // PROCESSING, NEEDS_REVIEW, VERIFIED, PURGED_RAW_EVIDENCE
    val createdAtEpochMs: Long,
    val verifiedAtEpochMs: Long?
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
    @PrimaryKey val pageId: String, // "{documentId}_P{pageNumber}"
    val documentId: String,
    val pageNumber: Int,
    val rawImageEncryptedPath: String?,
    val cleanOcrImageEncryptedPath: String?,
    val widthPx: Int,
    val heightPx: Int,
    /**
     * Comma-separated 9 floats representing the 3x3 homography matrix M_{ocr -> display}.
     */
    val ocrToDisplayHomographyCsv: String,
    val dominantHeaderHex: String?,
    val dominantBackgroundHex: String?,
    val meanOcrConfidence: Float
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
    indices = [Index("pageId"), Index(value = ["documentId", "scopedElementId"], unique = true)]
)
data class OcrElementEntity(
    @PrimaryKey val globalId: String, // "{documentId}_{scopedElementId}"
    val documentId: String,
    val pageId: String,
    val scopedElementId: String,      // "P01_T0014"
    val lineId: String,               // "P01_L0009"
    val rawText: String,
    val normalizedText: String,
    val normX: Float,
    val normY: Float,
    val normWidth: Float,
    val normHeight: Float,
    val baselineAngleDeg: Float,
    val region: String,
    val confidence: Float
)

/**
 * Stores canonical financial events separated by semantic kind so unpaid bills
 * and statement balances never pollute spending totals.
 */
@Entity(
    tableName = "financial_events",
    foreignKeys = [
        ForeignKey(
            entity = FinancialDocumentEntity::class,
            parentColumns = ["documentId"],
            childColumns = ["sourceDocumentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("sourceDocumentId"),
        Index("softDuplicateFingerprint"),
        Index("eventKind"),
        Index("eventDateIso")
    ]
)
data class FinancialEventEntity(
    @PrimaryKey val eventId: String,
    val sourceDocumentId: String?,
    val eventKind: String, // PURCHASE_EXPENSE, UNPAID_BILL_LIABILITY, PAID_BILL_EXPENSE, INTERNAL_OR_CC_TRANSFER, REFUND_OR_CREDIT
    val merchantOrPayee: String,
    val description: String,
    val eventDateIso: String,
    val dueDateIso: String?,
    val amountMinor: Long,
    val currencyCode: String,
    val currencyScale: Int,
    val category: String,
    val accountIdentifierHash: String?,
    val liabilityPaymentStatus: String?, // NULL, UNPAID, SCHEDULED, PAID
    val linkedTransferEventId: String?,  // Links bank CC autopay debit to CC statement payment credit
    val evidenceIdsCsv: String,          // "P01_T0014,P01_C0022"
    val softDuplicateFingerprint: String,
    val isUserVerified: Boolean
)

/**
 * Safe-by-default diagnostics record.
 * Raw prompt/response JSON is NEVER stored unless temporary debug capture is explicitly active.
 */
@Entity(
    tableName = "interpretation_runs",
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
data class InterpretationRunEntity(
    @PrimaryKey val runId: String,
    val documentId: String,
    val selectedPrimaryProvider: String,
    val executedProvider: String, // DETERMINISTIC_GATE, GEMMA_4_CLOUD_FREE, FIREBASE_AI_LOGIC, BYOK_GEMINI, ON_DEVICE_GEMINI_NANO, DETERMINISTIC_FALLBACK
    val modelId: String?,
    val fallbackReasonCode: String?,
    val payloadSizeBytes: Int,
    val estimatedInputTokens: Int,
    val latencyMs: Long,
    val validationPassedStrictly: Boolean,
    val validationViolationCodesCsv: String?,
    val arithmeticReconciled: Boolean,
    val timestampEpochMs: Long,
    val redactedDebugPayloadJson: String? = null, // Null in routine production runs
    val redactedDebugExpiresAtEpochMs: Long? = null
)
```

### 8.2 Evidence Retention & Cascading Deletion Policy

1. **Automated Raw Evidence Purge (`EvidenceRetentionWorker`):**
   * Once a document reaches `processingStatus = VERIFIED` and 30 days have elapsed (configurable in settings: `Immediately after verification`, `30 days`, `90 days`, `Keep locally until deleted`), the worker deletes `raw_display_page_{n}.enc`, `clean_ocr_page_{n}.enc`, and bulk `OcrElementEntity` rows while preserving the verified `FinancialEventEntity` records and compact SHA-256 provenance hashes.
2. **Debug Capture Auto-Expiry:**
   * Runs every 6 hours to set `redactedDebugPayloadJson = null` on any `InterpretationRunEntity` where `redactedDebugExpiresAtEpochMs < System.currentTimeMillis()`.
3. **User Document Deletion Choice:**
   * Deleting a `FinancialDocumentEntity` prompts the user: *"Delete source document only (keep recorded transactions) or delete both document and derived transactions?"* (`ForeignKey.SET_NULL` allows keeping verified `FinancialEventEntity` rows when purging the source scan).

---

## 9. Verification Matrix & Phased Execution Plan

### 9.1 Two-Tier Acceptance Testing Strategy

#### Tier 1: Exact Invariant Unit Tests (100% Pass Requirement)
These deterministic unit tests must pass with zero tolerance for error:
1. **Multi-Currency Scale Math:** Verify `CurrencyAmount` parsing, serialization, and reconciliation across 0-decimal (`JPY`, `KRW`), 2-decimal (`USD`, `EUR`), and 3-decimal (`BHD`, `KWD`, `OMR`) currencies without floating-point drift.
2. **Homography Coordinate Inversion:** Given synthetic perspective warp matrices \(M_{\text{raw}\rightarrow\text{ocr}}\) (15° rotation + trapezoidal skew), verify that projecting normalized OCR boxes through \(M_{\text{ocr}\rightarrow\text{raw}}\) recovers the exact original pixel coordinates within ≤ 0.5 px.
3. **Validator Rejection Rules:** Verify `PostLlmSemanticValidator` deterministically rejects:
   * Unscoped evidence IDs (`"T014"` instead of `"P01_T0014"`).
   * Hallucinated amounts not matching any candidate or cell `CurrencyAmount`.
   * Role conflation (e.g., mapping a `DUE_DATE` candidate to `primaryDate` or mapping a header phone/store number to `documentTotal`).
   * `STATEMENT_BALANCE_IGNORE` events attempting to persist as expenses.
4. **Financial Domain Event Separation:** Verify that an unpaid utility bill creates a `LiabilityBillRecord` (`UNPAID`) rather than an `ExpenseTransaction`, and a credit-card statement with $120 in purchases and a $120 balance due records exactly $120 in purchase expenses plus a $120 `LiabilityBillRecord`—never $240 in spending.

#### Tier 2: Corpus Benchmarks & Live Provider Contract Tests (Measured Targets)
Evaluate on a curated local corpus of 150 real/consented documents (curled thermal grocery receipts, multi-page bank statements, utility bills, tilted low-light captures):
1. **Curled Baseline Row Grouping:** ≥ 96% line-item description-to-price pairing accuracy on curled receipts using the \(\tan(\theta)\) vector-ray projection (compared against naive horizontal Y-overlap).
2. **Deterministic Gate Precision:** Documents passing `DeterministicConfidenceGate` must achieve ≥ 99.0% field accuracy on total amount, currency, and date without calling an LLM.
3. **Live Provider Contract Suite:** Integration tests against:
   * Hosted Gemma proxy (`gemma-4-31b-it` and `gemma-4-26b-a4b-it`) verifying markdown-fence stripping, untrusted JSON parsing, timeout handling, and HTTP 429 quota fallback.
   * Firebase AI Logic (`gemini-2.5-flash`) with App Check debug token enforcement.
   * `BundleTrimmer` chunking a 120-row multi-page bank statement into 8 bounded chunks (≤ 12 KB each) and reconciling the aggregate opening/closing balance locally.

### 9.2 Phased Implementation Order for the AI Coding Agent

1. **Phase 1 — Foundation, Security & Database (`:core:security`, `:core:database`, `:core:finance:model`):** Implement `CurrencyAmount`, `TinkCredentialVault`, SQLCipher passphrase management, Room 2.8.5 entities/DAOs, and `EvidenceRetentionWorker`. Run Tier 1 currency and persistence tests.
2. **Phase 2 — Vision, Homography & OCR (`:core:ocr`, `:core:document:vision`, `:core:document:layout`):** Implement native PDF text-layer fast path, dual-bitmap preprocessing, 3 × 3 homography inversion, CIELAB color analysis, and \(\tan(\theta)\) vector-ray row reconstruction.
3. **Phase 3 — Deterministic Finance Engine & Domain Events (`:core:finance:extract`, `:core:finance:domain`):** Implement locale-aware candidate extractors, arithmetic reconciliation, `DeterministicConfidenceGate`, `BundleTrimmer`, `PostLlmSemanticValidator`, and `FinancialEvent` separation.
4. **Phase 4 — Policy-Driven AI Router & Provider Adapters (`:core:ai`):** Implement `ModelCapabilityRegistry`, `PolicyDrivenDocumentRouter`, `HostedGemmaProxyAdapter` (`gemma-4-31b-it` / `gemma-4-26b-a4b-it`), `FirebaseAiGeminiAdapter` (with App Check), `ByokGeminiAdapter`, and `GeminiNanoOnDeviceAdapter`.
5. **Phase 5 — Compose UI Screens (`:feature:aiconfig`, `:feature:documentcapture`, `:feature:review`):** Implement `AiConfigurationCard`, `CloudConsentDialog`, `SafeDiagnosticsBottomSheet`, and the homography-aligned bounding-box review canvas.
