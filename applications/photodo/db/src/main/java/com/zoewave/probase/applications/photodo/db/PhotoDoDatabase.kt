package com.zoewave.probase.applications.photodo.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskItemEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskListEntity

@Database(
    entities = [
        CategoryEntity::class,
        TaskListEntity::class,
        PhotoEntity::class,
        TaskItemEntity::class // <--- ADD THIS
    ],
    version = 1,
    exportSchema = false
)

abstract class PhotoDoDB : RoomDatabase() {
    abstract fun photoDoDao(): PhotoDoDao
}
