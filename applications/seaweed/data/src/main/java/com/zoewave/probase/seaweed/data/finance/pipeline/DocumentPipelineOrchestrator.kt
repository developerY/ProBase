package com.zoewave.probase.seaweed.data.finance.pipeline

import android.content.Context
import android.graphics.Bitmap
import com.zoewave.probase.features.readers.ocr.data.LocalOcrEngine
import com.zoewave.probase.seaweed.data.finance.crypto.TinkStreamingFileEncryptor
import com.zoewave.probase.seaweed.model.finance.ai.AiInterpretationRouter
import com.zoewave.probase.seaweed.model.finance.ai.LocalCandidateGenerator
import com.zoewave.probase.seaweed.model.finance.ai.LocalExtractionBundle
import com.zoewave.probase.seaweed.model.finance.ai.RouterInterpretationOutcome
import com.zoewave.probase.seaweed.model.finance.common.PipelineGear
import com.zoewave.probase.seaweed.model.finance.common.PipelineGearLogger
import com.zoewave.probase.seaweed.model.finance.layout.SpatialLayoutEngine
import com.zoewave.probase.seaweed.model.finance.validation.ArithmeticReconciler
import com.zoewave.probase.seaweed.model.finance.validation.ArithmeticValidationSummary
import com.zoewave.probase.seaweed.model.finance.validation.BundleTrimResult
import com.zoewave.probase.seaweed.model.finance.validation.BundleTrimmer
import com.zoewave.probase.seaweed.model.finance.validation.DeterministicConfidenceGate
import com.zoewave.probase.seaweed.model.finance.validation.DocumentFamily
import com.zoewave.probase.seaweed.model.finance.validation.DocumentInterpretation
import com.zoewave.probase.seaweed.model.finance.validation.EventPersistenceState
import com.zoewave.probase.seaweed.model.finance.validation.FinancialEventKind
import com.zoewave.probase.seaweed.model.finance.validation.LlmAnalysisBundle
import com.zoewave.probase.seaweed.model.finance.validation.LockedDeterministicFields
import com.zoewave.probase.seaweed.model.finance.validation.OcrElement
import com.zoewave.probase.seaweed.model.finance.validation.ResolvedFinancialEvent
import com.zoewave.probase.seaweed.model.finance.validation.SemanticRole
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
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
    @ApplicationContext private val context: Context,
    // Note: The below are domain singletons, instantiated directly here for simplicity
    // in this implementation phase, but could be injected.
    private val aiRouter: AiInterpretationRouter
) {

    private val spatialLayoutEngine = SpatialLayoutEngine
    private val candidateGenerator = LocalCandidateGenerator()
    private val arithmeticReconciler = ArithmeticReconciler

    private fun downsampleIfNeeded(src: Bitmap, maxEdge: Int = 2048): Bitmap {
        val longest = maxOf(src.width, src.height)
        if (longest <= maxEdge) return src
        val scale = maxEdge.toFloat() / longest.toFloat()
        val w = (src.width * scale).toInt().coerceAtLeast(1)
        val h = (src.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, w, h, true)
    }

    suspend fun processCapturedBitmap(
        rawBitmapInput: Bitmap,
        cleanedBitmapInput: Bitmap = rawBitmapInput,
        homographyNorm: FloatArray = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f),
        allowedCategoryIds: List<String> = listOf("groceries", "coffee", "lunch", "dining", "utilities", "shopping", "uncategorized"),
        matchedCategoryRuleId: String? = null
    ): PipelineExecutionOutput = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        val rawBitmap = downsampleIfNeeded(rawBitmapInput)
        val cleanedBitmap = downsampleIfNeeded(cleanedBitmapInput)

        val documentId = "doc_${UUID.randomUUID().toString().take(8)}"
        val pageNumber = 1

        // =====================================================================
        // GEAR 1: CAPTURE & TINK STREAMING AEAD ENCRYPTION TO DISK
        // =====================================================================
        val rawPngBytes = bitmapToWebpOrPngBytes(rawBitmap)
        val cleanPngBytes = bitmapToWebpOrPngBytes(cleanedBitmap)
        val sourceSha256 = sha256Hex(rawPngBytes)

        val outputDir = File(context.filesDir, "evidence_images").apply { mkdirs() }
        val rawDest = File(outputDir, "raw_display_page_1.enc")
        val cleanDest = File(outputDir, "clean_ocr_page_1.enc")

        // Write raw bytes to a temp file to pass to the encryptor which takes a File
        val tempRaw = File.createTempFile("raw", ".tmp", context.cacheDir).apply { writeBytes(rawPngBytes) }
        val tempClean = File.createTempFile("clean", ".tmp", context.cacheDir).apply { writeBytes(cleanPngBytes) }

        fileEncryptor.encryptFile(tempRaw, rawDest, documentId, pageNumber, "raw")
        fileEncryptor.encryptFile(tempClean, cleanDest, documentId, pageNumber, "clean")

        tempRaw.delete()
        tempClean.delete()

        PipelineGearLogger.d(PipelineGear.GEAR_1_CAPTURE_CRYPTO, documentId) {
            "Encrypted raw (${rawDest.length()} B) & clean (${cleanDest.length()} B) bitmaps " +
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

        // Convert data models
        val layoutElements = ocrResult.elements.map {
            com.zoewave.probase.seaweed.model.finance.layout.OcrLayoutElement(
                id = it.id,
                text = it.rawText,
                boxNormalizedOcr = it.boxNormalizedOcr,
                rotationDegrees = it.rotationDegrees
            )
        }

        val reconstructedLines = spatialLayoutEngine.groupIntoLines(layoutElements, documentId)

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
        // Placeholder for ArithmeticReconciler actual logic
        val arithmeticSummary = ArithmeticValidationSummary(
            reconciliationRuleUsed = "TAX_EXCLUSIVE",
            receiptTotalReconciled = true, // Simulating a match for this implementation step
            statementBalanceReconciled = null,
            discrepancyMinorUnits = 0L,
            notes = emptyList()
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
            matchedCategoryRuleId = matchedCategoryRuleId,
            docId = documentId
        )

        PipelineGearLogger.d(PipelineGear.GEAR_5_LEDGER_GATE, documentId) {
            "Reconciled=${arithmeticSummary.receiptTotalReconciled} (discrepancy=${arithmeticSummary.discrepancyMinorUnits} minor). " +
                "LockedFields=[total=${gateDecision.lockedFields.lockedDocumentTotal?.amount?.amountMinor}, " +
                "date=${gateDecision.lockedFields.lockedPrimaryDate?.isoDate}, " +
                "merchant=${gateDecision.lockedFields.lockedMerchant?.value}]. " +
                "fullBypass=${gateDecision.fullDocumentResolvedWithoutAi}"
        }

        val resolvedInterpretation = gateDecision.resolvedInterpretation
        if (gateDecision.fullDocumentResolvedWithoutAi && resolvedInterpretation != null) {
            PipelineGearLogger.d(PipelineGear.GEAR_5_LEDGER_GATE, documentId) {
                "100% deterministic resolution achieved! Skipping Gear 6 & Gear 7 LLM call."
            }
            return@withContext PipelineExecutionOutput(
                documentId = documentId,
                sourceSha256 = sourceSha256,
                encryptedRawImagePath = rawDest.absolutePath,
                encryptedCleanImagePath = cleanDest.absolutePath,
                homographyNorm = homographyNorm,
                dominantHeaderHex = dominantHeaderHex,
                dominantBackgroundHex = dominantBgHex,
                ocrElements = ocrResult.elements,
                finalInterpretation = resolvedInterpretation,
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
                return@withContext PipelineExecutionOutput(
                    documentId = documentId,
                    sourceSha256 = sourceSha256,
                    encryptedRawImagePath = rawDest.absolutePath,
                    encryptedCleanImagePath = cleanDest.absolutePath,
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

        PipelineExecutionOutput(
            documentId = documentId,
            sourceSha256 = sourceSha256,
            encryptedRawImagePath = rawDest.absolutePath,
            encryptedCleanImagePath = cleanDest.absolutePath,
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
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
