package com.zoewave.probase.rxlogic.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.TypeConverters

@Database(
    entities = [MedicationEntity::class, MedicationLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RxLogicConverters::class)
abstract class RxLogicDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
}
