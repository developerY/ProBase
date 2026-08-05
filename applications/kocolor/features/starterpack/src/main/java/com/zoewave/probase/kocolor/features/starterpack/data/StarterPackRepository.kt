package com.zoewave.probase.kocolor.features.starterpack.data

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import com.zoewave.probase.core.model.ritual.CosmeticItem
import com.zoewave.probase.core.model.ritual.MacroCategory
import com.zoewave.probase.core.model.ritual.MicroCategory
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.features.starterpack.data.remote.KocolorApiService
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackItem
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.SearchIndexEntry
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.SignedPayloadEnvelope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarterPackRepository @Inject constructor(
    private val apiService: KocolorApiService,
    private val cosmeticRepository: CosmeticInventoryRepository,
    @ApplicationContext private val context: Context
) {
    private var searchIndexCache: List<SearchIndexEntry>? = null

    suspend fun getSearchIndex(): List<SearchIndexEntry> {
        return searchIndexCache ?: apiService.getSearchIndex().also {
            searchIndexCache = it
        }
    }

    suspend fun getManifest(): SignedPayloadEnvelope {
        return apiService.getManifest()
    }

    suspend fun getPackItems(packId: String): List<PackItem> {
        return apiService.getPackItems(packId)
    }

    suspend fun importItems(items: List<PackItem>) {
        withContext(Dispatchers.IO) {
            items.forEach { packItem ->
                // Map PackItem to CosmeticItem
                val cosmeticItem = CosmeticItem(
                    name = packItem.name,
                    brand = packItem.brand,
                    macroCategory = MacroCategory.COMPLEXION, // Default
                    microCategory = MicroCategory.FOUNDATION, // Default
                    colorHex = packItem.hexColor,
                    shadeName = packItem.shade,
                    imageUrl = packItem.imageUrl
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
