package com.zoewave.probase.photodo.features.timebudgeting.data.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.zoewave.probase.photodo.features.timebudgeting.data.db.dao.TimeBudgetDao
import com.zoewave.probase.photodo.features.timebudgeting.data.db.entity.TimeBudgetEntity

@Database(
    entities = [TimeBudgetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TimeBudgetDatabase : RoomDatabase() {
    abstract fun timeBudgetDao(): TimeBudgetDao
}
