# Master Engineering Specification: Local-Processing-First, Cloud-Assisted AI Financial Document Engine for Android

## 0. Architectural Mandate & Core System Boundaries

You are tasked with implementing a production-grade Android financial document intelligence engine in **Kotlin** using **Jetpack Compose (Material 3)**, **Coroutines/Flow**, **Hilt**, **Room 2.8.5 (with SQLCipher 4.19.1)**, **Jetpack DataStore + Google Tink (`Aead`, `StreamingAead`, and `Mac`)**, **Firebase AI Logic (BoM 35.1.0 with Play Integrity App Check)**, and **Google ML Kit**.

### The Three Explicit System Boundaries

1. **Local-First Document Preparation:** Raw camera images, gallery imports, and PDFs never leave the device. Image quality assessment, perspective/deskew transformations, OCR, perceptual color extraction, spatial table reconstruction, locale-aware candidate extraction, and arithmetic validation always execute locally on the phone.
2. **Cloud-Assisted Semantic Interpretation (with Field-Level & Document-Level Deterministic Gate):** When the deterministic engine resolves the entire document with high confidence, no LLM is invoked. When partial fields (such as a mathematically reconciled total and primary date) are locked deterministically, those fields are marked `DETERMINISTIC_LOCKED` and the LLM is invoked strictly to resolve remaining semantic ambiguities (such as category classification or event kind). The system defaults to **Hosted Gemma (`GEMMA_4_CLOUD_FREE`)** via an App Check–authenticated backend proxy, with **On-Device Gemini Nano** treated as an optional runtime optimization rather than a hardware requirement.
3. **Per-Provider Consent Records, Role-Based Redaction & Safe Diagnostics:** Because a text-only `LlmAnalysisBundle` still contains sensitive financial metadata, the system enforces **per-provider cloud consent with provider-specific disclosure versions and timestamps** (`consentByProvider`), **explicit fallback authorization** (`authorizedFallbackProviders`), a **Role- & Regex-Based PII Redaction Pass** across both candidates and table cells prior to serialization, a hard measured **12,288-byte UTF-8 ceiling** (`BundleTrimResult.BundleCannotFit`), a strict **Local-Only Mode**, and **safe-by-default diagnostics**.

---

## 1. Reproducible Late-2026 Toolchain & Multi-Module Architecture

### 1.1 Gradle Version Catalog (`gradle/libs.versions.toml`) & Toolchain Lock

* **Gradle Wrapper:** `8.14` (`distributionUrl=https\://services.gradle.org/distributions/gradle-8.14-bin.zip`)
* **JDK Toolchain:** Java `21` (`jvmToolchain(21)`)
* **KSP Versioning Note:** KSP uses standalone semantic versioning (`2.3.12`) decoupled from the Kotlin compiler string. Before writing domain code in Phase 1, the coding agent must run `./gradlew check` on the multi-module skeleton to verify dependency resolution.

```toml
[versions]
agp = "8.13.0"
kotlin = "2.4.21"
ksp = "2.3.12"
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

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
room = { id = "androidx.room", version.ref = "room" }
```

*Isolation Mandate for AICore:* The on-device Gemini Nano dependency (`com.google.ai.edge.aicore:aicore`) must be isolated strictly inside `:core:ai:nanoadapter` so experimental SDK changes can never destabilize the primary cloud or deterministic paths.

### 1.2 Gradle Module Topology

| Module | Core Responsibilities |
| :--- | :--- |
| `:feature:documentcapture` | ML Kit Document Scanner (`16.0.0`), gallery picker, PDF import, Tink `StreamingAead` file encryption. |
| `:core:ocr` | Native PDF text-layer extractor (with `/CropBox` & `/Rotate` normalization), ML Kit Text Recognition v2 (`16.0.1`), page-scoped ID assignment (`P01_T0001`). |
| `:core:document:vision` | Blur/glare detection, perspective/deskew homography capture, failure-safe normalized coordinate mapping, Sutherland-Hodgman polygon clipping, CIELAB color extraction. |
| `:core:document:layout` | Radian-converted rotation-aware vector line grouping, 1D k-means column clustering, table reconstruction, header/body/footer zoning. |
| `:core:finance:extract` | Complete ISO 4217 currency registry, strict non-rounding money parser, locale-aware date/amount candidate extraction, entity role scoring. |
| `:core:finance:validation` | Document-family classifier, mutually-exclusive signed-ledger arithmetic reconciliation, hybrid field-level deterministic gate, strict candidate-bound post-LLM validator. |
| `:core:ai` | Capability registry, per-provider consent router (with `CancellationException` propagation), role-redacting `BundleTrimmer`, cloud/local adapters, AI Config UI. |
| `:core:database` | SQLCipher-encrypted Room 2.8.5 database, `StatementSummaryEntity`, `EvidenceManifestEntity` (`HmacSHA256`), draft-capable `FinancialEventEntity`, Keystore lifecycle, retention worker. |
| `:feature:review` | Split-pane Compose review screen, 4-corner quadrilateral `Canvas` evidence overlay, draft amount/date resolution, rule learning. |

---

## 2. Local Capture, PDF Coordinate Normalization, Vision & Homography Mapping

### 2.1 Native Digital PDF Fast-Path vs. Camera Capture
1. **Native Digital PDF Coordinate & Provenance Contract:**
   * When a PDF is imported, inspect its embedded text layer before rasterizing.
   * Extracted PDF text elements are tagged with `extractionProvenance = ExtractionProvenance.PDF_EMBEDDED_TEXT_LAYER` and `ocrConfidence = null` (never conflating digital text extraction with `1.0f` optical recognition confidence or semantic correctness).
   * **PDF User-Space to Top-Left Normalized Space:** PDF user space places its origin $(0, 0)$ at the bottom-left of the `/CropBox` $[x_{\min}, y_{\min}, x_{\max}, y_{\max}]$ with width $W_{\text{crop}} = x_{\max} - x_{\min}$ and height $H_{\text{crop}} = y_{\max} - y_{\min}$, and pages may specify `/Rotate` $\in \{0, 90, 180, 270\}$. First convert each PDF character box $(x_{\text{pdf}}, y_{\text{pdf}})$ into unrotated top-left normalized coordinates:
     $$u_0 = \frac{x_{\text{pdf}} - x_{\min}}{W_{\text{crop}}}, \quad v_0 = 1 - \frac{y_{\text{pdf}} - y_{\min}}{H_{\text{crop}}}$$
     Then apply the page's `/Rotate` transform so the resulting `boxOcrNormalized` and rendered page preview share the exact same top-left $[0, 1]^2$ coordinate frame.
2. **Camera / Scanned Image Path:** Use `GmsDocumentScanner` (`16.0.0`) or `CameraX`. Retain two images per page in app-private encrypted storage:
   * `raw_display_page_{n}.enc`: Original color bitmap with dimensions $W_r \times H_r$ for CIELAB color extraction and the Review UI.
   * `clean_ocr_page_{n}.enc`: Deskewed, shadow-removed, contrast-normalized bitmap with dimensions $W_o \times H_o$ for ML Kit Text Recognition v2 (`extractionProvenance = ExtractionProvenance.MLKIT_OPTICAL_OCR`).

### 2.2 Explicit Coordinate Spaces & Failure-Safe Normalized Homography Mapping
The pipeline maintains a tested coordinate mapping for every affine or projective transformation it performs. Define three explicit coordinate spaces for each page:
* **Raw Display Pixel Space:** $(x_r, y_r) \in [0, W_r] \times [0, H_r]$
* **Cleaned OCR Pixel Space:** $(x_o, y_o) \in [0, W_o] \times [0, H_o]$
* **Normalized Unit Space:** $(\hat{x}, \hat{y}) \in [0, 1]^2$

Let $H_{\text{px}}$ be the $3 \times 3$ forward projective matrix mapping raw display pixels to cleaned OCR pixels during preprocessing, and $H_{\text{px}}^{-1}$ be its inverse. Define the scaling matrices:

$$N_{\text{raw}} = \begin{bmatrix} \frac{1}{W_r} & 0 & 0 \\ 0 & \frac{1}{H_r} & 0 \\ 0 & 0 & 1 \end{bmatrix}, \quad N_{\text{ocr}}^{-1} = \begin{bmatrix} W_o & 0 & 0 \\ 0 & H_o & 0 \\ 0 & 0 & 1 \end{bmatrix}$$

We compose and store the **Normalized Inverse Homography Matrix** $H_{\text{norm}}$ (9 floats in row-major order) whose inputs and outputs are both in normalized $[0, 1]^2$ coordinates:

$$H_{\text{norm}} = N_{\text{raw}} \cdot H_{\text{px}}^{-1} \cdot N_{\text{ocr}}^{-1}$$

For any normalized corner $(\hat{x}_o, \hat{y}_o) \in [0, 1]^2$ of an OCR bounding box in `clean_ocr_page`, its corresponding normalized position $(\hat{x}_r, \hat{y}_r)$ on `raw_display_page` is:

$$\begin{bmatrix} u \\ v \\ w \end{bmatrix} = H_{\text{norm}} \begin{bmatrix} \hat{x}_o \\ \hat{y}_o \\ 1 \end{bmatrix}, \quad \hat{x}_r = \frac{u}{w}, \quad \hat{y}_r = \frac{v}{w}$$

* **No Silent Fallback or Vertex-Clamping Distortion:**
  1. If $\det(H_{\text{norm}})$ is near zero, or if $|w| < 10^{-6}$, or if $\hat{x}_r, \hat{y}_r$ are non-finite (`NaN` or `Infinity`), `CoordinateTransformMapper` must return `ProjectionResult.SingularOrInvalidTransform`—never silently returning the untransformed input point.
  2. Vertices must **never** be clamped individually with `.coerceIn(0f, 1f)` prior to polygon construction, because individual vertex clamping distorts perspective edge slopes when a box crosses the page boundary. Instead, preserve the exact projected `NormalizedQuad`, flag `isPartiallyOutOfBounds` if any vertex lies outside $[0, 1]^2$, and clip the polygon edges against the $[0, 1]^2$ viewport during Canvas rendering.

```kotlin
package com.finance.engine.core.document.vision

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Serializable
data class NormalizedPoint(val x: Float, val y: Float) {
    fun isWithinUnitSquare(): Boolean = x in 0f..1f && y in 0f..1f
}

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
    fun vertices(): List<NormalizedPoint> = listOf(topLeft, topRight, bottomRight, bottomLeft)

    /**
     * Computes a conservative axis-aligned bounding box clipped to the [0, 1]^2 viewport.
     */
    fun clippedEnclosingRect(): NormalizedRect {
        val xs = vertices().map { it.x }
        val ys = vertices().map { it.y }
        val minX = (xs.minOrNull() ?: 0f).coerceIn(0f, 1f)
        val minY = (ys.minOrNull() ?: 0f).coerceIn(0f, 1f)
        val maxX = (xs.maxOrNull() ?: 1f).coerceIn(0f, 1f)
        val maxY = (ys.maxOrNull() ?: 1f).coerceIn(0f, 1f)
        return NormalizedRect(minX, minY, max(0f, maxX - minX), max(0f, maxY - minY))
    }
}

sealed interface ProjectionResult {
    data class Success(
        val quad: NormalizedQuad,
        val isPartiallyOutOfBounds: Boolean
    ) : ProjectionResult

    data class SingularOrInvalidTransform(val reason: String) : ProjectionResult
}

object CoordinateTransformMapper {
    private const val EPSILON = 1e-6f

    fun mapOcrRectToRawQuad(
        ocrRect: NormalizedRect,
        hNorm: FloatArray // 9 elements, row-major
    ): ProjectionResult {
        if (hNorm.size != 9 || hNorm.any { !it.isFinite() }) {
            return ProjectionResult.SingularOrInvalidTransform("H_norm must contain 9 finite floats.")
        }
        val det = determinant3x3(hNorm)
        if (abs(det) < EPSILON) {
            return ProjectionResult.SingularOrInvalidTransform("Singular homography matrix: det=$det")
        }

        val mapped = ArrayList<NormalizedPoint>(4)
        for (pt in ocrRect.corners()) {
            val projected = projectPointOrNull(pt, hNorm)
                ?: return ProjectionResult.SingularOrInvalidTransform(
                    "Perspective denominator w near zero or non-finite projection for point $pt"
                )
            mapped.add(projected)
        }

        val quad = NormalizedQuad(
            topLeft = mapped[0],
            topRight = mapped[1],
            bottomRight = mapped[2],
            bottomLeft = mapped[3]
        )
        val outOfBounds = mapped.any { !it.isWithinUnitSquare() }
        return ProjectionResult.Success(quad = quad, isPartiallyOutOfBounds = outOfBounds)
    }

    private fun projectPointOrNull(pt: NormalizedPoint, h: FloatArray): NormalizedPoint? {
        val u = h[0] * pt.x + h[1] * pt.y + h[2]
        val v = h[3] * pt.x + h[4] * pt.y + h[5]
        val w = h[6] * pt.x + h[7] * pt.y + h[8]
        if (!u.isFinite() || !v.isFinite() || !w.isFinite() || abs(w) < EPSILON) {
            return null
        }
        val rx = u / w
        val ry = v / w
        if (!rx.isFinite() || !ry.isFinite()) return null
        return NormalizedPoint(rx, ry)
    }

    private fun determinant3x3(m: FloatArray): Float =
        m[0] * (m[4] * m[8] - m[5] * m[7]) -
            m[1] * (m[3] * m[8] - m[5] * m[6]) +
            m[2] * (m[3] * m[7] - m[4] * m[6])
}
```

### 2.3 Pre-OCR Quality Metrics & Perceptual Color Extraction
* **Laplacian Blur Variance:** Compute $\sigma^2(\nabla^2 I)$ on a $1024\text{px}$ downscaled grayscale copy. Flag `isBlurry = true` if $\sigma^2 < 110.0$.
* **Glare Ratio:** Flag `hasGlare = true` if $> 4.5\%$ of document interior pixels have luminance $Y > 250$.
* **CIELAB Color Distance:** Extract dominant background and header colors in CIELAB $(L^*, a^*, b^*)$. Compare against stored merchant templates using $\Delta E_{ab}^* = \sqrt{(\Delta L^*)^2 + (\Delta a^*)^2 + (\Delta b^*)^2}$ (treating $\Delta E_{ab}^* < 12.0$ as a supporting template signal).

---

## 3. Spatial Layout Engine & Complete ISO 4217 Currency Extraction

### 3.1 Page-Scoped Evidence IDs & Radian-Converted Line Grouping
Every extracted element receives a globally unambiguous within-document ID formatted as `P{page:02d}_T{index:04d}` (e.g., `P01_T0014`).
To handle curved thermal receipts where $Y$-coordinates drift across a row, two elements $A$ and $B$ ($x_A < x_B$) are grouped into the same `LayoutLine` (`P01_L0009`) by converting `rotationDegrees` ($\theta_A^{\circ}$) into radians $\theta_{A,\text{rad}} = \theta_A^{\circ} \cdot \frac{\pi}{180}$ (`Math.toRadians(elementA.rotationDegrees.toDouble())`) and projecting $A$'s center to $x_B$:

$$\theta_{A,\text{rad}} = \theta_A^{\circ} \cdot \frac{\pi}{180}, \quad y_{\text{proj}} = y_A + (x_B - x_A) \tan(\theta_{A,\text{rad}}), \quad |y_B - y_{\text{proj}}| \le 0.45 \cdot \max(h_A, h_B)$$

### 3.2 Complete ISO 4217 Currency Registry & Strict Non-Rounding Money Model
* **Complete ISO 4217 Coverage:** `Iso4217CurrencyRegistry` seeds all circulating ISO 4217 currencies from `java.util.Currency.getAvailableCurrencies()` (excluding non-monetary codes with negative fraction digits such as `XAU`, `XAG`, `XXX`, `XTS`) and overlays an explicit, versioned ISO 4217 minor-unit scale table covering `0`, `2`, `3`, and `4` decimal currencies.
* **No Silent Rounding Mandate:** Financial parsers must **never** silently round excess fractional digits (`RoundingMode.HALF_UP` is strictly forbidden). If OCR reads `12.345` for a 2-decimal currency (`USD`), `StrictMoneyParser.parseExact()` returns `CurrencyParseResult.ExcessPrecisionFlagged`, which can be persisted as a `NEEDS_USER_REVIEW_DRAFT` event via its raw token without inventing a rounded minor-unit integer.

```kotlin
package com.finance.engine.core.finance.extract

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Currency

object Iso4217CurrencyRegistry {
    const val REGISTRY_VERSION = "2026.10-FULL"

    private val nonMonetaryCodes: Set<String> = setOf(
        "XAU", "XAG", "XPD", "XPT", "XBA", "XBB", "XBC", "XBD", "XTS", "XXX", "XUA", "XSU", "XDR"
    )

    // Explicit authoritative overrides for 0, 2, 3, and 4 decimal ISO 4217 currencies
    private val explicitIso4217Scales: Map<String, Int> = mapOf(
        // 0-decimal currencies
        "BIF" to 0, "CLP" to 0, "DJF" to 0, "GNF" to 0, "ISK" to 0, "JPY" to 0,
        "KMF" to 0, "KRW" to 0, "PYG" to 0, "RWF" to 0, "UGX" to 0, "UYI" to 0,
        "VND" to 0, "VUV" to 0, "XAF" to 0, "XOF" to 0, "XPF" to 0,
        // 3-decimal currencies
        "BHD" to 3, "IQD" to 3, "JOD" to 3, "KWD" to 3, "LYD" to 3, "OMR" to 3, "TND" to 3,
        // 4-decimal accounting units
        "CLF" to 4, "UYW" to 4,
        // Standard 2-decimal currencies (including MRU/MGA which use 2 decimals in modern ISO 4217 banking)
        "USD" to 2, "EUR" to 2, "GBP" to 2, "CAD" to 2, "AUD" to 2, "NZD" to 2,
        "CHF" to 2, "CNY" to 2, "HKD" to 2, "SGD" to 2, "INR" to 2, "MXN" to 2,
        "BRL" to 2, "ZAR" to 2, "SEK" to 2, "NOK" to 2, "DKK" to 2, "PLN" to 2,
        "CZK" to 2, "HUF" to 2, "RON" to 2, "BGN" to 2, "ILS" to 2, "TRY" to 2,
        "AED" to 2, "SAR" to 2, "QAR" to 2, "COP" to 2, "PEN" to 2, "ARS" to 2,
        "TWD" to 2, "THB" to 2, "MYR" to 2, "PHP" to 2, "IDR" to 2, "PKR" to 2,
        "EGP" to 2, "NGN" to 2, "KES" to 2, "GHS" to 2, "MAD" to 2, "UAH" to 2
    )

    private val completeScaleMap: Map<String, Int> by lazy {
        val map = HashMap<String, Int>(220)
        for (currency in Currency.getAvailableCurrencies()) {
            val code = currency.currencyCode.uppercase()
            if (code in nonMonetaryCodes) continue
            val digits = currency.defaultFractionDigits
            if (digits in 0..4) {
                map[code] = digits
            }
        }
        map.putAll(explicitIso4217Scales)
        map
    }

    fun getExpectedScale(currencyCode: String): Int? =
        completeScaleMap[currencyCode.uppercase().trim()]
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
                rawDecimal = rawNumericString.trim(),
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

## 4. Financial Domain Model, Draft Event Representation & Data Contracts

### 4.1 Canonical Serializable Domain Contracts

To ensure that amounts flagged with `ExcessPrecisionFlagged` (e.g., `"12.345 USD"`) or unresolved dates can be represented honestly without inventing a rounded `CurrencyAmount`, `ResolvedFinancialEvent` supports an explicit draft-amount state (`amount: CurrencyAmount? = null` paired with `draftRawAmountToken`, `draftCurrencyHypothesis`, and `draftAmountIssue`). A financial event can transition to `VERIFIED_FINAL` in the database **only** when `amount != null` and `isoDate != null`.

```kotlin
package com.finance.engine.core.model

import com.finance.engine.core.document.vision.NormalizedQuad
import com.finance.engine.core.document.vision.NormalizedRect
import com.finance.engine.core.finance.extract.CurrencyAmount
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ExtractionProvenance {
    @SerialName("pdf_embedded_text_layer") PDF_EMBEDDED_TEXT_LAYER,
    @SerialName("mlkit_optical_ocr") MLKIT_OPTICAL_OCR
}

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
    BANK_TXN_DEBIT, BANK_TXN_CREDIT, BANK_RUNNING_BALANCE,
    PAYMENT_STATUS_LABEL, ACCOUNT_NUMBER_MASKED, UNKNOWN
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
    val extractionProvenance: ExtractionProvenance,
    val ocrConfidence: Float?, // Null for PDF_EMBEDDED_TEXT_LAYER
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
    val amount: CurrencyAmount? = null, // Null only if ExcessPrecisionFlagged
    val draftRawAmountToken: String? = null,
    val draftCurrencyHypothesis: String? = null,
    val draftParseIssue: String? = null,
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
    val draftRawAmountToken: String? = null,
    val draftParseIssue: String? = null,
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
    val isoDate: String? = null, // If null, persists as NEEDS_USER_REVIEW_DRAFT
    val dateEvidenceIds: List<String> = emptyList(),
    val dueDateIso: String? = null,
    val dueDateEvidenceIds: List<String> = emptyList(),
    val billPaymentStatus: BillPaymentStatus? = null,
    val statusEvidenceIds: List<String> = emptyList(),
    val amount: CurrencyAmount? = null, // Null ONLY when draftRawAmountToken != null
    val draftRawAmountToken: String? = null,
    val draftCurrencyHypothesis: String? = null,
    val draftAmountIssue: String? = null,
    val categoryId: String,
    val sourceRowId: String? = null,
    val evidenceIds: List<String>
) {
    fun isReadyForVerifiedFinal(): Boolean =
        amount != null && isoDate != null && draftAmountIssue == null
}

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

## 5. Mutually-Exclusive Signed-Ledger Reconciliation, Hybrid Gate & Strict Validator

### 5.1 Mutually-Exclusive Signed-Ledger Reconciliation Rules

To prevent double-counting (such as counting a bank fee both as a `BANK_TXN_DEBIT` and a separate `Fees` term, or counting a credit-card refund twice), every statement table row $r \in \{1, \dots, m\}$ is classified into **at most one** mutually-exclusive `LedgerRowBucket` with a deterministic signed delta $\Delta_r$ before reconciliation:

1. **Bank Asset Statements (`BANK_STATEMENT`):**
   Each row $r$ with amount $A_r \ge 0$ is assigned to exactly one bucket:
   * `ASSET_INFLOW` (deposits, incoming credits, interest earned): $\Delta_r = +A_r$
   * `ASSET_OUTFLOW` (withdrawals, card debits, checks, bank fees): $\Delta_r = -A_r$
   * `NON_LEDGER_SUMMARY` (daily running balance rows): $\Delta_r = 0$
   $$\text{OpeningBalance} + \sum_{r=1}^{m} \Delta_r = \text{ClosingBalance}$$

2. **Credit Card Liability Statements (`CREDIT_CARD_STATEMENT`):**
   Each row $r$ with amount $A_r \ge 0$ is assigned to exactly one bucket:
   * `LIABILITY_INCREASE` (purchases, cash advances, fees, interest charges): $\Delta_r = +A_r$
   * `LIABILITY_DECREASE` (bill payments received, merchant refunds, statement credits): $\Delta_r = -A_r$
   * `NON_LEDGER_SUMMARY` (previous balance or total summary lines): $\Delta_r = 0$
   $$\text{OpeningBalance} + \sum_{r=1}^{m} \Delta_r = \text{ClosingBalance}$$

3. **Receipts & Invoices (`RECEIPT`, `INVOICE_OR_UTILITY_BILL`):**
   * **Tax-Exclusive Composition:** $\sum_{i=1}^{n} \text{LineItem}_i + \sum \text{Tax} + \text{Tip} + \text{ShippingOrFee} - \sum \text{DocumentLevelDiscount} = \text{Total}$
   * **Tax-Inclusive Composition (VAT/GST):** $\sum_{i=1}^{n} \text{LineItem}_i + \text{Tip} + \text{ShippingOrFee} - \sum \text{DocumentLevelDiscount} = \text{Total}$
   * **Subtotal-to-Total Check:** $\text{Subtotal} + \sum \text{Tax} + \text{Tip} + \text{ShippingOrFee} - \sum \text{PostSubtotalDiscount} = \text{Total}$

### 5.2 Candidate-Bound Post-LLM Semantic Validator (`PostLlmSemanticValidator`)

This validator enforces five strict invariants:
1. **No Partial Evidence-ID Overlap:** For every resolved field and event, **all** of its cited `evidenceIds` must belong to the matched candidate (or the matched `ReconstructedTableRow` when `sourceRowId` is provided) and exist in `validDocumentEvidenceIds`. A mixed list containing one valid candidate ID and one unrelated ID is rejected.
2. **Authoritative `DocumentFamily` Lock:** `raw.documentFamily` must be present in `bundle.detectedFamilyCandidates` (when non-empty). An LLM cannot override a locally detected `INVOICE_OR_UTILITY_BILL` into a `RECEIPT` to bypass unpaid-invoice guardrails.
3. **Full `StatementSummaryInterpretation` Validation:** `openingBalance`, `closingBalance`, `minimumPaymentDue`, `periodStartDate`, and `periodEndDate` are each validated against their respective candidate roles.
4. **No Silent Primary-Date Fallback on Invalid Event Dates:** If an event supplies an `isoDate` that fails validation (or points to a `DUE_DATE`, `PRINT_DATE`, or `STATEMENT_PERIOD_START`/`END`), the validator sets `isoDate = null` and records `"event.${event.eventId}.invalid_date"` in `unresolvedFields`, forcing the event into `NEEDS_USER_REVIEW_DRAFT`.
5. **Validated `dueDateIso`, `billPaymentStatus`, and Draft Amounts:** Event `dueDateIso` must match a `DUE_DATE` candidate, `billPaymentStatus` is validated against `PAYMENT_STATUS_LABEL` evidence, and events matching an `ExcessPrecisionFlagged` candidate retain their `draftRawAmountToken` as a draft requiring user review.

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

        fun allIdsOwnedBy(citedIds: List<String>, ownerIds: Collection<String>): Boolean =
            citedIds.isNotEmpty() && citedIds.all { it in ownerIds && it in validDocumentEvidenceIds }

        // 1. Validate DocumentFamily against authoritative local candidates
        val validatedFamily = when {
            lockedInterpretation != null && lockedInterpretation.documentFamily != DocumentFamily.UNKNOWN ->
                lockedInterpretation.documentFamily
            bundle.detectedFamilyCandidates.isEmpty() -> raw.documentFamily
            raw.documentFamily in bundle.detectedFamilyCandidates -> raw.documentFamily
            else -> {
                unresolved.add("documentFamily")
                bundle.detectedFamilyCandidates.first()
            }
        }

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
                    allIdsOwnedBy(field.evidenceIds, cand.evidenceIds)
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
                    allIdsOwnedBy(field.evidenceIds, cand.evidenceIds)
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
                cand.amount != null &&
                    cand.role in allowedRoles &&
                    cand.role == field.role &&
                    cand.amount == field.amount &&
                    allIdsOwnedBy(field.evidenceIds, cand.evidenceIds)
            }
            return if (matchingCandidate != null) {
                field
            } else {
                unresolved.add(fieldName)
                null
            }
        }

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

        // 2. Validate StatementSummaryInterpretation completely
        val validStatementSummary = raw.statementSummary?.let { summary ->
            StatementSummaryInterpretation(
                openingBalance = validateAmountField(
                    summary.openingBalance,
                    "statementSummary.openingBalance",
                    setOf(SemanticRole.STATEMENT_OPENING_BALANCE)
                ),
                closingBalance = validateAmountField(
                    summary.closingBalance,
                    "statementSummary.closingBalance",
                    setOf(SemanticRole.STATEMENT_CLOSING_BALANCE)
                ),
                minimumPaymentDue = validateAmountField(
                    summary.minimumPaymentDue,
                    "statementSummary.minimumPaymentDue",
                    setOf(SemanticRole.MINIMUM_PAYMENT_DUE, SemanticRole.AMOUNT_DUE)
                ),
                periodStartDate = validateDateField(
                    summary.periodStartDate,
                    "statementSummary.periodStartDate",
                    setOf(SemanticRole.STATEMENT_PERIOD_START)
                ),
                periodEndDate = validateDateField(
                    summary.periodEndDate,
                    "statementSummary.periodEndDate",
                    setOf(SemanticRole.STATEMENT_PERIOD_END)
                )
            )
        }

        // 3. Validate each financial event with strict subset ownership and field-level checks
        val allowedEventDateRoles = setOf(SemanticRole.PURCHASE_DATE, SemanticRole.INVOICE_DATE)

        val validEvents = raw.financialEvents.mapNotNull { event ->
            if (event.eventKind == FinancialEventKind.STATEMENT_BALANCE_IGNORE) {
                return@mapNotNull null
            }
            if (event.evidenceIds.isEmpty() || !event.evidenceIds.all { it in validDocumentEvidenceIds }) {
                unresolved.add("event.${event.eventId}.evidence")
                return@mapNotNull null
            }

            if (validatedFamily == DocumentFamily.INVOICE_OR_UTILITY_BILL &&
                event.eventKind == FinancialEventKind.PURCHASE_EXPENSE &&
                event.billPaymentStatus != BillPaymentStatus.PAID_IN_FULL
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

            val matchedRow = event.sourceRowId?.let { rowId ->
                bundle.tableRows.find { it.rowId == rowId }
            }
            if (event.sourceRowId != null && matchedRow == null) {
                unresolved.add("event.${event.eventId}.invalid_source_row")
                return@mapNotNull null
            }

            // Check exact amount OR excess-precision draft token with strict evidence subset ownership
            var resolvedAmount: CurrencyAmount? = null
            var draftToken: String? = null
            var draftCurrency: String? = null
            var draftIssue: String? = null

            if (event.amount != null) {
                val matchedCandidate = bundle.amountCandidates.find { cand ->
                    cand.amount == event.amount &&
                        cand.role in allowedAmountRolesForEvent &&
                        allIdsOwnedBy(event.evidenceIds, cand.evidenceIds)
                }
                val matchedRowCell = matchedRow?.let { row ->
                    val rowAllEvidenceIds = row.cells.flatMap { it.evidenceIds }.toSet()
                    val amountCell = row.cells.find { cell ->
                        cell.parsedAmount == event.amount &&
                            cell.role in allowedAmountRolesForEvent &&
                            cell.evidenceIds.all { it in event.evidenceIds }
                    }
                    if (amountCell != null && allIdsOwnedBy(event.evidenceIds, rowAllEvidenceIds)) amountCell else null
                }

                if (matchedCandidate == null && matchedRowCell == null) {
                    unresolved.add("event.${event.eventId}.amount_provenance_mismatch")
                    return@mapNotNull null
                }
                resolvedAmount = event.amount
            } else if (event.draftRawAmountToken != null) {
                val matchedDraftCand = bundle.amountCandidates.find { cand ->
                    cand.draftRawAmountToken == event.draftRawAmountToken &&
                        allIdsOwnedBy(event.evidenceIds, cand.evidenceIds)
                }
                val matchedDraftCell = matchedRow?.cells?.find { cell ->
                    cell.draftRawAmountToken == event.draftRawAmountToken &&
                        allIdsOwnedBy(event.evidenceIds, matchedRow.cells.flatMap { it.evidenceIds }.toSet())
                }
                if (matchedDraftCand == null && matchedDraftCell == null) {
                    unresolved.add("event.${event.eventId}.draft_amount_provenance_mismatch")
                    return@mapNotNull null
                }
                draftToken = event.draftRawAmountToken
                draftCurrency = matchedDraftCand?.draftCurrencyHypothesis ?: event.draftCurrencyHypothesis
                draftIssue = matchedDraftCand?.draftParseIssue ?: matchedDraftCell?.draftParseIssue ?: "EXCESS_PRECISION_OR_UNPARSEABLE"
                unresolved.add("event.${event.eventId}.draft_amount_requires_review")
            } else {
                unresolved.add("event.${event.eventId}.missing_amount")
                return@mapNotNull null
            }

            // Validate event date strictly: never accept DUE_DATE, PRINT_DATE, or STATEMENT_PERIOD_START/END,
            // and never silently overwrite an invalid supplied date with primaryDate.
            val validatedIsoDate: String? = if (event.isoDate != null) {
                val dateIds = event.dateEvidenceIds.ifEmpty { event.evidenceIds }
                val matchedDateCand = bundle.dateCandidates.find { cand ->
                    cand.isoDate == event.isoDate &&
                        cand.role in allowedEventDateRoles &&
                        allIdsOwnedBy(dateIds, cand.evidenceIds)
                }
                val matchedRowDateCell = matchedRow?.cells?.find { cell ->
                    cell.parsedIsoDate == event.isoDate &&
                        cell.role !in setOf(SemanticRole.DUE_DATE, SemanticRole.PRINT_DATE, SemanticRole.STATEMENT_PERIOD_START, SemanticRole.STATEMENT_PERIOD_END) &&
                        cell.evidenceIds.all { it in validDocumentEvidenceIds }
                }
                if (matchedDateCand != null || matchedRowDateCell != null) {
                    event.isoDate
                } else {
                    unresolved.add("event.${event.eventId}.invalid_date")
                    null
                }
            } else if (matchedRow == null && validPrimaryDate != null) {
                // Only inherit primaryDate for single-transaction receipts/bills when event.isoDate was omitted
                validPrimaryDate.value
            } else {
                unresolved.add("event.${event.eventId}.missing_date")
                null
            }

            // Validate optional dueDateIso on event
            val validatedEventDueDate: String? = event.dueDateIso?.let { dStr ->
                val dueIds = event.dueDateEvidenceIds.ifEmpty { event.evidenceIds }
                val matchedDue = bundle.dateCandidates.find { cand ->
                    cand.isoDate == dStr &&
                        cand.role == SemanticRole.DUE_DATE &&
                        allIdsOwnedBy(dueIds, cand.evidenceIds)
                }
                if (matchedDue != null) dStr else {
                    unresolved.add("event.${event.eventId}.invalid_due_date")
                    null
                }
            }

            // Validate optional billPaymentStatus on event
            val validatedBillStatus: BillPaymentStatus? = event.billPaymentStatus?.let { status ->
                val allowedKinds = setOf(
                    FinancialEventKind.UNPAID_BILL_LIABILITY,
                    FinancialEventKind.BILL_PAYMENT_SETTLEMENT,
                    FinancialEventKind.PURCHASE_EXPENSE
                )
                val hasStatusEvidence = event.statusEvidenceIds.isNotEmpty() &&
                    bundle.textCandidates.any { cand ->
                        cand.role == SemanticRole.PAYMENT_STATUS_LABEL &&
                            allIdsOwnedBy(event.statusEvidenceIds, cand.evidenceIds)
                    }
                if (event.eventKind in allowedKinds && hasStatusEvidence) status else {
                    unresolved.add("event.${event.eventId}.unverified_payment_status")
                    null
                }
            }

            val safeCategory = if (event.categoryId in bundle.allowedCategories) {
                event.categoryId
            } else {
                unresolved.add("event.${event.eventId}.category")
                "Uncategorized"
            }

            event.copy(
                isoDate = validatedIsoDate,
                dueDateIso = validatedEventDueDate,
                billPaymentStatus = validatedBillStatus,
                amount = resolvedAmount,
                draftRawAmountToken = draftToken,
                draftCurrencyHypothesis = draftCurrency,
                draftAmountIssue = draftIssue,
                categoryId = safeCategory
            )
        }

        val requiresReview = raw.reviewRequired ||
            unresolved.isNotEmpty() ||
            !bundle.arithmeticValidation.isReconciled ||
            validEvents.isEmpty() ||
            validEvents.any { !it.isReadyForVerifiedFinal() }

        return raw.copy(
            documentFamily = validatedFamily,
            merchantOrIssuer = validMerchant,
            issuerAddress = validAddress,
            primaryDate = validPrimaryDate,
            dueDate = validDueDate,
            documentTotal = validTotal,
            taxTotal = validTax,
            statementSummary = validStatementSummary,
            financialEvents = validEvents,
            reviewRequired = requiresReview,
            unresolvedFields = unresolved.toList()
        )
    }
}
```

---

## 6. AI Provider Capabilities, Per-Provider Consent Router & Guaranteed `BundleTrimmer`

### 6.1 Provider Enumeration, Readiness & Audited October 2026 Model Registry

```kotlin
package com.finance.engine.core.ai

import kotlinx.serialization.Serializable

@Serializable
enum class AiEngineProvider(
    val displayName: String,
    val badgeLabel: String,
    val isCloudProvider: Boolean,
    val requiredDisclosureVersion: Int
) {
    GEMMA_4_CLOUD_FREE(
        displayName = "Gemma 4 Cloud (Free Tier) - Default",
        badgeLabel = "Gemma",
        isCloudProvider = true,
        requiredDisclosureVersion = 2
    ),
    FIREBASE_AI_LOGIC(
        displayName = "Firebase AI Logic (Cloud)",
        badgeLabel = "Firebase",
        isCloudProvider = true,
        requiredDisclosureVersion = 2
    ),
    BYOK_GEMINI(
        displayName = "Bring Your Own Key (BYOK)",
        badgeLabel = "BYOK",
        isCloudProvider = true,
        requiredDisclosureVersion = 2
    ),
    ON_DEVICE_GEMINI_NANO(
        displayName = "On-Device Local AI (Gemini Nano)",
        badgeLabel = "Local AI",
        isCloudProvider = false,
        requiredDisclosureVersion = 0
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

### 6.2 Per-Provider Consent Records & Explicit Fallback Policy (`AiRoutingPolicy`)

Each cloud provider has its own service terms and disclosure version. `AiRoutingPolicy` tracks `ProviderConsentRecord` per `AiEngineProvider` in `consentByProvider`.
* **Deliberate Unconsented-Primary Policy:** When the user selects a cloud provider as `primaryProvider` (e.g., `GEMMA_4_CLOUD_FREE`) and has not yet accepted its required disclosure version, `PolicyDrivenAiRouter` immediately returns `RoutingOutcome.ConsentRequired(primaryProvider)` so the UI presents the consent dialog for the user's chosen primary engine rather than silently downgrading to a secondary engine without asking.

```kotlin
package com.finance.engine.core.ai

import kotlinx.serialization.Serializable

@Serializable
data class ProviderConsentRecord(
    val acceptedDisclosureVersion: Int,
    val consentAcceptedEpochMs: Long
)

@Serializable
data class AiRoutingPolicy(
    val primaryProvider: AiEngineProvider = AiEngineProvider.GEMMA_4_CLOUD_FREE,
    val preferredModelIdByProvider: Map<AiEngineProvider, String> = mapOf(
        AiEngineProvider.GEMMA_4_CLOUD_FREE to "gemma-4-31b-it",
        AiEngineProvider.FIREBASE_AI_LOGIC to "gemini-3.8-flash",
        AiEngineProvider.ON_DEVICE_GEMINI_NANO to "gemini-nano-aicore"
    ),
    val strictLocalOnlyMode: Boolean = false,
    val consentByProvider: Map<AiEngineProvider, ProviderConsentRecord> = emptyMap(),
    val authorizedFallbackProviders: List<AiEngineProvider> = listOf(
        AiEngineProvider.ON_DEVICE_GEMINI_NANO
    )
) {
    fun isProviderAuthorizedForTransfer(provider: AiEngineProvider): Boolean {
        if (!provider.isCloudProvider) return true
        if (strictLocalOnlyMode) return false
        val record = consentByProvider[provider] ?: return false
        return record.acceptedDisclosureVersion >= provider.requiredDisclosureVersion &&
            record.consentAcceptedEpochMs > 0L
    }
}
```

### 6.3 Role-Redacting, Byte-Verified `BundleTrimmer` (`BundleTrimResult`)

`BundleTrimmer` strips `CUSTOMER_ADDRESS` across **both** `textCandidates` and `tableRows` cells, masks formatted telephone numbers and separated/hyphenated account numbers, and performs a final measured byte-limit verification on every chunk. If a chunk still exceeds `maxUtf8Bytes` after dropping optional context, it returns `BundleTrimResult.BundleCannotFit`—never transmitting an oversized payload.

```kotlin
package com.finance.engine.core.ai

import com.finance.engine.core.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

sealed interface BundleTrimResult {
    data class Success(val chunks: List<LlmAnalysisBundle>) : BundleTrimResult
    data class BundleCannotFit(val reason: String, val measuredBytes: Int, val maxBytes: Int) : BundleTrimResult
}

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
    // Matches formatted US/international phone numbers without matching currency amounts or ISO dates
    private val phoneRegex = Regex("""(?<!\d)(?:\+\d{1,3}[\s.-]?)?(?:\(\d{3}\)|\d{3})[\s.-]\d{3}[\s.-]\d{4}(?!\d)""")
    // Matches contiguous or space/hyphen-separated 8 to 19 digit account/card numbers
    private val accountOrCardRegex = Regex("""(?<!\d)(?:\d[ -]?){7,18}\d(?!\d)""")

    fun buildRedactedAndBoundedChunks(
        rawBundle: LlmAnalysisBundle,
        maxUtf8Bytes: Int = DEFAULT_MAX_UTF8_BYTES
    ): BundleTrimResult {
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

        // Redact by SemanticRole AND regex inside all reconstructed table cells
        val redactedRows = rawBundle.tableRows.map { row ->
            row.copy(
                cells = row.cells
                    .filter { it.role !in redactedRoles }
                    .map { cell ->
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
            val trimmed = trimOptionalContextToFit(baseBundle, maxUtf8Bytes)
            val measured = measureUtf8Bytes(trimmed)
            return if (measured <= maxUtf8Bytes) {
                BundleTrimResult.Success(listOf(trimmed))
            } else {
                BundleTrimResult.BundleCannotFit(
                    reason = "Base candidate bundle exceeds byte budget even without table rows",
                    measuredBytes = measured,
                    maxBytes = maxUtf8Bytes
                )
            }
        }

        val resultChunks = mutableListOf<LlmAnalysisBundle>()
        var cursor = 0
        val maskedAccount = stratifiedTexts.find { it.role == SemanticRole.ACCOUNT_NUMBER_MASKED }?.normalizedValue
        val periodStart = stratifiedDates.find { it.role == SemanticRole.STATEMENT_PERIOD_START }?.isoDate
        val periodEnd = stratifiedDates.find { it.role == SemanticRole.STATEMENT_PERIOD_END }?.isoDate

        while (cursor < redactedRows.size) {
            var step = minOf(INITIAL_ROW_CHUNK_SIZE, redactedRows.size - cursor)
            var candidateSlice = redactedRows.subList(cursor, cursor + step)
            var candidateChunk = withChunkContext(baseBundle.copy(tableRows = candidateSlice), resultChunks.size, -1, maskedAccount, periodStart, periodEnd)

            while (measureUtf8Bytes(candidateChunk) > maxUtf8Bytes && step > 1) {
                step /= 2
                candidateSlice = redactedRows.subList(cursor, cursor + step)
                candidateChunk = withChunkContext(baseBundle.copy(tableRows = candidateSlice), resultChunks.size, -1, maskedAccount, periodStart, periodEnd)
            }

            val fittedChunk = trimOptionalContextToFit(candidateChunk, maxUtf8Bytes)
            val finalBytes = measureUtf8Bytes(fittedChunk)
            if (finalBytes > maxUtf8Bytes) {
                return BundleTrimResult.BundleCannotFit(
                    reason = "Row ${candidateSlice.first().rowId} exceeds byte limit after context trimming",
                    measuredBytes = finalBytes,
                    maxBytes = maxUtf8Bytes
                )
            }

            resultChunks.add(fittedChunk)
            cursor += step
        }

        val totalChunks = resultChunks.size
        val finalizedChunks = resultChunks.map { chunk ->
            val updated = chunk.copy(chunkContext = chunk.chunkContext?.copy(totalChunks = totalChunks))
            val checkBytes = measureUtf8Bytes(updated)
            if (checkBytes > maxUtf8Bytes) {
                return BundleTrimResult.BundleCannotFit(
                    reason = "Finalized chunk exceeded byte budget",
                    measuredBytes = checkBytes,
                    maxBytes = maxUtf8Bytes
                )
            }
            updated
        }
        return BundleTrimResult.Success(finalizedChunks)
    }

    private fun withChunkContext(
        bundle: LlmAnalysisBundle,
        chunkIdx: Int,
        totalChunks: Int,
        maskedAccount: String?,
        periodStart: String?,
        periodEnd: String?
    ): LlmAnalysisBundle {
        val rows = bundle.tableRows
        return bundle.copy(
            chunkContext = StatementChunkContext(
                chunkIndex = chunkIdx,
                totalChunks = totalChunks,
                startRowId = rows.firstOrNull()?.rowId,
                endRowId = rows.lastOrNull()?.rowId,
                maskedAccountContext = maskedAccount,
                statementPeriodStartIso = periodStart,
                statementPeriodEndIso = periodEnd
            )
        )
    }

    private fun trimOptionalContextToFit(bundle: LlmAnalysisBundle, maxUtf8Bytes: Int): LlmAnalysisBundle {
        if (measureUtf8Bytes(bundle) <= maxUtf8Bytes) return bundle
        // Stage 1: Drop optional address text candidates while preserving merchant/issuer & mandatory amounts/dates
        val stage1 = bundle.copy(
            textCandidates = bundle.textCandidates.filter {
                it.role == SemanticRole.MERCHANT_NAME ||
                    it.role == SemanticRole.ISSUER_NAME ||
                    it.role == SemanticRole.PAYMENT_STATUS_LABEL ||
                    it.role == SemanticRole.ACCOUNT_NUMBER_MASKED
            }
        )
        if (measureUtf8Bytes(stage1) <= maxUtf8Bytes) return stage1

        // Stage 2: Keep top-1 candidate per SemanticRole for amounts and dates
        return stage1.copy(
            amountCandidates = stage1.amountCandidates.groupBy { it.role }.values.map { list ->
                list.maxBy { it.confidence }
            },
            dateCandidates = stage1.dateCandidates.groupBy { it.role }.values.map { list ->
                list.maxBy { it.confidence }
            }
        )
    }

    fun redactPiiText(input: String): String =
        input.replace(emailRegex, "[REDACTED_EMAIL]")
            .replace(phoneRegex, "[REDACTED_PHONE]")
            .replace(accountOrCardRegex) { match ->
                val digitsOnly = match.value.filter { it.isDigit() }
                if (digitsOnly.length >= 8) {
                    "****" + digitsOnly.takeLast(4)
                } else {
                    match.value
                }
            }

    fun measureUtf8Bytes(bundle: LlmAnalysisBundle): Int =
        json.encodeToString(bundle).encodeToByteArray().size
}
```

### 6.4 Cancellation-Safe Policy-Driven AI Router (`PolicyDrivenAiRouter`)

`PolicyDrivenAiRouter` explicitly rethrows `kotlinx.coroutines.CancellationException` so a cancelled coroutine never continues down the fallback provider chain, and checks `BundleTrimResult.BundleCannotFit` before invoking any model adapter.

```kotlin
package com.finance.engine.core.ai

import com.finance.engine.core.finance.validation.PostLlmSemanticValidator
import com.finance.engine.core.model.DocumentInterpretation
import com.finance.engine.core.model.LlmAnalysisBundle
import kotlinx.coroutines.CancellationException
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

        // Step 3: Deliberate Primary Provider Consent Check
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
            val readiness = try {
                adapter.checkReadiness()
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                ProviderReadiness.DEGRADED
            }

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
            } else {
                val ex = result.exceptionOrNull()
                if (ex is BundleOversizedException) {
                    return RoutingOutcome.FallbackToDeterministicReview(
                        partialInterpretation = deterministicCandidate.copy(reviewRequired = true),
                        failureReason = "BUNDLE_CANNOT_FIT: ${ex.message}"
                    )
                }
                if (firstFailureReason == null) {
                    firstFailureReason = "${provider.name}_${ex?.javaClass?.simpleName}"
                }
            }
        }

        // Step 5: Graceful Deterministic Partial Result & Review
        return RoutingOutcome.FallbackToDeterministicReview(
            partialInterpretation = deterministicCandidate.copy(reviewRequired = true),
            failureReason = firstFailureReason ?: "NO_AUTHORIZED_PROVIDER_AVAILABLE"
        )
    }

    private class BundleOversizedException(message: String) : IllegalStateException(message)

    private suspend fun executeChunkedPipeline(
        adapter: DocumentInterpreterAdapter,
        modelId: String,
        rawBundle: LlmAnalysisBundle,
        validEvidenceIds: Set<String>,
        lockedInterpretation: DocumentInterpretation
    ): Result<DocumentInterpretation> {
        return try {
            val maxBytes = ModelCapabilityRegistry.defaultModelsByProvider[adapter.provider]
                ?.find { it.modelId == modelId }?.maxInputBytesUtf8 ?: BundleTrimmer.DEFAULT_MAX_UTF8_BYTES

            val trimResult = bundleTrimmer.buildRedactedAndBoundedChunks(rawBundle, maxBytes)
            val chunks = when (trimResult) {
                is BundleTrimResult.Success -> trimResult.chunks
                is BundleTrimResult.BundleCannotFit ->
                    throw BundleOversizedException("${trimResult.reason} (${trimResult.measuredBytes} > ${trimResult.maxBytes})")
            }

            val validatedChunks = chunks.map { chunk ->
                val rawInterpretation = try {
                    adapter.interpret(chunk, modelId).getOrThrow()
                } catch (ce: CancellationException) {
                    throw ce
                }
                validator.validateAndSanitize(rawInterpretation, chunk, validEvidenceIds, lockedInterpretation)
            }

            val first = validatedChunks.first()
            val seenRowIds = mutableSetOf<String>()
            val mergedEvents = validatedChunks.flatMap { it.financialEvents }.filter { event ->
                val rowId = event.sourceRowId
                if (rowId == null) true else seenRowIds.add(rowId)
            }
            val mergedUnresolved = validatedChunks.flatMap { it.unresolvedFields }.distinct()

            Result.success(
                first.copy(
                    financialEvents = mergedEvents,
                    reviewRequired = validatedChunks.any { it.reviewRequired },
                    unresolvedFields = mergedUnresolved
                )
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Result.failure(e)
        }
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
import com.finance.engine.core.ai.ProviderConsentRecord
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
    val consentByProvider: Map<AiEngineProvider, ProviderConsentRecord> = emptyMap(),
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

    state.pendingConsentProvider?.let { provider ->
        AlertDialog(
            onDismissRequest = onDismissProviderConsent,
            title = { Text("Authorize Cloud Financial Metadata Transfer") },
            text = {
                Text(
                    "To interpret ambiguous fields using ${provider.displayName} (Disclosure v${provider.requiredDisclosureVersion}), " +
                        "a redacted, text-only JSON bundle (merchant candidates, dates, amounts, and line descriptions) " +
                        "is transmitted. Camera images stay on your phone, and customer addresses, emails, phone numbers, " +
                        "and unmasked account numbers are redacted before serialization."
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

## 7. Encrypted Persistence, Key Lifecycle, Draft Schema & Post-Purge Provenance

### 7.1 Aligned Room 2.8.5 + SQLCipher 4.19.1 Schema (`Entities.kt`)

In `FinancialEventEntity`, `amountMinor`, `currencyCode`, `currencyScale`, and `eventDateIso` are nullable **only** when `persistenceState == EventPersistenceState.NEEDS_USER_REVIEW_DRAFT.name`, allowing drafts with `ExcessPrecisionFlagged` tokens (`draftRawAmountToken = "12.345"`) to be stored and reviewed without inventing a rounded minor-unit integer.

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
 * Lightweight provenance manifest that survives the 30-day raw OCR/bitmap purge.
 * Uses keyed HMAC-SHA256 (via Google Tink Mac) so normalized values are not stored in plaintext.
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
    val valueHmacSha256Hex: String, // Keyed HMAC-SHA256 of "$documentId:$evidenceId:$normalizedValue"
    val quadRawDisplayNormalizedJson: String,
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
        if (persistenceState == EventPersistenceState.VERIFIED_FINAL.name) {
            require(eventDateIso != null) { "VERIFIED_FINAL event requires non-null eventDateIso" }
            require(amountMinor != null && currencyCode != null && currencyScale != null) {
                "VERIFIED_FINAL event requires non-null amountMinor, currencyCode, and currencyScale"
            }
            require(draftParseIssue == null) { "VERIFIED_FINAL event cannot have unresolved draftParseIssue" }
        }
    }
}

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
    val redactedDiagnosticJson: String?
)
```

### 7.2 Cryptographic Key Lifecycle, Tink `StreamingAead` & `HmacSha256`
1. **Android Keystore Master Key & Tink Keysets:**
   * Provision an `AES256_GCM` master key at `android-keystore://finance_engine_master_key` inside `AndroidKeyStore`.
   * Manage three distinct Tink keysets via `AndroidKeysetManager` wrapped by that Keystore master key:
     1. `file_streaming_keyset` (`StreamingAead` / `AES256_GCM_HKDF_4KB`) for encrypting `.enc` page bitmaps, binding `associatedData = "$documentId:page_$pageNumber:$variant".encodeToByteArray()`.
     2. `vault_aead_keyset` (`Aead` / `AES256_GCM`) for wrapping the 256-bit random SQLCipher database passphrase and the user's optional BYOK API key.
     3. `manifest_hmac_keyset` (`Mac` / `HMAC_SHA256_256BITTAG`) for computing `EvidenceManifestEntity.valueHmacSha256Hex`.
   * **Key Rotation & Key Invalidation Recovery:** Tink `KeysetHandle` supports adding a new primary key version (`KeysetManager.rotate()`) while retaining prior key IDs in the keyset for decrypting existing `.enc` files and verifying older HMAC digests. If the hardware `AndroidKeyStore` key is permanently invalidated (`KeyPermanentlyInvalidatedException`), the app must fail closed, alert the user that encrypted local storage cannot be unlocked, and offer a clean database reset rather than silently falling back to unencrypted storage.
2. **Retention & Cascade Policy (`DocumentRetentionWorker`):**
   * Runs daily via `WorkManager`. For documents older than 30 days with `processingStatus == "VERIFIED"`, it deletes `raw_display_page_{n}.enc`, `clean_ocr_page_{n}.enc`, and raw `OcrElement` rows, sets `EvidenceManifestEntity.sourcePurged = true`, and updates `FinancialDocumentEntity.evidenceRetentionState = "RAW_SOURCE_PURGED_MANIFEST_RETAINED"`.
3. **Android Backup Exclusions (`res/xml/data_extraction_rules.xml`):**
   * Exclude the SQLCipher `.db` file, the encrypted image directory, all Tink keyset preferences, and the AI routing consent DataStore from cloud backup and device-to-device transfer.

---

## 8. Review & Correction UI (`DocumentReviewScreen`)

1. **Split-Pane Interactive Canvas Overlay with Viewport Polygon Clipping:**
   * Top/left pane renders `raw_display_page_{n}.enc` (when `RAW_SOURCE_AVAILABLE`) with an interactive Jetpack Compose `Canvas` overlay.
   * When the user taps any resolved field or `FinancialEventEntity`, the UI looks up its `evidenceIds` in `EvidenceManifestEntity`, clips the 4-corner `NormalizedQuad` against $[0, 1]^2$ using Sutherland-Hodgman polygon clipping, and draws the quadrilateral highlight.
   * **Honest Post-Purge Provenance Badge:** When `sourcePurged == true`, the UI displays: *"Provenance Metadata Retained (Original scan & OCR text purged per 30-day privacy policy; source visual re-verification unavailable)"*.
2. **Draft Amount & Date Resolution:**
   * Events in `NEEDS_USER_REVIEW_DRAFT` state (including events with `draftRawAmountToken` such as `"12.345 USD"` or missing `eventDateIso`) display an inline resolution prompt showing the raw token, the detected parse issue (`ExcessPrecisionFlagged`), and the highlighted source bounding box on the scan so the user can confirm or edit the exact minor-unit amount before transitioning the record to `VERIFIED_FINAL`.

---

## 9. Phased Implementation Plan & Measurable Acceptance Criteria

The coding agent must execute and verify this system in **five sequential phases**, running `./gradlew test` after each phase before proceeding.

### Phase 1: Toolchain Lock, Complete ISO 4217 Money, SQLCipher & Tink Keysets
* **Deliverables:** Validate `libs.versions.toml` (`AGP 8.13.0`, `Gradle 8.14`, `JDK 21`, `Kotlin 2.4.21`, `KSP 2.3.12`, `Room 2.8.5`, `SQLCipher 4.19.1`, `Hilt 2.60.1`, `Compose BOM 2026.09.00`), `Iso4217CurrencyRegistry`, `StrictMoneyParser`, `TinkStreamingFileEncryptor`, `ManifestHmacGenerator`, and Room entities.
* **Exact Invariant Gate (100% Pass Required):**
  * Clean `./gradlew check` dependency resolution across all modules.
  * `Iso4217CurrencyRegistry` resolves `0`, `2`, `3`, and `4` decimal ISO 4217 currencies (`JPY`, `USD`, `PLN`, `ZAR`, `KWD`, `CLF`) and rejects non-monetary codes (`XAU`, `XXX`).
  * `StrictMoneyParser` returns `ExcessPrecisionFlagged` (never rounding silently) on `12.345 USD`, and `FinancialEventEntity` persists that draft with `amountMinor = null`, `draftRawAmountToken = "12.345"`, and `persistenceState = NEEDS_USER_REVIEW_DRAFT`, while throwing `IllegalArgumentException` if `VERIFIED_FINAL` is attempted with `amountMinor = null` or `eventDateIso = null`.

### Phase 2: Vision, Failure-Safe Homography Quadrilaterals & Spatial Layout
* **Deliverables:** `:core:ocr`, `:core:document:vision`, and `:core:document:layout`. Implement PDF `/CropBox` & `/Rotate` normalization, `CoordinateTransformMapper`, Laplacian blur/glare detection, CIELAB extraction, and radian-converted (`Math.toRadians`) rotation-aware line/table reconstruction.
* **Exact Invariant Gate (100% Pass Required):**
  * `CoordinateTransformMapper` returns `ProjectionResult.SingularOrInvalidTransform` on singular matrices ($\det = 0$) or near-zero perspective denominators ($|w| < 10^{-6}$), preserves un-clamped vertex slopes on out-of-bounds boxes (`isPartiallyOutOfBounds = true`), and maps valid projective transforms within $\le 10^{-4}$ normalized error.

### Phase 3: Mutually-Exclusive Signed-Ledger Reconciliation, Hybrid Gate & Strict Validator
* **Deliverables:** `:core:finance:validation`. Implement receipt, bank asset statement, and credit-card liability statement mutually-exclusive bucket reconciliation, the hybrid field-level deterministic gate, and `PostLlmSemanticValidator`.
* **Exact Invariant Gate (100% Pass Required):**
  * `PostLlmSemanticValidator` rejects: (a) mixed evidence-ID lists where only a subset belongs to the matched candidate (`["P01_T0014", "P01_T9999"]`), (b) unverified `statementSummary` balances or period dates, (c) `documentFamily` overrides not in `detectedFamilyCandidates`, (d) event dates matching `DUE_DATE`, `PRINT_DATE`, or `STATEMENT_PERIOD_START`/`END`, (e) silent fallback to `primaryDate` when an event supplies an invalid date, and (f) unverified `dueDateIso` or `billPaymentStatus`.

### Phase 4: AI Capability Registry, Per-Provider Consent Router, `BundleTrimmer` & UI
* **Deliverables:** `:core:ai`. Implement `ModelCapabilityRegistry`, `BundleTrimmer`, `PolicyDrivenAiRouter`, provider adapters, and `AiConfigurationCard`.
* **Exact Invariant Gate (100% Pass Required):**
  * Router and trimmer tests verify: (a) `CancellationException` is rethrown immediately and never triggers fallback providers, (b) `BundleTrimmer` redacts `CUSTOMER_ADDRESS` in both `textCandidates` and `tableRows` cells, masks formatted phone numbers (`(555) 234-5678`) and hyphenated account numbers (`1234-5678-9012`), and returns `BundleTrimResult.BundleCannotFit` when a single oversized row exceeds `12,288` bytes, and (c) per-provider `ProviderConsentRecord` versions are enforced independently.

### Phase 5: End-to-End Review Screen, Retention Worker & 150-Document Split Corpus Benchmark
* **Deliverables:** `:feature:documentcapture`, `:feature:review`, `DocumentRetentionWorker`, and the split evaluation benchmark suite.
* **Corpus Allocation & Held-Out Evaluation Gate:**
  * Partition the 150-document corpus with disjoint merchant templates into:
    * **50-Document Calibration Split** (used for tuning thresholds): `12 retail/restaurant receipts`, `10 utility bills/invoices`, `12 bank statements`, `10 credit-card statements`, `6 refund notes/skewed low-light captures`.
    * **100-Document Held-Out Evaluation Split** (locked for final verification): `25 retail/restaurant receipts`, `20 utility bills/invoices`, `25 bank statements`, `20 credit-card statements`, `10 refund notes/skewed low-light captures`.
  * **Required Held-Out Metrics:**
    * **0% Silent Hallucination Rate:** Zero persisted `VERIFIED_FINAL` events with unverified amounts, mixed evidence IDs, or silent date fallbacks.
    * **Deterministic Gate Metrics:** Report **Exact-Match Accuracy ($\ge 99.0\%$)**, **Gate Coverage ($\ge 60.0\%$ of clean receipts/bills locking `documentTotal` and `primaryDate`)**, and **False-Lock Rate ($\le 0.5\%$)** across the 100 held-out documents.
    * **Row-to-Amount Association Recall & Precision:** $\ge 94.0\%$ across held-out single- and multi-page statements.