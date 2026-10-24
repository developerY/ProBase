# Seaweed Financial Document Engine: Implementation Report

This document details the complete, end-to-end implementation of the Seaweed Financial Document Intelligence Engine, built according to the strict architectural guidelines outlined in the `AGENT_BUILD_SPEC.md`. 

The system relies on a local-first, deterministic validation pipeline that minimizes the reliance on untrusted LLM outputs, enforces exact mathematical reconciliation, strictly isolates AI configurations, and respects stringent PII redaction and cryptographic retention policies.

---

## Phase 1: Core Domain, Currency, and Secure Persistence
**Goal:** Establish reproducible financial rules and a secure, encrypted persistence layer.

* **Strict ISO 4217 Currency Support (`Iso4217CurrencyRegistry.kt`)**: Implemented a hardcoded, deterministic currency registry to ensure scale sizes (e.g., 0 for JPY, 2 for USD, 3 for KWD, 4 for CLF) do not vary by Android OS version or OEM.
* **Non-Rounding Money Parser (`StrictMoneyParser.kt`)**: Parses minor units securely. Instead of performing silent, lossy decimal rounding, it immediately flags inputs like `"12.345 USD"` with an `ExcessPrecisionFlagged` error, forcing the system into a user-review state.
* **Cryptographic Data Protection**: 
  * `TinkStreamingFileEncryptor.kt`: Encrypts raw page bitmaps to disk via Google Tink's `StreamingAead` using `AES256_GCM_HKDF_4KB`, enforcing associated data (`AD`) to prevent file swapping.
  * `ManifestHmacGenerator.kt`: Generates HMAC-SHA256 digests of verified data, allowing us to purge OCR/raw data entirely after 30 days while mathematically proving provenance.
* **Strict Room Relationships (`Entities.kt` & `FinancialDocumentDao.kt`)**: Structured the complete relational database using integer minor units. Configured `onDelete = ForeignKey.SET_NULL` for events to survive source-document deletion, and mapped clear distinctions between purging evidence metadata vs. outright deletion.

---

## Phase 2: Vision, Homography Transforms & Spatial Layout
**Goal:** Mathematically process raw image and text data accounting for camera perspective and paper distortion.

* **Failure-Safe Homography (`CoordinateTransformMapper.kt`)**: 
  * Maps normalized axis-aligned OCR rectangles (`NormalizedRect`) into arbitrary 4-corner perspective quadrilaterals (`NormalizedQuad`). 
  * Safely calculates the 3x3 matrix determinant, explicitly failing out with `ProjectionResult.SingularOrInvalidTransform` on singular/invalid matrices to prevent silent projection corruptions.
* **Curved Paper Spatial Grouping (`SpatialLayoutEngine.kt`)**: 
  * Groups isolated OCR words into coherent physical lines by accommodating the curvature of physical thermal receipts. 
  * Uses vector math and radian conversions (`Math.toRadians`) to calculate a projected baseline ray, grouping elements if their rotation angle and vertical drift fall within allowed thresholds.

---

## Phase 3: Deterministic Gating & Semantic Validation
**Goal:** Enforce a "trust but verify" gate over all AI-extracted information, linking all outputs to provable visual evidence.

* **Domain Modeling (`DomainModels.kt`)**: Created airtight Kotlin data classes tracking extraction provenance, semantic roles (`MERCHANT_NAME`, `TOTAL_AMOUNT`), and event persistence states (`VERIFIED_FINAL`, `NEEDS_USER_REVIEW_DRAFT`).
* **Hybrid Gate (`DeterministicConfidenceGate.kt`)**: Before querying an AI model, the engine checks local regex candidates against internal accounting arithmetic. If amounts reconcile exactly and confidence is high, it locks the fields (e.g., `lockedDocumentTotal`), preventing the LLM from hallucinating or altering them.
* **Candidate-Bound Verification (`PostLlmSemanticValidator.kt`)**: 
  * Validates every single field returned by the AI parser against the actual extracted candidates. 
  * Rejects hallucinated data: If the LLM generates a transaction date or payment status that does not trace back to explicit `evidenceIds` extracted from the page, it gets rejected and marked for human review.

---

## Phase 4: AI Router, PII Redaction & Multi-Chunk Merging
**Goal:** Safely interact with multi-provider AI backends (Gemini Nano, Firebase Cloud, BYOK) while protecting privacy and fitting within token budgets.

* **Rigorous PII Redaction (`BundleTrimmer.kt`)**:
  * Strips emails, phone numbers, and account sequences via strict Regex, replacing them with placeholders (`[REDACTED_EMAIL]`, `****1234`).
  * Removes exact fields tagged with PII semantic roles (e.g., `CUSTOMER_ADDRESS`).
  * Enforces a hard `12,288` UTF-8 byte budget limit per request to prevent token window overruns, dividing data into manageable chunks (`INITIAL_ROW_CHUNK_SIZE`).
* **Conflict-Aware Merging (`ChunkInterpretationMerger.kt`)**: When a multipage statement is analyzed across several chunks, this merger deduplicates identical rows safely and strictly flags cross-chunk conflicts (e.g., Chunk A saying total is $10.00 and Chunk B saying $20.00), nulling the value and requiring user review.
* **Configurable AI Router Engine Integration**: Swapped raw Google AI SDK calls with Firebase Vertex AI (`Firebase.ai(backend = GenerativeBackend.googleAI())`) allowing App Check enforcement.

---

## Phase 5: Review UI & Retention Lifecycle
**Goal:** Present transparent, honest UI feedback to the user and rigorously enforce data privacy policies.

* **Retention Worker (`PurgeExpiredEvidenceWorker.kt`)**: A WorkManager CoroutineWorker designed to routinely purge physical encrypted images and raw OCR tables for documents older than 30 days, satisfying strict privacy policies.
* **Interactive UI & Provenance Status (`DocumentReviewScreen.kt`)**: 
  * Renders a `Canvas` that draws the exact 4-corner `NormalizedQuad` highlights over the document so the user can verify where the AI extracted its data.
  * Employs "Honest Badging": If the 30-day retention worker has purged the raw scan, it gracefully replaces the image with an explanatory placeholder rather than breaking: *"Provenance Metadata Retained (Original scan & OCR text purged per 30-day privacy policy; source visual re-verification unavailable)"*.
* **Evaluation Benchmark Suite (`CorpusBenchmarkTest.kt`)**: Established an automated invariant testing suite checking held-out corpus sets for a 0% Silent Hallucination Rate, ≥99% match accuracy, and ≤0.5% false-lock rate.

---

## System Integration & Modularity
* **Isolated `features/ai` Module**: 
  * Ensured `AiConfigurationCard` and its underlying settings are strictly decoupled. AI configurations depend strictly on `:core:data` and `:core:ui`. 
  * The AI module is completely reusable and has been successfully hooked into GoSwift, Seaweed, PhotoDo, and KoColor.
* **Material 3 Theming Integration**: Rewrote the `AiConfigurationCard` UI colors to use native `MaterialTheme.colorScheme` properties (`surfaceContainer`, `onSurface`, `onPrimary`). This allowed the single component to seamlessly drop into GoSwift's dark mode, Seaweed's minimal theme, and KoColor's custom palettes without UI anomalies.