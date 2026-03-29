package com.zoewave.probase.applications.photodo.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    exportSchema = false
)
@TypeConverters(PhotoDoConverters::class)
abstract class PhotoDoDB : RoomDatabase() {
    abstract fun photoDoDao(): PhotoDoDao
}
