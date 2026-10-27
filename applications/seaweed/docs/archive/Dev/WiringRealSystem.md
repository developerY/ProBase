Copy and paste the following prompt directly into your AI coding agent to replace the V1 multimodal bitmap shortcut and simulated ViewModel logs with the real V2 8-Gear Deterministic Pipeline.

---

```markdown
# Task: Replace V1 Multimodal Shortcut & Simulated Logs with the Real V2 8-Gear Deterministic Pipeline

Yes — build and wire up the entire V2 pipeline for real right now. 

You must **completely remove** the V1 shortcut (sending raw `Bitmap` images to multimodal Gemini and simulating `PipelineGearLogger` logs in `AddTransactionViewModel`) and replace it with the real 8-Gear local-first execution path:
1. Upgraded `LocalOcrEngine.kt` (extracting real ML Kit bounding boxes, corner points, and rotation angles).
2. `LocalCandidateGenerator.kt` & `ArithmeticReconciler.kt` (extracting candidates via heuristics/Regex and parsing money exclusively through `StrictMoneyParser`).
3. `DocumentPipelineOrchestrator.kt` (executing Gears 1–7 in strict sequence, encrypting bitmaps via `TinkStreamingFileEncryptor`, trimming/redacting PII via `BundleTrimmer`, sending **text-only JSON** to the AI router when needed, and validating via `PostLlmSemanticValidator`).
4. Upgraded `AddTransactionViewModel.kt` & persistence wiring (persisting `FinancialDocumentEntity`, `DocumentPageEntity`, `OcrElementEntity`, `EvidenceManifestEntity` with `ManifestHmacGenerator` digests, and `FinancialEventEntity` via `FinancialDocumentDao`, while enforcing the Envelope `InterventionDialog` boundary).

Do not stub or simulate any step. Implement the four components below and wire them into the existing dependency injection graph.

---

## 1. Upgrade `LocalOcrEngine.kt` (Real ML Kit Bounding Boxes, Angles & Normalization)

Update `LocalOcrEngine.kt` so that instead of returning a flat `String`, it inspects ML Kit's `Text` hierarchy (`TextBlock` -> `Line` -> `Element`), extracts pixel `boundingBox`, `cornerPoints`, `angle` (rotation degrees), and `confidence`, normalizes coordinates against the bitmap dimensions $(W_o \times H_o)$, and projects each box through `CoordinateTransformMapper` (`GEAR_2` & `GEAR_3`):

```kotlin
package com.example.financeengine.core.ocr

import android.graphics.Bitmap
import android.graphics.Point
import kotlin.math.atan2
import com.example.financeengine.core.common.PipelineGear
import com.example.financeengine.core.common.PipelineGearLogger
import com.example.financeengine.core.model.ExtractionProvenance
import com.example.financeengine.core.model.OcrElement
import com.example.financeengine.core.vision.CoordinateTransformMapper
import com.example.financeengine.core.vision.NormalizedRect
import com.example.financeengine.core.vision.ProjectionResult
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class OcrExtractionResult(
    val pageNumber: Int,
    val imageWidthPx: Int,
    val imageHeightPx: Int,
    val elements: List<OcrElement>,
    val singularTransformCount: Int
)

@Singleton
class LocalOcrEngine @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractOcrElements(
        documentId: String,
        pageNumber: Int,
        cleanedOcrBitmap: Bitmap,
        homographyNorm: FloatArray // 9-element 3x3 normalized matrix (or identity I_3)
    ): OcrExtractionResult {
        val widthPx = cleanedOcrBitmap.width.coerceAtLeast(1)
        val heightPx = cleanedOcrBitmap.height.coerceAtLeast(1)

        val visionText: Text = suspendCancellableCoroutine { cont ->
            val inputImage = InputImage.fromBitmap(cleanedOcrBitmap, 0)
            recognizer.process(inputImage)
                .addOnSuccessListener { result ->
                    if (cont.isActive) cont.resume(result)
                }
                .addOnFailureListener { e ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
        }

        val extractedElements = mutableListOf<OcrElement>()
        var elementCounter = 1
        var lineCounter = 1
        var singularFailures = 0

        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                val lineId = String.format("P%02d_L%04d", pageNumber, lineCounter++)
                val lineAngleDeg = computeRotationDegrees(line.angle, line.cornerPoints)

                // Emit line-level or element-level tokens with full spatial geometry
                val lineElements = if (line.elements.isNotEmpty()) line.elements else emptyList()
                if (lineElements.isEmpty()) continue

                for (element in lineElements) {
                    val rect = element.boundingBox ?: continue
                    val rawText = element.text.trim()
                    if (rawText.isEmpty()) continue

                    val elementId = String.format("P%02d_T%04d", pageNumber, elementCounter++)
                    val normX = (rect.left.toFloat() / widthPx).coerceIn(0f, 1f)
                    val normY = (rect.top.toFloat() / heightPx).coerceIn(0f, 1f)
                    val normW = (rect.width().toFloat() / widthPx).coerceIn(0.001f, 1f - normX)
                    val normH = (rect.height().toFloat() / heightPx).coerceIn(0.001f, 1f - normY)

                    val normRect = NormalizedRect(
                        x = normX,
                        y = normY,
                        width = normW,
                        height = normH
                    )

                    val projectedQuad = when (
                        val proj = CoordinateTransformMapper.projectOcrRectToRawQuad(normRect, homographyNorm)
                    ) {
                        is ProjectionResult.Success -> proj.quad
                        is ProjectionResult.SingularOrInvalidTransform -> {
                            singularFailures++
                            null
                        }
                    }

                    val elemAngleDeg = computeRotationDegrees(element.angle, element.cornerPoints)
                        .takeIf { it != 0f } ?: lineAngleDeg

                    val region = when {
                        normY < 0.22f -> "HEADER"
                        normY > 0.82f -> "FOOTER"
                        else -> "BODY"
                    }

                    extractedElements += OcrElement(
                        id = elementId,
                        pageNumber = pageNumber,
                        rawText = rawText,
                        normalizedText = rawText.uppercase(),
                        provenance = ExtractionProvenance.MLKIT_OCR_V2,
                        boxNormalizedOcr = normRect,
                        boxNormalizedRawQuad = projectedQuad,
                        ocrConfidence = element.confidence.takeIf { it > 0f } ?: 0.90f,
                        rotationDegrees = elemAngleDeg,
                        lineId = lineId,
                        region = region
                    )
                }
            }
        }

        PipelineGearLogger.d(PipelineGear.GEAR_2_VISION_HOMOGRAPHY, documentId) {
            "Page $pageNumber homography projection completed: mapped=${extractedElements.size - singularFailures}, singularFailures=$singularFailures"
        }

        PipelineGearLogger.d(PipelineGear.GEAR_3_LAYOUT_OCR, documentId) {
            val firstId = extractedElements.firstOrNull()?.id ?: "NONE"
            val lastId = extractedElements.lastOrNull()?.id ?: "NONE"
            "ML Kit OCR extracted ${extractedElements.size} elements ($firstId..$lastId) from ${widthPx}x${heightPx} bitmap"
        }

        return OcrExtractionResult(
            pageNumber = pageNumber,
            imageWidthPx = widthPx,
            imageHeightPx = heightPx,
            elements = extractedElements,
            singularTransformCount = singularFailures
        )
    }

    private fun computeRotationDegrees(mlKitAngle: Float, corners: Array<Point>?): Float {
        if (mlKitAngle.isFinite() && mlKitAngle != 0f) return mlKitAngle
        if (corners != null && corners.size >= 2) {
            val dx = (corners[1].x - corners[0].x).toDouble()
            val dy = (corners[1].y - corners[0].y).toDouble()
            if (dx != 0.0 || dy != 0.0) {
                return Math.toDegrees(atan2(dy, dx)).toFloat()
            }
        }
        return 0f
    }
}
```

---

## 2. Build `LocalCandidateGenerator.kt` (Gear 4 Heuristics & `StrictMoneyParser` Wiring)

Create `LocalCandidateGenerator.kt` in `:core:finance:extract` (or your model/domain package) to loop over the reconstructed lines from `SpatialLayoutEngine`, detect document family candidates, extract dates, merchants, addresses, payment statuses, and line items, and feed every monetary token through `StrictMoneyParser.parse()` (`GEAR_4`):

```kotlin
package com.example.financeengine.core.finance

import com.example.financeengine.core.common.PipelineGear
import com.example.financeengine.core.common.PipelineGearLogger
import com.example.financeengine.core.layout.ReconstructedLine
import com.example.financeengine.core.layout.SpatialLayoutEngine
import com.example.financeengine.core.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class LocalExtractionBundle(
    val detectedFamilies: List<DocumentFamily>,
    val primaryFamilyConfidence: Float,
    val detectedCurrencyCode: String,
    val textCandidates: List<TextCandidate>,
    val dateCandidates: List<DateCandidate>,
    val amountCandidates: List<AmountCandidate>,
    val tableRows: List<ReconstructedTableRow>
)

@Singleton
class LocalCandidateGenerator @Inject constructor() {

    private val currencySymbolMap = mapOf(
        "$" to "USD", "USD" to "USD", "US$" to "USD",
        "€" to "EUR", "EUR" to "EUR",
        "£" to "GBP", "GBP" to "GBP",
        "¥" to "JPY", "JPY" to "JPY",
        "₩" to "KRW", "KRW" to "KRW",
        "KWD" to "KWD", "CAD" to "CAD", "AUD" to "AUD", "CHF" to "CHF"
    )

    // Matches amounts like $12.34, 1,234.56, 12.345, -45.00
    private val amountTokenRegex = Regex(
        """(?i)(?:(USD|EUR|GBP|JPY|KRW|KWD|CAD|AUD|CHF|US\$|\$|€|£|¥|₩)\s*)?(-?\d{1,3}(?:,\d{3})*(?:\.\d{1,4})?|-?\d+\.\d{1,4})(?:\s*(USD|EUR|GBP|JPY|KRW|KWD|CAD|AUD|CHF))?"""
    )

    // Matches YYYY-MM-DD, MM/DD/YYYY, DD/MM/YYYY, MM-DD-YY
    private val isoDateRegex = Regex("""\b(20\d{2})[-/](0[1-9]|1[0-2])[-/](0[1-9]|[12]\d|3[01])\b""")
    private val slashDateRegex = Regex("""\b(0?[1-9]|1[0-2])[/-](0?[1-9]|[12]\d|3[01])[/-](20\d{2}|\d{2})\b""")

    private val streetAddressRegex = Regex(
        """(?i)\b\d{1,6}\s+[A-Z0-9.\s]{2,30}\b(ST|STREET|AVE|AVENUE|BLVD|RD|ROAD|DR|DRIVE|LN|LANE|WAY|CT|PLAZA|PKWY|SUITE|STE)\b"""
    )
    private val emailRegex = Regex("""[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}""")
    private val phoneRegex = Regex("""(?<!\d)(?:\+?\d{1,3}[\s.-]?)?\(?\d{3}\)?[\s.-]\d{3}[\s.-]\d{4}(?!\d)""")

    fun generateCandidates(
        documentId: String,
        elements: List<OcrElement>,
        reconstructedLines: List<ReconstructedLine>,
        defaultCurrencyCode: String = "USD"
    ): LocalExtractionBundle {
        val detectedCurrency = detectDocumentCurrency(reconstructedLines, defaultCurrencyCode)
        val (families, familyConf) = classifyDocumentFamily(reconstructedLines)

        val textCandidates = mutableListOf<TextCandidate>()
        val dateCandidates = mutableListOf<DateCandidate>()
        val amountCandidates = mutableListOf<AmountCandidate>()
        val tableRows = mutableListOf<ReconstructedTableRow>()

        var textIdx = 1
        var dateIdx = 1
        var amountIdx = 1
        var rowIdx = 1

        // 1. Identify Merchant candidate from prominent top HEADER lines
        val headerLines = reconstructedLines
            .filter { it.region == "HEADER" }
            .sortedBy { it.centerY }

        for (line in headerLines.take(4)) {
            val cleanText = line.mergedText.trim()
            if (cleanText.length >= 3 &&
                !streetAddressRegex.containsMatchIn(cleanText) &&
                !phoneRegex.containsMatchIn(cleanText) &&
                !cleanText.contains("RECEIPT", ignoreCase = true) &&
                !cleanText.contains("INVOICE", ignoreCase = true) &&
                !cleanText.contains("STATEMENT", ignoreCase = true) &&
                cleanText.any { it.isLetter() }
            ) {
                val boost = if (line == headerLines.first()) 0.96f else 0.89f
                textCandidates += TextCandidate(
                    candidateId = "TC_${textIdx++}",
                    role = SemanticRole.MERCHANT_NAME,
                    rawText = cleanText,
                    normalizedValue = cleanText.uppercase(),
                    confidence = (line.averageConfidence * boost).coerceAtLeast(0.95f),
                    evidenceIds = line.elementIds
                )
            }
        }

        // 2. Scan all lines for Addresses, PII roles, Payment Status, Dates, and Amounts
        for (line in reconstructedLines) {
            val text = line.mergedText.trim()
            val upper = text.uppercase()

            // Address detection
            if (streetAddressRegex.containsMatchIn(upper)) {
                val isCustomer = upper.contains("BILL TO") || upper.contains("SHIP TO") || upper.contains("CUSTOMER")
                textCandidates += TextCandidate(
                    candidateId = "TC_${textIdx++}",
                    role = if (isCustomer) SemanticRole.CUSTOMER_ADDRESS else SemanticRole.ISSUER_ADDRESS,
                    rawText = text,
                    normalizedValue = upper,
                    confidence = 0.95f,
                    evidenceIds = line.elementIds
                )
            }

            // Email & Phone PII tagging so BundleTrimmer strips them
            if (emailRegex.containsMatchIn(text)) {
                textCandidates += TextCandidate(
                    candidateId = "TC_${textIdx++}",
                    role = SemanticRole.CUSTOMER_EMAIL,
                    rawText = text,
                    normalizedValue = upper,
                    confidence = 0.98f,
                    evidenceIds = line.elementIds
                )
            }
            if (phoneRegex.containsMatchIn(text)) {
                textCandidates += TextCandidate(
                    candidateId = "TC_${textIdx++}",
                    role = SemanticRole.CUSTOMER_PHONE,
                    rawText = text,
                    normalizedValue = upper,
                    confidence = 0.95f,
                    evidenceIds = line.elementIds
                )
            }

            // Payment Status Label detection
            if (upper.contains("PAID IN FULL") || upper.contains("STATUS: PAID") ||
                upper.contains("UNPAID") || upper.contains("PAST DUE") || upper.contains("OVERDUE")
            ) {
                textCandidates += TextCandidate(
                    candidateId = "TC_${textIdx++}",
                    role = SemanticRole.PAYMENT_STATUS_LABEL,
                    rawText = text,
                    normalizedValue = upper,
                    confidence = 0.96f,
                    evidenceIds = line.elementIds
                )
            }

            // Date extraction
            extractDatesFromLine(line, dateIdx).forEach { cand ->
                dateIdx++
                dateCandidates += cand
            }

            // Amount extraction via StrictMoneyParser
            val lineAmounts = extractAmountsFromLine(line, detectedCurrency, amountIdx)
            amountIdx += lineAmounts.size
            amountCandidates += lineAmounts

            // Reconstruct table rows for line items / statement transactions
            val lineItemAmount = lineAmounts.firstOrNull {
                it.role in setOf(
                    SemanticRole.LINE_ITEM_AMOUNT,
                    SemanticRole.DEBIT_AMOUNT,
                    SemanticRole.CREDIT_AMOUNT
                )
            }
            if (lineItemAmount != null && line.elements.size >= 2) {
                val amountElemIds = lineItemAmount.evidenceIds.toSet()
                val descElements = line.elements.filter { it.id !in amountElemIds }
                val descText = descElements.joinToString(" ") { it.rawText }.trim()
                if (descText.isNotEmpty() && descElements.isNotEmpty()) {
                    val descEvIds = descElements.map { it.id }
                    textCandidates += TextCandidate(
                        candidateId = "TC_${textIdx++}",
                        role = SemanticRole.LINE_ITEM_DESCRIPTION,
                        rawText = descText,
                        normalizedValue = descText.uppercase(),
                        confidence = 0.94f,
                        evidenceIds = descEvIds
                    )

                    val rowId = String.format("P%02d_R%03d", line.pageNumber, rowIdx)
                    tableRows += ReconstructedTableRow(
                        rowId = rowId,
                        pageNumber = line.pageNumber,
                        rowIndex = rowIdx++,
                        cells = listOf(
                            ReconstructedTableCell(
                                cellId = "${rowId}_C01",
                                columnIndex = 0,
                                headerLabel = "Description",
                                role = SemanticRole.LINE_ITEM_DESCRIPTION,
                                rawText = descText,
                                evidenceIds = descEvIds
                            ),
                            ReconstructedTableCell(
                                cellId = "${rowId}_C02",
                                columnIndex = 1,
                                headerLabel = "Amount",
                                role = lineItemAmount.role,
                                rawText = lineItemAmount.draftRawToken
                                    ?: lineItemAmount.amount?.toBigDecimal()?.toPlainString().orEmpty(),
                                parsedAmount = lineItemAmount.amount,
                                draftRawAmountToken = lineItemAmount.draftRawToken,
                                evidenceIds = lineItemAmount.evidenceIds
                            )
                        )
                    )
                }
            }
        }

        PipelineGearLogger.d(PipelineGear.GEAR_4_MONEY_EXTRACT, documentId) {
            val exactCount = amountCandidates.count { it.amount != null }
            val excessCount = amountCandidates.count { it.draftRawToken != null }
            "Generated candidates: family=${families.firstOrNull()} ($familyConf), currency=$detectedCurrency, " +
                "text=${textCandidates.size}, dates=${dateCandidates.size}, " +
                "amounts=$exactCount exact / $excessCount excessPrecisionFlagged, rows=${tableRows.size}"
        }

        return LocalExtractionBundle(
            detectedFamilies = families,
            primaryFamilyConfidence = familyConf,
            detectedCurrencyCode = detectedCurrency,
            textCandidates = textCandidates,
            dateCandidates = dateCandidates,
            amountCandidates = amountCandidates,
            tableRows = tableRows
        )
    }

    private fun detectDocumentCurrency(lines: List<ReconstructedLine>, fallback: String): String {
        val fullText = lines.joinToString(" ") { it.mergedText }
        for ((symbol, code) in currencySymbolMap) {
            if (fullText.contains(symbol, ignoreCase = true)) return code
        }
        return fallback
    }

    private fun classifyDocumentFamily(lines: List<ReconstructedLine>): Pair<List<DocumentFamily>, Float> {
        val fullUpper = lines.joinToString(" ") { it.mergedText.uppercase() }
        return when {
            fullUpper.contains("OPENING BALANCE") || fullUpper.contains("CLOSING BALANCE") -> {
                if (fullUpper.contains("MINIMUM PAYMENT") || fullUpper.contains("CREDIT LIMIT")) {
                    listOf(DocumentFamily.CREDIT_CARD_STATEMENT, DocumentFamily.BANK_STATEMENT) to 0.95f
                } else {
                    listOf(DocumentFamily.BANK_STATEMENT, DocumentFamily.CREDIT_CARD_STATEMENT) to 0.95f
                }
            }
            fullUpper.contains("AMOUNT DUE") || fullUpper.contains("DUE DATE") || fullUpper.contains("INVOICE") ->
                listOf(DocumentFamily.INVOICE_OR_UTILITY_BILL, DocumentFamily.RECEIPT) to 0.94f
            fullUpper.contains("REFUND") || fullUpper.contains("CREDIT MEMO") ->
                listOf(DocumentFamily.REFUND_OR_CREDIT_NOTE, DocumentFamily.RECEIPT) to 0.94f
            else ->
                listOf(DocumentFamily.RECEIPT, DocumentFamily.INVOICE_OR_UTILITY_BILL) to 0.95f
        }
    }

    private fun extractDatesFromLine(line: ReconstructedLine, startIndex: Int): List<DateCandidate> {
        val results = mutableListOf<DateCandidate>()
        val upper = line.mergedText.uppercase()
        val role = when {
            upper.contains("DUE") -> SemanticRole.DUE_DATE
            upper.contains("POST") -> SemanticRole.POSTING_DATE
            upper.contains("START") || upper.contains("FROM") -> SemanticRole.STATEMENT_PERIOD_START
            upper.contains("END") || upper.contains("THROUGH") -> SemanticRole.STATEMENT_PERIOD_END
            upper.contains("PRINTED") -> SemanticRole.PRINT_DATE
            else -> SemanticRole.TRANSACTION_DATE
        }

        var idx = startIndex
        isoDateRegex.findAll(line.mergedText).forEach { match ->
            results += DateCandidate(
                candidateId = "DC_${idx++}",
                role = role,
                isoDate = match.value,
                confidence = 0.97f,
                evidenceIds = line.elementIds
            )
        }

        if (results.isEmpty()) {
            slashDateRegex.findAll(line.mergedText).forEach { match ->
                val month = match.groupValues[1].padStart(2, '0')
                val day = match.groupValues[2].padStart(2, '0')
                val yearRaw = match.groupValues[3]
                val year = if (yearRaw.length == 2) "20$yearRaw" else yearRaw
                val iso = "$year-$month-$day"
                if (runCatching { LocalDate.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE) }.isSuccess) {
                    results += DateCandidate(
                        candidateId = "DC_${idx++}",
                        role = role,
                        isoDate = iso,
                        confidence = 0.96f,
                        evidenceIds = line.elementIds
                    )
                }
            }
        }
        return results
    }

    private fun extractAmountsFromLine(
        line: ReconstructedLine,
        defaultCurrency: String,
        startIndex: Int
    ): List<AmountCandidate> {
        val results = mutableListOf<AmountCandidate>()
        val upper = line.mergedText.uppercase(Locale.US)

        val role = when {
            upper.contains("SUBTOTAL") || upper.contains("SUB-TOTAL") || upper.contains("SUB TOTAL") ->
                SemanticRole.SUBTOTAL_AMOUNT
            upper.contains("TAX") || upper.contains("VAT") || upper.contains("GST") || upper.contains("HST") ->
                SemanticRole.TAX_AMOUNT
            upper.contains("TIP") || upper.contains("GRATUITY") ->
                SemanticRole.TIP_AMOUNT
            upper.contains("DISCOUNT") || upper.contains("COUPON") || upper.contains("SAVINGS") ->
                SemanticRole.DISCOUNT_AMOUNT
            upper.contains("MINIMUM PAYMENT") || upper.contains("MIN PAYMENT") ->
                SemanticRole.MINIMUM_PAYMENT_DUE
            upper.contains("AMOUNT DUE") || upper.contains("BALANCE DUE") ->
                SemanticRole.AMOUNT_DUE
            upper.contains("OPENING BALANCE") || upper.contains("PREVIOUS BALANCE") ->
                SemanticRole.OPENING_BALANCE
            upper.contains("CLOSING BALANCE") || upper.contains("NEW BALANCE") ->
                SemanticRole.CLOSING_BALANCE
            upper.contains("FEE") || upper.contains("SURCHARGE") || upper.contains("INTEREST") ->
                SemanticRole.SURCHARGE_OR_FEE_AMOUNT
            upper.contains("TOTAL") ->
                SemanticRole.TOTAL_AMOUNT
            else ->
                SemanticRole.LINE_ITEM_AMOUNT
        }

        var idx = startIndex
        amountTokenRegex.findAll(line.mergedText).forEach { match ->
            val prefixSymbol = match.groupValues[1]
            val numericPart = match.groupValues[2].replace(",", "")
            val suffixSymbol = match.groupValues[3]

            // Skip bare integers without a decimal point unless preceded/followed by a currency symbol
            if (!numericPart.contains(".") && prefixSymbol.isBlank() && suffixSymbol.isBlank()) {
                return@forEach
            }

            val resolvedCurrency = currencySymbolMap[prefixSymbol.uppercase()]
                ?: currencySymbolMap[suffixSymbol.uppercase()]
                ?: defaultCurrency

            // Find the exact OCR elements matching this numeric token
            val matchedElementIds = line.elements
                .filter { el -> el.rawText.contains(numericPart.take(4)) || match.value.contains(el.rawText) }
                .map { it.id }
                .ifEmpty { line.elementIds }

            when (val parsed = StrictMoneyParser.parse(numericPart, resolvedCurrency)) {
                is MoneyParseResult.Exact -> {
                    results += AmountCandidate(
                        candidateId = "AC_${idx++}",
                        role = role,
                        amount = parsed.amount,
                        confidence = if (role == SemanticRole.TOTAL_AMOUNT) 0.98f else 0.95f,
                        evidenceIds = matchedElementIds
                    )
                }
                is MoneyParseResult.ExcessPrecisionFlagged -> {
                    results += AmountCandidate(
                        candidateId = "AC_${idx++}",
                        role = role,
                        amount = null,
                        draftRawToken = parsed.rawToken,
                        draftParseIssue = "EXCESS_PRECISION_${parsed.observedScale}_VS_${parsed.expectedScale}",
                        confidence = 0.50f,
                        evidenceIds = matchedElementIds
                    )
                }
                else -> Unit
            }
        }
        return results
    }
}
```

---

## 3. Build `DocumentPipelineOrchestrator.kt` (Real 8-Gear Execution Engine)

Create `DocumentPipelineOrchestrator.kt` to execute Gears 1 through 7 in strict sequence.
* **CRITICAL RULE:** Do **not** pass `Bitmap` objects to Firebase AI / Gemini. The AI Router must only receive the PII-redacted, byte-bounded `LlmAnalysisBundle` JSON chunks produced by `BundleTrimmer.prepareChunks()` (`GEAR_6`), and only when `DeterministicConfidenceGate` (`GEAR_5`) sets `fullDocumentResolvedWithoutAi == false`.

```kotlin
package com.example.financeengine.core.pipeline

import android.graphics.Bitmap
import com.example.financeengine.core.ai.AiInterpretationRouter
import com.example.financeengine.core.ai.RouterInterpretationOutcome
import com.example.financeengine.core.common.PipelineGear
import com.example.financeengine.core.common.PipelineGearLogger
import com.example.financeengine.core.crypto.TinkStreamingFileEncryptor
import com.example.financeengine.core.finance.ArithmeticReconciler
import com.example.financeengine.core.finance.DeterministicConfidenceGate
import com.example.financeengine.core.finance.LocalCandidateGenerator
import com.example.financeengine.core.layout.SpatialLayoutEngine
import com.example.financeengine.core.model.*
import com.example.financeengine.core.ocr.LocalOcrEngine
import com.example.financeengine.core.validation.BundleTrimResult
import com.example.financeengine.core.validation.BundleTrimmer
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class PipelineExecutionOutput(
    val documentId: String,
    val sourceSha256: String,
    val encryptedRawImagePath: String,
    val encryptedCleanImagePath: String,
    val homographyNorm: FloatArray,
    val dominantHeaderHex: String,
    val dominantBackgroundHex: String,
    val ocrElements: List<OcrElement>,
    val finalInterpretation: DocumentInterpretation,
    val bypassedLlm: Boolean,
    val providerUsed: String
)

@Singleton
class DocumentPipelineOrchestrator @Inject constructor(
    private val fileEncryptor: TinkStreamingFileEncryptor,
    private val localOcrEngine: LocalOcrEngine,
    private val spatialLayoutEngine: SpatialLayoutEngine,
    private val candidateGenerator: LocalCandidateGenerator,
    private val arithmeticReconciler: ArithmeticReconciler,
    private val aiRouter: AiInterpretationRouter
) {

    suspend fun processCapturedBitmap(
        rawBitmap: Bitmap,
        cleanedBitmap: Bitmap = rawBitmap,
        homographyNorm: FloatArray = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f),
        allowedCategoryIds: List<String> = listOf("groceries", "coffee", "lunch", "dining", "utilities", "shopping", "uncategorized"),
        matchedCategoryRuleId: String? = null
    ): PipelineExecutionOutput {
        val documentId = "doc_${UUID.randomUUID().toString().take(8)}"
        val pageNumber = 1

        // =====================================================================
        // GEAR 1: CAPTURE & TINK STREAMING AEAD ENCRYPTION TO DISK
        // =====================================================================
        val rawPngBytes = bitmapToWebpOrPngBytes(rawBitmap)
        val cleanPngBytes = bitmapToWebpOrPngBytes(cleanedBitmap)
        val sourceSha256 = sha256Hex(rawPngBytes)

        val rawEncFile = fileEncryptor.encryptToDisk(
            documentId = documentId,
            pageNumber = pageNumber,
            variant = "raw",
            plaintextBytes = rawPngBytes
        )
        val cleanEncFile = fileEncryptor.encryptToDisk(
            documentId = documentId,
            pageNumber = pageNumber,
            variant = "clean",
            plaintextBytes = cleanPngBytes
        )

        PipelineGearLogger.d(PipelineGear.GEAR_1_CAPTURE_CRYPTO, documentId) {
            "Encrypted raw (${rawEncFile.length()} B) & clean (${cleanEncFile.length()} B) bitmaps " +
                "via Tink StreamingAead with AD='$documentId:$pageNumber:raw|clean'"
        }

        // =====================================================================
        // GEAR 2 & GEAR 3: VISION HOMOGRAPHY, ML KIT OCR & SPATIAL LAYOUT
        // =====================================================================
        val dominantHeaderHex = sampleDominantColorHex(rawBitmap, 0.0f, 0.20f)
        val dominantBgHex = sampleDominantColorHex(rawBitmap, 0.20f, 0.85f)

        val ocrResult = localOcrEngine.extractOcrElements(
            documentId = documentId,
            pageNumber = pageNumber,
            cleanedOcrBitmap = cleanedBitmap,
            homographyNorm = homographyNorm
        )

        val reconstructedLines = spatialLayoutEngine.groupElementsIntoLines(
            documentId = documentId,
            elements = ocrResult.elements
        )

        // =====================================================================
        // GEAR 4: LOCAL CANDIDATE GENERATION & STRICT MONEY PARSING
        // =====================================================================
        val extraction = candidateGenerator.generateCandidates(
            documentId = documentId,
            elements = ocrResult.elements,
            reconstructedLines = reconstructedLines
        )

        // =====================================================================
        // GEAR 5: SIGNED-LEDGER ARITHMETIC RECONCILIATION & DETERMINISTIC GATE
        // =====================================================================
        val arithmeticSummary = arithmeticReconciler.reconcile(
            documentId = documentId,
            family = extraction.detectedFamilies.firstOrNull() ?: DocumentFamily.RECEIPT,
            amountCandidates = extraction.amountCandidates,
            tableRows = extraction.tableRows
        )

        val gateDecision = DeterministicConfidenceGate.evaluate(
            familyCandidate = extraction.detectedFamilies.firstOrNull() ?: DocumentFamily.UNKNOWN,
            familyConfidence = extraction.primaryFamilyConfidence,
            merchantCandidate = extraction.textCandidates
                .filter { it.role == SemanticRole.MERCHANT_NAME }
                .maxByOrNull { it.confidence },
            addressCandidate = extraction.textCandidates
                .filter { it.role == SemanticRole.ISSUER_ADDRESS }
                .maxByOrNull { it.confidence },
            dateCandidates = extraction.dateCandidates,
            amountCandidates = extraction.amountCandidates,
            arithmetic = arithmeticSummary,
            matchedCategoryRuleId = matchedCategoryRuleId
        )

        PipelineGearLogger.d(PipelineGear.GEAR_5_LEDGER_GATE, documentId) {
            "Reconciled=${arithmeticSummary.receiptTotalReconciled} (discrepancy=${arithmeticSummary.discrepancyMinorUnits} minor). " +
                "LockedFields=[total=${gateDecision.lockedFields.lockedDocumentTotal?.amount?.amountMinor}, " +
                "date=${gateDecision.lockedFields.lockedPrimaryDate?.isoDate}, " +
                "merchant=${gateDecision.lockedFields.lockedMerchant?.value}]. " +
                "fullBypass=${gateDecision.fullDocumentResolvedWithoutAi}"
        }

        if (gateDecision.fullDocumentResolvedWithoutAi && gateDecision.resolvedInterpretation != null) {
            PipelineGearLogger.d(PipelineGear.GEAR_5_LEDGER_GATE, documentId) {
                "100% deterministic resolution achieved! Skipping Gear 6 & Gear 7 LLM call."
            }
            return PipelineExecutionOutput(
                documentId = documentId,
                sourceSha256 = sourceSha256,
                encryptedRawImagePath = rawEncFile.absolutePath,
                encryptedCleanImagePath = cleanEncFile.absolutePath,
                homographyNorm = homographyNorm,
                dominantHeaderHex = dominantHeaderHex,
                dominantBackgroundHex = dominantBgHex,
                ocrElements = ocrResult.elements,
                finalInterpretation = gateDecision.resolvedInterpretation,
                bypassedLlm = true,
                providerUsed = "DETERMINISTIC_LOCAL_GATE"
            )
        }

        // =====================================================================
        // GEAR 6: PII REDACTION & 12 KB BYTE-BUDGET CHUNK TRIMMING
        // =====================================================================
        val rawBundle = LlmAnalysisBundle(
            documentId = documentId,
            chunkIndex = 0,
            totalChunks = 1,
            detectedFamilyCandidates = extraction.detectedFamilies,
            lockedDeterministicFields = gateDecision.lockedFields,
            textCandidates = extraction.textCandidates,
            dateCandidates = extraction.dateCandidates,
            amountCandidates = extraction.amountCandidates,
            tableRows = extraction.tableRows,
            arithmeticValidation = arithmeticSummary,
            allowedCategoryIds = allowedCategoryIds
        )

        val fallbackPartial = buildDeterministicPartialFallback(gateDecision.lockedFields, extraction)
        val validOcrIds = ocrResult.elements.map { it.id }.toSet()

        val trimResult = BundleTrimmer.prepareChunks(rawBundle)
        val chunks = when (trimResult) {
            is BundleTrimResult.Success -> {
                PipelineGearLogger.d(PipelineGear.GEAR_6_REDACT_TRIM, documentId) {
                    val sizes = trimResult.chunks.map { BundleTrimmer.measureUtf8Bytes(it) }
                    "PII scrubbed & trimmed into ${trimResult.chunks.size} text-only JSON chunk(s); byteSizes=$sizes (cap=${BundleTrimmer.MAX_UTF8_BYTES})"
                }
                trimResult.chunks
            }
            is BundleTrimResult.BundleCannotFit -> {
                PipelineGearLogger.d(PipelineGear.GEAR_6_REDACT_TRIM, documentId) {
                    "BundleCannotFit (${trimResult.reason}, row=${trimResult.offendingRowId}) -> routing directly to manual review"
                }
                return PipelineExecutionOutput(
                    documentId = documentId,
                    sourceSha256 = sourceSha256,
                    encryptedRawImagePath = rawEncFile.absolutePath,
                    encryptedCleanImagePath = cleanEncFile.absolutePath,
                    homographyNorm = homographyNorm,
                    dominantHeaderHex = dominantHeaderHex,
                    dominantBackgroundHex = dominantBgHex,
                    ocrElements = ocrResult.elements,
                    finalInterpretation = fallbackPartial,
                    bypassedLlm = true,
                    providerUsed = "BUNDLE_OVERFLOW_FALLBACK"
                )
            }
        }

        // =====================================================================
        // GEAR 7: TEXT-ONLY AI ROUTING, CANDIDATE-BOUND VALIDATION & MERGING
        // =====================================================================
        val routerOutcome = aiRouter.interpretDocumentChunks(
            chunks = chunks,
            validOcrIds = validOcrIds,
            fallbackPartialInterpretation = fallbackPartial
        )

        val (finalInterpretation, providerUsed) = when (routerOutcome) {
            is RouterInterpretationOutcome.ValidatedSuccess -> {
                PipelineGearLogger.d(PipelineGear.GEAR_7_AI_VALIDATOR, documentId) {
                    "AI interpretation validated via ${routerOutcome.providerUsed} (${routerOutcome.modelIdUsed}) " +
                        "in ${routerOutcome.totalLatencyMs}ms; events=${routerOutcome.interpretation.financialEvents.size}, " +
                        "reviewRequired=${routerOutcome.interpretation.reviewRequired}"
                }
                routerOutcome.interpretation to "${routerOutcome.providerUsed}:${routerOutcome.modelIdUsed}"
            }
            is RouterInterpretationOutcome.DeterministicFallbackOnly -> {
                PipelineGearLogger.d(PipelineGear.GEAR_7_AI_VALIDATOR, documentId) {
                    "AI fallback triggered (${routerOutcome.fallbackReason}); preserving deterministic locked fields for review"
                }
                routerOutcome.partialInterpretation to "DETERMINISTIC_FALLBACK"
            }
        }

        return PipelineExecutionOutput(
            documentId = documentId,
            sourceSha256 = sourceSha256,
            encryptedRawImagePath = rawEncFile.absolutePath,
            encryptedCleanImagePath = cleanEncFile.absolutePath,
            homographyNorm = homographyNorm,
            dominantHeaderHex = dominantHeaderHex,
            dominantBackgroundHex = dominantBgHex,
            ocrElements = ocrResult.elements,
            finalInterpretation = finalInterpretation,
            bypassedLlm = false,
            providerUsed = providerUsed
        )
    }

    private fun buildDeterministicPartialFallback(
        locked: LockedDeterministicFields,
        extraction: LocalExtractionBundle
    ): DocumentInterpretation {
        val draftEvents = extraction.amountCandidates
            .filter { it.role == SemanticRole.TOTAL_AMOUNT }
            .take(1)
            .map { totalCand ->
                val merchantText = locked.lockedMerchant?.value
                    ?: extraction.textCandidates.firstOrNull { it.role == SemanticRole.MERCHANT_NAME }?.normalizedValue
                    ?: "UNKNOWN MERCHANT"
                val merchantEv = locked.lockedMerchant?.evidenceIds
                    ?: extraction.textCandidates.firstOrNull { it.role == SemanticRole.MERCHANT_NAME }?.evidenceIds
                    ?: totalCand.evidenceIds

                ResolvedFinancialEvent(
                    eventKind = FinancialEventKind.PURCHASE_EXPENSE,
                    description = merchantText,
                    descriptionEvidenceIds = merchantEv,
                    amount = totalCand.amount,
                    draftRawAmountToken = totalCand.draftRawToken,
                    draftParseIssue = totalCand.draftParseIssue,
                    amountEvidenceIds = totalCand.evidenceIds,
                    isoDate = locked.lockedPrimaryDate?.isoDate,
                    dateEvidenceIds = locked.lockedPrimaryDate?.evidenceIds ?: emptyList(),
                    categoryId = "uncategorized",
                    persistenceState = EventPersistenceState.NEEDS_USER_REVIEW_DRAFT
                )
            }

        return DocumentInterpretation(
            documentFamily = locked.lockedDocumentFamily
                ?: extraction.detectedFamilies.firstOrNull()
                ?: DocumentFamily.UNKNOWN,
            merchantOrIssuer = locked.lockedMerchant,
            issuerAddress = locked.lockedIssuerAddress,
            primaryDate = locked.lockedPrimaryDate,
            dueDate = locked.lockedDueDate,
            documentTotal = locked.lockedDocumentTotal,
            taxTotal = locked.lockedTaxTotal,
            statementSummary = null,
            financialEvents = draftEvents,
            reviewRequired = true,
            unresolvedFields = listOf("ai_fallback_manual_review")
        )
    }

    private fun bitmapToWebpOrPngBytes(bitmap: Bitmap): ByteArray {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 90, out)
        return out.toByteArray()
    }

    private fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }

    private fun sampleDominantColorHex(bitmap: Bitmap, startYRatio: Float, endYRatio: Float): String {
        val startY = (bitmap.height * startYRatio).toInt().coerceIn(0, bitmap.height - 1)
        val endY = (bitmap.height * endYRatio).toInt().coerceIn(startY + 1, bitmap.height)
        val centerX = bitmap.width / 2
        val sampleY = (startY + endY) / 2
        val pixel = bitmap.getPixel(centerX.coerceIn(0, bitmap.width - 1), sampleY)
        val r = (pixel shr 16) and 0xFF
        val g = (pixel shr 8) and 0xFF
        val b = pixel and 0xFF
        return String.format("#%02X%02X%02X", r, g, b)
    }
}
```

---

## 4. Wire `AddTransactionViewModel.kt` to `DocumentPipelineOrchestrator`, `FinancialDocumentDao` & Envelope Intervention (Gear 8)

Update `AddTransactionViewModel.kt` so that:
1. All simulated `PipelineGearLogger` logs and direct V1 multimodal image calls are deleted.
2. Scanning/importing a receipt calls `pipelineOrchestrator.processCapturedBitmap(bitmap)`.
3. Before saving a `PURCHASE_EXPENSE` or `BILL_PAYMENT_SETTLEMENT` to the database, it checks the **Envelope** boundary (`EnvelopeInterventionGate`). If the Envelope is exceeded, it emits `UiState.InterventionRequired` so the user must explicitly approve or cancel in `InterventionDialog`.
4. Saving executes a single Room `@Transaction` in `FinancialDocumentDao` that persists `FinancialDocumentEntity`, `DocumentPageEntity`, all `OcrElementEntity` rows, `StatementSummaryEntity`, `EvidenceManifestEntity` rows (signed via `ManifestHmacGenerator`), and `FinancialEventEntity` (`GEAR_8`).

```kotlin
package com.example.financeengine.feature.addtransaction

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeengine.core.common.PipelineGear
import com.example.financeengine.core.common.PipelineGearLogger
import com.example.financeengine.core.crypto.ManifestHmacGenerator
import com.example.financeengine.core.database.*
import com.example.financeengine.core.model.*
import com.example.financeengine.core.pipeline.DocumentPipelineOrchestrator
import com.example.financeengine.core.pipeline.PipelineExecutionOutput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

sealed class AddTransactionUiState {
    data object Idle : AddTransactionUiState()
    data object ProcessingPipeline : AddTransactionUiState()
    data class ReadyForReview(val output: PipelineExecutionOutput) : AddTransactionUiState()
    data class EnvelopeInterventionRequired(
        val output: PipelineExecutionOutput,
        val envelopeName: String,
        val currentSpentMinor: Long,
        val newAmountMinor: Long,
        val limitMinor: Long,
        val overageMinor: Long
    ) : AddTransactionUiState()
    data class CommittedSuccess(val documentId: String, val savedEventCount: Int) : AddTransactionUiState()
    data class Error(val message: String) : AddTransactionUiState()
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val orchestrator: DocumentPipelineOrchestrator,
    private val documentDao: FinancialDocumentDao,
    private val hmacGenerator: ManifestHmacGenerator,
    private val envelopeRepository: EnvelopeRepository // Checks Category -> Envelope spending limit
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Idle)
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val json = Json { encodeDefaults = true }

    /**
     * Entry point when a user captures or selects a receipt/bill bitmap.
     * Runs the real 8-Gear local-first pipeline instead of the V1 multimodal shortcut.
     */
    fun onDocumentBitmapCaptured(rawBitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = AddTransactionUiState.ProcessingPipeline
            runCatching {
                orchestrator.processCapturedBitmap(rawBitmap = rawBitmap)
            }.onSuccess { output ->
                _uiState.value = AddTransactionUiState.ReadyForReview(output)
            }.onFailure { err ->
                if (err is kotlinx.coroutines.CancellationException) throw err
                _uiState.value = AddTransactionUiState.Error(err.message ?: "Pipeline processing failed")
            }
        }
    }

    /**
     * Evaluates the Envelope boundary (Decision-Time Control) first, then commits all
     * relational entities and HMAC provenance manifests to SQLCipher Room DB (Gear 8).
     */
    fun onCommitReviewedDocument(
        output: PipelineExecutionOutput,
        userApprovedEnvelopeOverride: Boolean = false
    ) {
        viewModelScope.launch {
            val primaryExpense = output.finalInterpretation.financialEvents.firstOrNull {
                it.eventKind == FinancialEventKind.PURCHASE_EXPENSE && it.amount != null
            }

            if (primaryExpense != null && !userApprovedEnvelopeOverride) {
                val envelopeCheck = envelopeRepository.evaluateEnvelopeImpact(
                    categoryId = primaryExpense.categoryId,
                    additionalAmountMinor = primaryExpense.amount!!.amountMinor
                )
                if (envelopeCheck.exceedsBoundary) {
                    PipelineGearLogger.d(PipelineGear.GEAR_8_DB_ENVELOPE, output.documentId) {
                        "Category '${primaryExpense.categoryId}' exceeds Envelope '${envelopeCheck.envelopeName}' " +
                            "by ${envelopeCheck.overageMinor} minor units -> Triggering InterventionDialog!"
                    }
                    _uiState.value = AddTransactionUiState.EnvelopeInterventionRequired(
                        output = output,
                        envelopeName = envelopeCheck.envelopeName,
                        currentSpentMinor = envelopeCheck.currentSpentMinor,
                        newAmountMinor = primaryExpense.amount.amountMinor,
                        limitMinor = envelopeCheck.limitMinor,
                        overageMinor = envelopeCheck.overageMinor
                    )
                    return@launch
                }
            }

            persistFullDocumentGraphToDatabase(output)
        }
    }

    private suspend fun persistFullDocumentGraphToDatabase(output: PipelineExecutionOutput) {
        val docId = output.documentId
        val interp = output.finalInterpretation
        val pageId = "${docId}_P01"

        val docEntity = FinancialDocumentEntity(
            documentId = docId,
            documentFamily = interp.documentFamily.name,
            sourceSha256 = output.sourceSha256,
            captureTimestampEpochMs = System.currentTimeMillis(),
            rawEvidencePurged = false,
            processingStatus = if (interp.reviewRequired) "NEEDS_REVIEW" else "VERIFIED"
        )

        val pageEntity = DocumentPageEntity(
            pageId = pageId,
            documentId = docId,
            pageNumber = 1,
            encryptedRawImagePath = output.encryptedRawImagePath,
            encryptedCleanImagePath = output.encryptedCleanImagePath,
            homographyNormJson = json.encodeToString(output.homographyNorm.toList()),
            dominantHeaderHex = output.dominantHeaderHex,
            dominantBackgroundHex = output.dominantBackgroundHex
        )

        val ocrEntities = output.ocrElements.map { el ->
            OcrElementEntity(
                elementId = el.id,
                documentId = docId,
                pageId = pageId,
                pageNumber = el.pageNumber,
                rawText = el.rawText,
                normalizedText = el.normalizedText,
                provenance = el.provenance.name,
                boxNormalizedOcrJson = json.encodeToString(el.boxNormalizedOcr),
                boxNormalizedRawQuadJson = el.boxNormalizedRawQuad?.let { json.encodeToString(it) },
                ocrConfidence = el.ocrConfidence,
                rotationDegrees = el.rotationDegrees,
                lineId = el.lineId,
                region = el.region
            )
        }

        // Generate keyed HMAC-SHA256 EvidenceManifestEntity rows for every cited evidence ID
        val ocrById = output.ocrElements.associateBy { it.id }
        val manifestEntities = mutableListOf<EvidenceManifestEntity>()

        fun recordManifests(evidenceIds: List<String>, role: SemanticRole, normalizedVal: String) {
            for (evId in evidenceIds) {
                val ocrEl = ocrById[evId]
                val hmacHex = hmacGenerator.computeHmacHex(
                    documentId = docId,
                    evidenceId = evId,
                    semanticRole = role.name,
                    normalizedValue = normalizedVal
                )
                manifestEntities += EvidenceManifestEntity(
                    evidenceId = evId,
                    documentId = docId,
                    pageNumber = ocrEl?.pageNumber ?: 1,
                    semanticRole = role.name,
                    normalizedValueHmac = hmacHex,
                    hmacKeyVersion = hmacGenerator.currentKeyVersion,
                    rawQuadNormalizedJson = ocrEl?.boxNormalizedRawQuad?.let { json.encodeToString(it) },
                    verifiedByUser = !interp.reviewRequired
                )
            }
        }

        interp.merchantOrIssuer?.let { recordManifests(it.evidenceIds, it.role, it.value) }
        interp.primaryDate?.let { recordManifests(it.evidenceIds, it.role, it.isoDate) }
        interp.documentTotal?.let {
            recordManifests(it.evidenceIds, it.role, "${it.amount.amountMinor}:${it.amount.currencyCode}")
        }

        val eventEntities = interp.financialEvents.map { ev ->
            val amountVal = ev.amount?.let { "${it.amountMinor}:${it.currencyCode}" } ?: ev.draftRawAmountToken.orEmpty()
            recordManifests(ev.amountEvidenceIds, SemanticRole.TOTAL_AMOUNT, amountVal)

            val merchantNorm = interp.merchantOrIssuer?.value ?: ev.description.uppercase()
            val currencyCode = ev.amount?.currencyCode
                ?: interp.documentTotal?.amount?.currencyCode
                ?: "USD"
            val currencyScale = ev.amount?.currencyScale
                ?: interp.documentTotal?.amount?.currencyScale
                ?: 2

            val dedupInput = "$docId|${ev.sourceRowId}|${ev.eventKind}|${ev.amount?.amountMinor}|${ev.isoDate}|$merchantNorm"
            val dedupHash = hmacGenerator.computeHmacHex(docId, ev.sourceRowId ?: "ROW0", ev.eventKind.name, dedupInput)

            FinancialEventEntity(
                eventId = "ev_${UUID.randomUUID().toString().take(12)}",
                documentId = docId,
                eventKind = ev.eventKind.name,
                persistenceState = ev.persistenceState.name,
                eventDateIso = ev.isoDate,
                dueDateIso = ev.dueDateIso,
                billPaymentStatus = ev.billPaymentStatus.name,
                amountMinor = ev.amount?.amountMinor,
                draftRawAmountToken = ev.draftRawAmountToken,
                draftParseIssue = ev.draftParseIssue,
                currencyCode = currencyCode,
                currencyScale = currencyScale,
                merchantNormalized = merchantNorm,
                description = ev.description,
                categoryId = ev.categoryId,
                accountIdentifierMasked = null,
                sourceRowId = ev.sourceRowId,
                evidenceIdsCsv = (ev.amountEvidenceIds + ev.descriptionEvidenceIds + ev.dateEvidenceIds).distinct().joinToString(","),
                dedupFingerprint = dedupHash,
                verifiedByUser = ev.persistenceState == EventPersistenceState.VERIFIED_FINAL
            )
        }

        documentDao.insertFullDocumentGraph(
            document = docEntity,
            page = pageEntity,
            ocrElements = ocrEntities,
            manifests = manifestEntities.distinctBy { it.evidenceId },
            events = eventEntities
        )

        PipelineGearLogger.d(PipelineGear.GEAR_8_DB_ENVELOPE, docId) {
            "Persisted full graph to SQLCipher DB: ocrElements=${ocrEntities.size}, " +
                "hmacManifests=${manifestEntities.size}, financialEvents=${eventEntities.size} (provider=${output.providerUsed})"
        }

        _uiState.value = AddTransactionUiState.CommittedSuccess(
            documentId = docId,
            savedEventCount = eventEntities.size
        )
    }
}
```

---

## 5. Add `@Transaction insertFullDocumentGraph` to `FinancialDocumentDao.kt`

Ensure `FinancialDocumentDao.kt` includes the atomic `@Insert` transaction used by `AddTransactionViewModel`:

```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertDocument(document: FinancialDocumentEntity)

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertPage(page: DocumentPageEntity)

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertOcrElements(elements: List<OcrElementEntity>)

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertEvidenceManifests(manifests: List<EvidenceManifestEntity>)

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertFinancialEvents(events: List<FinancialEventEntity>)

@Transaction
suspend fun insertFullDocumentGraph(
    document: FinancialDocumentEntity,
    page: DocumentPageEntity,
    ocrElements: List<OcrElementEntity>,
    manifests: List<EvidenceManifestEntity>,
    events: List<FinancialEventEntity>
) {
    insertDocument(document)
    insertPage(page)
    insertOcrElements(ocrElements)
    insertEvidenceManifests(manifests)
    insertFinancialEvents(events)
}
```

---

## 6. Verification Requirements Before Completing Task

1. Search the codebase for any remaining multimodal `content { image(bitmap) }` calls in the receipt/document flow and remove them so the LLM only ever receives serialized `LlmAnalysisBundle` text chunks.
2. Run `./gradlew testDebugUnitTest` and verify all unit and E2E tests compile and pass.
3. Capture or import a test receipt in the app and verify via `adb logcat -s SeaWeedEngine:D` that Gears 1 through 8 log real ML Kit element counts (`P01_T0001..`), real `StrictMoneyParser` minor-unit amounts, real `BundleTrimmer` byte sizes, and real `FinancialDocumentDao` SQLCipher row counts.

```

---

### How to Confirm It Is 100% Real (Not Simulated) After the Agent Runs

Once your Coding Agent applies this prompt, run a single receipt scan with `adb logcat -s SeaWeedEngine:D` open and check these three indicators:

1. **Dynamic OCR IDs (`GEAR-3`):** The element count (`P01_T0001..P01_T00XX`) and pixel dimensions (`widthPx x heightPx`) will change dynamically with every photo you take rather than printing static mock numbers.


2. **Airplane Mode Test (`GEAR-5`):** Put your phone in **Airplane Mode** and scan a clean receipt from a known merchant where `LineItems + Tax == Total`. Because Gears 1–5 execute 100% locally via ML Kit + `StrictMoneyParser` + `DeterministicConfidenceGate`, the receipt will parse, reconcile, highlight its 4-corner bounding boxes (`NormalizedQuad`), and save to SQLCipher with **zero network connection**.


3. **Database Inspector Check (`GEAR-8`):** Open Android Studio's **App Inspection -> Database Inspector** while running debug mode and inspect `financial_documents`, `ocr_elements`, `evidence_manifests`, and `financial_events` — you will see every individual OCR bounding box and HMAC-SHA256 digest populated in real time.