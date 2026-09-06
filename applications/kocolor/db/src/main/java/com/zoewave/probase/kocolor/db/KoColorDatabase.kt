package com.zoewave.probase.kocolor.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.ColumnTypeConverters
import androidx.room3.Transaction
import com.zoewave.probase.kocolor.db.converter.FashionConverters
import com.zoewave.probase.kocolor.db.converter.KoColorTypeConverters
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.FashionProfileDao
import com.zoewave.probase.kocolor.db.dao.GarmentRotationDao
import com.zoewave.probase.kocolor.db.dao.InstalledPackDao
import com.zoewave.probase.kocolor.db.dao.InventoryDao
import com.zoewave.probase.kocolor.db.dao.PlaylistDao
import com.zoewave.probase.kocolor.db.dao.ProductDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.db.dao.SavedSuggestionDao
import com.zoewave.probase.kocolor.db.dao.ShoppingCartDao
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.db.entity.ClothingUsageEntity
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.db.entity.DailyStylePlanEntity
import com.zoewave.probase.kocolor.db.entity.FashionProfileEntity
import com.zoewave.probase.kocolor.db.entity.GlobalRotationMetricsEntity
import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import com.zoewave.probase.kocolor.db.entity.InventoryItemEntity
import com.zoewave.probase.kocolor.db.entity.ProductEntity
import com.zoewave.probase.kocolor.db.entity.RoutineEntity
import com.zoewave.probase.kocolor.db.entity.SavedSuggestionEntity
import com.zoewave.probase.kocolor.db.entity.ShoppingCartItemEntity
import com.zoewave.probase.kocolor.db.entity.StylePlaylistEntity
import com.zoewave.probase.kocolor.model.playlist.DailyPlanStatus
import com.zoewave.probase.kocolor.model.playlist.PlaylistStatus

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
        ClothingUsageEntity::class,
        StylePlaylistEntity::class,
        DailyStylePlanEntity::class
    ],
    version = 2,
    exportSchema = false
)
@ColumnTypeConverters(FashionConverters::class, KoColorTypeConverters::class)
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
    abstract val playlistDao: PlaylistDao

    @Transaction
    suspend fun savePlaylist(playlist: StylePlaylistEntity, plans: List<DailyStylePlanEntity>) {
        playlistDao.insertPlaylist(playlist)
        playlistDao.insertDailyPlans(plans)
    }

    @Transaction
    suspend fun commitDailyStylePlan(
        planId: String,
        actuallyWornProductIds: List<String>
    ) {
        // 1. Fetch current plan
        val currentPlan = playlistDao.getDailyPlan(planId) ?: return
        
        // 2. Idempotency Check
        if (currentPlan.status == DailyPlanStatus.COMMITTED) return

        // 3. Write to V1 Historical Memory (Feedback Stream)
        commitOutfitUsage(actuallyWornProductIds)

        // 4. Mark V2 Daily Plan as COMMITTED
        playlistDao.updateDailyPlanStatus(planId, DailyPlanStatus.COMMITTED)

        // 5. Evaluate terminal state of the parent Playlist
        val playlistWithDays = playlistDao.getPlaylistWithDays(currentPlan.playlistId) ?: return
        val allDaysFinished = playlistWithDays.dailyPlans.all { 
            it.status == DailyPlanStatus.COMMITTED ||
            it.status == DailyPlanStatus.SKIPPED
        }
        
        if (allDaysFinished) {
            playlistDao.updatePlaylistStatus(currentPlan.playlistId, PlaylistStatus.COMPLETED)
        }
    }

    @Transaction
    suspend fun purchaseStagedProduct(
        cosmeticEntities: List<CosmeticItemEntity>,
        clothingEntities: List<ClothingItemEntity>,
        productId: String
    ) {
        if (cosmeticEntities.isNotEmpty()) cosmeticDao.insertCosmetics(cosmeticEntities)
        if (clothingEntities.isNotEmpty()) clothingDao.insertClothingList(clothingEntities)
        shoppingCartDao.deleteByProductId(productId)
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
        
        // Fetch existing usages to increment them
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
