# Review: AI Financial Document Engine Specification

This is a strong engineering blueprint, but I would revise several important parts before handing it to an AI coding agent. The local document-processing architecture is excellent. The biggest problems are the relationship between the user's selected AI and automatic fallbacks, the cloud privacy policy, and a few implementation details that could cause incorrect financial records.

My assessment: 7.5/10 as an architecture specification; not yet ready for implementation as written.

The document already establishes the right core principles: no images sent to the LLM, integer minor-unit monetary values, evidence provenance, deterministic extraction, provider adapters and a validation layer.&#x20;

ai_financial_document_engine_specification.md



## 1. The most important change: respect the user's AI selection

Your spec has four AI options in Settings, but its fallback policy always emphasizes this order:

`BYOK → Local AI → Firebase → Gemma → Deterministic`

It also allows cloud fallback when local AI is selected, with the default set to `true`.&#x20;

ai_financial_document_engine_specification.md



That is the first thing I would change.

For a financial app, choosing on-device AI should not silently result in sending extracted financial text to a cloud provider. Even though the image stays local, the OCR JSON can contain names, addresses, account details, transactions and balances.

I recommend separating the user's selected AI from the fallback policy.

## Recommended AI Settings

Keep your existing multiple-provider selection, but make the routing behavior explicit.

Primary AI Engine

The engine that receives the first request when deterministic processing cannot resolve the document.

On-device Gemini Nano

Private, on-device interpretation when supported.

BYOK

Use the user's configured API provider and key.

Firebase AI Logic

Use the configured Firebase cloud backend.

Hosted Gemma

Use a specifically configured hosted Gemma model.

Fallback Policy

On-device only — deterministic fallback if local AI is unavailable

Selected AI first, then explicitly approved fallback providers

Allow cloud fallback when the selected AI fails

Recommended privacy default: document text never leaves the device for AI processing.

The UI above illustrates the proposed behavior, rather than modifying the uploaded specification.

I would make on-device-only mode the default for a privacy-first finance app, with cloud fallback disabled unless the user explicitly enables it. A selected cloud provider can still be used directly when the user knowingly chooses it.

Provider availability should also have an `UNKNOWN` or `NOT_CHECKED` state. Your current example initializes Firebase and Gemma as ready before a health check has occurred, which can misrepresent their availability.&#x20;

ai_financial_document_engine_specification.md



## 2. Clarify the Gemma 4 cloud option

This is a specific issue worth fixing before development.

Gemma 4 is real, and Google currently documents hosted access to `gemma-4-31b-it` and `gemma-4-26b-a4b-it` through the Gemini Developer API. Your example model name `gemma-4-27b-it` does not match those documented model identifiers.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://ai.google.dev\&sz=32)

Google AI for Developers



The distinction I would make is:

- On-device Gemini Nano: a local inference option.
- On-device Gemma: a separate local model/runtime option, if you intend to support it.
- Hosted Gemma through Gemini API: a cloud model option that requires an API key.
- Firebase AI Logic: a distinct cloud integration and configuration path.

Your enum currently calls the hosted Gemma option `GEMMA_4_CLOUD_FREE`, but that is a model/hosting choice rather than a clearly independent provider. Also, Firebase's supported-model documentation distinguishes its supported Gemini models from Gemma models available in certain hybrid or on-device configurations.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://firebase.google.com\&sz=32)

Firebase

+1



There is a more important privacy consideration. Google's Gemini API documentation says its free tier may use submitted content to improve its products, whereas its paid tier says submitted content is not used for that purpose.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://ai.google.dev\&sz=32)

Google AI for Developers



For financial documents, do not label a hosted model simply “Free” and make it the default without explaining those data-handling terms. Verify the applicable terms and availability when implementing the provider.

## 3. The provider adapters need capability-specific implementations

Your shared `DocumentInterpreter` interface is a good idea. However, the specification asks every provider to use the same system prompt and request `application/json`.&#x20;

ai_financial_document_engine_specification.md



The intent can be shared, but the technical request format should not be assumed to be identical for every model.

Google's current documentation describes Kotlin data classes with `@Generable` and KSP for structured output using the ML Kit Prompt API, as well as provider-specific structured-output support for Firebase AI Logic. The supported model capabilities also differ.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers

+2



I would require every adapter to declare its capabilities, including:

- Text-only inference support.
- Structured-output or JSON-schema support.
- Maximum supported request size and practical context budget.
- Availability checks and initialization failures.
- Provider-specific authentication.
- Rate limits, timeouts and retry behavior.

The application should use a common domain-level output schema, but each adapter should translate it to the appropriate API request and validate the returned interpretation independently.

One other important point: your generic `BYOK` option currently looks like a Gemini API key configuration, with a “Gemini Model” selector. If BYOK is intended to support multiple vendors, each vendor needs its own adapter and connection/model configuration. Otherwise, rename it to make clear that it means a user-provided Gemini API key.

## 4. Correct the dependency and security section

The Gradle section needs a version and API review. For example, the document pins Room 2.6.1, while Android's current stable release is Room 2.8.5 as of September 9, 2026.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers



I would specifically revise the following:

| Current specification        | Recommended correction                                                                             |
| ---------------------------- | -------------------------------------------------------------------------------------------------- |
| `room-ktx:2.6.1`             | Use a current, compatible Room release from the version catalog.                                   |
| `EncryptedSharedPreferences` | Replace this deprecated API with a supported secret-storage design.                                |
| `aicore:0.0.1-exp01`         | Verify the artifact and replace it if necessary with the current documented on-device integration. |
| Fixed model identifiers      | Use provider-specific, capability-checked model configuration.                                     |
| SQLCipher 4.5.4              | Verify the current Android integration, Room compatibility and migration strategy.                 |

Android marks `EncryptedSharedPreferences` deprecated in version 1.1.0.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers



For BYOK, protecting a key at rest is necessary, but it cannot make a user-owned API key completely immune to extraction from a compromised device. Make that limitation clear, avoid logging keys, and ensure the user can delete their stored credential.

For the database, the specification should explain how the SQLCipher passphrase is generated and protected by Android Keystore, rather than merely saying that a 256-bit key is protected. It should also define how the encrypted document files are handled separately from database encryption.

## 5. Fix the document and transaction data contracts

The JSON contracts are a strong start, but I would make four changes.

Evidence IDs must be globally unique. Your examples use IDs such as `T014`, while OCR records also have a page number. On a multi-page bank statement, page 1 and page 2 could both contain `T014`. The LLM could then reference the wrong evidence. Use IDs incorporating the document and page, or otherwise guarantee document-wide uniqueness.&#x20;

ai_financial_document_engine_specification.md



The output schema is currently receipt-heavy. `ResolvedTransaction` has a required `total`, but a bank statement is organized around transaction amounts, debit/credit direction, running balances and account context. Add explicit statement-level fields for the institution, statement period, opening and closing balances, masked account reference, and signed transaction amounts or an equivalent unambiguous debit/credit representation.&#x20;

ai_financial_document_engine_specification.md



Do not assume every currency uses cents. The integer minor-unit approach is correct, but a general finance app should interpret the currency's actual minor-unit precision. Avoid comments that imply every currency uses two decimal places.

Validate semantic evidence, not just evidence existence. The current validator checks that IDs exist and that monetary amounts match candidate values. Extend it to confirm that the evidence supports the particular field role. A valid OCR ID for a total should not be accepted as evidence for the merchant name. Permit deterministic derived amounts only when the computation and source evidence are recorded explicitly.&#x20;

ai_financial_document_engine_specification.md



## 6. Fix the OCR coordinate and layout details

There is a subtle problem with your two-bitmap strategy.

You propose running OCR on `cleanedBitmap` while displaying evidence boxes over the original `rawBitmap`.&#x20;

ai_financial_document_engine_specification.md



If preprocessing changes rotation, perspective, crop or deskew, the OCR coordinates may not line up with the original image. Save the transformation between the coordinate spaces and map bounding boxes back to the original image for review, or render the review overlay against the exact OCR image.

I would also revise the “polynomial baseline row clustering” requirement. The detailed algorithm subsequently describes a linear projection based on a single text-line angle. That does not fully specify how curved thermal receipts are handled.&#x20;

ai_financial_document_engine_specification.md



Keep the layout engine modular, establish a reliable baseline using OCR boxes and statistical grouping, and add more advanced dewarping or curved-baseline analysis only when representative test documents demonstrate a need.

Finally, the diagnostics design stores `promptBundleJson` and `rawResponseJson` in the database and exposes them in the diagnostics screen. Those fields may contain sensitive financial text.&#x20;

ai_financial_document_engine_specification.md



Store redacted diagnostics by default. Make full prompt/response retention an explicit, time-limited debugging option with a clear deletion mechanism.

## 7. Adjust the testing requirements

The implementation phases are sensibly ordered, but I would not require 100% accuracy on synthetic receipt and statement fixtures as the main acceptance criterion.&#x20;

ai_financial_document_engine_specification.md



Use synthetic fixtures to test exact arithmetic and known layout cases, then build a separate test corpus covering real-world variation: multi-page statements, skew, glare, wrapped descriptions, uncertain dates, refunds, transfers and indistinguishable amounts.

Evaluate the complete pipeline as well as the deterministic engine alone. Measure field accuracy, transaction classification, reconciliation failures, duplicate detection, correction rates and performance across devices. The important production question is not whether the model returns valid JSON, but whether the resulting financial records are correct and traceable.

## Final recommendation

Keep the overall architecture. The local OCR, layout reconstruction, deterministic extraction and evidence-backed database design are the right foundation.

Before implementation, prioritize three changes: make the selected AI and fallback privacy policy unambiguous; verify and correct the provider/model APIs and dependencies; and harden the output contracts for multi-page evidence and bank-statement transactions.

After those changes, this becomes a much stronger implementation specification for an Android finance app with user-selectable AI engines.


---
Most phones (almost all) today do not have local ai. That is why we call Gemma from the clould
---

You're right. That changes how I would prioritize the architecture, and your decision to use hosted Gemma as the default AI makes sense for an Android app targeting the broadest range of phones.

I was overemphasizing local inference in my review. Your specification already accounts for the reality that Gemini Nano isn't available on every device and that a cloud model provides broader coverage.

## The AI routing I recommend for your app

Keep all four AI options in Settings, but make hosted Gemma the default for users who haven't configured anything else.

1\. Deterministic document engine

OCR, geometry, color, tables, amounts and arithmetic — all local

2\. Selected AI provider

Default: Hosted Gemma

Send compact, text-only JSON for document interpretation

3\. Fallback policy

Use an explicitly permitted alternative if the selected provider fails

4\. Validate and persist

Verify evidence, amounts and transaction types before saving to Room

## What I would change in your document

Your existing chain:

`BYOK → Local AI → Firebase → Gemma → Deterministic`

can remain a supported fallback configuration, but I would distinguish it from the default primary provider.

| Setting                   | Recommended default                                |
| ------------------------- | -------------------------------------------------- |
| Primary AI                | Hosted Gemma                                       |
| Local Gemini Nano         | Optional optimization when supported               |
| BYOK                      | User-configurable alternative                      |
| Firebase AI Logic         | User-configurable alternative or approved fallback |
| Deterministic engine      | Always available; no AI required                   |
| Cloud fallback permission | Explicitly configurable                            |

One nuance: if hosted Gemma is the default and fails, trying Gemini Nano before another cloud provider may be a sensible optimization on supported devices. However, do not assume local AI is installed or available; check at runtime.

## The important distinction: local processing versus local inference

Your app can still be privacy-oriented even when its AI runs in the cloud.

- Local processing: the original image never leaves the device. OCR, table reconstruction, color analysis and arithmetic run locally.
- Cloud inference: the compact JSON bundle is sent to the selected provider. This still contains potentially sensitive financial information, even without an image.
- Local fallback: if no permitted AI provider is available, the deterministic engine continues and flags ambiguous fields for review.

That is the architecture I would use for your target market.

My revised verdict: keep the hosted Gemma default. I would change the specification to make the distinction between primary selection and fallback routing explicit, not replace your cloud-first default with a local-first AI requirement. The local-first part should describe the document processing and data handling, not require the LLM itself to run locally.

