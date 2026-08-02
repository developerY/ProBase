package com.zoewave.probase.kocolor.features.inventory.data.repository

import com.zoewave.probase.core.util.color.ColorQuantizer
import com.zoewave.probase.kocolor.features.obf.data.repository.ObfRepository
import com.zoewave.probase.kocolor.data.mapper.toEntity
import com.zoewave.probase.kocolor.data.mapper.toModel
import com.zoewave.probase.kocolor.data.repository.CosmeticInventoryRepository
import com.zoewave.probase.kocolor.data.remote.KocolorApiService
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.core.model.ritual.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CosmeticInventoryRepositoryImpl @Inject constructor(
    private val obfRepository: ObfRepository,
    private val cosmeticDao: CosmeticDao,
    private val apiService: KocolorApiService
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

    override suspend fun deleteCosmeticItem(id: Long) {
        cosmeticDao.deleteCosmetic(id)
    }

    override suspend fun ingestStarterPack(): Result<Unit> = runCatching {
        val response = apiService.getStarterPack()
        response.cosmetics.forEach { dto ->
            val macro = MacroCategory.entries.find { it.displayName == dto.macroCategory } ?: MacroCategory.TOOLS
            val micro = try { MicroCategory.valueOf(dto.microCategory.uppercase()) } catch (e: Exception) { MicroCategory.OTHER }
            
            val item = CosmeticItem(
                name = dto.name,
                brand = "KoColor",
                macroCategory = macro,
                microCategory = micro,
                formulation = try { Formulation.valueOf(dto.formulation.uppercase()) } catch (e: Exception) { Formulation.UNKNOWN },
                chemistryBase = try { ChemistryBase.valueOf(dto.chemistry.uppercase()) } catch (e: Exception) { ChemistryBase.UNKNOWN },
                finish = try { Finish.valueOf(dto.finish.uppercase()) } catch (e: Exception) { Finish.UNKNOWN },
                coverage = try { Coverage.valueOf(dto.coverage.uppercase()) } catch (e: Exception) { Coverage.NOT_APPLICABLE },
                temperature = try { Temperature.valueOf(dto.temperature.uppercase()) } catch (e: Exception) { Temperature.UNKNOWN },
                colorHex = dto.colorHex,
                imageUrl = dto.imageUrl
            )
            saveCosmeticItem(item)
        }
    }
}
