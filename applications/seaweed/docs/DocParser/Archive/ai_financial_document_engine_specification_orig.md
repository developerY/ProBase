# Master Engineering Specification: Hybrid Local-First AI Financial Document Engine for Android

## 0. Instructions for the AI Coding Agent
You are tasked with implementing a production-grade, privacy-first Android financial document intelligence and transaction extraction system in **Kotlin** using **Jetpack Compose (Material 3)**, **Coroutines/Flow**, **Hilt**, **Room (with SQLCipher)**, and **Google ML Kit**.

### Core Architectural Laws (Non-Negotiable)
1. **No Raw Images Sent to LLMs:** All image preprocessing, OCR, spatial layout reconstruction, color analysis, and arithmetic validation MUST happen locally on the device prior to any LLM call. The LLM only ever receives a compact, text-based `LlmAnalysisBundle` JSON payload.
2. **Integer Minor Units Only:** Never use `Float` or `Double` for currency or financial amounts anywhere in Kotlin code, JSON payloads, or SQLite schemas. Always represent monetary values in minor units (`Long`, e.g., `$20.07` is stored as `2007L`).
3. **Strict Evidence Provenance:** Every extracted field (merchant, date, total, line item, tax) must reference one or more `evidence_ids` (e.g., `["T014"]`) pointing to exact OCR bounding boxes on the document page. The LLM is forbidden from inventing numbers that do not map to an `evidence_id`.
4. **Multi-Provider AI Routing & Deterministic Fallback:** The AI layer sits behind a unified `DocumentInterpreter` interface supporting four configurable providers (`ON_DEVICE_GEMINI_NANO`, `BYOK`, `FIREBASE_AI_LOGIC`, `GEMMA_4_CLOUD_FREE`) with an automatic health-checked fallback chain:
   $$\text{BYOK} \rightarrow \text{Local AI} \rightarrow \text{Firebase} \rightarrow \text{Gemma} \rightarrow \text{Deterministic Fallback}$$

---

## 1. Gradle Module Hierarchy & Dependencies

Create the following modular structure in `settings.gradle.kts`:

* `:feature:documentcapture` — ML Kit Document Scanner, CameraX fallback, Gallery picker, and native PDF import.
* `:feature:aiconfig` — The `AI Configuration` Compose UI card, provider health state, connection tester, model selector, and diagnostics inspector.
* `:feature:review` — Side-by-side interactive document viewer with bounding-box overlays and field correction UI.
* `:core:ocr` — Native PDF text-layer fast-path extractor and ML Kit Text Recognition v2 wrapper.
* `:core:document:vision` — Dual-bitmap management, CIELAB perceptual color extraction, blur/glare metrics.
* `:core:document:layout` — Polynomial baseline row clustering (for curled receipts), column detection, table reconstruction, and reading-order sorting.
* `:core:finance:extract` — Deterministic regex parsers, locale-aware date/currency extractors, entity candidate scoring.
* `:core:finance:validation` — Integer-cent arithmetic reconciliation, duplicate transaction fingerprinting, and domain guardrails.
* `:core:ai` — `DocumentInterpreter` router, provider adapters (Gemini Nano via AICore, BYOK, Firebase AI Logic, Gemma 4 Cloud), prompt builder, and output schema validator.
* `:core:database` — Encrypted Room database (SQLCipher + Android Keystore), DAOs, and provenance entities.

### Key Library Dependencies (`libs.versions.toml`)
* **ML Kit Document Scanner:** `com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1`
* **ML Kit Text Recognition v2:** `com.google.mlkit:text-recognition:16.0.1`
* **Gemini Nano (On-Device AICore):** `com.google.ai.edge.aicore:aicore:0.0.1-exp01` (or ML Kit GenAI Prompt API)
* **Firebase AI Logic (Cloud):** `com.google.firebase:firebase-ai`
* **Serialization & Networking:** `org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3`, `com.squareup.okhttp3:okhttp:4.12.0`
* **Database & Security:** `androidx.room:room-ktx:2.6.1`, `net.zetetic:android-database-sqlcipher:4.5.4`, `androidx.security:security-crypto:1.1.0-alpha06`

---

## 2. End-to-End Processing Pipeline

Every document imported or scanned passes through this exact 7-stage pipeline:

1. **Stage 1: Local Capture & Native PDF Fast-Path (`:feature:documentcapture` & `:core:ocr`)**
   * Check if input is a PDF with an embedded digital text layer. If yes, extract characters and normalized bounding boxes directly via PDF parser without rasterizing to a Bitmap for OCR.
   * If camera/gallery image or scanned PDF: invoke ML Kit Document Scanner. Retain **two** local bitmaps in app-private encrypted storage:
     * `rawBitmap`: Preserved for perceptual color measurement and user review UI.
     * `cleanedBitmap`: Grayscale/contrast-normalized and deskewed for OCR.
2. **Stage 2: Visual Feature & Color Extraction (`:core:document:vision`)**
   * Compute image dimensions, blur variance (Laplacian variance), brightness, glare ratio, and CIELAB dominant colors across `header`, `body`, `footer`, and `logo` regions.
3. **Stage 3: Spatial OCR Extraction (`:core:ocr`)**
   * Run ML Kit Text Recognition v2 on `cleanedBitmap`. Extract blocks, lines, elements, symbols, confidence scores, rotation angles, and pixel bounding boxes. Normalize all coordinates to $[0.0, 1.0]$.
4. **Stage 4: Geometric Layout & Table Reconstruction (`:core:document:layout`)**
   * Group elements into lines using rotation-aware vector projection (handling curved thermal receipts), detect vertical column alignments, reconstruct table rows/cells, and classify regions (`header`, `upper_body`, `lower_body`, `footer`).
5. **Stage 5: Deterministic Financial Extraction & Arithmetic Validation (`:core:finance:extract` & `:core:finance:validation`)**
   * Extract candidate merchants, addresses (`issuer_address`, `customer_address`, `remittance_address`), dates, and amounts (in integer minor units).
   * Run deterministic arithmetic checks (e.g., $\text{subtotal} + \text{tax} + \text{tip} - \text{discount} = \text{total}$).
6. **Stage 6: Multi-Provider AI Semantic Interpretation (`:core:ai`)**
   * Construct the compact `LlmAnalysisBundle`. Route through the active provider configured in `AI Configuration` (or traverse the live fallback chain: $\text{BYOK} \rightarrow \text{Local AI} \rightarrow \text{Firebase} \rightarrow \text{Gemma} \rightarrow \text{Deterministic}$).
7. **Stage 7: Output Validation, Guardrail Enforcement & Persistence (`:core:database` & `:feature:review`)**
   * Verify all returned `evidence_ids` exist and match authoritative candidate amounts. Apply financial guardrails (reject statement balances as expenses, flag transfers). Persist to Room or route to the interactive Review UI.

---

## 3. Detailed Algorithms for Local Vision & Layout

### 3.1 Normalized Bounding Box Math
For an image of width $W$ and height $H$ (where $W > 0, H > 0$), convert every pixel bounding box $(x_{\text{px}}, y_{\text{px}}, w_{\text{px}}, h_{\text{px}})$ into normalized coordinates:

$$x_{\text{norm}} = \frac{x_{\text{px}}}{W}, \quad y_{\text{norm}} = \frac{y_{\text{px}}}{H}, \quad w_{\text{norm}} = \frac{w_{\text{px}}}{W}, \quad h_{\text{norm}} = \frac{h_{\text{px}}}{H}$$

Assign each element to a vertical region based on its vertical center $y_c = y_{\text{norm}} + \frac{h_{\text{norm}}}{2}$:
* `header`: $y_c \le 0.22$
* `upper_body`: $0.22 < y_c \le 0.60$
* `lower_body`: $0.60 < y_c \le 0.82$
* `footer`: $y_c > 0.82$

### 3.2 Rotation-Aware Line Grouping (Curved Receipt Compensation)
Standard $Y$-axis bounding-box overlap fails when a thermal receipt curls horizontally. Use the element angle $\theta$ (in radians, provided by ML Kit `Text.Line.angle`) to group elements $A$ (left) and $B$ (right) into the same logical row if:
1. $x_c(B) > x_c(A)$
2. The projected vertical center of $A$ at $x_c(B)$:
   $$y_{\text{proj}} = y_c(A) + (x_c(B) - x_c(A)) \cdot \tan(\theta_A)$$
   satisfies:
   $$|y_{\text{proj}} - y_c(B)| \le 0.65 \cdot \max(h_{\text{norm}}(A), h_{\text{norm}}(B))$$

### 3.3 Perceptual Color Extraction (CIELAB)
Sample pixels in `rawBitmap` across the `header` ($y_c \le 0.22$) and full page background. Convert RGB to CIELAB $(L^*, a^*, b^*)$. Compare extracted header dominant colors against stored `MerchantRule` templates using perceptual color distance:

$$\Delta E_{ab}^* = \sqrt{(L_2^* - L_1^*)^2 + (a_2^* - a_1^*)^2 + (b_2^* - b_1^*)^2}$$

A color match ($\Delta E_{ab}^* < 12.0$) boosts the candidate merchant confidence score by $+0.10$ when combined with layout or text similarity, but **never** identifies a merchant on its own.

### 3.4 Context-Aware OCR Character Disambiguation
Preserve raw `ocr_text`. Generate `normalized_text` using context-sensitive rules:
* Inside numeric currency/amount regex contexts (`$`, decimal points, right-aligned amount columns): map `O`/`o` $\rightarrow$ `0`, `I`/`l` $\rightarrow$ `1`, `S` $\rightarrow$ `5`, `B` $\rightarrow$ `8`.
* Inside alpha contexts (merchant headers, descriptions): preserve original characters.

---

## 4. Data Contracts & JSON Schemas (`:core:finance` & `:core:ai`)

Define all contracts as `@Serializable` Kotlin data classes using `kotlinx.serialization`.

```kotlin
package com.app.finance.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NormalizedBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

@Serializable
data class PixelBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

@Serializable
data class OcrElementRecord(
    val id: String, // e.g., "T001"
    val page: Int,
    @SerialName("line_id") val lineId: String,
    @SerialName("raw_text") val rawText: String,
    @SerialName("normalized_text") val normalizedText: String,
    @SerialName("box_px") val boxPx: PixelBox,
    @SerialName("box_normalized") val boxNormalized: NormalizedBox,
    val region: String, // "header", "upper_body", "lower_body", "footer"
    val column: String, // "left", "center", "right", "col_0", "col_1", etc.
    @SerialName("rotation_deg") val rotationDeg: Float,
    @SerialName("ocr_confidence") val ocrConfidence: Float
)

@Serializable
data class DominantColorRecord(
    val hex: String,
    @SerialName("lab_l") val labL: Float,
    @SerialName("lab_a") val labA: Float,
    @SerialName("lab_b") val labB: Float,
    val proportion: Float,
    val role: String // "background", "header", "logo", "ink"
)

@Serializable
data class ReconstructedTableCell(
    @SerialName("column_index") val columnIndex: Int,
    @SerialName("column_role") val columnRole: String, // "date", "description", "qty", "unit_price", "debit", "credit", "balance", "line_total"
    val text: String,
    @SerialName("amount_minor") val amountMinor: Long? = null,
    @SerialName("evidence_ids") val evidenceIds: List<String>
)

@Serializable
data class ReconstructedTableRow(
    @SerialName("row_index") val rowIndex: Int,
    val cells: List<ReconstructedTableCell>,
    @SerialName("is_ambiguous") val isAmbiguous: Boolean = false
)

@Serializable
data class ReconstructedTable(
    val id: String,
    val kind: String, // "receipt_line_items", "bank_statement_transactions", "invoice_summary"
    val headers: List<String>,
    val rows: List<ReconstructedTableRow>
)

@Serializable
data class TextCandidate(
    val value: String,
    val role: String, // "merchant", "issuer_address", "customer_address", "remittance_address", "phone", "website"
    @SerialName("evidence_ids") val evidenceIds: List<String>,
    val confidence: Float
)

@Serializable
data class AmountCandidate(
    @SerialName("amount_minor") val amountMinor: Long, // e.g., 2007 for $20.07
    val currency: String, // ISO 4217 e.g., "USD"
    @SerialName("candidate_role") val candidateRole: String, // "subtotal", "tax", "tip", "discount", "total", "amount_due", "opening_balance", "closing_balance"
    @SerialName("evidence_ids") val evidenceIds: List<String>,
    val confidence: Float
)

@Serializable
data class DateCandidate(
    @SerialName("iso_date") val isoDate: String, // YYYY-MM-DD
    @SerialName("candidate_role") val candidateRole: String, // "purchase_date", "posting_date", "due_date", "statement_start", "statement_end"
    @SerialName("evidence_ids") val evidenceIds: List<String>,
    val confidence: Float
)

@Serializable
data class ArithmeticValidationSummary(
    @SerialName("reconciliation_passed") val reconciliationPassed: Boolean,
    @SerialName("check_type") val checkType: String, // "receipt_sum", "statement_balance", "none"
    @SerialName("expected_total_minor") val expectedTotalMinor: Long? = null,
    @SerialName("observed_total_minor") val observedTotalMinor: Long? = null,
    @SerialName("discrepancy_minor") val discrepancyMinor: Long = 0L,
    val notes: String? = null
)

/**
 * Compact payload sent to the LLM. Never includes raw pixel coordinates or image bytes.
 * For multi-page bank statements, chunk tables into max 15 rows per LLM request.
 */
@Serializable
data class LlmAnalysisBundle(
    @SerialName("document_id") val documentId: String,
    @SerialName("doc_type_candidates") val docTypeCandidates: List<String>,
    @SerialName("text_candidates") val textCandidates: List<TextCandidate>,
    @SerialName("date_candidates") val dateCandidates: List<DateCandidate>,
    @SerialName("amount_candidates") val amountCandidates: List<AmountCandidate>,
    val tables: List<ReconstructedTable>,
    @SerialName("arithmetic_validation") val arithmeticValidation: ArithmeticValidationSummary,
    @SerialName("compact_ocr_lines") val compactOcrLines: List<CompactOcrLine>,
    @SerialName("allowed_categories") val allowedCategories: List<String>
)

@Serializable
data class CompactOcrLine(
    @SerialName("line_id") val lineId: String,
    val region: String,
    val text: String,
    @SerialName("evidence_ids") val evidenceIds: List<String>
)
```

### 4.1 LLM Output Contract (`DocumentInterpretation`)
The LLM (and the deterministic fallback adapter) MUST return this exact flattened structure:

```kotlin
@Serializable
data class DocumentInterpretation(
    @SerialName("document_type") val documentType: String, // "receipt", "invoice", "utility_bill", "bank_statement", "credit_card_statement", "transfer_confirmation", "unknown"
    val merchant: ResolvedStringField? = null,
    @SerialName("issuer_address") val issuerAddress: ResolvedStringField? = null,
    @SerialName("customer_address") val customerAddress: ResolvedStringField? = null,
    @SerialName("remittance_address") val remittanceAddress: ResolvedStringField? = null,
    val transactions: List<ResolvedTransaction>,
    @SerialName("review_required") val reviewRequired: Boolean,
    @SerialName("unresolved_fields") val unresolvedFields: List<String> = emptyList()
)

@Serializable
data class ResolvedStringField(
    val value: String,
    @SerialName("evidence_ids") val evidenceIds: List<String>
)

@Serializable
data class ResolvedAmountField(
    @SerialName("amount_minor") val amountMinor: Long,
    val currency: String,
    @SerialName("evidence_ids") val evidenceIds: List<String>
)

@Serializable
data class ResolvedTransaction(
    @SerialName("transaction_kind") val transactionKind: String, // "purchase", "bill_payment", "internal_transfer", "refund_credit", "fee", "statement_balance_ignore"
    @SerialName("transaction_date") val transactionDate: ResolvedStringField? = null,
    @SerialName("posting_date") val postingDate: ResolvedStringField? = null,
    @SerialName("due_date") val dueDate: ResolvedStringField? = null,
    val description: ResolvedStringField? = null,
    val subtotal: ResolvedAmountField? = null,
    val tax: ResolvedAmountField? = null,
    val tip: ResolvedAmountField? = null,
    val total: ResolvedAmountField,
    val category: String,
    @SerialName("category_source") val categorySource: String, // "merchant_rule", "model_suggestion", "deterministic_default"
    @SerialName("line_items") val lineItems: List<ResolvedLineItem> = emptyList()
)

@Serializable
data class ResolvedLineItem(
    val description: String,
    val quantity: Int = 1,
    @SerialName("unit_price_minor") val unitPriceMinor: Long? = null,
    @SerialName("total_minor") val totalMinor: Long,
    @SerialName("evidence_ids") val evidenceIds: List<String>
)
```

---

## 5. Multi-Provider AI Architecture & "AI Configuration" UI Specification (`:core:ai` & `:feature:aiconfig`)

This section specifies the exact behavior and UI shown in the **AI Configuration** settings screen.

### 5.1 Provider Enum & Fallback Chain Logic
The system supports 4 AI engines plus a built-in deterministic fallback:

```kotlin
package com.app.finance.core.ai

enum class AiEngineProvider(
    val id: String,
    val displayName: String,
    val shortBadgeName: String
) {
    GEMMA_4_CLOUD_FREE(
        id = "gemma_4_cloud_free",
        displayName = "Gemma 4 Cloud (Free Tier) - Default",
        shortBadgeName = "Gemma"
    ),
    FIREBASE_AI_LOGIC(
        id = "firebase_ai_logic",
        displayName = "Firebase AI Logic (Cloud)",
        shortBadgeName = "Firebase"
    ),
    BYOK(
        id = "byok",
        displayName = "Bring Your Own Key (BYOK)",
        shortBadgeName = "BYOK"
    ),
    ON_DEVICE_GEMINI_NANO(
        id = "on_device_gemini_nano",
        displayName = "On-Device Local AI (Gemini Nano)",
        shortBadgeName = "Local AI"
    )
}

enum class ProviderHealthStatus {
    READY,       // Renders Green Dot (#2E7D32)
    UNAVAILABLE, // Renders Red Dot (#D32F2F)
    TESTING      // Renders Amber pulsing dot
}
```

#### The Fallback Status Header Chain
At the top of the **AI Configuration** card, directly under the title, display the live fallback status pipeline in this exact visual order:

$$\text{BYOK} \rightarrow \text{Local AI} \rightarrow \text{Firebase} \rightarrow \text{Gemma}$$

* **Status Dot Color Rules:**
  * `BYOK`: **Green** (`READY`) if a user API key is stored in `EncryptedSharedPreferences` AND has passed connection verification; **Red** (`UNAVAILABLE`) if no key is saved or verification failed.
  * `Local AI` (`ON_DEVICE_GEMINI_NANO`): **Green** (`READY`) if Android AICore reports Gemini Nano is supported and downloaded on the device; **Red** (`UNAVAILABLE`) if device hardware/AICore does not support it or model is not downloaded.
  * `Firebase` (`FIREBASE_AI_LOGIC`): **Green** (`READY`) if Firebase AI Logic is initialized and network is reachable; **Red** (`UNAVAILABLE`) if offline or unconfigured.
  * `Gemma` (`GEMMA_4_CLOUD_FREE`): **Green** (`READY`) if the free-tier Gemma 4 Cloud endpoint is reachable and within rate limits; **Red** (`UNAVAILABLE`) if offline or quota exhausted.
* **Text Styling in the Chain:**
  * Providers with `READY` (green dot) render their label in **Bold Dark Text** (`#1D1B20`).
  * Providers with `UNAVAILABLE` (red dot) render their label in **Muted Gray Text** (`#79747E`).

#### Runtime Routing Policy (`AiRouterRepository`)
1. Before invoking any AI provider, check if `DeterministicRuleEngine.canResolveWithHighConfidence(bundle)` is `true` (e.g., known recurring merchant template + exact arithmetic reconciliation pass). If so, skip LLM invocation completely (`0ms` AI latency).
2. Otherwise, attempt execution starting with the user's selected `Primary AI Engine`.
3. If the selected `Primary AI Engine` is currently `UNAVAILABLE` or throws a runtime exception / timeout (`> 12,000ms`) / schema validation failure:
   * **Privacy Guardrail for Local AI Selection:** Include a user preference `allowCloudFallbackWhenLocalSelected` (default `true` when cloud providers are green in the fallback chain, or configurable by the user). Follow the fallback order across healthy (`READY`) providers:
     $$\text{BYOK} \rightarrow \text{Local AI} \rightarrow \text{Firebase} \rightarrow \text{Gemma} \rightarrow \text{Deterministic Fallback}$$
4. Every execution attempt (and any fallback hop) is logged to the `InterpretationRun` table and viewable via the bottom-right **Bug / Diagnostics Icon**.

### 5.2 Connection Testing & Dynamic Model Discovery
* When the user selects `Bring Your Own Key (BYOK)` (or changes the `Primary AI Engine`), they can enter an API key (stored in `EncryptedSharedPreferences`) and trigger **Test Connection**.
* **Before Connection Test Succeeds:**
  * The **Preferred AI Model** (`Gemini Model`) field displays `"Test connection to see models"` and is locked/disabled with helper text `"Test connection to unlock available models."`
* **After Connection Test Succeeds:**
  * For `BYOK` / `FIREBASE_AI_LOGIC`: Query the `models.list` endpoint (or supported model registry), filter for structured-output-capable models (e.g., `gemini-2.5-flash`, `gemini-2.5-pro`, `gemma-4-27b-it`), unlock the `Preferred AI Model` dropdown, and auto-select the recommended default model.
  * For `ON_DEVICE_GEMINI_NANO`: Verify AICore availability and populate `"Gemini Nano (AICore On-Device)"`.
  * For `GEMMA_4_CLOUD_FREE`: Verify endpoint health and populate `"Gemma 4 IT (Free Tier)"`.

### 5.3 Jetpack Compose Implementation (`AiConfigurationCard.kt`)
Implement the exact UI from the design specification in `:feature:aiconfig`:

```kotlin
package com.app.finance.feature.aiconfig

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
import com.app.finance.core.ai.AiEngineProvider
import com.app.finance.core.ai.ProviderHealthStatus

data class AiConfigUiState(
    val isExpanded: Boolean = true,
    val selectedEngine: AiEngineProvider = AiEngineProvider.ON_DEVICE_GEMINI_NANO,
    val providerStatusMap: Map<AiEngineProvider, ProviderHealthStatus> = mapOf(
        AiEngineProvider.BYOK to ProviderHealthStatus.UNAVAILABLE,
        AiEngineProvider.ON_DEVICE_GEMINI_NANO to ProviderHealthStatus.UNAVAILABLE,
        AiEngineProvider.FIREBASE_AI_LOGIC to ProviderHealthStatus.READY,
        AiEngineProvider.GEMMA_4_CLOUD_FREE to ProviderHealthStatus.READY
    ),
    val apiKeyInput: String = "",
    val isConnectionTested: Boolean = false,
    val isTestingConnection: Boolean = false,
    val availableModels: List<String> = emptyList(),
    val selectedModel: String? = null,
    val statusMessage: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConfigurationCard(
    state: AiConfigUiState,
    onToggleExpand: () -> Unit,
    onSelectEngine: (AiEngineProvider) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onTestConnection: () -> Unit,
    onSelectModel: (String) -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackground = Color(0xFFEDE7F6) // Soft lavender surface matching design
    val primaryPurple = Color(0xFF5E35B1)
    val greenReady = Color(0xFF2E7D32)
    val redUnavailable = Color(0xFFD32F2F)

    var engineDropdownExpanded by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Header Row: Sparkle Icon + Title + Fallback Status Chain + Collapse Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Configuration",
                    tint = Color(0xFFE5A93C),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Configuration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = Color(0xFF1D1B20)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FallbackChainIndicatorRow(
                        statusMap = state.providerStatusMap,
                        greenReady = greenReady,
                        redUnavailable = redUnavailable
                    )
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (state.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand or Collapse",
                        tint = Color(0xFF1D1B20)
                    )
                }
            }

            AnimatedVisibility(visible = state.isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Section 1: Primary AI Engine
                    Text(
                        text = "Primary AI Engine",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = primaryPurple,
                            fontWeight = FontWeight.Bold
                        )
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
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryPurple,
                                unfocusedBorderColor = primaryPurple,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = engineDropdownExpanded,
                            onDismissRequest = { engineDropdownExpanded = false },
                            modifier = Modifier.background(Color(0xFFF7F2FA))
                        ) {
                            AiEngineProvider.entries.forEach { provider ->
                                val isSelected = provider == state.selectedEngine
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = provider.displayName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF79747E) else Color(0xFF1D1B20)
                                        )
                                    },
                                    onClick = {
                                        onSelectEngine(provider)
                                        engineDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Optional API Key field (shown when BYOK is selected) + Test Connection Button
                    if (state.selectedEngine == AiEngineProvider.BYOK) {
                        OutlinedTextField(
                            value = state.apiKeyInput,
                            onValueChange = onApiKeyChange,
                            label = { Text("API Key") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = onTestConnection,
                        enabled = !state.isTestingConnection,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (state.isTestingConnection) "Testing Connection..." else "Test Connection"
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Section 2: Preferred AI Model
                    Text(
                        text = "Preferred AI Model",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = primaryPurple,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = modelDropdownExpanded && state.isConnectionTested,
                        onExpandedChange = {
                            if (state.isConnectionTested && state.availableModels.isNotEmpty()) {
                                modelDropdownExpanded = !modelDropdownExpanded
                            }
                        }
                    ) {
                        OutlinedTextField(
                            value = if (state.isConnectionTested && state.selectedModel != null) {
                                state.selectedModel
                            } else {
                                "Test connection to see models"
                            },
                            onValueChange = {},
                            readOnly = true,
                            enabled = state.isConnectionTested,
                            label = { Text("Gemini Model") },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = modelDropdownExpanded && state.isConnectionTested,
                            onDismissRequest = { modelDropdownExpanded = false }
                        ) {
                            state.availableModels.forEach { modelName ->
                                DropdownMenuItem(
                                    text = { Text(modelName) },
                                    onClick = {
                                        onSelectModel(modelName)
                                        modelDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = state.statusMessage ?: "Test connection to unlock available models.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF49454F)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom-Right Bug / Diagnostics Button
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                        IconButton(onClick = onOpenDiagnostics) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = "Open AI Diagnostics & Logs",
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
private fun FallbackChainIndicatorRow(
    statusMap: Map<AiEngineProvider, ProviderHealthStatus>,
    greenReady: Color,
    redUnavailable: Color
) {
    val chainOrder = listOf(
        AiEngineProvider.BYOK,
        AiEngineProvider.ON_DEVICE_GEMINI_NANO,
        AiEngineProvider.FIREBASE_AI_LOGIC,
        AiEngineProvider.GEMMA_4_CLOUD_FREE
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        chainOrder.forEachIndexed { index, provider ->
            val status = statusMap[provider] ?: ProviderHealthStatus.UNAVAILABLE
            val isReady = status == ProviderHealthStatus.READY
            val dotColor = if (isReady) greenReady else redUnavailable

            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = provider.shortBadgeName,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isReady) FontWeight.Bold else FontWeight.Normal,
                    color = if (isReady) Color(0xFF1D1B20) else Color(0xFF79747E),
                    fontSize = 12.sp
                )
            )
            if (index < chainOrder.lastIndex) {
                Text(
                    text = " -> ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF79747E),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
```

---

## 6. Prompt Construction & Output Validation (`:core:ai`)

### 6.1 System Prompt Template
Every LLM adapter (`GeminiNanoAdapter`, `ByokGeminiAdapter`, `FirebaseAiLogicAdapter`, `Gemma4CloudAdapter`) must use this exact system prompt and request `application/json` output:

```text
You are a deterministic-assisted financial document interpretation engine.
Use ONLY the supplied LlmAnalysisBundle JSON containing OCR text lines, spatial layout metadata, candidate fields, and arithmetic validation results.

STRICT RULES:
1. Classify `document_type` ("receipt", "invoice", "utility_bill", "bank_statement", "credit_card_statement", "transfer_confirmation", "unknown").
2. Identify `merchant`, `issuer_address`, `customer_address`, and `remittance_address` with their `evidence_ids`.
3. Distinguish `transaction_kind`:
   - "purchase": actual spending on goods/services.
   - "bill_payment": paying an invoice or credit card bill.
   - "internal_transfer": moving money between owned accounts.
   - "refund_credit": money returned.
   - "statement_balance_ignore": opening/closing/running balances. NEVER classify a statement balance as a purchase!
4. All monetary values MUST use integer minor units (`amount_minor`, e.g., 2007 for $20.07) matching a candidate or table cell in the bundle.
5. Every resolved field MUST include non-empty `evidence_ids` referencing IDs present in the input bundle (e.g., ["T014"]).
6. Do NOT invent missing dates or amounts, and do NOT silently alter source numbers. If a required field is missing or ambiguous, set `review_required = true` and list the field path in `unresolved_fields`.
7. Assign `category` strictly from `allowed_categories`.
8. Return ONLY valid JSON matching the DocumentInterpretation schema.
```

### 6.2 Post-LLM Output Validator (`InterpretationValidator`)
Before accepting any `DocumentInterpretation` from an LLM provider, run these checks in Kotlin:
1. **Evidence Existence Check:** Every ID in `evidence_ids` across all fields must exist in the page's `OcrElementRecord` set. If the LLM hallucinates an ID (e.g., `"T999"`), reject the output.
2. **Authoritative Amount Check:** Every `amount_minor` returned by the LLM must match either a detected `AmountCandidate.amountMinor` or a `ReconstructedTableCell.amountMinor` derived from those exact `evidence_ids`.
3. **Taxonomy Check:** `transaction.category` must be in `bundle.allowedCategories`; otherwise replace with `"Uncategorized"` and set `reviewRequired = true`.
4. **Statement Balance Guardrail:** Any `ResolvedTransaction` with `transactionKind == "statement_balance_ignore"` is stripped prior to creating Room `TransactionEntity` rows.

---

## 7. Encrypted Room Database Schema (`:core:database`)

Initialize Room with **SQLCipher** (`SupportFactory`) using a 256-bit AES key protected by the **Android Keystore**. Set `android:allowBackup="false"` in `AndroidManifest.xml`.

```kotlin
package com.app.finance.core.database.entity

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
    val sourceType: String, // "CAMERA_SCAN", "GALLERY_IMAGE", "PDF_NATIVE", "PDF_SCANNED"
    val sourceSha256: String,
    val documentType: String,
    val issuerName: String?,
    val captureTimestampMs: Long,
    val processingStatus: String, // "CAPTURED", "OCR_COMPLETE", "INTERPRETED", "NEEDS_REVIEW", "VERIFIED"
    val reviewRequired: Boolean
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
    val encryptedImagePath: String,
    val widthPx: Int,
    val heightPx: Int,
    val blurScore: Float,
    val dominantColorsJson: String
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
    indices = [Index("pageId")]
)
data class OcrElementEntity(
    @PrimaryKey val elementUid: String, // "${documentId}_${id}"
    val pageId: String,
    val elementId: String, // "T014"
    val lineId: String,
    val rawText: String,
    val normalizedText: String,
    val normX: Float,
    val normY: Float,
    val normWidth: Float,
    val normHeight: Float,
    val region: String,
    val columnName: String,
    val confidence: Float
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = FinancialDocumentEntity::class,
            parentColumns = ["documentId"],
            childColumns = ["documentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("documentId"), Index("dedupFingerprint")]
)
data class TransactionEntity(
    @PrimaryKey val transactionId: String,
    val documentId: String?,
    val transactionKind: String, // "purchase", "bill_payment", "internal_transfer", "refund_credit"
    val merchantName: String,
    val merchantEvidenceIdsJson: String,
    val isoTransactionDate: String?,
    val isoPostingDate: String?,
    val subtotalMinor: Long?,
    val taxMinor: Long?,
    val tipMinor: Long?,
    val totalAmountMinor: Long, // Stored in integer cents
    val currency: String,
    val totalEvidenceIdsJson: String,
    val category: String,
    val categorySource: String,
    val isUserVerified: Boolean,
    val dedupFingerprint: String // SHA-256(normalizedMerchant + isoDate + totalAmountMinor + currency)
)

@Entity(tableName = "interpretation_runs")
data class InterpretationRunEntity(
    @PrimaryKey val runId: String,
    val documentId: String,
    val timestampMs: Long,
    val configuredPrimaryProvider: String,
    val executedProvider: String, // Which provider actually succeeded in the fallback chain
    val modelName: String,
    val fallbackReason: String?, // Null if primary succeeded
    val ocrDurationMs: Long,
    val layoutDurationMs: Long,
    val llmLatencyMs: Long,
    val reconciliationPassed: Boolean,
    val promptBundleJson: String,
    val rawResponseJson: String?
)
```

---

## 8. Interactive Review UI & Diagnostics Sheet (`:feature:review` & `:feature:aiconfig`)

### 8.1 Side-by-Side / Overlay Evidence Review Screen
1. Render the encrypted `rawBitmap` inside a zoomable/pannable Compose `Canvas`.
2. Draw rounded bounding-box overlays using each field's `evidence_ids` $\rightarrow$ `OcrElementEntity(normX, normY, normWidth, normHeight)` scaled to the canvas size.
3. Color-code bounding boxes:
   * **Green (`#2E7D32`):** High confidence ($\ge 0.90$) and arithmetic reconciliation passed.
   * **Amber (`#EF6C00`):** Medium confidence or unresolved field requiring user confirmation.
   * **Purple (`#5E35B1`):** Currently selected field in the editor form below.
4. When the user taps any OCR bounding box on the image while editing a field (e.g., tapping the correct Total on the receipt), bind that `OcrElementEntity.elementId` and parsed value directly to the active field and save a `MerchantRule` so future captures from the same merchant succeed deterministically.

### 8.2 AI Diagnostics Bottom Sheet (Triggered by the Bug Icon)
When the user taps the bug icon (`Icons.Default.BugReport`) in the bottom-right corner of the **AI Configuration** card, display a `ModalBottomSheet` containing:
1. **Real-Time Provider Health Probe:** Live ping/availability breakdown for `BYOK`, `Local AI (Gemini Nano / AICore version)`, `Firebase AI Logic`, and `Gemma 4 Cloud`.
2. **Recent `InterpretationRunEntity` Logs:** List of recent document runs showing:
   * Selected Primary Engine vs. Actual Executed Engine (highlighting any fallback hops).
   * Stage latencies: `ocrDurationMs`, `layoutDurationMs`, `llmLatencyMs`.
   * Arithmetic Reconciliation result (`PASSED` / `DISCREPANCY`).
   * Expandable JSON viewer for `promptBundleJson` and `rawResponseJson`.

---

## 9. Phased Implementation Plan for the AI Coding Agent

Execute the build in the following sequence, running unit tests after each phase:

* **Phase 1: Core Data Models & Encrypted Database (`:core:finance`, `:core:database`)**
  * Implement `@Serializable` models, Room entities, DAOs, and SQLCipher Keystore initialization.
  * *Verification:* Write JUnit tests verifying integer-cent serialization and Room CRUD operations.
* **Phase 2: Deterministic Layout, Math & Extraction Engine (`:core:document:layout`, `:core:finance:extract`, `:core:finance:validation`)**
  * Implement rotation-aware vector line grouping, column clustering, table reconstruction, regex candidate extractors, and integer-cent arithmetic reconciliation.
  * *Verification:* Feed synthetic skewed receipt and bank statement OCR element fixtures; assert $100\%$ accuracy on row grouping and arithmetic validation.
* **Phase 3: OCR & Vision Pipeline (`:core:ocr`, `:core:document:vision`, `:feature:documentcapture`)**
  * Implement native PDF text-layer fast-path, ML Kit Document Scanner integration, dual-bitmap storage, CIELAB color extraction, and ML Kit Text Recognition v2 normalization.
* **Phase 4: Multi-Provider AI Router & Adapters (`:core:ai`)**
  * Implement `DocumentInterpreter`, the 4 provider adapters (`ON_DEVICE_GEMINI_NANO`, `BYOK`, `FIREBASE_AI_LOGIC`, `GEMMA_4_CLOUD_FREE`), the deterministic fallback adapter, connection tester, model list fetcher, and `InterpretationValidator`.
  * *Verification:* Test automatic fallback across the chain $\text{BYOK} \rightarrow \text{Local AI} \rightarrow \text{Firebase} \rightarrow \text{Gemma} \rightarrow \text{Deterministic}$ when upstream providers throw timeouts or schema errors.
* **Phase 5: AI Configuration UI & Review Screen (`:feature:aiconfig`, `:feature:review`)**
  * Wire up `AiConfigurationCard`, the live fallback status header, dynamic model unlocking, the Diagnostics bottom sheet, and the interactive bounding-box Review screen.