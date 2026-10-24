# On-device AI financial document engine for Android

The best design is to build a local-first financial document intelligence engine that converts receipts, bills, invoices, and bank statements into structured, evidence-backed JSON before an LLM ever sees the data.

The LLM should not have to look at an image, perform basic arithmetic, determine pixel coordinates, or rediscover information that conventional algorithms can already extract. Its job is to interpret ambiguous text, identify the document type, resolve the meaning of fields, and help classify transactions.

Your app should use this pipeline:

1\. Capture document locally

Camera, gallery, scanned document or PDF

2\. OCR and image analysis

Text, coordinates, confidence, colors, document boundaries and layout

3\. Deterministic intelligence engine

Statistics, spatial analysis, regex, table reconstruction, dates, amounts, addresses and arithmetic validation

4\. Evidence-rich JSON

Every value retains its source, position, confidence and alternatives

5\. On-device LLM

Gemini Nano or a locally deployed Gemma model interprets the extracted evidence

6\. Validate and save

Reconcile totals, resolve confidence, request user review when necessary, then write to the financial database

This is achievable with existing Android technology. Google's ML Kit Text Recognition API provides text at block, line, word and character levels, with bounding coordinates and confidence information. Gemini Nano's ML Kit Prompt API can process text-only prompts and return structured output; Google's current documentation also describes an on-device expense-management use case.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developers.google.com\&sz=32)

Google for Developers

+3



The central principle is that the raw document stays on the device, the algorithms do the measurable work, and the LLM handles interpretation.

## 1. Use the camera image for measurement, not for the LLM

The app should retain access to the captured pixels locally, even though the LLM will never receive them. Those pixels contain information that OCR cannot provide on its own.

### Document dimensions and quality

Extract the following before OCR:

| Feature        | What to calculate                                         |
| -------------- | --------------------------------------------------------- |
| Image size     | Width, height, pixel count, file size, format             |
| Document shape | Detected corners, aspect ratio, orientation               |
| Page geometry  | Perspective distortion, rotation, crop boundaries         |
| Image quality  | Blur, contrast, brightness, shadows, glare                |
| OCR quality    | Text recognition confidence, missed or suspicious regions |
| Source         | Camera, gallery image, imported PDF, page index           |

Important distinction: you can determine the image's pixel dimensions exactly. You can determine a PDF's physical page dimensions when available. You cannot reliably determine a piece of paper's actual physical dimensions from an ordinary photograph alone without a known reference or scale.

For capture, consider ML Kit Document Scanner. It supports on-device page detection, cropping, rotation correction and cleanup, without requiring your app to request camera permission for its scanner flow.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developers.google.com\&sz=32)

Google for Developers



Preserve the original image locally. Create a separate cleaned version for OCR so that correcting perspective, removing shadows or converting to grayscale does not destroy information needed for color analysis.

## 2. Extract every text element and its location

Use ML Kit Text Recognition v2 as the first OCR engine.

It provides a hierarchy of recognized text blocks, lines, elements and symbols, with bounding coordinates, rotation information and confidence attributes. This gives you a spatial representation rather than just a long string.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developers.google.com\&sz=32)

Google for Developers



For each recognized element, retain:

- Exact OCR text and a separately normalized version.
- Page number and element ID.
- Bounding box in original pixel coordinates.
- Normalized bounding box, with coordinates expressed between 0 and 1.
- OCR confidence and rotation.
- Parent block, line and table-row identifiers.
- Inferred region, such as header, body, footer, left column or right column.
- Candidate semantic role, such as merchant, date, subtotal, tax, account number or transaction description.
- Evidence IDs for any financial values derived from it.

For example:

```
{
  "id": "T014",
  "text": "TOTAL $20.07",
  "page": 1,
  "line_id": "L009",
  "box_px": {
    "x": 720,
    "y": 1890,
    "width": 310,
    "height": 44
  },
  "box_normalized": {
    "x": 0.60,
    "y": 0.7875,
    "width": 0.2583,
    "height": 0.0183
  },
  "region": "lower_body",
  "column": "right",
  "ocr_confidence": 0.98
}
```

These are illustrative coordinates and confidence values, not the output from an actual receipt.

The system can now answer questions such as:

- What is printed in the upper-left corner?
- Which text is closest to the total?
- Which amount is aligned with a particular transaction description?
- Is a date in the header, the transaction table or the footer?
- Did two values appear on the same line, or in different columns?

This is the foundation for the financial parser. Do not discard the boxes after obtaining the recognized text.

## 3. Analyze the document's colors locally

Color should be measured directly from the image, not guessed by the LLM.

Use Android bitmap operations or an appropriate local computer-vision implementation to calculate:

Page and background colors

Estimate the dominant paper/background color, brightness, color cast, and variation caused by lighting or scanning.

Header and logo colors

Sample colored regions around the merchant's name and logo. These features can help distinguish document templates and repeated merchants.

Text and ink characteristics

Measure text contrast, grayscale intensity, and differences between printed regions and the background.

Regional color features

Calculate color histograms and dominant colors for the header, body, footer and detected logo areas separately.

Store RGB values and hex colors where useful, together with a perceptual color representation such as CIELAB, dominant-color proportions and regional coordinates.

For instance, a green header, a centered logo and a distinctive column arrangement may collectively support a match against a previously seen merchant template. None of these should independently establish the merchant's identity.

Color analysis can also help detect poor-quality captures. A document with low contrast, excessive yellow illumination or uneven shadows may warrant an additional OCR pass using a corrected image.

## 4. Locate the company name and address intelligently

Do not simply search the first five OCR lines and assume the first uppercase text is the company name.

Build a local entity candidate detector that considers all relevant document regions, including both the top and bottom.

| Candidate             | Local detection methods                                                               |
| --------------------- | ------------------------------------------------------------------------------------- |
| Company name          | Header position, text prominence, capitalization, logo proximity, merchant dictionary |
| Postal address        | Street, suite, city, region and postal-code patterns; line grouping                   |
| Phone                 | Country-aware phone-number patterns                                                   |
| Website               | URL and domain patterns                                                               |
| Email                 | Email pattern                                                                         |
| Company identifiers   | Known business identifier formats and document-specific labels                        |
| Document issuer       | Company name plus address, phone, website and recurring template features             |
| Customer or recipient | Labels such as `BILL TO`, `CUSTOMER`, `SHIP TO` or `ACCOUNT HOLDER`                   |

### How top and bottom detection should work

First, divide the page into geometric regions. Search both the header and footer for company and address candidates.

Then score each candidate using several independent signals:

- Its position relative to the document's boundaries.
- Whether several address-like lines appear together.
- Whether it is near a phone number, website or logo.
- Whether associated words such as `Invoice`, `Statement`, `Receipt` or `Remit To` suggest an issuer role.
- Whether the candidate matches a previously recognized merchant or document template.

Keep alternatives. A utility bill might display its issuer at the top and its payment-remittance address at the bottom. A bank statement may include both the bank's mailing address and the customer's address.

The correct output is therefore not simply `address`. It should include `issuer_address`, `customer_address` and `remittance_address` as separate candidate roles when the evidence supports them.

Avoid network-based geocoding during document processing. It is unnecessary to extract an address and can expose private financial information.

## 5. Reconstruct tables, rows and columns

This is one of the most important parts of the design. OCR text alone often loses the relationship between a description and the amount printed beside it.

Use geometry and statistical grouping to reconstruct the document's layout.

### Local layout algorithms

Line grouping: group OCR elements that have similar vertical positions, compatible text heights and nearby horizontal edges.

Column detection: cluster element centers and edges, measure gaps between text groups, and detect repeated vertical alignments.

Table reconstruction: group lines into rows using vertical overlap and spacing. Infer headers from labels such as `Date`, `Description`, `Debit`, `Credit`, `Balance`, `Qty`, `Price` and `Amount`.

Reading order: order elements within columns and then establish the likely document-specific reading sequence.

Key-value association: pair a label with values to its right, below it or within the same aligned region.

Typography and prominence: use relative text height, visual darkness and region size to identify potentially prominent headings and totals. Treat these as features, not definitive font-style classifications.

You can build these algorithms in Kotlin and run them locally. The initial implementation does not need an LLM to identify the geometry of each row.

For example, a receipt line might contain:

| Item description | Quantity | Unit price | Line total |
| ---------------- | -------- | ---------- | ---------- |
| Coffee beans     | 2        | $8.00      | $16.00     |
| Paper filters    | 1        | $2.50      | $2.50      |

The parser should preserve each row and each cell's evidence IDs and bounding boxes. If OCR places an amount in the wrong column, the parser should flag the ambiguity rather than silently create a transaction.

For bank statements, reconstruct separate columns for date, description, debit, credit and balance. A negative amount in a debit column means something different from the same number in a credit column; layout is part of the financial meaning.

## 6. Extract financial information before the LLM

Implement a deterministic financial extraction layer using regular expressions, locale-aware parsers, arithmetic checks, known labels, and document-specific rules.

Its job is to generate candidate fields and evidence rather than make every final semantic decision.

| Data group          | Fields to extract locally                                                                              |
| ------------------- | ------------------------------------------------------------------------------------------------------ |
| Document identity   | Receipt, invoice, bill, bank statement, credit-card statement, transfer confirmation, check or unknown |
| Merchant and issuer | Business name, address, phone, website, company identifiers                                            |
| Document dates      | Purchase date, invoice date, due date, statement start and end dates                                   |
| Amounts             | Subtotal, tax, tip, discount, total, amount paid, amount due, minimum payment and balance              |
| Line items          | Description, quantity, unit price, amount, tax status and category candidates                          |
| Bank transactions   | Posting date, transaction date, payee, description, debit, credit, running balance                     |
| Account details     | Institution, account type, safely masked account identifier and statement period                       |
| Payment details     | Payment method, reference number, payment status and due date                                          |

Use context-aware OCR correction. For example, `O` may be a zero in an amount but a letter in a company name. Keep the original OCR text and store any corrected interpretation separately.

### Validate with arithmetic

Arithmetic validation is particularly valuable because it does not require generative AI.

For a receipt, compare the detected subtotal, taxes, tips, discounts and total. For a statement, reconcile opening balance, credits, debits and closing balance where the statement provides those figures.

A reconciliation failure does not necessarily mean the OCR is wrong: a receipt may contain rounding, excluded items or other charges. The system should identify the discrepancy and lower confidence rather than invent a correction.

The same principle applies to dates and currencies. A candidate date must be plausible within the document context; an amount must retain its currency and sign interpretation.

## 7. Design the enriched JSON as an evidence bundle

I recommend maintaining two connected representations:

1. Full local document record: contains all OCR elements, geometry, color features, table structure, candidate values, confidence values and provenance.
2. LLM analysis bundle: contains the portions of that record needed to make a semantic decision.

This distinction matters. You want to preserve as much information as possible without forcing the LLM to repeatedly process every pixel coordinate and every low-value OCR element.

Here is an illustrative structure for the full local record:

```
{
  "schema_version": "1.0",
  "document_id": "local-doc-001",
  "source": {
    "type": "camera_scan",
    "page_count": 1
  },
  "pages": [
    {
      "page_number": 1,
      "image": {
        "width_px": 1200,
        "height_px": 2400,
        "file_size_bytes": 245000,
        "orientation": "portrait",
        "physical_size": null
      },
      "visual_features": {
        "dominant_colors": [
          {
            "hex": "#F5F1E8",
            "role": "background"
          },
          {
            "hex": "#264D3A",
            "role": "header"
          }
        ]
      },
      "ocr_elements": [
        {
          "id": "T001",
          "text": "GREENFIELD MARKET",
          "confidence": 0.98,
          "region": "header",
          "box_normalized": {
            "x": 0.20,
            "y": 0.05,
            "width": 0.60,
            "height": 0.025
          },
          "line_id": "L001"
        },
        {
          "id": "T002",
          "text": "123 MAIN ST",
          "confidence": 0.97,
          "region": "header",
          "box_normalized": {
            "x": 0.25,
            "y": 0.085,
            "width": 0.50,
            "height": 0.018
          },
          "line_id": "L002"
        },
        {
          "id": "T014",
          "text": "TOTAL $20.07",
          "confidence": 0.98,
          "region": "lower_body",
          "box_normalized": {
            "x": 0.60,
            "y": 0.7875,
            "width": 0.2583,
            "height": 0.0183
          },
          "line_id": "L009"
        }
      ],
      "tables": [
        {
          "id": "TABLE001",
          "kind": "receipt_line_items",
          "rows": []
        }
      ],
      "field_candidates": {
        "merchant": [
          {
            "value": "GREENFIELD MARKET",
            "evidence_ids": ["T001"],
            "confidence": 0.98
          }
        ],
        "issuer_address": [
          {
            "value": "123 MAIN ST",
            "evidence_ids": ["T002"],
            "confidence": 0.75
          }
        ],
        "total": [
          {
            "amount": 20.07,
            "currency": "USD",
            "evidence_ids": ["T014"],
            "confidence": 0.98
          }
        ]
      }
    }
  ]
}
```

The example omits many optional fields and uses illustrative sample values. A production record should retain all recognized elements, not only the three shown here. It should also preserve raw pixel coordinates, OCR hierarchy, line-item cells, alternate candidates and processing provenance.

The `physical_size` is `null` because a camera image alone does not establish the actual physical dimensions of the original paper.

Every normalized financial value should point back to its source evidence. If the model identifies `20.07` as the total, the app should know exactly which OCR element produced that value and where it was located.

## 8. Give Gemini Nano or Gemma a narrow, explicit task

Once the deterministic pipeline has produced the enriched record, send the LLM a compact representation containing:

- Document type candidates.
- Merchant and address candidates.
- Relevant OCR lines with element IDs and normalized positions.
- Reconstructed table headers and rows.
- Candidate dates, amounts, currencies and payment terms.
- Arithmetic reconciliation results.
- Confidence and ambiguity flags.
- Relevant, local merchant and category history where useful.

Do not ask the LLM to repeat OCR, recompute the entire document geometry or manufacture missing financial fields.

A good instruction for the model would be:

```
You are a financial document interpretation engine.

Use only the supplied extracted text, layout metadata,
candidate fields and validation results.

1. Classify the document type.
2. Identify the issuer, merchant and document recipient.
3. Determine the semantic role of each supplied financial field.
4. Distinguish a purchase transaction from a bill payment,
   account transfer, statement balance or credit.
5. Suggest categories using the supplied category taxonomy.
6. Return evidence IDs for every extracted or resolved field.
7. Do not invent missing values or correct source amounts silently.
8. Preserve ambiguity when the evidence is insufficient.
9. Return only the requested structured result.
```

The model should return a separate interpretation object, not rewrite the entire original OCR JSON. The app then merges the validated interpretation into the document record.

### Recommended LLM output

```
{
  "document_type": "receipt",
  "merchant": {
    "value": "GREENFIELD MARKET",
    "evidence_ids": ["T001"]
  },
  "transaction": {
    "date": null,
    "total": {
      "amount": 20.07,
      "currency": "USD",
      "evidence_ids": ["T014"]
    },
    "category": "Groceries",
    "category_source": "model_suggestion"
  },
  "review_required": true,
  "unresolved_fields": ["transaction.date"]
}
```

This is illustrative. The model should not produce `20.07` unless the supplied evidence actually supports that value, and the category should be checked against your application's category taxonomy and applicable user preferences.

The app's output validator should reject malformed JSON, invalid category identifiers, references to nonexistent evidence IDs and financial amounts that conflict with authoritative source candidates.

## 9. Which on-device AI should you use?

There are two practical paths. They should sit behind the same Kotlin interface, so your financial logic does not depend on a specific model.

Option A — Gemini Nano via ML Kit Prompt API

Best first prototype

Uses Android's AICore service and processes text locally. It is the easiest starting point if the target device supports the API. The Prompt API accepts text-only requests, so there is no requirement to send an image to the LLM.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developers.google.com\&sz=32)

Google for Developers

+1



Limitation: device and model availability varies. The Prompt API is currently documented as beta, and its structured-output functionality is alpha. Check availability at runtime and keep your parser independent of these APIs.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developers.google.com\&sz=32)

Google for Developers

+2



[Google Launches AI Edge Gallery on GitHub to Run Hugging Face AI Models Locally Without Internet on Android, iOS Coming Soon | DeepNewz Software](https://images.openai.com/static-rsc-4/zYVWwVOG2RqJGyY-PFy_ybLjwqOF4VXSLXF8K7HM16Aa2qBe3wfdAPSi8Gc-TtfliR-opeb4G0Cshgm1GVOZ2Iy_KKBKzSbE5rkMvtvSc69_H6Lf2-Dpju_na8xb5P6LWmkctp964dNytBqL_JHuLUMtMOn4QdF45HguzxthhL4?purpose=inline)

Option B — Gemma via LiteRT-LM

Best for model control

Use Google's LiteRT-LM Kotlin API to run a compatible Gemma model locally with CPU, GPU or supported NPU acceleration. This gives you more control over the selected model and its deployment. Google's current guidance recommends LiteRT-LM over the older MediaPipe LLM Inference API, which is in maintenance-only mode.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developers.google.com\&sz=32)

Google for Developers

+2



Limitation: model files, RAM requirements, startup time and inference performance must be tested on your target devices. Larger models can impose significant storage and memory costs. LiteRT-LM supports hardware acceleration, but the most appropriate backend depends on the device.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developers.google.com\&sz=32)

Google for Developers

+1



### My recommendation

Start with ML Kit OCR + Gemini Nano where supported + deterministic fallback everywhere. Put the model behind an interface so you can add a locally deployed Gemma model without rewriting the finance engine.

```
interface DocumentInterpreter {
    suspend fun interpret(
        bundle: FinancialDocumentBundle
    ): DocumentInterpretation
}
```

Implement separate adapters for Gemini Nano, LiteRT-LM Gemma and the deterministic rules engine. The model provider should report availability, initialization failures and unsupported devices without blocking document capture or database access.

Do not silently route financial documents to a cloud model when an on-device model is unavailable. In a privacy-oriented finance app, an on-device deterministic fallback and a review screen are better defaults.

## 10. Build the algorithms into a modular Android architecture

I would structure the app as Kotlin modules with a clean separation between capture, document understanding, AI interpretation and the finance database.

`:feature:documentcapture`

Scanner UI, CameraX or document scanner integration, gallery and PDF import.

`:core:ocr` and `:core:document:vision`

ML Kit OCR, image quality analysis, image preprocessing, color measurement and coordinate normalization.

`:core:document:layout`

Text grouping, headers and footers, column detection, reading order, key-value matching and table reconstruction.

`:core:finance:extract` and `:core:finance:validation`

Financial patterns, field candidates, transaction rows, date and currency parsing, arithmetic reconciliation and duplicate detection.

`:core:ai`

Gemini Nano and Gemma adapters, prompt construction, JSON serialization, model availability and interpretation validation.

`:core:database` and `:feature:review`

Room persistence, transaction records, evidence provenance, category rules, document history and manual correction.

The critical boundary is that the AI module returns interpretation candidates. The finance domain layer owns validation and the decision to persist an actual transaction.

For a Compose interface, the review screen should display the captured document beside its extracted fields, with bounding boxes highlighting the evidence behind each value. Let the user tap a total, date, merchant or line item to correct it. Save those corrections as structured feedback for merchant rules and future tests rather than silently training the model from a single correction.

## 11. Prevent financial interpretation errors

The hardest part is not extracting amounts. It is understanding what each amount represents.

A bank statement might contain a purchase, a credit-card payment, a transfer between two of the user's accounts and an account balance. These cannot all be treated as expenses.

I would establish the following rules:

- Do not create a transaction from a statement balance. A statement balance is not an individual purchase.
- Distinguish transfers from spending. Transfers between owned accounts should not inflate spending totals.
- Separate transaction date, posting date and due date. The labels and their document positions matter.
- Prevent duplicates. Compare normalized transaction fingerprints and document hashes, with safeguards against treating legitimate recurring payments as duplicates.
- Reconcile statement transactions. Where data permits, check totals against opening and closing balances.
- Retain provenance. Keep original OCR text, normalized values, model interpretations and user corrections distinguishable.

You should also define field-level confidence and an explicit review policy. A merchant might be confidently recognized while its total remains ambiguous. A single document-wide confidence score would conceal that distinction.

For a first release, make high-confidence extraction eligible for fast review, but do not let a model's confidence estimate alone authorize a financial database write. Validate amounts, types, currencies and evidence references independently.

## 12. Store evidence, documents and transactions separately

Your Room database should distinguish at least these concepts:

| Entity                                | Purpose                                                                                     |
| ------------------------------------- | ------------------------------------------------------------------------------------------- |
| `FinancialDocument`                   | Document type, issuer, source hash, capture time and processing status                      |
| `DocumentPage`                        | Page geometry, image metadata and page-level visual features                                |
| `OcrElement`                          | Raw text, coordinates, OCR confidence, reading order and parent IDs                         |
| `FieldCandidate`                      | Candidate merchant, address, date, amount or other semantic field, with evidence references |
| `DocumentTable` / `DocumentTableCell` | Reconstructed rows, columns and individual cell provenance                                  |
| `Transaction`                         | Canonical financial event with date, amount, currency, account and category                 |
| `TransactionItem`                     | Optional receipt line items, quantities, unit prices and totals                             |
| `CategoryRule` / `MerchantRule`       | User-defined or validated merchant and category mappings                                    |
| `InterpretationRun`                   | Algorithm version, model/runtime version, processing diagnostics and output provenance      |

The financial database should not be the only place where the information lives. A statement may contain many transactions, and an invoice may contain line items and a payment event. The document and its derived financial records need a traceable relationship.

For privacy, keep images and OCR text in app-private storage, minimize retention of original images, and avoid logging raw financial text. Protect stored financial records and consider database encryption and backup exclusions according to the app's threat model. On-device inference alone does not automatically make all local data secure.

## 13. Optimize performance and model input

The processing pipeline should be asynchronous and incremental.

1. Capture and prepare the page.
2. Run OCR and calculate text and image features.
3. Re-run OCR only for low-quality or ambiguous areas.
4. Build layout tables and deterministic financial candidates.
5. Check whether the rules engine can resolve the document without a model call.
6. Invoke the on-device LLM only for unresolved semantic decisions.
7. Validate the result and present corrections where necessary.

For long bank statements, parse and validate the table locally, page by page. Give the LLM the transaction rows and relevant field evidence, rather than asking it to process a whole statement as an undifferentiated block of text.

Track OCR duration, layout processing duration, model latency, token count when available, peak memory, low-confidence fields, correction rates and model initialization failures. Those measurements will show whether the bottleneck is OCR, table reconstruction or LLM inference.

## 14. Test against a document corpus, not just a few receipts

Create a local test corpus of consented or synthetic documents covering retail receipts, restaurant receipts, invoices, utility bills, bank statements, credit-card statements, multi-page statements and photographed documents with poor lighting.

Measure:

- OCR character and field accuracy.
- Merchant and address extraction accuracy.
- Date, amount and currency accuracy.
- Line-item-to-amount association accuracy.
- Debit, credit and transfer classification accuracy.
- Total and balance reconciliation rates.
- Percentage of fields requiring user correction.
- Duplicate transaction rate.
- Processing time and memory use across representative devices.

Compare the deterministic parser alone with the complete pipeline. This establishes whether the model actually improves extraction enough to justify its latency, storage and memory costs.

## Final architecture recommendation

Build the system around a deterministic document engine with optional on-device semantic reasoning.

The high-value engineering work is the local image feature extractor, OCR evidence store, spatial layout engine, financial candidate detector, arithmetic validator and transaction reconciliation logic. Those components will give both Gemini Nano and Gemma substantially better context than a plain OCR text dump.

The LLM should be a replaceable interpretation layer, not the source of truth for the financial database. The app should remain useful when the model is unavailable, preserve every field's evidence and avoid transmitting images or financial document text to a server by default.

This design gives you the best combination of privacy, debuggability, low recurring inference cost and long-term flexibility.