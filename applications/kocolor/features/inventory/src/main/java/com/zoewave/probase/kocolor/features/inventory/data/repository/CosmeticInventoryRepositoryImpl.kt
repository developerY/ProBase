package com.zoewave.probase.kocolor.features.inventory.data.repository

import android.util.Log
import com.zoewave.probase.core.util.color.ColorQuantizer
import com.zoewave.probase.kocolor.features.obf.data.repository.ObfRepository
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.core.model.ritual.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CosmeticInventoryRepositoryImpl @Inject constructor(
    private val obfRepository: ObfRepository,
    private val cosmeticDao: CosmeticDao
) : CosmeticInventoryRepository {

    override fun getAllCosmetics(): Flow<List<CosmeticItem>> {
        return cosmeticDao.getAllCosmetics().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun fetchProductByBarcode(barcode: String): Result<CosmeticItem> {
        android.util.Log.d("CosmeticRepo", "fetchProductByBarcode: Fetching $barcode from OBF")
        return obfRepository.fetchProductByBarcode(barcode)
    }

    override suspend fun saveCosmeticItem(item: CosmeticItem): Long {
        val bucketedItem = item.copy(
            colorFamily = ColorQuantizer.snapToFamily(item.colorHex)
        )
        return if (bucketedItem.id == 0L) {
            cosmeticDao.insertCosmetic(bucketedItem.toEntity())
        } else {
            cosmeticDao.updateCosmetic(bucketedItem.toEntity())
            bucketedItem.id
        }
    }

    override suspend fun saveCosmeticItems(items: List<CosmeticItem>) {
        val entities = items.map { item ->
            item.copy(colorFamily = ColorQuantizer.snapToFamily(item.colorHex)).toEntity()
        }
        cosmeticDao.insertCosmetics(entities)
    }

    override suspend fun deleteCosmeticItem(id: Long) {
        cosmeticDao.deleteCosmetic(id)
    }

    override suspend fun deleteCosmeticsByPack(packId: String): Result<Unit> = runCatching {
        Log.d("CosmeticRepo", "deleteCosmeticsByPack: Deleting items for pack $packId")
        cosmeticDao.deleteCosmeticsByPackId(packId)
    }

    override suspend fun cloneToPersonalArchive(id: Long): Result<Unit> = runCatching {
        Log.d("CosmeticRepo", "cloneToPersonalArchive: Cloning item $id")
        cosmeticDao.cloneToPersonalArchive(id)
    }
}
