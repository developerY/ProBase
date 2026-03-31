package com.zoewave.probase.applications.photodo.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.TypeConverters
import com.zoewave.probase.applications.photodo.db.converter.PhotoDoConverters
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.ExpenseEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity

@Database(
    entities = [
        CategoryEntity::class,
        ProjectEntity::class,
        PhotoEntity::class,
        TaskEntity::class,
        ExpenseEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(PhotoDoConverters::class)
@Suppress("ROOM_MISSING_CONSTRUCTED_BY")
abstract class PhotoDoDB : RoomDatabase() {
    abstract fun photoDoDao(): PhotoDoDao
}
