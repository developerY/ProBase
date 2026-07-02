package com.zoewave.probase.kocolor.features.inventory.data.repository

import com.zoewave.probase.kocolor.features.obf.data.repository.ObfRepository
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.core.model.ritual.CosmeticItem
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
        return if (item.id == 0L) {
            cosmeticDao.insertCosmetic(item.toEntity())
        } else {
            cosmeticDao.updateCosmetic(item.toEntity())
            item.id
        }
    }

    override suspend fun deleteCosmeticItem(id: Long) {
        cosmeticDao.deleteCosmetic(id)
    }
}
