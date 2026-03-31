package com.zoewave.probase.core.database

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

@Database(entities = [BaseProEntity::class], version = 1, exportSchema = false)
@Suppress("ROOM_MISSING_CONSTRUCTED_BY")
abstract class BaseProDB : RoomDatabase() {

    abstract val baseproDao: BaseProDao

    companion object {
        const val DATABASE_NAME = "basepro_db"

        @JvmStatic
        fun getDatabase(context: Context): BaseProDB {
            return Room.databaseBuilder(
                context,
                BaseProDB::class.java,
                DATABASE_NAME
            )
                .setDriver(BundledSQLiteDriver())
                .build()
        }
    }
}
