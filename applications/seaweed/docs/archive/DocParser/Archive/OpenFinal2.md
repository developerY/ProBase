This is a substantial improvement. The specification is now much closer to a production architecture: it separates document understanding from financial events, protects deterministic values from model overrides, makes cloud consent provider-specific, and defines concrete persistence and acceptance criteria.

My rating: 8/10 for architecture, around 6.5–7/10 for implementation readiness. I would keep this as the engineering specification, but I would fix the issues below before asking a coding agent to implement it. Several are correctness bugs in the actual Kotlin examples, not hypothetical concerns.

## What is working well

The strongest parts are:

- Local-first processing: OCR, image preprocessing, candidate extraction, and arithmetic checks happen locally. Only the minimized text bundle goes to the cloud.
- Hybrid deterministic gate: The design can lock trustworthy amounts and dates without forcing every document through an LLM.
- Candidate-based provenance: You are moving in the right direction by requiring returned values to match extracted evidence and semantic roles.
- Separate financial event types: Receipts, unpaid bills, transfers, refunds, and statement balances are not all treated as expenses.
- Explicit provider policy: Cloud consent and fallback authorization are separate, and local-only mode is defined.
- Practical implementation phases: The acceptance tests provide a useful starting point for validating each subsystem.

The remaining problems are concentrated in implementation details that could undermine those strengths.

## The issues I would fix first

1\. The semantic validator still has provenance gaps

Critical

In `PostLlmSemanticValidator`, event amount validation requires only that at least one of an event's evidence IDs overlaps the matched candidate's evidence IDs. A mixed evidence list can therefore include one ID belonging to the amount candidate and additional IDs belonging to unrelated evidence. That is weaker than the stated requirement that a single candidate own the entire evidence set.&#x20;

ai_financial_document_engine_specification_final.md



There are other important gaps in this same validator:

- `statementSummary` is returned without validating its opening balance, closing balance, minimum payment, or period dates.
- `documentFamily` is not validated against authoritative document-family candidates.
- `dueDateIso` and `billPaymentStatus` on financial events are not validated.
- Event dates can match an unrelated date candidate; the current role check excludes `PRINT_DATE` but can still accept a due date or statement-period date as a transaction date.
- An invalid event date can fall back to the primary date, which may assign a plausible but incorrect date to a transaction.

Fix: Validate every returned field independently against an authoritative candidate, with field-specific evidence provenance. For events, require the amount, date, description, event kind, and source row to satisfy their own validation rules. Reject mixed or ambiguous provenance rather than accepting a partial evidence-ID overlap.

2\. `BundleTrimmer` does not guarantee its advertised byte limit or redaction

Critical

The trimmer reduces row chunk sizes, then calls `enforceByteBudget()`. But that function only filters certain text candidates and does not measure the resulting bundle again or fail if it remains over the limit. A large single row, or oversized candidate metadata repeated in every chunk, can still exceed 12,288 bytes.&#x20;

ai_financial_document_engine_specification_final.md



Its PII protection is also incomplete. Customer addresses are excluded from `textCandidates`, but table cells with the `CUSTOMER_ADDRESS` role remain in `redactedRows`. The regexes do not reliably remove formatted telephone numbers or account numbers containing separators. The UI's assertion that customer addresses and full account numbers are never sent is therefore stronger than the actual code supports.&#x20;

ai_financial_document_engine_specification_final.md



Fix: Redact by semantic role in every serialized structure, add robust handling for formatted identifiers and telephone numbers, and enforce the byte limit with a final measured check. If a mandatory candidate cannot fit, return an explicit `BundleCannotFit` outcome and route the document to local processing or manual review. Never transmit an oversized bundle as a fallback.

3\. The dependency catalog is not yet a verified, buildable baseline

Build blocker

Kotlin 2.4.21 is a real release, and the official release listing currently shows KSP 2.3.12 as its latest release. The proposed KSP version `2.4.21-1.0.32` does not appear in that official release list, so I would not trust it without a successful Gradle resolution.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://kotlinlang.org\&sz=32)

Kotlin Documentation

+1



Several other versions check out: Room 2.8.5 is officially released, SQLCipher Android 4.19.1 exists, and Firebase BoM 35.1.0 is the current release listed by Firebase.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers

+2



But the catalog alone is not sufficient to prove compatibility. It lacks the complete Android Gradle Plugin, Gradle wrapper, JDK, Kotlin plugin, Compose compiler, and processor/plugin configuration needed to reproduce the build.

Fix: Select an actual KSP release compatible with the chosen Kotlin and Android Gradle Plugin versions, lock the complete toolchain, and require a clean build plus dependency resolution in CI before calling the baseline audited.

4\. The draft event model cannot represent one of its required review cases

The design explicitly says an amount flagged for excess decimal precision can become a `NEEDS_USER_REVIEW_DRAFT`. However, `ResolvedFinancialEvent.amount` is non-null, and `FinancialEventEntity.amountMinor` is also non-null. An invalid amount such as `12.345 USD` has no valid two-decimal `CurrencyAmount` to persist.

The model and persistence contract therefore cannot fully represent the workflow they describe.&#x20;

ai_financial_document_engine_specification_final.md

ai_financial_document_engine_specification_final.md



Fix: Introduce an explicit draft-resolution representation that can retain the raw amount token, currency hypothesis, parse issue, and source evidence without inventing a minor-unit amount. A draft should become `VERIFIED_FINAL` only once all required financial fields are valid and confirmed.

5\. Coordinate mapping and PDF confidence need stricter handling

`CoordinateTransformMapper` returns the original point when the homography denominator approaches zero. That makes a failed projection look like a valid coordinate. It also clamps projected coordinates individually, which can disguise a broken transform or distort a quadrilateral.&#x20;

ai_financial_document_engine_specification_final.md



The PDF fast path also assigns `confidence = 1.0f` to extracted text and an identity homography. Extracting a digital text layer avoids OCR, but it does not prove the extracted content is semantically correct. PDF page rotation, crop boxes, coordinate origins, and user-space units also need explicit transformation handling.

Fix: Return a projection error for singular or non-finite results, preserve out-of-range points until polygon clipping, and test the complete coordinate chain. Treat PDF text extraction as a different provenance method rather than perfect recognition.

## Additional corrections before implementation

### Currency registry and spatial layout

The class is named `Iso4217CurrencyRegistry`, but the supplied currency map contains only a subset of valid ISO 4217 currency codes. It will reject legitimate currencies that are not included.&#x20;

ai_financial_document_engine_specification_final.md



Use a complete, versioned source of ISO 4217 currency metadata, including minor-unit scales and currency changes. Also, `rotationDegrees` is explicitly measured in degrees, whereas the line-projection equation uses `tan(θ)`. The implementation must convert degrees to radians before calling the Kotlin tangent function.

### Provider consent and fallback routing

The policy is much better than a single cloud-consent Boolean, but there is still a subtle inconsistency: consented providers are held in a set while the accepted disclosure version and timestamp are global. If disclosures or service terms differ by provider, track the accepted disclosure version and timestamp per provider.

There is also a routing decision to make explicit. When the default primary provider is cloud Gemma and the user has not consented to that provider, the router immediately returns `ConsentRequired`, even if an authorized on-device fallback is available. This may be an acceptable UX choice, but it should be deliberate and tested.&#x20;

ai_financial_document_engine_specification_final.md

ai_financial_document_engine_specification_final.md



Finally, `runCatching` around suspend operations can capture coroutine cancellation as a regular failure. Rethrow `CancellationException` so a canceled import or interpretation cannot continue down the provider chain.

### Financial reconciliation

The formulas are a good start, but define the ledger sign convention and prevent double counting. In a bank statement, fees may already be included among debits; in a credit-card statement, payments and refunds may both appear in the issuer's credit totals. The implementation needs to classify each row once and reconcile the resulting signed ledger, rather than applying overlapping categories.

Also validate `statementSummary` independently and prevent a model-provided document family from bypassing document-family-specific guardrails.

### Encryption and retention

Tink `StreamingAead` and associated data are appropriate directions for per-file authenticated encryption, but the specification should describe how the Tink keyset is generated and wrapped by an Android Keystore-backed key, and how the SQLCipher database key is created, retained, rotated, and invalidated.&#x20;

ai_financial_document_engine_specification_final.md



For the evidence manifest, choose a precise digest scheme rather than leaving “HMAC-SHA256 or salted SHA-256” as interchangeable options. Prefer a keyed HMAC with a documented key lifecycle. Also reconsider the UI label “Verified via Evidence Manifest”: once the original scan and OCR are purged, retained metadata is provenance, not an independent way for the user to recheck the source evidence.

## The model choices are mostly correct

I verified the model IDs separately from the supplied code:

- Google documents `gemma-4-31b-it` and `gemma-4-26b-a4b-it` for hosted access through the Gemini API.&#x20;

  [image](https://www.google.com/s2/favicons?domain=https://ai.google.dev\&sz=32)

  Google AI for Developers

  +1

- Firebase AI Logic lists `gemini-3.8-flash` and `gemini-3.5-flash-lite` as supported stable models.&#x20;

  [image](https://www.google.com/s2/favicons?domain=https://firebase.google.com\&sz=32)

  Firebase

  +1



The architectural separation is correct: hosted Gemma should have its own backend adapter; Firebase AI Logic should be a separate Gemini provider. Do not assume Firebase AI Logic itself provides the Gemma route. Also, treat `GEMMA_4_CLOUD_FREE` as a product policy label rather than a guarantee of unlimited, permanently free production inference. Actual quota and billing behavior must be configured and monitored.

## The acceptance criteria need two small changes

First, the corpus counts need clarification. The five family counts listed add up to 100, while the specified overall corpus contains 150 documents split into 50 calibration and 100 held-out documents. Explicitly identify those five-family counts as the held-out test allocation, then define the calibration allocation separately.&#x20;

ai_financial_document_engine_specification_final.md



Second, the current tests do not fully test the guarantees claimed in the specification. Add tests for statement-summary provenance, mixed evidence-ID lists, phone/address redaction in table cells, oversized single rows, invalid homographies, cancellation propagation, and drafts with unparseable amounts. For deterministic field evaluation, report exact-match accuracy, coverage, and false-lock rate alongside precision; a high precision figure alone can hide a gate that abstains on most documents.

## Final verdict

This is a strong engineering direction and a meaningful step forward from the previous version. The architecture is well thought through, but the remaining gaps directly affect data integrity, privacy guarantees, and whether the code compiles.

My recommendation: fix the five priority issues above first, then run the dependency-resolution build and the expanded invariants. Once those pass, I would consider the document ready to serve as the implementation contract rather than merely an architectural blueprint.