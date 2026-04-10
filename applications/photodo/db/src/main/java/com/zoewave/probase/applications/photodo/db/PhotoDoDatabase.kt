package com.zoewave.probase.applications.photodo.db

import androidx.room3.AutoMigration
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.TypeConverters
import com.zoewave.probase.applications.photodo.db.converter.PhotoDoConverters
import com.zoewave.probase.applications.photodo.db.entity.CategoryEntity
import com.zoewave.probase.applications.photodo.db.entity.ExpenseEntity
import com.zoewave.probase.applications.photodo.db.entity.PhotoEntity
import com.zoewave.probase.applications.photodo.db.entity.ProjectEntity
import com.zoewave.probase.applications.photodo.db.entity.TaskEntity
import com.zoewave.probase.applications.photodo.db.entity.time.TimeLogEntity

@Database(
    entities = [
        CategoryEntity::class,
        ProjectEntity::class,
        PhotoEntity::class,
        TaskEntity::class,
        ExpenseEntity::class,
        TimeLogEntity::class
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(PhotoDoConverters::class)
@Suppress("ROOM_MISSING_CONSTRUCTED_BY")
abstract class PhotoDoDB : RoomDatabase() {
    abstract fun photoDoDao(): PhotoDoDao
    internal abstract fun timeTrackingDao(): TimeTrackingDao
}
