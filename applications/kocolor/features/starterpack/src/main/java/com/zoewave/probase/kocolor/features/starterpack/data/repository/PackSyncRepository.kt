package com.zoewave.probase.kocolor.features.starterpack.data.repository

import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.KcpsPayload
import com.zoewave.probase.kocolor.features.starterpack.data.remote.model.PackInfo
import kotlinx.coroutines.flow.Flow

interface PackSyncRepository {
    val cartProductIds: Flow<Set<String>>
    val ownedProductIds: Flow<Set<String>>
    fun getInstalledPacks(): Flow<List<InstalledPackEntity>>
    suspend fun fetchManifest(): Result<List<PackInfo>>
    suspend fun ingestPack(pack: PackInfo): Result<Unit>
    suspend fun importSelectedItems(packId: String, payload: KcpsPayload): Result<Unit>
    suspend fun toggleCartItem(productId: String, packId: String): Result<Unit>
    suspend fun purchaseStagedProduct(productId: String): Result<Unit>
    suspend fun wipePack(packId: String): Result<Unit>
}
