Almost, but I would not give this a final approval just yet. This version fixes most of the issues from the previous review and is substantially stronger. I would rate it 8.5/10 architecturally, but about 7.5/10 as an implementation contract.

The remaining problems are specific and fixable. Two deserve priority before a coding agent starts implementing the full system.

## The remaining blockers

1\. The Android build-tool versions are inconsistent

Build blocker

Your catalog specifies AGP `8.13.0`, Gradle `8.14`, and Hilt `2.60.1`, and declares the Hilt Gradle plugin. The Dagger release notes state that Hilt's Gradle plugin requires AGP 9.0 or later starting with version 2.59; AGP 9.0 itself requires Gradle 9.1 or later.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://github.com\&sz=32)

GitHub

+1



That means the specified toolchain should not be treated as a verified, compatible baseline.

Fix: Choose one coherent combination. Either use a Hilt Gradle plugin version compatible with your AGP 8 setup, or migrate the whole build to a compatible AGP 9/Gradle 9 toolchain. AGP 9 also enables built-in Kotlin, so the existing Kotlin Android plugin configuration needs to be reconciled with that migration.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers



Then run a clean Gradle build and dependency resolution. The specification currently describes that test as a future acceptance criterion; it does not prove the build has passed.

2\. Deterministic field locking still locks too much

Data-integrity blocker

The router passes the entire `deterministicCandidate` as `lockedInterpretation` to the validator. The validator then prefers every non-null field in that object over the model result, including merchant, address, primary date, due date, total, and tax.&#x20;

ai_financial_document_engine_specification (3).md

ai_financial_document_engine_specification (3).md



That conflicts with your intended field-level deterministic gate. A field can be populated by the deterministic engine without meeting the threshold required to lock it.

Fix: Pass an explicit set of locked fields—or a separate, typed locked-field object—into the validator. Only fields that actually passed their deterministic acceptance criteria may override model output. Use the same explicit lock policy consistently across the bundle, validator, and persistence layer.

## Three correctness and privacy gaps remain

### 3. Event evidence validation is not yet complete

The revised validator is much stronger, but some paths still accept values without proving that the cited evidence supports those exact values:

- Transaction dates: The table-cell date branch checks that the date exists in the row and that the cell's evidence IDs are valid, but it does not require those IDs to match the event's `dateEvidenceIds`.&#x20;

  ai_financial_document_engine_specification (3).md

- Payment status: The validator confirms that cited evidence belongs to a `PAYMENT_STATUS_LABEL` candidate, but it does not compare the candidate's text with the returned status. For example, the code does not prove that a label reading “UNPAID” supports `PAID_IN_FULL`.&#x20;

  ai_financial_document_engine_specification (3).md

- Description: `ResolvedFinancialEvent.description` is not validated against a `LINE_ITEM_DESCRIPTION` candidate or matching table cell. The model can therefore generate an unsupported description even when its amount is valid.&#x20;

  ai_financial_document_engine_specification (3).md



Fix: Bind each field to its own evidence IDs and verify the semantic value—not merely that some associated evidence exists. Require tests for contradictory payment labels, unrelated date cells, and invented descriptions.

### 4. PII redaction still misses some serialized fields

The new `BundleTrimmer` correctly removes customer-address cells, redacts emails and phone numbers in text, and verifies the final byte count. That is a meaningful improvement.&#x20;

ai_financial_document_engine_specification (3).md



However, it does not redact every free-text field in `LlmAnalysisBundle`. In particular, `lockedDeterministicFields` is copied through without redaction, `arithmeticValidation.notes` is retained unchanged, and `ReconstructedTableCell.headerLabel` is not sanitized alongside `cell.text`.

Fix: Apply redaction to every text-bearing field that can reach serialization, not just candidate values and table-cell text. Add a serialized-payload test that seeds every free-text field with synthetic addresses, emails, phone numbers, and account identifiers and verifies that sensitive values do not escape.

### 5. Chunk merging can lose or retain the wrong events

The aggregation code deduplicates financial events using only `sourceRowId`. It drops every subsequent event associated with a row, while events without a source-row ID are never deduplicated. It also builds the final interpretation from the first chunk's document-level fields without reconciling conflicting fields returned by later chunks.&#x20;

ai_financial_document_engine_specification (3).md



Fix: Define a stable event identity and deduplication rule, preserve legitimate multiple events when appropriate, and reconcile document-level fields across chunks. Conflicting totals, dates, document families, or statement summaries should trigger review rather than silently taking the first result.

## Two final design checks

BYOK routing: `BYOK_GEMINI` is present as a provider, but the default `preferredModelIdByProvider` map and `ModelCapabilityRegistry` do not include it.&#x20;

ai_financial_document_engine_specification (3).md

ai_financial_document_engine_specification (3).md

&#x20;The router can therefore skip BYOK if the UI or configuration layer has not supplied a provider-specific model ID. Define that contract explicitly and test a complete BYOK request, including key availability and failure handling.



Persistence consistency: `FinancialEventEntity.documentId` is described as nullable so verified events can survive source-document deletion, but the entity declaration has no foreign key or explicit deletion workflow implementing that behavior. The retention section also refers to deleting stored `OcrElement` rows without defining an `OcrElementEntity` in the supplied Room schema.&#x20;

ai_financial_document_engine_specification (3).md

ai_financial_document_engine_specification (3).md

&#x20;Clarify whether OCR elements are persisted and specify how document deletion preserves verified events while removing source evidence.



Your chosen hosted model IDs are supported by the current official documentation for the Gemini API and Firebase AI Logic.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://ai.google.dev\&sz=32)

Google AI for Developers

+1

&#x20;The remaining concern is not the basic model choice, but the compatibility, provenance, and routing contracts around those models.



## Final verdict

The specification now has the right overall architecture. The local-first processing boundary, provider-specific consent, normalized homography, strict currency parsing, signed-ledger reconciliation, draft amount representation, and byte-verified bundle trimming are all strong design decisions.

I would make five fixes mandatory before implementation: the build-toolchain conflict, field-locking behavior, complete event-field provenance, complete serialized-payload redaction, and safe chunk aggregation. Then resolve the BYOK and persistence inconsistencies.

Once those are corrected—and the clean build plus invariant tests actually pass—I would be comfortable calling this implementation-ready. You are close, but the document itself is not yet proof that the implementation will compile or meet its zero-silent-hallucination target.