Verdict: A major improvement, but not quite ready to hand to a coding agent
8.6/10
Architecture

Excellent direction; several implementation contracts need another pass
This version addresses nearly all the major architectural concerns from the previous review. Hosted Gemma is now the real default, local AI is optional, sensitive data has an explicit cloud boundary, and financial documents are separated from financial events.

Implementation readiness: approximately 7.5/10. The design is strong, but some code shown as a specification does not yet enforce all the guarantees the prose promises.

My recommendation is to keep this architecture. I would not start over or fundamentally change the local-processing-first approach. I would make a targeted correction pass before implementation.

The strongest change is the new central contract:
- All capture, OCR, image analysis, layout reconstruction, candidate extraction, and arithmetic validation stay on the device.
- Hosted Gemma handles semantic interpretation only when the deterministic gate cannot confidently resolve the document.
- On-device Gemini Nano is an optional optimization, not a hardware requirement.
- Cloud transfer is disclosed, local-only operation remains available, and routine diagnostics omit raw financial text.
  That is the right architecture for a broadly compatible Android finance app. It also describes the privacy boundary more honestly than saying the entire system is “local-first”: the processing is local-first, but some financial metadata is still sent to a model provider when cloud interpretation is authorized.
  What is particularly good now
1. The AI provider architecture is much clearer. HostedGemmaProxyAdapter, FirebaseAiGeminiAdapter, ByokGeminiAdapter, and GeminiNanoOnDeviceAdapter make provider differences explicit. That is better than a generic adapter which assumes every API accepts the same request, schema, and error behavior.

2. The financial domain has improved substantially. Distinguishing expenses, unpaid liabilities, transfers, refunds, income, and statement summaries addresses one of the biggest risks in financial-document extraction: interpreting every prominent amount as spending.

3. Evidence IDs are more useful. Page-scoped identifiers combined with semantic-role validation and a review overlay provide a stronger foundation for explaining and correcting extracted values.

4. The security model is moving in the right direction. A server-side provider key, an encrypted BYOK credential vault, encrypted database storage, and safe-by-default diagnostic telemetry are all appropriate design choices.

5. The testing plan is more credible. Separating exact invariants from measured corpus accuracy is better than demanding 100% accuracy for all visual processing. The proposed tests for currency scale, homography, evidence rejection, statement balances, and duplicate transactions are well chosen.

Those improvements are substantial. The remaining problems are specific and fixable.

The seven changes I would make before implementation
1. Fix the consent model: one cloud-consent Boolean contradicts the routing policy
   This is the most important logical issue in the new specification.

Your architecture says that users must consent to cloud transfer and that the app must never silently switch between cloud providers. But AiRoutingPolicy has only:
val cloudTransferConsentGranted: Boolean = false


Meanwhile, the default fallback list contains both on-device AI and Firebase AI Logic:
val authorizedFallbackProviders: List<AiEngineProvider> = listOf(
AiEngineProvider.ON_DEVICE_GEMINI_NANO,
AiEngineProvider.FIREBASE_AI_LOGIC
)


Consider this scenario:
1. The user selects Hosted Gemma and grants consent to send financial metadata to that service.
2. Hosted Gemma fails or hits its quota.
3. The router tries Gemini Nano and then Firebase AI Logic.
4. If Firebase succeeds, the text may now go to a different cloud service under its own data-handling and billing arrangements.
   That conflicts with the stated promise against silent cross-provider switching. A single Boolean tells the router that some cloud transfer was allowed, not that this particular provider and fallback route were approved.
   My correction
   Separate three concepts:
- Cloud data-transfer consent: whether the user allows financial metadata to leave the phone at all.
- Provider authorization: which named providers the user has approved.
- Fallback authorization: which of those providers the router may automatically try after the primary fails.
  The routing decision should require all three conditions where applicable. A practical model would contain consentedCloudProviders and authorizedFallbackProviders, plus the selected provider's data-use disclosure version and acceptance timestamp.

For example:
data class AiRoutingPolicy(
val primaryProvider: AiEngineProvider,
val strictLocalOnlyMode: Boolean,
val consentedCloudProviders: Set<AiEngineProvider>,
val authorizedFallbackProviders: List<AiEngineProvider>
)


The router should enforce that a cloud provider is both consented to and authorized as a fallback before calling it. Local-only mode must exclude all cloud providers regardless of readiness.

Important: do not let a readiness check, a quota error, or a successful connection test grant consent. Readiness and user authorization are separate things.

2. Correct the homography coordinate math
   The intent is excellent, but the equations mix coordinate spaces.

Your specification says that the inverse homography maps cleaned OCR coordinates back to the original display image, then says to map a normalized OCR corner \((x,y)\in[0,1]^2\) through the matrix. The matrix is described as the inverse of a transformation between raw and cleaned bitmaps. Unless that matrix was specifically constructed in normalized coordinate space, its inputs and outputs will be pixel coordinates, not normalized coordinates.

Applying a pixel-space matrix directly to normalized values will give incorrect positions.
My correction
Define the coordinate spaces explicitly:
- Raw/display pixel space: \(W_r \times H_r\)
- OCR/cleaned pixel space: \(W_o \times H_o\)
- Normalized coordinate space: \([0,1]^2\)
  If the stored homography maps OCR pixels to raw pixels, convert normalized OCR coordinates into OCR pixels, apply the homography, then normalize by the raw dimensions. Alternatively, mathematically compose normalization matrices with the homography and store a matrix whose input and output are both normalized coordinates.

Also, transform all four corners of each OCR bounding box. Perspective transformations can turn a rectangular box into a quadrilateral. Either draw that polygon in the review UI or calculate a conservative enclosing rectangle; don't transform only its origin and reuse the original width and height.

There is a second qualification: a single \(3\times3\) homography handles global projective transformations such as perspective correction and affine transforms. It cannot exactly invert arbitrary nonlinear dewarping used to flatten a genuinely curved receipt. For any nonlinear transform, preserve the actual remapping function or displacement map instead.

So I would revise the promise from “guarantees exact pixels” to maintains a tested mapping for every transformation the pipeline actually performs.

3. Strengthen the validator: the code does not yet prove every field is grounded in the correct evidence
   The validator is one of the strongest parts of the specification conceptually. However, the implementation of PostLlmSemanticValidator.validateAndSanitize() leaves several gaps.
   The most significant gap is event evidence matching
   The code validates that an event's evidence IDs exist. It then checks whether the amount matches any amount candidate or table cell:
   val amountExists = bundle.amountCandidates.any {
   it.amount == event.amount
   } || bundle.tableRows.flatMap { it.cells }.any {
   it.parsedAmount == event.amount
   }


This checks for an amount match, but does not require the candidate that matches the amount to share the event's evidence IDs. An event could therefore reuse a valid amount from another row and point to unrelated evidence.

For a finance app, that's not enough.
My correction
For each financial event, validate a single consistent chain:
1. Every referenced evidence ID exists in the original document's evidence registry.
2. The specified evidence IDs intersect the evidence IDs of the candidate supporting that event.
3. The candidate's amount and currency match the event exactly.
4. The candidate's semantic role is permitted for that event kind.
5. The document family and row/column role are consistent with the interpretation.
6. The event's sign, date, and account context are valid for that family.
   The same rule must apply to all resolved fields, not only documentTotal and primaryDate.

Additional gaps in the sample code:
- issuerAddress, dueDate, taxTotal, and merchantOrIssuer do not receive equivalent candidate-role validation.
- documentTotal currently permits a SUBTOTAL_AMOUNT candidate. A subtotal should not be accepted as the final document total merely because no better candidate was found.
- The primary-date check rejects a due date, but does not comprehensively distinguish transaction date, posting date, statement-period date, and print date.
- ResolvedStringField.role and ResolvedAmountField.role are not consistently compared with the matching candidate's role.
- The validator does not verify all document-family-specific invariants before accepting financial events.
  I would make the post-LLM validator the canonical gate to persistence. Invalid fields should be rejected individually, and the interpretation should be marked as requiring review whenever required fields are dropped or unresolved.
  Improve untrusted JSON extraction too
  The current parseUntrustedJson() implementation uses the first { and the last } in the response. That is a convenient prototype, but brittle. Braces can occur in surrounding text or multiple JSON objects may appear in one response.

Prefer strict JSON parsing of the complete response first. If the provider returns a documented Markdown fence, remove that exact outer fence and parse again. If the response still cannot be parsed, report a parse failure and fall back. Do not silently extract a broad substring and assume it is the intended object.

4. Fix currency handling: never silently round a financial amount
   The new CurrencyAmount model is a major improvement, but its factory method currently rounds values:
   value.setScale(scale, RoundingMode.HALF_UP)
   .movePointRight(scale)
   .longValueExact()


Imagine OCR reads a value incorrectly as 12.345 for a currency using two decimal places. The factory silently converts it to 12.35. That changes the observed number rather than identifying the input as inconsistent.

The issue is not floating-point precision anymore; it is silent data correction.

There is another problem in this line:
Currency.getInstance(normalizedCode)
.defaultFractionDigits.coerceAtLeast(0)


This makes the runtime Java currency registry the sole source of scale and can turn a negative “fraction digits unavailable” result into zero. The model's constructor checks only that the code has three characters and that the scale is between zero and four. Those checks do not establish that the code is a valid ISO 4217 currency or that its scale matches the code.
My correction
Make parsing strict and separate it from normalization:
- Validate the currency code against a controlled ISO 4217 metadata source.
- Resolve the expected minor-unit scale from that metadata.
- Compare the parsed decimal precision with the expected scale.
- Reject or flag excess fractional precision rather than rounding silently.
- Persist the scale and, if useful, the version of the currency metadata used.
- Keep formatting and display conversion separate from the stored integer amount.
  If a source value contains more decimal places than the currency permits, preserve the original OCR evidence and flag it for review. An explicit, tested normalization rule can be applied where justified, but it must not quietly alter the source amount.

5. Make the deterministic gate and reconciliation rules document-family-aware
   The new confidence gate is much better than a vague “high confidence” check. The defined thresholds are measurable, and the gate is deliberately conservative. I like that it requires document family confidence, an issuer/merchant match, date unambiguity, exact reconciliation, and a reliable category rule.

Two things still need refinement.
First, the arithmetic formulas are not universal
The receipt formula assumes:
\[
\sum \text{LineItems}+\text{Tax}+\text{Tip}-\text{Discount}=\text{Total}
\]

That may fail legitimately if line items already include a discount, if shipping or a service charge exists, or if deposits, surcharges, or other fees are included. The system should identify which components the document actually provides and select a reconciliation rule appropriate to those components.

The statement equation also needs account-type-specific rules. For a conventional bank account, the familiar relationship is:
\[
\text{Opening Balance}+\text{Credits}-\text{Debits}=\text{Closing Balance}
\]

But credit-card statements commonly treat purchases as increases in the liability and payments as reductions. A single debit/credit formula must not be applied indiscriminately to bank and card statements.
Second, the gate's confidence requirements may be too rigid
A merchant can be identified correctly even when the category has not yet been resolved. A transaction date might be unambiguous even if statement-period dates also appear. Requiring all five conditions to pass before skipping the model is a legitimate conservative policy, but it might prevent deterministic processing from resolving simpler documents without AI.

I would measure this behavior on the real corpus rather than lowering the thresholds by intuition. Make the gate's target explicit: is it deciding that the entire document can be interpreted without AI, or that specific fields can be resolved without AI? The latter enables useful hybrid behavior—for example, accepting a locally reconciled total while asking the model only to classify an ambiguous transaction.

The gate must never turn its confidence score into permission to persist an unsupported amount. Confidence governs routing; evidence validation governs acceptance.

6. Make the payload budget a genuine limit, not an assumption
   The BundleTrimmer is a good addition, and the 12 KB UTF-8 cap is useful as a hard payload bound. However, the current plan does not guarantee that every chunk will meet it.

The specification says to take the top five text candidates, six date candidates, and ten amount candidates. That could discard an important but lower-confidence candidate—such as the actual payment amount, a refund, or a statement-balance label—while keeping several irrelevant amounts.

Likewise, splitting tables into chunks of 15 rows is reasonable, but 15 rows is not a size guarantee. A row containing long descriptions or many cells could still exceed 12 KB, especially when the repeated header/candidate context is added.

Also, 12 KB is not reliably equal to 3,000 tokens for arbitrary content. Tokenization depends on the model and the text, and JSON punctuation and non-English text affect the relationship.
My correction
Use a staged trimming strategy:
1. Preserve all mandatory candidates required to interpret the document family and its relevant events.
2. Rank alternative candidates within semantic roles—not just by one global confidence score.
3. Keep row-level evidence and the metadata needed to reconcile each chunk.
4. Measure the final UTF-8 size after serialization and, where supported, use a provider/model-appropriate token estimator or token-count API.
5. If the bundle exceeds a limit, split further or remove nonessential context under explicit rules; if it still does not fit, return a partial deterministic result for review.
   For statements, each chunk also needs a stable row range, source row identifiers, account context, statement-period context, and enough carry-forward information to reconcile correctly. The final aggregation should check that rows aren't dropped, duplicated, or reordered across chunks.

One additional privacy improvement: the bundle currently permits customer-address candidates. Those should not be sent by default unless they are demonstrably needed for the specific interpretation. Apply a redaction/minimization pass before cloud transfer for account identifiers, personal addresses, phone numbers, email addresses, and other unnecessary identifying values.

7. Reconcile the persistence model with the promised financial events and retention policy
   The domain model is better now, but the persistent contract is not yet fully aligned with it.
   Missing and inconsistent fields
   The specification talks about a separate StatementSummaryRecord, but the schema does not define a statement-summary table. FinancialEventEntity covers many event types with a generic row, but it does not clearly persist the statement-level opening and closing balances as a separate aggregate.

The output contract also allows ResolvedFinancialEvent.isoDate to be null, while the Room FinancialEventEntity.eventDateIso is non-null. You need to decide whether an event without a date is rejected before persistence or is stored as an unresolved draft. As written, both contracts cannot be honored for that case.

Likewise, the financial-event contract should provide a clear way to carry bill payment status and due dates, transfer linkage, and row/account context. Some of this information appears in the entity but not on the resolved event object.

I would choose one canonical design explicitly: for example, sealed domain event types with shared persistence fields and a separate statement-summary entity, or a unified event table with all required nullable fields and strictly validated event kinds. Either approach can work; the current hybrid leaves implementation choices to the coding agent.
Retention must preserve honest provenance
After 30 days or another configured interval, the worker deletes the images and bulk OCR rows but keeps financial events whose evidenceIdsCsv points to those rows. The specification mentions preserving compact SHA-256 provenance hashes, but it doesn't define exactly what the surviving evidence manifest contains.

A hash by itself cannot show the user what a value was extracted from, and the IDs won't resolve once the original records have been deleted.

Define what provenance means after purging. A privacy-conscious option is to retain a minimal evidence manifest containing opaque IDs, page references, semantic roles, normalized values or appropriately protected value digests, and the verification status—without keeping the full OCR text or pixels. The UI should explicitly distinguish between evidence that is still reviewable and evidence whose raw source was purged.

Also, the document deletion UI and cascade rules need to be consistent: deleting the source while keeping verified transactions should preserve a defined provenance status; deleting both should remove their related events and source material according to the user's choice.

Dependency and implementation audit
There is one more important correction: the section titled “Verified 2026 Dependency Baseline” is not yet verified. Several entries are already behind the currently published Android ecosystem as of October 24, 2026.

The good news is that some of your choices are right. The Compose BOM 2026.09.00 is current, Room 2.8.5 is the current stable Room 2 release, and ML Kit Text Recognition 16.0.1 is still listed in Google's current documentation.


Android Developers
+2



These are the baseline changes I would make:
Dependency	In the spec	What I'd change
Kotlin	2.1.20	Re-evaluate against Kotlin 2.4.21, published October 8, 2026.  Kotlin Documentation
Coroutines	1.10.1	Re-evaluate against 1.11.0.  GitHub
Serialization	1.8.0	Re-evaluate against stable 1.11.0, ensuring the serialization compiler plugin matches the chosen Kotlin toolchain.  GitHub
Hilt / Dagger	2.56	Re-evaluate against the current Dagger/Hilt release line, which includes 2.60.1.  GitHub
Room	2.8.5	Keep, subject to SQLCipher compatibility testing.  Android Developers
AndroidX SQLite KTX	2.5.0	Update the compatibility evaluation to current stable 2.7.1; don't pin an older SQLite layer without a tested reason.  Android Developers
SQLCipher for Android	4.6.1	Check the current 4.19.1 release and its exact Room 2 integration API before choosing the compatible version.  GitHub +1
Firebase BoM	34.4.0	Evaluate 35.1.0, the current BoM listed in the October 21 release notes. Firebase AI Logic is mapped to 18.1.0.  Firebase
ML Kit Document Scanner	16.0.0-beta1	Prefer the documented stable 16.0.0 unless a beta-specific feature is required.  Google for Developers
Compose BOM	2026.09.00	Keep; it is the published current stable BOM.  Android Developers

I would not simply change every version to the latest available version. First choose a compatible Kotlin, KSP, Compose, Room, SQLite, and Hilt set, then run a clean build, unit tests, instrumentation tests, and SQLCipher migration tests. Check the Tink and AICore versions against their current release documentation as well. AICore's experimental dependency should remain isolated behind its adapter so it cannot destabilize the primary cloud path.
The Firebase model registry needs updating too
Your decision to keep Firebase AI Logic as a distinct Gemini route is correct. But the model registry currently lists gemini-2.5-flash as its preferred model. Google's current Firebase model documentation identifies gemini-3.8-flash and gemini-3.5-flash-lite as featured models; it also says Gemini 2.5 models are restricted to projects that actively used them previously. I would not use gemini-2.5-flash as the default for a new application in October 2026.


Firebase
+1



The two hosted Gemma identifiers in your spec—gemma-4-31b-it and gemma-4-26b-a4b-it—do match Google's current Gemini API Gemma documentation.


Google AI for Developers


Keep them in ModelCapabilityRegistry, but make the model ID, input/output limits, and availability remotely configurable. The spec's maxInputTokens = 8192 and maxOutputTokens = 2048 should be treated as chosen application limits unless you verify that they are the actual current API limits for those exact model endpoints.
One naming refinement: “Free Tier” is a service availability and quota assumption, not a permanent model capability. Make sure the backend enforces its own free-use quota and cost controls, and make the UI reflect that provider availability and account terms can change. Also benchmark the 26B A4B model before advertising it as the “low-latency” alternative.

Finally, keep the Firebase App Check requirement in the plan. Google's current documentation says enforcement will be required beginning November 2, 2026, so this is an imminent release requirement, not merely an optional security enhancement.


Firebase




Smaller but important implementation issues
These are not architecture blockers on their own, but they should be captured in the coding spec.
Encrypted image files need an actual encryption contract
The paths raw_display_page_{n}.enc and clean_ocr_page_{n}.enc are filenames, not an encryption mechanism. Define the file-encryption implementation: authenticated encryption, nonce handling, per-file identity, key versioning, atomic writes, and what happens after key loss or deletion. App-private storage alone should not be described as equivalent to explicitly encrypted image bytes.

Likewise, TinkCredentialVault should clear the BYOK key from UI state after saving it, avoid storing the plaintext key in saved state, and never include it in exceptions, logs, or diagnostics. For the database, document how the SQLCipher key is created, wrapped with Android Keystore, rotated, and recovered—or deliberately not recovered—after key invalidation.

Because the app handles finance data, specify backup exclusions for the database, images, credential vault, and sensitive DataStore values. The spec should explicitly state what is and is not recoverable from Android backup.
The sample Compose UI is illustrative, not a complete build target
The UI gives you a solid visual starting point, but the full project still needs the appropriate Compose icons dependency and the other omitted modules and plugins. The CloudConsentDialog is named in the architecture, but the shown AiConfigurationCard does not itself demonstrate the consent flow. Make sure the consent experience is a real route/state transition rather than just a Boolean in AiConfigUiState.

The provider readiness header should explain why a provider is unavailable and distinguish a network check from a model-capability verification. A successful connection test should not be treated as proof that a model supports the complete financial interpretation contract.
The acceptance corpus needs a proper evaluation split
The proposed 150-document corpus is a good start, but specify the number of examples for each document family and metric. Separate calibration data from a held-out evaluation set, and avoid having the same merchant templates appear in both sets in ways that inflate deterministic-gate results. The target of at least 99% field accuracy is meaningful only with clear definitions of a correct field, a gate-passing document, and the sample size supporting the result.

Keep the exact unit tests at 100% pass. For row grouping and extraction, define the denominator, failure criteria, and evaluation method so the reported percentages are reproducible.
Final recommendation
I would make this the next revision checklist:
[ ] Replace the global cloud-consent Boolean with per-provider consent and explicit fallback authorization.
[ ] Define homography coordinate units and support all four box corners; document nonlinear transform limitations.
[ ] Bind every accepted event amount and field to matching evidence, candidate role, and document context.
[ ] Remove silent currency rounding and validate codes and scales against controlled currency metadata.
[ ] Make receipt and statement reconciliation rules account- and document-family-aware.
[ ] Make BundleTrimmer enforce actual serialized limits without dropping required candidates.
[ ] Align event contracts, statement-summary persistence, unresolved event handling, and post-purge provenance.
[ ] Refresh and lock a compatible late-2026 dependency baseline.
0 of 8 reviewed

Bottom line: this is now a strong, coherent architecture for the problem you are trying to solve. The local-processing-first pipeline and hosted Gemma default are appropriate; the domain separation and privacy controls are real improvements. The remaining issues are mostly about turning promises into enforceable contracts—especially consent, coordinate correctness, evidence validation, and persistence.

Once those are addressed, I would consider it ready to hand to a coding agent in phased implementation, with an explicit instruction to build and test each phase rather than generate the entire application at once.