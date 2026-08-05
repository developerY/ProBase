package com.zoewave.probase.kocolor.features.starterpack.data

import android.content.Context
import android.util.Log
import coil.imageLoader
import coil.request.ImageRequest
import com.zoewave.probase.core.model.ritual.*
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

class PayloadVerificationException(message: String) : Exception(message)

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
        return apiService.getManifest()
    }

    suspend fun getPackItems(packId: String): List<PackItem> {
        Log.d("StarterPackRepo", "getPackItems: Fetching $packId")
        val envelope = apiService.getPackItems(packId)
        
        // 1. Run signature verification (Stub/Actual)
        if (!signatureVerifier.verify(envelope.data.toString(), envelope.signature)) {
            throw PayloadVerificationException("Pack $packId signature verification failed!")
        }
        
        // 2. Cache provenance for later import
        lastFetchedProvenance = Provenance(
            packId = packId,
            packVersion = envelope.version,
            publisher = "KoColor Official", // Default publisher
            installedAtTimestamp = System.currentTimeMillis(),
            isSignatureVerified = true
        )
        
        return envelope.data
    }

    suspend fun importItems(items: List<PackItem>) {
        withContext(Dispatchers.IO) {
            val provenance = lastFetchedProvenance
            
            items.forEach { packItem ->
                // Map PackItem to CosmeticItem with rich Provenance
                val cosmeticItem = CosmeticItem(
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
                cosmeticRepository.saveCosmeticItem(cosmeticItem)
            }

            // Refinement: Trigger asynchronous pre-loading of full-resolution imageUrl assets
            items.forEach { item ->
                val request = ImageRequest.Builder(context)
                    .data(item.imageUrl)
                    .build()
                context.imageLoader.enqueue(request)
            }
        }
    }
}
