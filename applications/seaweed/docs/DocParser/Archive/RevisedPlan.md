# Revised verdict: Local-first financial document engine

# 8.2/10

Revised score

## Strong architecture; several production-critical changes remain

Your clarification changes the recommendation: hosted Gemma should be the default AI engine, with on-device AI treated as an optional capability rather than a requirement for the app to work.

The central idea in your specification is right: do the expensive document preparation locally, send a compact text-only representation to a cloud model when interpretation is needed, and validate its output before creating financial records. The specification explicitly establishes local OCR and image analysis, integer-based monetary values, evidence-linked fields, and a deterministic fallback. Those are the right foundations for a financial document engine.&#x20;

ai_financial_document_engine_specification.md



I would keep the overall architecture and refine its boundaries, not start over.

The main qualification is that three things need to be distinguished:

- Local-first processing: images, OCR, layout, amount parsing, and arithmetic checks run on the phone.
- Cloud-assisted interpretation: the compact JSON bundle goes to hosted Gemma when a model is needed.
- Privacy: the bundle can still contain sensitive financial information, even though it contains no images.

That makes this a local-processing-first, cloud-assisted finance application. It can be privacy-conscious, but keeping the image local does not by itself make the entire workflow private. The cloud boundary, provider terms, credentials, and diagnostics must be designed explicitly.

## 1. What I would keep

A local document-understanding pipeline

Keep the PDF text-layer fast path, ML Kit OCR, geometric layout reconstruction, regional image analysis, merchant matching, locale-aware candidate extraction, and arithmetic reconciliation. This is the part of the system that makes the AI calls smaller and more grounded. The specification already gives these responsibilities their own modules.&#x20;

ai_financial_document_engine_specification.md



Evidence provenance and validation

Keep the rule that every resolved amount, date, merchant, and line item must point back to OCR evidence. The post-model checks that reject unknown evidence IDs, require amounts to match detected candidates or cells, enforce the category taxonomy, and suppress statement-balance records are particularly important.&#x20;

ai_financial_document_engine_specification.md



One refinement is needed: valid evidence IDs establish where a value came from, not that the value has the right financial meaning. The validator must also verify its role and context.

Deterministic financial arithmetic

Keep monetary arithmetic outside the LLM. If line items, tax, discounts, and total reconcile reliably, the app should not need an LLM just to repeat that calculation. Keep the high-confidence deterministic path before AI invocation, but make confidence rules measurable and testable rather than relying on an undefined notion of “high confidence.”

Modular architecture and encrypted persistence

The separation of capture, OCR, vision, layout, finance extraction, validation, AI, database, and review is sensible for Kotlin, Compose, Hilt, and Room. Keep the module boundaries, database encryption, review flow, and deterministic tests. I would refine the persistence and security details rather than remove this structure.&#x20;

ai_financial_document_engine_specification.md



## 2. The first major change: make hosted Gemma the real default

Your reasoning is sound: an app intended for a broad Android audience cannot require users to have a compatible, downloaded on-device model. Gemini Nano availability should be discovered at runtime and used only when available and appropriate. Hosted Gemma provides a common cloud path instead.

However, the specification currently disagrees with itself about which provider is the default:

- The provider enum labels `GEMMA_4_CLOUD_FREE` as the default.
- The sample UI state sets `selectedEngine` to `ON_DEVICE_GEMINI_NANO`.
- The initial status map marks Firebase and Gemma as ready before a real runtime check.
- The fallback chain has a fixed ordering that starts with BYOK, even though the selected primary should control the first attempt.&#x20;

  ai_financial_document_engine_specification.md

  ai_financial_document_engine_specification.md



I would make the persisted provider setting and routing policy the single source of truth. The default configuration should select hosted Gemma, and the UI should reflect actual readiness rather than assume a provider is operational.

### Current hosted Gemma details to correct

Google’s current hosted Gemma documentation lists these Gemini API model IDs:

- `gemma-4-31b-it`
- `gemma-4-26b-a4b-it`

The spec’s example `gemma-4-27b-it` does not match those currently documented IDs and should be replaced. Model IDs should be maintained in a versioned capability registry or remotely managed configuration, not scattered as strings throughout the app.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://ai.google.dev\&sz=32)

Google AI for Developers

+1



There is a second, more important provider distinction. Firebase AI Logic should not be treated as an interchangeable hosted Gemma endpoint. Its documented model support focuses on Gemini models; Google notes that Gemma-based models are supported only in some hybrid or on-device configurations. If you want a Firebase AI Logic fallback, define it as a supported Gemini-model route, not as another way to call hosted Gemma.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://firebase.google.com\&sz=32)

Firebase

+1



That requires a small but meaningful architecture change: model providers must describe their actual capabilities, not just implement a common interface.

## 3. Resolve the cloud API key and privacy model before implementation

This is the most important production decision after choosing hosted Gemma.

### The shared API key problem

The Gemma API documentation describes hosted access through the Gemini API and requires an API key.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://ai.google.dev\&sz=32)

Google AI for Developers

&#x20;If the app ships with one developer-owned key embedded in the APK, a determined user can extract it. Storing that shared key in encrypted preferences does not fix the problem: the app must eventually access the key to make requests.



Choose one of these approaches deliberately:

| Approach                                               | My verdict                                                                                                                                                        |
| ------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Backend proxy for hosted Gemma                         | Best for a public consumer app using a shared provider account. Keep the provider key on the server, authenticate app users, impose quotas, and rate-limit calls. |
| BYOK                                                   | Reasonable for users who explicitly provide their own provider key. Clearly explain that usage and provider terms belong to that account.                         |
| Direct requests using a shared key embedded in the app | Do not use this for production. Obfuscation or encrypted local storage does not make a shared key secret.                                                         |
| Firebase AI Logic with a supported Gemini model        | A useful alternative cloud route with app-attestation protections, but it is not a drop-in hosted Gemma replacement.                                              |

A backend introduces hosting, abuse-prevention, and operational costs; a free model tier does not make the entire service cost-free. If you choose BYOK-only access to avoid operating a backend, users without their own key need an explicit alternative.

### “No images sent” is necessary, but not sufficient

Even a compact `LlmAnalysisBundle` can contain merchant names, addresses, purchase histories, due dates, amounts, and bank-statement descriptions. That is still sensitive financial data.

The current Gemini API terms distinguish paid services from free-tier usage: prompts and responses used through paid services are not used to improve Google’s products, while the pricing information indicates that free-tier data may be used for product improvement. Free-tier availability is also not guaranteed for every project or account.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://ai.google.dev\&sz=32)

Google AI for Developers

+2



I would therefore make these requirements explicit:

1. Disclose the cloud transfer before the first cloud analysis. Explain which text and financial fields may be sent, to which provider, and under what service terms.
2. Minimize the bundle. Include candidate values and the context required to classify them—not the full OCR output simply because it is available.
3. Offer a genuine local-only fallback mode. It can use deterministic extraction and review even when no compatible local model exists. Do not imply that local-only mode provides equivalent interpretation quality.
4. Do not silently switch to a different cloud provider. Each provider has separate terms, data handling, availability, and potentially billing implications. Cloud fallback should be allowed only when the user has enabled that route or the applicable disclosure clearly covers it.
5. Keep sensitive text out of routine diagnostics. Record metrics by default, not entire prompts and responses.

Firebase AI Logic also deserves special attention because of the timing. As of October 24, 2026, Google's documentation says App Check enforcement will be required beginning November 2, 2026. Use a production App Check provider such as Play Integrity, enforce App Check for Firebase AI Logic, and validate the integration before release.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://firebase.google.com\&sz=32)

Firebase



## 4. Replace the fixed fallback chain with a policy-driven router

The current specification puts a user-selectable primary engine and a fixed chain in the same design. It also makes cloud fallback from local AI configurable, but defaults that preference to `true`.&#x20;

ai_financial_document_engine_specification.md



That is too ambiguous for a financial app.

The router should distinguish four questions:

- Can deterministic processing resolve this document confidently?
- Which provider did the user select as primary?
- Which fallback providers has the user authorized?
- Can those providers handle the request and data under the current configuration?

I recommend this flow:

1\. Local deterministic gate

OCR, candidate extraction, arithmetic, known merchant rules

2\. Hosted Gemma — default primary

Compact text-only JSON, through the selected secure access path

3\. Approved alternatives only

Configured BYOK, compatible local AI, or a supported Firebase Gemini route

4\. Deterministic partial result and review

Preserve extracted evidence; flag unresolved fields rather than inventing them

This diagram is a logical flow, not a mandate to call every provider in sequence. The router should stop after a valid result, and it should skip any provider that is unconfigured, unsupported, unaffordable under the selected policy, or not authorized for the current data.

The default cloud-first flow does not mean calling hosted Gemma whenever the app sees a document. If local rules have already resolved the relevant data confidently, skip the LLM. If hosted Gemma fails, use only an approved fallback. If none qualifies, return the reliable deterministic fields and send uncertain fields to review.

### Provider readiness must be real

The spec defines readiness largely through connection, configuration, and availability checks.&#x20;

ai_financial_document_engine_specification.md

&#x20;That is a useful starting point, but a successful network ping does not prove that a particular model can process the request or return the expected output.



I would add explicit states such as `UNCONFIGURED`, `READY`, `DEGRADED`, `QUOTA_LIMITED`, `OFFLINE`, and `UNSUPPORTED`. Check the selected model’s configuration and actual capabilities; do not initialize cloud providers as ready by default.

Also, `BYOK` is not truly provider-agnostic in the current UI: the specification assumes Gemini models and a Gemini-style model selector. Either label it specifically as a Gemini API key or define the provider, model ID, authentication type, endpoint, and capabilities explicitly. Do not assume every provider supports the same model-list endpoint, JSON schema, rate limits, or error codes.

## 5. Tighten the model output contract

The requirement for valid JSON is good. The mistake would be assuming that JSON-looking output is necessarily valid, complete, or financially correct.

Google’s Gemini structured-output documentation supports a subset of JSON Schema and emphasizes that schema-conforming output still requires application-level semantic validation. The documented structured-output model support should be checked separately for the chosen hosted Gemma endpoint; do not assume that a schema option supported by a Gemini model is supported identically by Gemma.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://ai.google.dev\&sz=32)

Google AI for Developers

+1



For the first implementation, I would:

- Maintain one canonical `DocumentInterpretation` schema and validate it in the app regardless of provider.
- Use provider-specific response-format support only when that model and endpoint have been verified to support it.
- For Gemma, treat output as untrusted JSON text until the exact endpoint and model have passed integration tests.
- Validate each field against existing candidates, evidence, semantic role, allowed categories, currency, and document type.
- Reject or flag unsupported, ambiguous, or contradictory results rather than silently coercing them into a transaction.

Evidence IDs should prove both origin and meaning. A returned total should point to the actual amount evidence and agree with a locally parsed total candidate. An ID that merely points somewhere on the page is not enough. A date found in a footer, for example, should not automatically become the purchase date simply because its evidence ID is valid.

The spec already checks that IDs exist and that amounts match extracted candidates or cells. I would preserve that and add semantic-role, currency, and date-role validation.&#x20;

ai_financial_document_engine_specification.md



## 6. Fix the financial data model before polishing the AI

This is the next major correctness risk. The app needs to distinguish what a document says from what financial event should be recorded.

A receipt, an unpaid utility bill, a credit-card statement, and a bank statement can all contain amounts that look like totals, but those amounts do not have the same meaning.

For example, a credit-card statement might contain $120 of purchases and a $120 payment due. Recording the purchases as expenses and then recording the card payment as another expense would double-count the spending. Similarly, an invoice’s amount due is not necessarily proof that the bill has been paid.

The current contract already distinguishes `purchase`, `bill_payment`, `internal_transfer`, `refund_credit`, `fee`, and `statement_balance_ignore`, which is a good foundation.&#x20;

ai_financial_document_engine_specification.md

&#x20;I would build on it with a document classification step before transaction interpretation.



| Document family         | What the engine should determine                                                                           |
| ----------------------- | ---------------------------------------------------------------------------------------------------------- |
| Receipt                 | Merchant, purchase date, line items where useful, tax, total, currency                                     |
| Invoice or utility bill | Issuer, billing period, amount due, due date, payment status if supported by evidence                      |
| Bank statement          | Account identity where safely available, transaction rows, dates, debits/credits, opening/closing balances |
| Credit-card statement   | Purchases and credits, payments, fees, statement balance, payment due date                                 |
| Refund or credit note   | Whether it represents a return/credit, and whether it can be linked to an original purchase                |

These should map to separate domain concepts, even when they share OCR and amount-parsing infrastructure. A document-level “total” should not automatically become a transaction. Store the document and its evidence separately from the financial events derived from it.

I would also make duplicate detection account-aware. The spec's fingerprint uses normalized merchant, date, amount, and currency.&#x20;

ai_financial_document_engine_specification.md

&#x20;That is a useful baseline, but identical purchases can occur on the same day. Where relevant, incorporate the account or statement identity, transaction reference, and source document, then let the review UI resolve uncertain duplicates. Handle credit-card payment versus purchase duplicates and internal transfers as domain rules, not only string matching.



### Money needs one small but important correction

The integer-minor-unit rule is absolutely right; it avoids ordinary floating-point errors in monetary calculations. But “minor unit” should not always be hardcoded to two decimal places. Currencies differ in their minor-unit conventions.

Keep the `Long` representation, but define the currency and scale explicitly. Parsing, formatting, arithmetic reconciliation, and persistence must all use the currency-specific scale. Never infer USD-style cents merely because a value comes from a dollar receipt. The spec already carries a currency code with amount candidates and resolved amounts, so this is a focused refinement rather than a redesign.&#x20;

ai_financial_document_engine_specification.md

ai_financial_document_engine_specification.md



## 7. Make evidence robust across pages and image transformations

The raw/cleaned bitmap design is sensible: one version supports image analysis and review, while the cleaned version improves OCR. The spec extracts OCR boxes from the cleaned image and later overlays evidence on the raw image.&#x20;

ai_financial_document_engine_specification.md

ai_financial_document_engine_specification.md



There is one implementation detail I would make mandatory: preserve the transform from the OCR coordinate space back to the displayed document.

Deskewing, rotation, cropping, rescaling, and perspective corrections can change coordinates. If OCR boxes are produced from the cleaned bitmap but shown over the original bitmap without the correct transform, the review UI may highlight the wrong amount. Keep a transform or coordinate mapping for every page, and test the bounding-box overlay with rotated, cropped, and deskewed documents.

Evidence IDs also need a clear multi-page policy. The validator currently describes checking the returned IDs against the page's OCR records.&#x20;

ai_financial_document_engine_specification.md

&#x20;For multi-page statements, use IDs that are unique within the full document, or use explicit page-scoped references such as `{documentId, pageIndex, elementId}`. Do not let `T014` ambiguously refer to different elements on separate pages.



Finally, don't store raw OCR text, cleaned images, and analysis output indefinitely just because storage is encrypted. Specify a retention and deletion policy, including what happens when a user deletes a document and whether derived transactions are kept.

## 8. Make diagnostics safe by default

The specification proposes storing `promptBundleJson` and `rawResponseJson` in the interpretation-run record and exposing them through the diagnostics UI.&#x20;

ai_financial_document_engine_specification.md

ai_financial_document_engine_specification.md



That is useful during development, but risky as a production default. These fields can contain the same financial text that the cloud call was designed to minimize.

I recommend this policy:

Routine diagnostics: store provider ID, model ID, selected versus actual provider, fallback reason, payload size or estimated token count, latency, parse/validation outcome, reconciliation status, and non-sensitive error codes.

Raw payload and response logging: disabled by default. If developers or users need it for troubleshooting, make it a separate, explicit diagnostic action, redact sensitive content where possible, disclose what will be retained, and use a short retention period.

Also, keep provider API keys out of diagnostic payloads, exception messages, crash reports, and analytics. The `EncryptedSharedPreferences` API referenced in the spec is deprecated in AndroidX Security Crypto; do not carry that choice into a new implementation without replacing it with a currently supported credential-storage approach.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers



## 9. Update the dependency baseline before generating code

The specification pins some older dependency versions. In particular, it lists Room `2.6.1`, whereas the official Android release page lists Room `2.8.5`, released September 9, 2026.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers



I would refresh the version catalog before implementation and verify the current supported versions of every dependency rather than simply copying the listed versions. Pay particular attention to:

- Room, its compiler/KSP setup, and SQLCipher compatibility.
- The document scanner dependency, which the spec lists as a beta.
- The Gemini Nano/AICore integration, which the spec lists using an experimental artifact.
- Firebase AI Logic SDK configuration and App Check.
- Kotlin serialization and networking dependencies.

The important qualification is that a newer version is not automatically the right version for every dependency. The target should be a tested, compatible dependency set whose APIs, security characteristics, and Android requirements are known—not a collection of version strings gathered at different times.

## 10. Change the acceptance tests from perfect synthetic results to measurable real-world performance

The phased plan is a strength, but one verification item expects 100% accuracy on synthetic row-grouping and arithmetic fixtures.&#x20;

ai_financial_document_engine_specification.md



A perfect result on carefully constructed fixtures is useful for validating known cases. It is not a realistic target for row grouping on all receipts and bank statements.

I would split the test strategy into two categories.

Tests that should be exact: integer arithmetic, currency scaling, serialization, evidence-ID integrity, deterministic validation rules, category allowlists, and known regression examples. These should have exact expected results.

Tests that need a measured accuracy target: merchant matching, OCR corrections, reading order, table reconstruction, document classification, and AI interpretation. Measure these on a representative corpus with real documents, including noisy scans, rotated pages, long statements, multiple locales, different currency formats, and ambiguous totals.

Add provider contract tests against the actual hosted Gemma endpoint. Test its current model ID, maximum payload handling, JSON parsing, timeout behavior, quota exhaustion, and fallback rules. A model can be reachable and still fail the application’s output contract.

I would also put an explicit token and payload budget into the AI contract. The spec says the LLM receives compact JSON, but it should define how the bundle is trimmed: prioritize document classification, relevant amount/date candidates, table rows needed for interpretation, evidence references, and only the contextual text that affects a decision. Set bounds for payload size, response size, and retries. If a long statement exceeds the budget, process it in bounded sections and reconcile the results locally rather than sending an unbounded OCR dump.

## 11. My recommended final architecture

I would keep your modules and the overall pipeline, with these changes incorporated into the implementation specification:

| Area              | Final recommendation                                                                                                |
| ----------------- | ------------------------------------------------------------------------------------------------------------------- |
| Default AI        | Hosted Gemma, not Gemini Nano                                                                                       |
| On-device AI      | Optional optimization, checked at runtime; never required for basic functionality                                   |
| Cloud access      | Secure backend proxy for a shared Gemma account, or explicit BYOK; never rely on an embedded shared API key         |
| Firebase AI Logic | A separate, explicitly supported Gemini-model route with App Check, not a generic hosted Gemma route                |
| Routing           | Deterministic high-confidence gate first, then selected primary, then only authorized and compatible fallbacks      |
| Privacy           | Local image/OCR processing, minimal text-only cloud bundle, provider disclosure and opt-in policy, safe diagnostics |
| Financial model   | Classify document family first; separate documents, liabilities, purchases, payments, transfers, and balances       |
| Evidence          | Page-scoped/global-unique references, semantic-role checks, and preserved coordinate transforms                     |
| Money             | Integer minor units with explicit currency scale                                                                    |
| Testing           | Exact financial invariants plus measured real-world extraction performance                                          |
| Dependencies      | Refreshed, compatible version catalog and provider integration tests                                                |

The architectural idea remains strong. The highest-risk issues are not the color analysis, OCR, or Kotlin module layout. They are secure hosted access, honest privacy behavior, inconsistent routing defaults, provider capability assumptions, and financial-event semantics.

## Final scorecard

Local document processing

#### 9/10

Evidence and deterministic validation

#### 9/10

Module separation and implementation plan

#### 8.5/10

Financial data semantics

#### 7.5/10

Provider routing and cloud integration

#### 6.5/10

Production privacy and diagnostics

#### 6.5/10

## Overall: 8.2/10

Architecture specification, before these changes

Keep the design

My final verdict: I would proceed with this project. I would not rewrite the architecture or make local-model support a prerequisite. I would first correct the provider defaults, establish a secure hosted-Gemma access path, define the cloud-data policy, separate document classification from financial-event creation, and strengthen the evidence and test contracts.

Once those changes are in the spec, it becomes a much stronger implementation blueprint for a production Android finance app—and a better foundation for adding optional on-device AI as compatible devices and models become available.