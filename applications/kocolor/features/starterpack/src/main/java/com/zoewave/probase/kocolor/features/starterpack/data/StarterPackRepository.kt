package com.zoewave.probase.kocolor.features.starterpack.data

import android.content.Context
import android.util.Log
import coil.imageLoader
import coil.request.ImageRequest
import com.zoewave.probase.core.model.ritual.*
import com.zoewave.probase.core.util.HashUtils
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.KocolorApiService
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItem
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackManifest
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.SearchIndexEntry
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.SignedPayloadEnvelope
import com.zoewave.probase.kocolor.features.starterpack.domain.security.SignatureVerifier
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarterPackRepository @Inject constructor(
    private val apiService: KocolorApiService,
    private val cosmeticRepository: CosmeticInventoryRepository,
    private val signatureVerifier: SignatureVerifier,
    @ApplicationContext private val context: Context
) {
    private var searchIndexCache: List<SearchIndexEntry>? = null
    
    // Caches provenance info from the last fetched pack to attach during import
    private var lastFetchedProvenance: Provenance? = null

    suspend fun getSearchIndex(): List<SearchIndexEntry> {
        return searchIndexCache ?: apiService.getSearchIndex().also {
            searchIndexCache = it
        }
    }

    suspend fun getManifest(): SignedPayloadEnvelope<PackManifest> {
        val envelope = apiService.getManifest()
        // Boundary Enforcement: Verify Manifest first
        if (!signatureVerifier.verify(envelope.data.toString(), envelope.signature)) {
            throw PackException.SignatureException("Manifest signature verification failed!")
        }
        return envelope
    }

    suspend fun getPackItems(packId: String, expectedSha256: String? = null): List<PackItem> {
        Log.d("StarterPackRepo", "getPackItems: Fetching $packId")
        val envelope = try {
            apiService.getPackItems(packId)
        } catch (e: Exception) {
            throw PackException.DownloadException("Failed to download pack $packId", e)
        }
        
        val rawData = envelope.data.toString()

        // 1. Pre-signature Integrity Check (SHA-256)
        if (expectedSha256 != null) {
            val actualSha256 = HashUtils.calculateSha256(rawData)
            if (actualSha256 != expectedSha256) {
                throw PackException.ManifestException("Pack $packId content corruption detected (Hash mismatch)!")
            }
        }

        // 2. Run signature verification
        if (!signatureVerifier.verify(rawData, envelope.signature)) {
            throw PackException.SignatureException("Pack $packId signature verification failed!")
        }

        // 3. Schema Version check
        if (envelope.schemaVersion < 2) {
             throw PackException.SchemaException("Pack $packId uses an outdated schema version (${envelope.schemaVersion})")
        }
        
        // 4. Cache provenance for later import
        lastFetchedProvenance = Provenance(
            packId = packId,
            packageVersion = envelope.packageVersion,
            schemaVersion = envelope.schemaVersion,
            publisher = "KoColor Official",
            installedAtTimestamp = System.currentTimeMillis(),
            verificationState = VerificationState.VERIFIED
        )
        
        return envelope.data
    }

    suspend fun importItems(items: List<PackItem>) {
        withContext(Dispatchers.IO) {
            val provenance = lastFetchedProvenance
            
            // Boundary Enforcement: Atomic Room Transaction via bulk insert
            val cosmeticItems = items.map { packItem ->
                CosmeticItem(
                    name = packItem.name,
                    brand = packItem.brand,
                    macroCategory = packItem.macroCategory?.let { macro ->
                        MacroCategory.entries.find { it.name == macro.uppercase() }
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

            // Async pre-loading of assets
            items.forEach { item ->
                val request = ImageRequest.Builder(context)
                    .data(item.imageUrl)
                    .build()
                context.imageLoader.enqueue(request)
            }
        }
    }
}
