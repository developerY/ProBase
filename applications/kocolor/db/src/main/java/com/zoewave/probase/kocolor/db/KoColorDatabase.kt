package com.zoewave.probase.kocolor.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.ColumnTypeConverters
import androidx.room3.Transaction
import com.zoewave.probase.kocolor.db.converter.FashionConverters
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.FashionProfileDao
import com.zoewave.probase.kocolor.db.dao.GarmentRotationDao
import com.zoewave.probase.kocolor.db.dao.InstalledPackDao
import com.zoewave.probase.kocolor.db.dao.InventoryDao
import com.zoewave.probase.kocolor.db.dao.ProductDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.db.dao.SavedSuggestionDao
import com.zoewave.probase.kocolor.db.dao.ShoppingCartDao
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.db.entity.ClothingUsageEntity
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.db.entity.FashionProfileEntity
import com.zoewave.probase.kocolor.db.entity.GlobalRotationMetricsEntity
import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import com.zoewave.probase.kocolor.db.entity.InventoryItemEntity
import com.zoewave.probase.kocolor.db.entity.ProductEntity
import com.zoewave.probase.kocolor.db.entity.RoutineEntity
import com.zoewave.probase.kocolor.db.entity.SavedSuggestionEntity
import com.zoewave.probase.kocolor.db.entity.ShoppingCartItemEntity

@Database(
    entities = [
        FashionProfileEntity::class,
        SavedSuggestionEntity::class,
        InventoryItemEntity::class,
        RoutineEntity::class,
        CosmeticItemEntity::class,
        ClothingItemEntity::class,
        ProductEntity::class,
        InstalledPackEntity::class,
        ShoppingCartItemEntity::class,
        GlobalRotationMetricsEntity::class,
        ClothingUsageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@ColumnTypeConverters(FashionConverters::class)
@Suppress("ROOM_MISSING_CONSTRUCTED_BY")
abstract class KoColorDatabase : RoomDatabase() {
    abstract val fashionProfileDao: FashionProfileDao
    abstract val savedSuggestionDao: SavedSuggestionDao
    abstract val inventoryDao: InventoryDao
    abstract val routineDao: RoutineDao
    abstract val cosmeticDao: CosmeticDao
    abstract val clothingDao: ClothingDao
    abstract val productDao: ProductDao
    abstract val installedPackDao: InstalledPackDao
    abstract val shoppingCartDao: ShoppingCartDao
    abstract val garmentRotationDao: GarmentRotationDao

    @Transaction
    suspend fun purchaseStagedProduct(
        cosmeticEntities: List<CosmeticItemEntity>,
        clothingEntities: List<ClothingItemEntity>,
        productId: String
    ) {
        if (cosmeticEntities.isNotEmpty()) cosmeticDao.insertCosmetics(cosmeticEntities)
        if (clothingEntities.isNotEmpty()) clothingDao.insertClothingList(clothingEntities)
        shoppingCartDao.deleteByProductId(productId)
        @Transaction
    suspend fun commitOutfitUsage(
        productIds: List<String>,
        timestamp: Long = System.currentTimeMillis()
    ) {
        // 1. Increment Global Metrics
        val currentMetrics = garmentRotationDao.getGlobalMetrics() ?: GlobalRotationMetricsEntity()
        garmentRotationDao.updateGlobalMetrics(
            currentMetrics.copy(
                totalOutfitsCommitted = currentMetrics.totalOutfitsCommitted + 1,
                lastOutfitTimestamp = timestamp
            )
        )

        // 2. Increment Individual Item Usages
        val distinctIds = productIds.distinct()
        val existingUsages = garmentRotationDao.getUsagesForProducts(distinctIds)
        
        val updatedUsages = distinctIds.map { pid ->
            val existing = existingUsages.find { it.productId == pid }
            ClothingUsageEntity(
                productId = pid,
                useCount = (existing?.useCount ?: 0) + 1,
                lastUsedTimestamp = timestamp
            )
        }
        
        garmentRotationDao.updateGarmentUsages(updatedUsages)
    }
}
    @Transaction
    suspend fun commitOutfitUsage(
        productIds: List<String>,
        timestamp: Long = System.currentTimeMillis()
    ) {
        // 1. Increment Global Metrics
        val currentMetrics = garmentRotationDao.getGlobalMetrics() ?: GlobalRotationMetricsEntity()
        garmentRotationDao.updateGlobalMetrics(
            currentMetrics.copy(
                totalOutfitsCommitted = currentMetrics.totalOutfitsCommitted + 1,
                lastOutfitTimestamp = timestamp
            )
        )

        // 2. Increment Individual Item Usages
        val distinctIds = productIds.distinct()
        val existingUsages = garmentRotationDao.getUsagesForProducts(distinctIds)
        
        val updatedUsages = distinctIds.map { pid ->
            val existing = existingUsages.find { it.productId == pid }
            ClothingUsageEntity(
                productId = pid,
                useCount = (existing?.useCount ?: 0) + 1,
                lastUsedTimestamp = timestamp
            )
        }
        
        garmentRotationDao.updateGarmentUsages(updatedUsages)
    }
}
