package com.zoewave.probase.kocolor.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.TypeConverters
import com.zoewave.probase.kocolor.db.converter.FashionConverters
import com.zoewave.probase.kocolor.db.dao.FashionProfileDao
import com.zoewave.probase.kocolor.db.dao.InventoryDao
import com.zoewave.probase.kocolor.db.dao.SavedSuggestionDao
import com.zoewave.probase.kocolor.db.entity.FashionProfileEntity
import com.zoewave.probase.kocolor.db.entity.InventoryItemEntity
import com.zoewave.probase.kocolor.db.entity.SavedSuggestionEntity

@Database(
    entities = [
        FashionProfileEntity::class,
        SavedSuggestionEntity::class,
        InventoryItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(FashionConverters::class)
@Suppress("ROOM_MISSING_CONSTRUCTED_BY")
abstract class KoColorDatabase : RoomDatabase() {
    abstract val fashionProfileDao: FashionProfileDao
    abstract val savedSuggestionDao: SavedSuggestionDao
    abstract val inventoryDao: InventoryDao
}
