package com.zoewave.probase.kocolor.features.starterpack.data

import android.content.Context
import android.util.Log
import coil.imageLoader
import coil.request.ImageRequest
import com.github.luben.zstd.Zstd
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.core.util.HashUtils
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.KocolorApiService
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.*
import com.zoewave.probase.kocolor.features.starterpack.domain.security.SignatureVerifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarterPackRepository @Inject constructor(
    private val apiService: KocolorApiService,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val verifier: SignatureVerifier,
    private val json: Json,
    @ApplicationContext private val context: Context
) {
    private var searchIndexCache: List<SearchIndexEntry>? = null
    
    // Caches provenance info from the last fetched pack to attach during import
    private var lastFetchedProvenance: Provenance? = null

    private companion object {
        const val TAG = "StarterPackRepo"
        const val MAX_PACKAGE_SIZE = 32 * 1024 * 1024L // 32 MB safety limit
        val ZSTD_MAGIC = byteArrayOf(0x28.toByte(), 0xB5.toByte(), 0x2F.toByte(), 0xFD.toByte())
    }

    suspend fun getSearchIndex(): List<SearchIndexEntry> {
        return searchIndexCache ?: apiService.getSearchIndex().also {
            searchIndexCache = it
        }
    }

    suspend fun getManifest(): SignedPayloadEnvelope<PackManifest> {
        val envelope = apiService.getManifest()
        val rawDataBytes = envelope.data.toString().toByteArray(Charsets.UTF_8)
        
        // Root of Trust Verification
        if (!verifier.verify(rawDataBytes, envelope.signature, "")) {
            throw PackException.SignatureException("Manifest signature verification failed!")
        }
        
        val manifest: PackManifest = json.decodeFromJsonElement(envelope.data)
        return SignedPayloadEnvelope(
            data = manifest,
            signature = envelope.signature,
            packageVersion = envelope.packageVersion,
            schemaVersion = envelope.schemaVersion
        )
    }

    suspend fun fetchVerifiedPackage(packInfo: PackInfo): List<PackItem> {
        Log.d(TAG, "fetchVerifiedPackage: Starting secure download for ${packInfo.id}")
        
        // 1. Stream binary from CDN
        val responseBody = try {
            apiService.downloadPackageBinary(packInfo.endpoint)
        } catch (e: Exception) {
            throw PackException.DownloadException("Failed to download package from ${packInfo.endpoint}", e)
        }
        val rawBytes = responseBody.bytes()
        
        // 2. Size Validation (Early rejection)
        if (rawBytes.size.toLong() != packInfo.compressedSizeBytes) {
             throw PackException.IntegrityException("Size mismatch! Expected ${packInfo.compressedSizeBytes}, got ${rawBytes.size}")
        }
        if (rawBytes.size > MAX_PACKAGE_SIZE) {
            throw PackException.IntegrityException("Package exceeds maximum allowed size of 32MB.")
        }

        // 3. Integrity Check (SHA-256) - Detect accidental corruption
        val actualHash = HashUtils.calculateSha256(rawBytes)
        if (!actualHash.equals(packInfo.sha256, ignoreCase = true)) {
            throw PackException.IntegrityException("Integrity check failed for ${packInfo.id}. Hash mismatch.")
        }

        // 4. Authenticity Check (Ed25519) - Prove publisher identity
        val isAuthentic = verifier.verify(
            payloadBytes = rawBytes,
            signatureHex = packInfo.signature,
            expectedSha256 = packInfo.sha256
        )
        if (!isAuthentic) {
            throw PackException.SignatureException("Authenticity verification failed for ${packInfo.id}. Signature is invalid.")
        }

        // 5. Version Negotiation
        if (packInfo.packageFormatVersion > 1) {
            throw PackException.VersionMismatchException("Unsupported package format version: ${packInfo.packageFormatVersion}")
        }
        if (packInfo.schemaVersion > 2) {
            throw PackException.SchemaException("Schema version ${packInfo.schemaVersion} too new for this client.")
        }

        // 6. Algorithm Validation
        if (packInfo.compressionAlgorithm != "zstd") {
            throw PackException.VersionMismatchException("Unsupported compression algorithm: ${packInfo.compressionAlgorithm}")
        }

        // 7. Header Check (Zstd Magic Bytes)
        if (!isZstdHeader(rawBytes)) {
            throw PackException.IntegrityException("Binary is not a valid Zstd archive.")
        }

        // 8. Decompress (Verify-First Rule enforced: decompression ONLY after successful verification)
        val decompressedBytes = try {
            Zstd.decompress(rawBytes, packInfo.uncompressedSizeBytes.toInt())
        } catch (e: Exception) {
            throw PackException.IntegrityException("Decompression failed. Binary might be corrupt.")
        }

        // 9. Parse & Validate
        val response: RemoteStarterPackResponse = try {
            val jsonString = String(decompressedBytes, Charsets.UTF_8)
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            throw PackException.SchemaException("Failed to parse decompressed JSON payload.")
        }

        // Flatten items for UI
        val allItems = response.cosmetics + response.clothing

        // 10. Cache Provenance
        lastFetchedProvenance = Provenance(
            packId = packInfo.id,
            packageVersion = packInfo.version.toString(),
            schemaVersion = packInfo.schemaVersion,
            publisher = packInfo.publisher,
            packageHash = packInfo.sha256,
            installedAtTimestamp = System.currentTimeMillis(),
            verificationState = VerificationState.VERIFIED
        )
        
        return allItems
    }

    suspend fun getPackItems(packId: String): List<PackItem> {
        val manifest = getManifest().data
        val packInfo = manifest.packs.find { it.id == packId } 
            ?: throw PackException.ManifestException("Pack $packId not found in manifest.")
        
        return fetchVerifiedPackage(packInfo)
    }

    private fun isZstdHeader(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        return bytes[0] == ZSTD_MAGIC[0] && bytes[1] == ZSTD_MAGIC[1] && 
               bytes[2] == ZSTD_MAGIC[2] && bytes[3] == ZSTD_MAGIC[3]
    }

    suspend fun importItems(items: List<PackItem>) {
        withContext(Dispatchers.IO) {
            val provenance = lastFetchedProvenance
            
            val cosmeticItems = items.map { packItem ->
                CosmeticItem(
                    name = packItem.name,
                    brand = packItem.brand,
                    macroCategory = packItem.macroCategory?.let { macro ->
                        MacroCategory.entries.find { it.displayName == macro }
                    } ?: MacroCategory.COMPLEXION,
                    microCategory = packItem.microCategory?.let { micro ->
                        try { MicroCategory.valueOf(micro.uppercase()) } catch (e: Exception) { null }
                    } ?: MicroCategory.FOUNDATION,
                    formulation = packItem.formulation?.let { 
                        try { Formulation.valueOf(it.uppercase()) } catch (e: Exception) { null }
                    } ?: Formulation.UNKNOWN,
                    finish = packItem.finish?.let { 
                        try { Finish.valueOf(it.uppercase()) } catch (e: Exception) { null }
                    } ?: Finish.UNKNOWN,
                    temperature = packItem.temperature?.let { 
                        try { Temperature.valueOf(it.uppercase()) } catch (e: Exception) { null }
                    } ?: Temperature.UNKNOWN,
                    chemistryBase = packItem.chemistryBase?.let { 
                        try { ChemistryBase.valueOf(it.uppercase()) } catch (e: Exception) { null }
                    } ?: ChemistryBase.UNKNOWN,
                    coverage = packItem.coverage?.let { 
                        try { Coverage.valueOf(it.uppercase()) } catch (e: Exception) { null }
                    } ?: Coverage.NOT_APPLICABLE,
                    colorHex = packItem.hexColor,
                    shadeName = packItem.shade,
                    imageUrl = packItem.imageUrl,
                    provenance = provenance
                )
            }
            
            cosmeticRepository.saveCosmeticItems(cosmeticItems)

            items.forEach { item ->
                val request = ImageRequest.Builder(context)
                    .data(item.imageUrl)
                    .build()
                context.imageLoader.enqueue(request)
            }
        }
    }
}
