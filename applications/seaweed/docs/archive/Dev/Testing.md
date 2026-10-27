Here is the complete **AI Coding Agent Prompt** to instrument every stage of the pipeline with structured `Log.d` telemetry and build the End-to-End (E2E) test suite, followed by your **Step-by-Step Testing & Logcat Guide**.

---

## Part 1: Prompt for Your AI Coding Agent

Copy and paste the prompt below directly into your AI coding agent:

```markdown
# Task: Implement Structured Pipeline Gear Telemetry (`Log.d`) & End-to-End (E2E) Test Suite

You are building the observability and End-to-End (E2E) verification layer for the **SeaWeed Financial Document Intelligence Engine** and **Envelope Intervention System**.

We need to see "all the gears working" in real time via `Log.d` across every stage of the pipeline while strictly preserving our production privacy guarantees, and we need an automated E2E test suite that exercises the entire lifecycle from capture to database persistence, Envelope intervention, and 30-day retention purge.

---

### 1. Build `PipelineGearLogger.kt` (Dual Android `Log.d` + JVM Test Console Tracer)

Create a centralized logger in `:core:model` (or `:core:common`) named `PipelineGearLogger`.
* **Unified Tag:** `"SeaWeedEngine"`
* **Security Gate:** It must ONLY emit logs when `isDebugEnabled = true` (wired to `BuildConfig.DEBUG` in Android modules, and enabled in JVM unit tests). In release builds (`isDebugEnabled = false`), it must be a strict no-op so zero financial metadata ever reaches system logs.
* **Dual Output:** In JVM unit tests where `android.util.Log` is stubbed or unmocked, automatically fall back to `println("D/$TAG: [$gear] $message")` so `./gradlew test --info` prints the exact same gear trace as `adb logcat`.

```kotlin
package com.example.financeengine.core.common

import android.util.Log

enum class PipelineGear(val prefix: String) {
    GEAR_1_CAPTURE_CRYPTO("⚙️ [GEAR-1:CAPTURE-CRYPTO]"),
    GEAR_2_VISION_HOMOGRAPHY("📐 [GEAR-2:VISION-HOMOGRAPHY]"),
    GEAR_3_LAYOUT_OCR("🧱 [GEAR-3:LAYOUT-OCR]"),
    GEAR_4_MONEY_EXTRACT("💱 [GEAR-4:MONEY-EXTRACT]"),
    GEAR_5_LEDGER_GATE("🔒 [GEAR-5:LEDGER-GATE]"),
    GEAR_6_REDACT_TRIM("🛡️ [GEAR-6:REDACT-TRIM]"),
    GEAR_7_AI_VALIDATOR("🧠 [GEAR-7:AI-VALIDATOR]"),
    GEAR_8_DB_ENVELOPE("🏦 [GEAR-8:DB-ENVELOPE]")
}

object PipelineGearLogger {
    const val TAG = "SeaWeedEngine"

    @Volatile
    var isDebugEnabled: Boolean = true // Bind to BuildConfig.DEBUG in Application.onCreate()

    @Volatile
    var forceStdoutForTests: Boolean = false

    inline fun d(gear: PipelineGear, docId: String = "GLOBAL", message: () -> String) {
        if (!isDebugEnabled) return
        val formatted = "${gear.prefix} [doc=$docId] ${message()}"
        if (forceStdoutForTests) {
            println("D/$TAG: $formatted")
            return
        }
        try {
            val written = Log.d(TAG, formatted)
            if (written <= 0) println("D/$TAG: $formatted")
        } catch (_: Throwable) {
            println("D/$TAG: $formatted")
        }
    }
}
```

---

### 2. Instrument All 8 Gears Across the Pipeline

Insert `PipelineGearLogger.d(...)` calls at these exact decision points:

1. **`GEAR_1_CAPTURE_CRYPTO` (`TinkStreamingFileEncryptor.kt` & Capture/PDF Import):**
    * Log source type (`CAMERA_SCAN` vs `PDF_TEXT_LAYER`), page count, raw vs. cleaned bitmap dimensions ($W_r \times H_r$, $W_o \times H_o$), and Tink `StreamingAead` encryption completion with the Associated Data binding (`AD = "$documentId:$pageNumber:$variant"`) and encrypted file byte size.
2. **`GEAR_2_VISION_HOMOGRAPHY` (`CoordinateTransformMapper.kt` & Color Extractor):**
    * Log the $3 \times 3$ normalized homography determinant $\det(H_{\text{norm}})$, whether projection succeeded (`ProjectionResult.Success` with sample `NormalizedQuad` vertices and `requiresViewportClipping`) or failed (`SingularOrInvalidTransform`), and the dominant header/background CIELAB/hex colors.
3. **`GEAR_3_LAYOUT_OCR` (`SpatialLayoutEngine.kt`):**
    * Log total `OcrElement` count, page-scoped ID range (e.g., `P01_T0001..P01_T0048`), radian-converted vector line merges ($\theta_i = \theta_i^{\circ} \times \pi/180$, $|\hat{y}_{i\rightarrow j} - y_j|$), detected column clusters, and reconstructed table row/cell counts.
4. **`GEAR_4_MONEY_EXTRACT` (`Iso4217CurrencyRegistry.kt` & `StrictMoneyParser.kt`):**
    * Log every candidate extraction count by `SemanticRole` and every `StrictMoneyParser.parse()` outcome (`Exact(amountMinor, currencyCode, scale)` vs. `ExcessPrecisionFlagged(rawToken, observedScale, expectedScale)`).
5. **`GEAR_5_LEDGER_GATE` (`ArithmeticReconciler.kt` & `DeterministicConfidenceGate.kt`):**
    * Log the reconciliation formula selected (`TAX_EXCLUSIVE`, `TAX_INCLUSIVE`, `BANK_ASSET_LEDGER`, `CC_LIABILITY_LEDGER`), exact equation balance (`discrepancyMinorUnits`), which fields passed the threshold into `LockedDeterministicFields`, and `fullDocumentResolvedWithoutAi = true/false`.
6. **`GEAR_6_REDACT_TRIM` (`BundleTrimmer.kt`):**
    * Log how many PII candidates/cells (`CUSTOMER_ADDRESS`, `ACCOUNT_HOLDER_NAME`, `CUSTOMER_PHONE`, `CUSTOMER_EMAIL`) were stripped, how many regex redactions (`[REDACTED_EMAIL]`, `[REDACTED_PHONE]`, `****1234`) fired, the final chunk count, and the exact serialized UTF-8 byte size of each chunk against the `12,288` byte cap (or `BundleCannotFit`).
7. **`GEAR_7_AI_VALIDATOR` (`AiInterpretationRouter`, `PostLlmSemanticValidator.kt` & `ChunkInterpretationMerger.kt`):**
    * Log the active provider/model selected, latency in ms, and inside `PostLlmSemanticValidator` log every field/event validation decision:
        * `"ACCEPTED field=<name> evidence=<ids> (lockedOverride=<bool>)"`
        * `"REJECTED field=<name> reason=<mixed_evidence_ids | role_mismatch | payment_status_text_contradiction | invented_description>"`
    * Inside `ChunkInterpretationMerger`, log deduplicated event counts (`EventDedupKey`) and any cross-chunk conflicts (`conflict:<fieldName>`).
8. **`GEAR_8_DB_ENVELOPE` (`FinancialDocumentDao.kt`, `ManifestHmacGenerator.kt`, `EnvelopeInterventionEngine`, `PurgeExpiredEvidenceWorker.kt`):**
    * Log SQLCipher transaction commits (`VERIFIED_FINAL` vs `NEEDS_USER_REVIEW_DRAFT`), Tink `Mac` HMAC-SHA256 digest generation (`evidenceId -> hmacPrefix`), Category-to-Envelope balance evaluation (`currentSpentMinor + eventAmountMinor vs envelopeLimitMinor -> InterventionDialog triggered: true/false`), and 30-day retention purge execution (`OcrElement` rows deleted, `.enc` files removed, `rawEvidencePurged = true`).

---

### 3. Implement the End-to-End (E2E) Test Suite (`EndToEndPipelineGearsTest.kt`)

Create a comprehensive E2E test suite that sets `PipelineGearLogger.forceStdoutForTests = true` in `@Before` and verifies all 8 gears across five mandatory E2E scenarios:

1. **`e2eScenario1_curledReceipt_deterministicLock_bypassesLlmAndCommits()`**:
    * Feeds synthetic curled receipt OCR elements ($6^\circ$ rotation drift across $X$), verifies `SpatialLayoutEngine` groups the line via radian tangent projection, reconciles line items + tax = total (`2007` minor units `USD`), locks all required fields in `DeterministicConfidenceGate`, bypasses the AI router (`0` LLM calls), generates HMAC manifests, and persists a `VERIFIED_FINAL` `PURCHASE_EXPENSE`.
2. **`e2eScenario2_ambiguousInvoice_piiRedacted_hallucinationsRejected_lockedTotalPreserved()`**:
    * Feeds an utility bill where `lockedDocumentTotal` (`$145.00`) is locked deterministically, while merchant and payment status require AI interpretation.
    * Seeds customer address, email, phone number, and unmasked 16-digit account number into candidates, table cells, and notes; asserts `BundleTrimmer` strips 100% of raw PII and stays $\le 12,288$ bytes.
    * Mock AI Router returns: (a) a hallucinated total of `$999.00` (must be overridden by `lockedDocumentTotal = $145.00`), (b) `billPaymentStatus = PAID_IN_FULL` citing an OCR label reading `"STATUS: UNPAID"` (must be rejected to `UNKNOWN`), and (c) an event citing mixed evidence `["P01_T0010", "P01_T9999"]` (must be rejected).
3. **`e2eScenario3_excessPrecisionAmount_savedAsDraft_thenResolvedInReviewUi()`**:
    * Feeds an OCR amount token `"12.345"` `USD`. Asserts `StrictMoneyParser` returns `ExcessPrecisionFlagged`, pipeline persists the event as `NEEDS_USER_REVIEW_DRAFT` with `amountMinor = null` and `draftRawAmountToken = "12.345"`, and simulates user correction to `1235` (`$12.35`), transitioning the row to `VERIFIED_FINAL`.
4. **`e2eScenario4_multiChunkStatement_mergesRowSplitAndFlagsHeaderConflict()`**:
    * Feeds a 25-row statement split by `BundleTrimmer` into 2 chunks. Verifies two distinct events on the same row (purchase + inline fee) both survive deduplication, while a conflicting unlocked `closingBalance` between Chunk 0 and Chunk 1 sets `closingBalance = null`, adds `"conflict:statementSummary.closingBalance"`, and forces `reviewRequired = true`.
5. **`e2eScenario5_envelopeInterventionAnd30DayRetentionPurgeLifecycle()`**:
    * Maps `categoryId = "coffee"` and `categoryId = "lunch"` to the `"Food & Dining"` Envelope (`limitMinor = 5000` / `$50.00`). Commits `$42.00` in prior spending, then runs a `$15.00` receipt through the pipeline; asserts the Envelope gate triggers `InterventionRequired(overageMinor = 700)` before saving.
    * Advances clock by 31 days, runs `PurgeExpiredEvidenceWorker`, and asserts `.enc` image files and `OcrElementEntity` rows are deleted, `rawEvidencePurged == true`, `EvidenceManifestEntity` HMAC digests remain valid, and `deleteSourceDocumentPreservingVerifiedEvents()` leaves the verified `FinancialEventEntity` intact with `documentId = null`.

```

---

## Part 2: Instructions to Test the Whole System & Watch the Gears Spin

Once your AI coder finishes applying the prompt above, use these workflows in your terminal and Android Studio to verify every subsystem.

### Step 1: Run the Full E2E Gear Suite in Terminal (with Live Stdout Trace)

Open your Zsh terminal at the project root and run the E2E suite with `--info` so `PipelineGearLogger` streams every gear transition directly to your console:

```zsh
./gradlew testDebugUnitTest --tests "*EndToEndPipelineGearsTest*" --info | grep -E "SeaWeedEngine|PASSED|FAILED"

```

To run the entire unit test suite (including `StrictMoneyParserTest`, `CoordinateTransformMapperTest`, `ValidatorProvenanceAndRoleTest`, `BundleTrimmerRedactionAndBudgetTest`, `ChunkInterpretationMergerTest`, and `CorpusBenchmarkTest`):

```zsh
./gradlew testDebugUnitTest --continue

```

### Step 2: Watch the Live Gears in `adb logcat` (Color-Highlighted Zsh Filter)

When running the app on a physical device or emulator, open a dedicated terminal tab and run this `adb logcat` command to filter strictly for `SeaWeedEngine`:

```zsh
adb logcat -c && adb logcat -v time -s SeaWeedEngine:D

```

If you are using the **Logcat window in Android Studio**, paste this query into the filter bar:

```text
tag:SeaWeedEngine

```

You can also filter for a single gear when debugging a specific stage:

* Vision & Homography only: `tag:SeaWeedEngine "GEAR-2"`
* PII Redaction & Byte Budgets only: `tag:SeaWeedEngine "GEAR-6"`
* Post-LLM Validator rejections only: `tag:SeaWeedEngine "GEAR-7"`
* SQLCipher, HMAC & Envelopes only: `tag:SeaWeedEngine "GEAR-8"`

### Step 3: Expected Healthy 8-Gear Logcat Trace

When you scan a receipt in the app, you should see this exact 8-gear sequence fire in order:

```text
D/SeaWeedEngine: ⚙️ [GEAR-1:CAPTURE-CRYPTO] [doc=doc_8f92] Source=CAMERA_SCAN pages=1 raw=1536x2048 clean=1480x1990
D/SeaWeedEngine: ⚙️ [GEAR-1:CAPTURE-CRYPTO] [doc=doc_8f92] Tink StreamingAead encrypted raw_display_page_1.enc (AD=doc_8f92:1:raw, 184320 bytes)
D/SeaWeedEngine: 📐 [GEAR-2:VISION-HOMOGRAPHY] [doc=doc_8f92] det(H_norm)=0.9842 -> ProjectionResult.Success(clipping=false) HeaderHex=#264D3A
D/SeaWeedEngine: 🧱 [GEAR-3:LAYOUT-OCR] [doc=doc_8f92] Extracted 34 OcrElements (P01_T0001..P01_T0034), radian vector grouped into 14 lines, 4 table rows
D/SeaWeedEngine: 💱 [GEAR-4:MONEY-EXTRACT] [doc=doc_8f92] Parsed Exact: 1600 USD (scale=2) from P01_T0018; 250 USD from P01_T0021; 157 USD tax from P01_T0028; 2007 USD total from P01_T0031
D/SeaWeedEngine: 🔒 [GEAR-5:LEDGER-GATE] [doc=doc_8f92] Rule=TAX_EXCLUSIVE reconciled=true discrepancy=0 minor units. Locked=[TOTAL_AMOUNT=2007 USD, TAX_AMOUNT=157 USD, PRIMARY_DATE=2026-10-25] fullBypass=false (category ambiguous)
D/SeaWeedEngine: 🛡️ [GEAR-6:REDACT-TRIM] [doc=doc_8f92] Stripped 2 PII candidates (CUSTOMER_ADDRESS, CUSTOMER_PHONE), masked 1 account number (****4829). Emitted 1 chunk (3412 / 12288 UTF-8 bytes)
D/SeaWeedEngine: 🧠 [GEAR-7:AI-VALIDATOR] [doc=doc_8f92] Routed to Firebase AI Logic (gemini-3.8-flash) latency=640ms
D/SeaWeedEngine: 🧠 [GEAR-7:AI-VALIDATOR] [doc=doc_8f92] ACCEPTED documentTotal=2007 USD (lockedOverride=true, evidence=[P01_T0031])
D/SeaWeedEngine: 🧠 [GEAR-7:AI-VALIDATOR] [doc=doc_8f92] ACCEPTED event[0] kind=PURCHASE_EXPENSE amount=2007 USD category=coffee evidence=[P01_T0031] state=VERIFIED_FINAL
D/SeaWeedEngine: 🏦 [GEAR-8:DB-ENVELOPE] [doc=doc_8f92] Category 'coffee' maps to Envelope 'Food & Dining' (spent=4200 + new=2007 = 6207 > limit=5000). Triggering InterventionDialog (overage=1207 minor units)!
D/SeaWeedEngine: 🏦 [GEAR-8:DB-ENVELOPE] [doc=doc_8f92] Generated 4 HMAC-SHA256 EvidenceManifest digests; committed FinancialEventEntity(state=VERIFIED_FINAL) to SQLCipher DB.

```

### Step 4: Interactive Manual QA Checklist (5-Minute End-to-End Walkthrough)

1. Verify AI Configuration & Routing (`AiConfigurationCard`):


* Open the AI Settings screen. Toggle between **Hosted Cloud**, **Firebase AI Logic**, **BYOK**, and **On-Device Local AI (Gemini Nano)**.


* Tap **Test Connection** and verify the status indicator dots update and `GEAR-7` logs the capability check.


2. Test the 4-Corner Polygon Review Overlay (`DocumentReviewScreen.kt`):


* Scan a physical thermal receipt at a slight $15^\circ$ angle.
* On the Review screen, tap the extracted **Total** and **Line Items**. Confirm the `Canvas` overlay draws an angled 4-corner quadrilateral (`NormalizedQuad`) tightly around the text on the original photo.




3. Trigger an Envelope Intervention (`concept.md`):


* Create a **"Food & Dining"** Envelope with a `$20.00` limit containing the *Coffee* and *Lunch* categories.


* Scan a restaurant receipt for `$25.00`. Confirm `GEAR-8` intercepts the save and presents the `InterventionDialog` ("Transparent Friction") before the transaction commits.




4. Verify Cryptographic Encryption & 30-Day Purge Badge:


* In Android Studio's **Device Explorer**, navigate to `/data/data/<your.package.name>/files/evidence_images/`. Attempt to open `raw_display_page_1.enc`—confirm it is unreadable Tink `StreamingAead` ciphertext.


* Trigger `PurgeExpiredEvidenceWorker` (via WorkManager test trigger or debug button). Re-open the document in `DocumentReviewScreen` and confirm the image canvas is replaced by the badge: *"Provenance Metadata Retained (Original scan & OCR text purged per 30-day privacy policy; source visual re-verification unavailable)"*.