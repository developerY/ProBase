package com.zoewave.probase.kocolor.features.starterpack.data

import android.content.Context
import android.util.Log
import coil.imageLoader
import coil.request.ImageRequest
import com.github.luben.zstd.Zstd
import com.zoewave.probase.core.model.ritual.ChemistryBase
import com.zoewave.probase.core.model.ritual.ClothingCategory
import com.zoewave.probase.core.model.ritual.ClothingItem
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.Coverage
import com.zoewave.probase.core.model.ritual.Finish
import com.zoewave.probase.core.model.ritual.Formality
import com.zoewave.probase.core.model.ritual.Formulation
import com.zoewave.probase.core.model.ritual.InventorySource
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.core.model.ritual.Provenance
import com.zoewave.probase.core.model.ritual.Temperature
import com.zoewave.probase.core.model.ritual.VerificationState
import com.zoewave.probase.kocolor.features.starterpack.data.remote.KocolorApiService
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.KcpsPayload
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItemDto
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackManifest
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.ProductEditorialNotes
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.SignedPayloadEnvelope
import com.zoewave.probase.kocolor.features.starterpack.domain.security.SignatureVerifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import okio.HashingSink
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarterPackRepository @Inject constructor(
    private val apiService: KocolorApiService,
    private val verifier: SignatureVerifier,
    private val json: Json,
    @ApplicationContext private val context: Context
) {
    private var searchIndexCache: Map<String, List<String>>? = null

    private companion object {
        const val TAG = "StarterPackRepo"
        const val MAX_PACKAGE_SIZE = 32 * 1024 * 1024L // 32 MB safety limit
        val ZSTD_MAGIC = byteArrayOf(0x28.toByte(), 0xB5.toByte(), 0x2F.toByte(), 0xFD.toByte())
    }

    suspend fun getSearchIndex(): Map<String, List<String>> {
        return searchIndexCache ?: apiService.getSearchIndex().also {
            searchIndexCache = it
        }
    }

    suspend fun getManifest(): SignedPayloadEnvelope<PackManifest> {
        val responseBody = apiService.getManifestRaw()
        val rawJson = responseBody.string()
        
        // 1. Parse the envelope structure once to get signature and metadata
        val envelope: SignedPayloadEnvelope<JsonElement> = json.decodeFromString(rawJson)
        
        // 2. Trust Bootstrap: Extract the EXACT "data" string from the raw JSON
        val dataPart = extractDataProperty(rawJson)
        val rawDataBytes = dataPart.toByteArray(Charsets.UTF_8)
        
        Log.d(TAG, "getManifest: Data part for signature: $dataPart")
        Log.d(TAG, "getManifest: Signature from envelope: ${envelope.signature}")

        // Root of Trust Verification
        if (!verifier.verify(rawDataBytes, envelope.signature, "")) {
            Log.e(TAG, "getManifest: Signature verification failed!")
            throw PackException.SignatureException("Trust Bootstrap Failed: Manifest signature is invalid!")
        }
        
        val manifest: PackManifest = json.decodeFromJsonElement(envelope.data)
        return SignedPayloadEnvelope(
            data = manifest,
            signature = envelope.signature,
            packageVersion = envelope.packageVersion,
            schemaVersion = envelope.schemaVersion
        )
    }

    suspend fun fetchVerifiedPackage(packInfo: PackInfo): KcpsPayload = withContext(Dispatchers.IO) {
        Log.d(TAG, "fetchVerifiedPackage: Starting secure streaming download for ${packInfo.id}")
        
        val tempFile = File(context.cacheDir, "${packInfo.id}-temp.kpkg")
        
        try {
            // 1. Stream binary from CDN
            val responseBody = try {
                apiService.downloadPackageBinary(packInfo.endpoint)
            } catch (e: Exception) {
                throw PackException.DownloadException("Failed to download package from ${packInfo.endpoint}", e)
            }

            // 2. Spool to Disk & Hash Incrementally
            val hashingSink = HashingSink.sha256(tempFile.sink())
            var streamedBytes = 0L

            responseBody.source().use { source ->
                hashingSink.buffer().use { sink ->
                    streamedBytes = sink.writeAll(source)
                }
            }

            // 3. Size Validation (Early rejection)
            if (streamedBytes != packInfo.compressedSizeBytes) {
                 throw PackException.IntegrityException("Size mismatch! Expected ${packInfo.compressedSizeBytes}, got $streamedBytes")
            }
            if (streamedBytes > MAX_PACKAGE_SIZE) {
                throw PackException.IntegrityException("Package exceeds maximum allowed size of 32MB.")
            }

            // 4. Integrity Check (SHA-256) - Detect accidental corruption
            val actualHash = hashingSink.hash.hex()
            if (!actualHash.equals(packInfo.sha256, ignoreCase = true)) {
                throw PackException.IntegrityException("Integrity check failed for ${packInfo.id}. Hash mismatch.")
            }

            // 5. Authenticity Check (Ed25519) - Prove publisher identity
            val isAuthentic = tempFile.source().use { source ->
                verifier.verify(source, packInfo.signature)
            }
            if (!isAuthentic) {
                throw PackException.SignatureException("Authenticity verification failed for ${packInfo.id}. Signature is invalid.")
            }

            // 6. Version Negotiation
            if (packInfo.packageFormatVersion > 1) {
                throw PackException.VersionMismatchException("Unsupported package format version: ${packInfo.packageFormatVersion}")
            }
            if (packInfo.schemaVersion > 1) {
                throw PackException.SchemaException("Schema version ${packInfo.schemaVersion} too new for this client.")
            }

            // 7. Algorithm Validation
            if (packInfo.compressionAlgorithm != "zstd") {
                throw PackException.VersionMismatchException("Unsupported compression algorithm: ${packInfo.compressionAlgorithm}")
            }

            // 8. Header Check (Zstd Magic Bytes)
            val fileBytes = tempFile.readBytes() 
            if (!isZstdHeader(fileBytes)) {
                throw PackException.IntegrityException("Binary is not a valid Zstd archive.")
            }

            // 9. Decompress (Verify-First Rule enforced: decompression ONLY after successful verification)
            if (packInfo.uncompressedSizeBytes > MAX_PACKAGE_SIZE) {
                throw PackException.IntegrityException("Uncompressed payload too large (>32MB). Possible JSON bomb.")
            }

            val decompressedBytes = try {
                Zstd.decompress(fileBytes, packInfo.uncompressedSizeBytes.toInt())
            } catch (e: Exception) {
                throw PackException.IntegrityException("Decompression failed. Binary might be corrupt.")
            }

            // 10. Parse & Validate
            val response: KcpsPayload = try {
                val jsonString = String(decompressedBytes, Charsets.UTF_8)
                json.decodeFromString(jsonString)
            } catch (e: Exception) {
                throw PackException.SchemaException("Failed to parse decompressed JSON payload.")
            }

            if (response.schemaVersion > 1) {
                throw PackException.SchemaException("Payload schema version ${response.schemaVersion} too new for this client.")
            }

            response
        } finally {
            // Clean up: Always delete the temporary binary stream file
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    suspend fun getPackItems(packId: String): List<PackItemDto> {
        val manifest = getManifest().data
        val packInfo = manifest.packs.find { it.id == packId } 
            ?: throw PackException.ManifestException("Pack $packId not found in manifest.")
        
        val payload = fetchVerifiedPackage(packInfo)
        return payload.cosmetics + payload.clothing
    }

    suspend fun getProductEditorialNotes(productId: String): Result<ProductEditorialNotes> = runCatching {
        apiService.getProductNotes(productId)
    }

    private fun isZstdHeader(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        return bytes[0] == ZSTD_MAGIC[0] && bytes[1] == ZSTD_MAGIC[1] && 
               bytes[2] == ZSTD_MAGIC[2] && bytes[3] == ZSTD_MAGIC[3]
    }

    /**
     * Extracts the raw JSON string for the "data" property from a SignedPayloadEnvelope.
     */
    private fun extractDataProperty(rawJson: String): String {
        val dataKey = "\"data\":"
        val startIdx = rawJson.indexOf(dataKey)
        if (startIdx == -1) throw PackException.ManifestException("Manifest missing 'data' field.")
        
        val valueStart = startIdx + dataKey.length
        
        var actualStart = -1
        for (i in valueStart until rawJson.length) {
            if (rawJson[i] == '{' || rawJson[i] == '[') {
                actualStart = i
                break
            }
        }
        if (actualStart == -1) throw PackException.ManifestException("Manifest 'data' field has invalid format.")
        
        var braceCount = 0
        var inQuote = false
        var escaped = false
        
        for (i in actualStart until rawJson.length) {
            val char = rawJson[i]
            if (escaped) { escaped = false; continue }
            if (char == '\\') { escaped = true } 
            else if (char == '"') { inQuote = !inQuote } 
            else if (!inQuote) {
                if (char == '{' || char == '[') braceCount++
                else if (char == '}' || char == ']') {
                    braceCount--
                    if (braceCount == 0) return rawJson.substring(actualStart, i + 1)
                }
            }
        }
        throw PackException.ManifestException("Malformed JSON: Unbalanced braces in 'data' field.")
    }

    /**
     * Map pack items to domain models with appropriate provenance.
     */
    fun mapToDomainItems(payload: KcpsPayload, packInfo: PackInfo): Pair<List<CosmeticItem>, List<ClothingItem>> {
        val provenance = Provenance(
            packId = packInfo.id,
            packageVersion = packInfo.version.toString(),
            schemaVersion = packInfo.schemaVersion,
            publisher = packInfo.publisher,
            packageHash = packInfo.sha256,
            installedAtTimestamp = System.currentTimeMillis(),
            verificationState = VerificationState.VERIFIED
        )

        val sourceType = try { InventorySource.valueOf(packInfo.packType) } catch (e: Exception) { InventorySource.UNKNOWN }

        val cosmetics = payload.cosmetics.map { dto ->
            CosmeticItem(
                remoteId = dto.id,
                name = dto.name,
                brand = dto.brand,
                macroCategory = try { MacroCategory.valueOf(dto.macroCategory.uppercase()) } catch (e: Exception) { MacroCategory.COMPLEXION },
                microCategory = try { MicroCategory.valueOf(dto.microCategory.uppercase()) } catch (e: Exception) { MicroCategory.FOUNDATION },
                formulation = dto.formulation?.let { try { Formulation.valueOf(it.uppercase()) } catch (e: Exception) { Formulation.UNKNOWN } } ?: Formulation.UNKNOWN,
                chemistryBase = dto.chemistryBase?.let { try { ChemistryBase.valueOf(it.uppercase()) } catch (e: Exception) { ChemistryBase.UNKNOWN } } ?: ChemistryBase.UNKNOWN,
                finish = dto.finish?.let { try { Finish.valueOf(it.uppercase()) } catch (e: Exception) { Finish.UNKNOWN } } ?: Finish.UNKNOWN,
                coverage = dto.coverage?.let { try { Coverage.valueOf(it.uppercase()) } catch (e: Exception) { Coverage.NOT_APPLICABLE } } ?: Coverage.NOT_APPLICABLE,
                temperature = dto.temperature?.let { try { Temperature.valueOf(it.uppercase()) } catch (e: Exception) { Temperature.UNKNOWN } } ?: Temperature.UNKNOWN,
                colorHex = dto.colorHex,
                shadeName = dto.shadeName,
                imageUrl = dto.imageUrl,
                notes = dto.notes,
                instructions = dto.instructions,
                price = dto.price,
                volume = dto.volume,
                paoMonths = dto.paoMonths,
                expiryDate = dto.expiryDate,
                ingredients = dto.ingredients,
                allergens = dto.allergens,
                isVegan = dto.isVegan,
                isCrueltyFree = dto.isCrueltyFree,
                fdaDataVerified = dto.fdaDataVerified,
                sourceType = sourceType,
                sourceName = packInfo.name,
                provenance = provenance,
                
                // --- Engine Enrichment (Calculated at Compile Time) ---
                calculatedChemistryPhase = dto.calculatedChemistryPhase,
                calculatedCielabL = dto.cielab?.getOrNull(0)?.toDouble(),
                calculatedCielabA = dto.cielab?.getOrNull(1)?.toDouble(),
                calculatedCielabB = dto.cielab?.getOrNull(2)?.toDouble(),
                calculatedHueAngle = null, // Logic for hue angle can be added if needed, or if Rust provides 4 values
                blurhash = dto.blurhash,
                isSiliconeFree = dto.calculatedSafetyFlags?.isSiliconeFree,
                isParabenFree = dto.calculatedSafetyFlags?.isParabenFree,
                isSulfateFree = dto.calculatedSafetyFlags?.isSulfateFree,
                heroActives = dto.calculatedHeroActives,
                calculatedUnitPrice = dto.calculatedUnitPrice,
                searchTokens = dto.calculatedSearchTokens
            )
        }

        val clothing = payload.clothing.map { dto ->
            // High-Fidelity Category Mapping
            val mappedCategory = when (dto.microCategory.uppercase()) {
                "SHIRT" -> ClothingCategory.TOPS
                "PANTS" -> ClothingCategory.BOTTOMS
                "SHOES" -> ClothingCategory.SHOES
                "OUTERWEAR" -> ClothingCategory.OUTERWEAR
                "ACTIVEWEAR" -> ClothingCategory.ACTIVEWEAR
                "DRESS" -> ClothingCategory.DRESSES
                "ONE_PIECE" -> ClothingCategory.DRESSES
                else -> {
                    try {
                        ClothingCategory.valueOf(dto.macroCategory.uppercase())
                    } catch (e: Exception) {
                        // Fallback to macro if micro doesn't match
                        if (dto.macroCategory.uppercase() == "APPAREL") ClothingCategory.TOPS
                        else ClothingCategory.OTHER
                    }
                }
            }

            ClothingItem(
                remoteId = dto.id,
                name = dto.name,
                brand = dto.brand,
                category = mappedCategory,
                formality = dto.formality?.let { try { Formality.valueOf(it.uppercase()) } catch (e: Exception) { Formality.CASUAL } } ?: Formality.CASUAL,
                colorHex = dto.colorHex,
                size = dto.volume,
                material = if (dto.ingredients.isNotEmpty()) dto.ingredients.joinToString(", ") else dto.material,
                price = dto.price,
                imageUrl = dto.imageUrl,
                dominantHex = dto.dominantHex,
                vibrantHex = dto.vibrantHex,
                mutedHex = dto.mutedHex,
                paletteHexes = dto.paletteHexes,
                colorTemperature = dto.colorTemperature,
                seasonalPalette = dto.seasonalPalette,
                contrastLevel = dto.contrastLevel,
                koColorGroup = dto.koColorGroup,
                sourceType = sourceType,
                sourceName = packInfo.name,
                provenance = provenance,
                
                // --- Engine Enrichment (Calculated at Compile Time) ---
                blurhash = dto.blurhash,
                searchTokens = dto.calculatedSearchTokens
            )
        }
        
        return Pair(cosmetics, clothing)
    }

    fun prefetchImages(payload: KcpsPayload) {
        (payload.cosmetics.map { it.imageUrl } + payload.clothing.map { it.imageUrl }).forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()
            context.imageLoader.enqueue(request)
        }
    }
}
