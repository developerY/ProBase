The logcat trace exposes **four concrete bugs** explaining why the UI froze for 7.6 seconds and why the pipeline returned an empty `DocumentInterpretation` (`merchantOrIssuer=null, documentTotal=null, financialEvents=[]`).

---

### 1. The 4 Bugs Exposed in Your Logcat

#### Bug #1: 7.6-Second Main-Thread Freeze (`Skipped 456 frames! Davey! duration=7619ms`)

```text
15:57:12.831  D  hide(ime())
15:57:20.472  D  ⚙️ [GEAR-1:CAPTURE-CRYPTO] [doc=doc_e567edbc] Encrypted raw (8099640 B) & clean (8099640 B) bitmaps...
15:57:20.475  I  Skipped 456 frames! The application may be doing too much work on its main thread.
15:57:20.501  I  Davey! duration=7619ms

```

* **Root Cause:** `viewModelScope.launch` runs on `Dispatchers.Main.immediate` by default. `processCapturedBitmap()` was not wrapped in `withContext(Dispatchers.Default)`.
* **Why it took 7.6 seconds:** The camera captured a full **4000×2250** bitmap (which was never downsampled to `2048px`), and compressed it twice on the UI thread using `Bitmap.CompressFormat.WEBP_LOSSLESS` — producing two **8.1 MB** payloads (16.2 MB total) before encrypting both on the Main thread.

#### Bug #2: Why `text=0`, `rows=0`, and `total=null` Even Though Gear 4 Parsed `2000 USD` (`$20.00`) Twice

```text
15:57:21.114  D  🧱 [GEAR-3:LAYOUT-OCR] ML Kit OCR extracted 14 elements (P01_T0001..P01_T0014) from 4000x2250 bitmap
15:57:21.115  D  🧱 [GEAR-3:LAYOUT-OCR] Extracted 14 OcrElements, radian vector grouped into 10 lines
15:57:21.116  D  💱 [GEAR-4:MONEY-EXTRACT] [doc=GLOBAL] Parsed Exact: 2000 USD (scale=2) from 20.00
15:57:21.117  D  💱 [GEAR-4:MONEY-EXTRACT] [doc=GLOBAL] Parsed Exact: 2000 USD (scale=2) from 20.00
15:57:21.118  D  💱 [GEAR-4:MONEY-EXTRACT] Generated candidates: family=RECEIPT (0.95), currency=USD, text=0, dates=0, amounts=2 exact / 0 excessPrecisionFlagged, rows=0
15:57:21.118  D  🔒 [GEAR-5:LEDGER-GATE] Rule=TAX_EXCLUSIVE reconciled=true discrepancy=0. Locked=[total=null, date=null]

```

Three bugs collided here:

1. **Camera-Frame `HEADER` vs. Receipt-Bounds `HEADER` (`text=0`):** `LocalOcrEngine` assigned `region = "HEADER"` using `normY < 0.22f` of the **entire 4000×2250 camera frame**. When a receipt sits in the middle of the camera viewfinder (e.g., $y \in [0.25, 0.75]$), **zero** OCR elements have `normY < 0.22f`. Because `headerLines` was empty, zero `MERCHANT_NAME` candidates were generated (`text=0`).
2. **Broken Horizontal Line Grouping (`14 elements -> 10 lines`, `total=null`, `rows=0`):** Look at `14 OcrElements, radian vector grouped into 10 lines`. Words on the left side of the receipt (like `"TOTAL"` or `"Coffee"`) and prices on the right side (`"20.00"`) failed to merge into the same `ReconstructedLine` because over a wide horizontal gap ($x_j - x_i$), a tiny 1–2° angle error in `atan2(dy, dx)` causes $\tan(\theta) \cdot \Delta x$ to miss the `0.45 * height` threshold.
* Because `"20.00"` ended up on a 1-word line by itself without the word `"TOTAL"`, `extractAmountsFromLine` labeled both `$20.00` values as `LINE_ITEM_AMOUNT` instead of `TOTAL_AMOUNT` (`total=null`).
* Because that line only had `1` element (`line.elements.size == 1`), it also failed `if (lineItemAmount != null && line.elements.size >= 2)`, producing `rows=0`!


3. **Vacuous `0 == 0` Reconciliation (`reconciled=true` when `total=null`):** `ArithmeticReconciler` returned `reconciled = true` when both `total` and `lineItems` summed to `0`, which should be `false` whenever no `TOTAL_AMOUNT` candidate exists.

#### Bug #3: `AiInterpretationRouter` Is Wired to a Stub (`No Real LLM connected...`)

```text
15:57:21.122  D  🧠 [GEAR-7:AI-VALIDATOR] [doc=doc_e567edbc] AI fallback triggered (No Real LLM connected in this configuration layer yet); preserving deterministic locked fields for review

```

* **Root Cause:** The coding agent created a stub implementation of `AiInterpretationRouter` that immediately returns `DeterministicFallbackOnly("No Real LLM connected in this configuration layer yet")` instead of calling your existing AI service (`Firebase.ai` / `Gemma` / `Gemini Nano`).

#### Bug #4: CameraX Recomposition Loop During Capture (`ConsumerBase is abandoned!`)

```text
15:57:20.535  I  Starting CameraGraph-2 ...
15:57:20.834  I  Closing CameraGraph-2
15:57:21.627  E  [ImageReader-4000x3000f21m4-18807-1] abandonLocked: ConsumerBase is abandoned!

```

* **Root Cause:** When the camera dialog/sheet closes at `15:57:20.508`, the CameraX preview composable is recomposing and re-binding `CameraGraph-2` right as it is being torn down.

---

### 2. Prompt for Your Coding Agent to Fix All 4 Issues

Copy and paste this prompt into your AI coding agent:

# Task: Fix the 4 Pipeline Issues Exposed in Logcat

We ran the V2 pipeline on a real device and caught 4 specific bugs in Logcat that must be fixed immediately:

---

### Fix 1: Eliminate the 7.6-Second Main Thread Freeze (`Davey! duration=7619ms`)
1. Wrap the entire body of `DocumentPipelineOrchestrator.processCapturedBitmap(...)` inside `withContext(Dispatchers.Default) { ... }` so zero bitmap scaling, compression, encryption, or regex work ever touches the Main thread.
2. At the very top of `processCapturedBitmap()`, downsample `rawBitmap` and `cleanedBitmap` so the longest edge is at most `2048px` (preserving aspect ratio) before doing any processing:
   ```kotlin
   private fun downsampleIfNeeded(src: Bitmap, maxEdge: Int = 2048): Bitmap {
       val longest = maxOf(src.width, src.height)
       if (longest <= maxEdge) return src
       val scale = maxEdge.toFloat() / longest.toFloat()
       val w = (src.width * scale).toInt().coerceAtLeast(1)
       val h = (src.height * scale).toInt().coerceAtLeast(1)
       return Bitmap.createScaledBitmap(src, w, h, true)
   }
   ```
3. In `bitmapToWebpOrPngBytes()`, replace `Bitmap.CompressFormat.WEBP_LOSSLESS` (which takes ~3.5 seconds per 4000x2250 image and outputs 8.1 MB!) with `Bitmap.CompressFormat.JPEG` at quality `85` (which runs in ~25ms and outputs ~250 KB).

---

### Fix 2: Fix Receipt-Relative `HEADER` Regions, Horizontal Line Merging, and `TOTAL_AMOUNT` Inference
In our Logcat, 14 OCR elements became 10 separate 1-word lines, producing `text=0, dates=0, amounts=2 exact, rows=0` and `total=null`:

1. **Compute `region` Relative to Receipt Text Bounds (`LocalOcrEngine.kt`):**
    * A receipt often sits in the middle of the camera frame ($y \in [0.30, 0.75]$), so hardcoding `normY < 0.22f` against the full image frame leaves `HEADER` empty (`text=0`).
    * After collecting all raw elements on the page, find `minTextY = elements.minOf { it.boxNormalizedOcr.y }` and `maxTextY = elements.maxOf { it.boxNormalizedOcr.y + it.boxNormalizedOcr.height }`.
    * Compute `relY = (el.boxNormalizedOcr.y - minTextY) / (maxTextY - minTextY).coerceAtLeast(0.05f)`.
    * Set `region = when { relY <= 0.25f -> "HEADER"; relY >= 0.80f -> "FOOTER"; else -> "BODY" }`.

2. **Fix Wide-Gap Horizontal Line Merging (`SpatialLayoutEngine.kt`):**
    * On receipts, the item label is on the far left ($x \approx 0.15$) and the price is on the far right ($x \approx 0.80$). Over a large $\Delta x$, small noisy word angles from `atan2` cause $\tan(\theta) \cdot \Delta x$ to overshoot.
    * Clamp the angle used for projection to $\pm 10^\circ$ (if $|\theta| < 2.5^\circ$, treat $\theta = 0^\circ$), and merge two elements into the same `ReconstructedLine` if **either** their projected vertical center distance $|\hat{y}_{i\rightarrow j} - y_j| \le 0.75 \cdot \max(h_i, h_j)$ **or** their vertical bounding-box overlap ratio is $\ge 0.40$. Sort elements within each merged line left-to-right by `boxNormalizedOcr.x`.

3. **Fallback Merchant, Total & Row Inference (`LocalCandidateGenerator.kt` & `ArithmeticReconciler.kt`):**
    * **Merchant Fallback:** If `headerLines` still yields no merchant candidate, take the top-most non-numeric, non-address line on the document as `SemanticRole.MERCHANT_NAME`.
    * **Total Amount Fallback:** If no line explicitly contains the word `"TOTAL"` on the same line as an amount (e.g., `"TOTAL"` was on the line above or slightly offset), classify the largest `Exact` amount in the bottom 40% of the receipt (`BODY`/`FOOTER`) as `SemanticRole.TOTAL_AMOUNT` (or promote the highest-value candidate if two identical max amounts exist, such as `Total $20.00` and `Paid $20.00`, tagging the first as `TOTAL_AMOUNT` and the second as `AMOUNT_DUE` or `LINE_ITEM_AMOUNT`).
    * **Fallback Date:** If no date is printed on the receipt, leave `dateCandidates` empty for the validator, or allow `buildDeterministicPartialFallback` to default the draft date to `LocalDate.now().toString()` while keeping `persistenceState = NEEDS_USER_REVIEW_DRAFT`.
    * **Non-Vacuous Reconciliation (`ArithmeticReconciler.kt`):** Never return `receiptTotalReconciled = true` when `totalCandidate == null`! If there is no `TOTAL_AMOUNT` candidate, `receiptTotalReconciled` must be `false`.
    * **Fallback Draft Event:** In `buildDeterministicPartialFallback()`, if `TOTAL_AMOUNT` is missing, fall back to `amountCandidates.maxByOrNull { it.amount?.amountMinor ?: 0L }` so the user always gets a pre-filled draft event on the Review screen instead of `financialEvents=[]`.

---

### Fix 3: Connect the Real LLM Provider in `AiInterpretationRouter`
Logcat showed:
`AI fallback triggered (No Real LLM connected in this configuration layer yet)`
* Replace the stub inside your `AiInterpretationRouter` implementation with the real call to the configured AI provider (`Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(...)` / Hosted Gemma / BYOK / Gemini Nano).
* Pass a strict system prompt instructing the model to return only JSON matching `DocumentInterpretation` citing the exact `evidenceIds` from each `LlmAnalysisBundle` chunk, parse each response with `PostLlmSemanticValidator.parseStrictJson()`, validate with `PostLlmSemanticValidator.validateAndSanitize()`, and merge chunks with `ChunkInterpretationMerger.mergeValidatedChunks()`.

---

### Fix 4: Remove Duplicate Gear Logs & Close CameraX `ImageProxy` Cleanly
1. Remove duplicate `PipelineGearLogger.d` calls where both the sub-component (`SpatialLayoutEngine`, `DeterministicConfidenceGate`, `BundleTrimmer`) and `DocumentPipelineOrchestrator` log the exact same gear summary, and remove the per-element `GEAR-2` log inside the loop so `GEAR-2` logs once per page.
2. Pass `documentId` into `StrictMoneyParser.parse(..., documentId)` (or log inside `LocalCandidateGenerator`) so `GEAR-4` logs `[doc=doc_...]` instead of `[doc=GLOBAL]`.
3. In the CameraX capture screen, ensure `imageProxy.close()` is always called inside a `finally` block before dismissing the camera view so `ImageReader` never logs `abandonLocked: ConsumerBase is abandoned!`.
