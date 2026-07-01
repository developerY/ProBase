package com.zoewave.probase.kocolor.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.ColumnTypeConverters
import com.zoewave.probase.kocolor.db.converter.FashionConverters
import com.zoewave.probase.kocolor.db.dao.ClothingDao
import com.zoewave.probase.kocolor.db.dao.CosmeticDao
import com.zoewave.probase.kocolor.db.dao.FashionProfileDao
import com.zoewave.probase.kocolor.db.dao.InventoryDao
import com.zoewave.probase.kocolor.db.dao.RoutineDao
import com.zoewave.probase.kocolor.db.dao.SavedSuggestionDao
import com.zoewave.probase.kocolor.db.entity.ClothingItemEntity
import com.zoewave.probase.kocolor.db.entity.CosmeticItemEntity
import com.zoewave.probase.kocolor.db.entity.FashionProfileEntity
import com.zoewave.probase.kocolor.db.entity.InventoryItemEntity
import com.zoewave.probase.kocolor.db.entity.RoutineEntity
import com.zoewave.probase.kocolor.db.entity.SavedSuggestionEntity

@Database(
    entities = [
        FashionProfileEntity::class,
        SavedSuggestionEntity::class,
        InventoryItemEntity::class,
        RoutineEntity::class,
        CosmeticItemEntity::class,
        ClothingItemEntity::class
    ],
    version = 6,
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
}
