Almost. The scope is now right, and this is the strongest version so far. I would approve the architecture direction, but I would make a few final corrections before giving the document to a coding agent as the implementation contract.

I’m treating dependency versioning and the existing AI router internals as out of scope, as requested.

## Final go/no-go review

1\. Fix the event validator's silent data loss

This is the most important remaining issue. In `validateEvent()`, several failure paths return `null` without recording an unresolved field—for example, invalid evidence IDs, an invalid source row, or an amount that fails candidate matching. The final `needsReview` calculation does not necessarily detect that an event was discarded.

As a result, a document could lose a transaction and still appear successfully processed if its other fields and events pass validation. An invalid `dueDateIso` is also cleared without necessarily forcing review.

Required fix: Every rejected event must record a specific validation failure. Any rejected financial event must force document review, and an invalid supplied due date must also be recorded. Add document-family compatibility checks so an event cannot become a financial record of an inappropriate type merely because its amount is valid.

2\. Strengthen the PII guarantee in `BundleTrimmer`

The new trimmer is much better, but its stated privacy guarantee exceeds what the regex implementation can enforce. `redactFreeText()` removes emails, certain phone numbers, and long digit sequences; it does not recognize arbitrary postal addresses.

Your acceptance test explicitly requires customer addresses embedded in notes, headers, and other free-text fields to be removed. That test will fail for an address such as `123 Main St` unless the field is otherwise excluded or the address is detected.

Required fix: Use an outbound allowlist for fields that genuinely need to reach the model, exclude address-bearing or unnecessary free-text fields, and apply role-based redaction throughout. Do not rely on regexes alone to promise that all personal information is removed.

3\. Do not truncate required candidates to fit the byte budget

`fitSingleChunkToBudget()` eventually keeps `textCandidates.take(6)` and `amountCandidates.take(8)`. Those lists are not guaranteed to contain the candidates required to validate the rows in that chunk.

A chunk could fit within 12,288 bytes while silently losing important evidence or candidates. Passing the byte-limit test does not mean it is a valid interpretation bundle.

Required fix: Preserve the candidates required by the chunk and its locked fields. Remove optional information in a deterministic, role-aware manner. If the required content still does not fit, return `BundleCannotFit` and require local fallback or user review. Never sacrifice mandatory evidence just to satisfy the size limit.

4\. Make reconciliation and review requirements family-specific

There are three related inconsistencies:

- `DeterministicConfidenceGate` bases amount and tax locking on `receiptTotalReconciled`, even though the model also represents statement reconciliation.
- `LockedDeterministicFields` cannot lock statement-summary fields such as opening balance, closing balance, and statement-period dates.
- `needsReview` requires `documentTotal` and `primaryDate` for every document family. Bank and credit-card statements may instead have transaction dates and a statement summary, so this rule can force review even when their relevant fields have been resolved.

Refund and credit-note documents are also routed through receipt/invoice arithmetic formulas without a sufficiently explicit policy for negative amounts and refund sign conventions.

Required fix: Define required and lockable fields by document family. Add statement-summary fields to the lock contract where appropriate, and define dedicated refund reconciliation rules. Keep ambiguous or unreconciled results in review rather than forcing them into a receipt-shaped model.

5\. Complete the currency and deletion invariants

The currency registry relies on `Currency.getAvailableCurrencies()`. That makes the claim of a complete, deterministic ISO 4217 registry dependent on the runtime's currency data. For a financial engine, use a checked-in, maintained currency dataset with explicit minor-unit scales so supported currencies do not vary unexpectedly between devices.

There is also a deletion inconsistency: `deleteDocumentAndAllDerivedEvents()` does not delete `evidence_manifests`, and those entities have no foreign key that would clean them up automatically. The full-deletion path should remove those manifests; the source-purge path can retain them when that is the deliberate provenance policy.

Required fix: Make currency support reproducible and distinguish evidence retention from full user-requested deletion.

## One smaller but important correction

In Section 2.1, PDF text-layer extraction assigns `ocrConfidence = 0.99f`. I would change that to a nullable OCR confidence and set it to `null` for embedded PDF text. Deterministic character extraction is not an OCR confidence measurement, and a high value could inadvertently inflate downstream candidate confidence. Keep `ExtractionProvenance.PDF_TEXT_LAYER` as the separate indicator of how the text was obtained.

Also add one regression test to the chunk merger: when two chunks appear to contain the same event but disagree on its category or payment status, the merger must detect the conflict instead of silently retaining whichever event appears first.

## Verdict

## Architecture approved; implementation contract needs final corrections

The scope boundaries, local-first design, field-level locking, evidence provenance, encrypted storage, and test strategy are now well structured. The remaining issues concern correctness and enforceability, not a need to redesign the system.

Before coding: fix the five items above, correct the PDF confidence representation, and add the corresponding regression tests.

After those changes, the specification should be ready to implement. The final release gate should still require the actual tests and integration build to pass; a specification alone cannot establish that the implementation is correct.