package com.zoewave.probase.ashbike.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.TypeConverters
import com.zoewave.probase.ashbike.database.converter.Converters

@Database(
    entities = [BikeRideEntity::class, RideLocationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
@Suppress("ROOM_MISSING_CONSTRUCTED_BY")
abstract class BikeRideDatabase : RoomDatabase() {
    abstract val bikeRideDao: BikeRideDao

    companion object {
        const val DATABASE_NAME = "ashbike_db"
    }
}
