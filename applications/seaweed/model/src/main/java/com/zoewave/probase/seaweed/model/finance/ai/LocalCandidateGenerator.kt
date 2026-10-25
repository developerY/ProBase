package com.zoewave.probase.seaweed.model.finance.ai

import com.zoewave.probase.seaweed.model.finance.common.PipelineGear
import com.zoewave.probase.seaweed.model.finance.common.PipelineGearLogger
import com.zoewave.probase.seaweed.model.finance.layout.ReconstructedLine
import com.zoewave.probase.seaweed.model.finance.layout.SpatialLayoutEngine
import com.zoewave.probase.seaweed.model.finance.validation.*
import com.zoewave.probase.seaweed.model.finance.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


data class LocalExtractionBundle(
    val detectedFamilies: List<DocumentFamily>,
    val primaryFamilyConfidence: Float,
    val detectedCurrencyCode: String,
    val textCandidates: List<TextCandidate>,
    val dateCandidates: List<DateCandidate>,
    val amountCandidates: List<AmountCandidate>,
    val tableRows: List<ReconstructedTableRow>
)

class LocalCandidateGenerator {

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
            .filter { line -> line.elements.firstOrNull()?.let { it.boxNormalizedOcr.y < 0.22f } == true }
            // Using first element's Y for simplicity here

        for (line in headerLines.take(4)) {
            val cleanText = line.elements.joinToString(" ") { it.text }.trim()
            if (cleanText.length >= 3 &&
                !streetAddressRegex.containsMatchIn(cleanText) &&
                !phoneRegex.containsMatchIn(cleanText) &&
                !cleanText.contains("RECEIPT", ignoreCase = true) &&
                !cleanText.contains("INVOICE", ignoreCase = true) &&
                !cleanText.contains("STATEMENT", ignoreCase = true) &&
                cleanText.any { it.isLetter() }
            ) {
                val boost = if (line == headerLines.first()) 0.96f else 0.89f
                // Assuming average confidence for elements
                textCandidates += TextCandidate(
                    candidateId = "TC_${textIdx++}",
                    role = SemanticRole.MERCHANT_NAME,
                    rawText = cleanText,
                    normalizedValue = cleanText.uppercase(),
                    confidence = 0.95f * boost,
                    evidenceIds = line.elements.map { it.id }
                )
            }
        }

        // 2. Scan all lines for Addresses, PII roles, Payment Status, Dates, and Amounts
        for (line in reconstructedLines) {
            val text = line.elements.joinToString(" ") { it.text }.trim()
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
                    evidenceIds = line.elements.map { it.id }
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
                    evidenceIds = line.elements.map { it.id }
                )
            }
            if (phoneRegex.containsMatchIn(text)) {
                textCandidates += TextCandidate(
                    candidateId = "TC_${textIdx++}",
                    role = SemanticRole.CUSTOMER_PHONE,
                    rawText = text,
                    normalizedValue = upper,
                    confidence = 0.95f,
                    evidenceIds = line.elements.map { it.id }
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
                    evidenceIds = line.elements.map { it.id }
                )
            }

            // Date extraction
            extractDatesFromLine(line, dateIdx).forEach { cand ->
                dateIdx++
                dateCandidates += cand
            }

            // Amount extraction via StrictMoneyParser
            val lineAmounts = extractAmountsFromLine(line, detectedCurrency, amountIdx, documentId)
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
                val descText = descElements.joinToString(" ") { it.text }.trim()
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

                    val rowId = String.format("P%02d_R%03d", 1, rowIdx)
                    tableRows += ReconstructedTableRow(
                        rowId = rowId,
                        pageNumber = 1,
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

        // --- Merchant Fallback ---
        if (textCandidates.none { it.role == SemanticRole.MERCHANT_NAME }) {
            val topLines = reconstructedLines.take(5)
            for (line in topLines) {
                val text = line.elements.joinToString(" ") { it.text }.trim()
                val upper = text.uppercase()
                if (text.length >= 2 &&
                    !streetAddressRegex.containsMatchIn(upper) &&
                    !phoneRegex.containsMatchIn(text) &&
                    !emailRegex.containsMatchIn(text) &&
                    text.any { it.isLetter() }
                ) {
                    textCandidates += TextCandidate(
                        candidateId = "TC_${textIdx++}",
                        role = SemanticRole.MERCHANT_NAME,
                        rawText = text,
                        normalizedValue = upper,
                        confidence = 0.95f,
                        evidenceIds = line.elements.map { it.id }
                    )
                    break
                }
            }
        }

        // --- Total Amount Fallback ---
        if (amountCandidates.none { it.role == SemanticRole.TOTAL_AMOUNT }) {
            val maxAmountCandidate = amountCandidates
                .filter { it.amount != null }
                .maxByOrNull { it.amount!!.amountMinor }

            if (maxAmountCandidate != null) {
                val promotedTotal = maxAmountCandidate.copy(
                    candidateId = "AC_PROMOTED_TOTAL_${maxAmountCandidate.candidateId}",
                    role = SemanticRole.TOTAL_AMOUNT,
                    confidence = 0.96f
                )
                amountCandidates += promotedTotal
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
        val fullText = lines.joinToString(" ") { it.elements.joinToString(" ") { e -> e.text } }
        for ((symbol, code) in currencySymbolMap) {
            if (fullText.contains(symbol, ignoreCase = true)) return code
        }
        return fallback
    }

    private fun classifyDocumentFamily(lines: List<ReconstructedLine>): Pair<List<DocumentFamily>, Float> {
        val fullUpper = lines.joinToString(" ") { it.elements.joinToString(" ") { e -> e.text }.uppercase() }
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
        val lineText = line.elements.joinToString(" ") { it.text }
        val upper = lineText.uppercase()
        val role = when {
            upper.contains("DUE") -> SemanticRole.DUE_DATE
            upper.contains("POST") -> SemanticRole.POSTING_DATE
            upper.contains("START") || upper.contains("FROM") -> SemanticRole.STATEMENT_PERIOD_START
            upper.contains("END") || upper.contains("THROUGH") -> SemanticRole.STATEMENT_PERIOD_END
            upper.contains("PRINTED") -> SemanticRole.PRINT_DATE
            else -> SemanticRole.TRANSACTION_DATE
        }

        var idx = startIndex
        isoDateRegex.findAll(lineText).forEach { match ->
            results += DateCandidate(
                candidateId = "DC_${idx++}",
                role = role,
                isoDate = match.value,
                confidence = 0.97f,
                evidenceIds = line.elements.map { it.id }
            )
        }

        if (results.isEmpty()) {
            slashDateRegex.findAll(lineText).forEach { match ->
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
                        evidenceIds = line.elements.map { it.id }
                    )
                }
            }
        }
        return results
    }

    private fun extractAmountsFromLine(
        line: ReconstructedLine,
        defaultCurrency: String,
        startIndex: Int,
        documentId: String
    ): List<AmountCandidate> {
        val results = mutableListOf<AmountCandidate>()
        val lineText = line.elements.joinToString(" ") { it.text }
        val upper = lineText.uppercase(Locale.US)

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
        amountTokenRegex.findAll(lineText).forEach { match ->
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
                .filter { el -> el.text.contains(numericPart.take(4)) || match.value.contains(el.text) }
                .map { it.id }
                .ifEmpty { line.elements.map { it.id } }

            when (val parsed = StrictMoneyParser.parse(numericPart, resolvedCurrency, docId = documentId)) {
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
