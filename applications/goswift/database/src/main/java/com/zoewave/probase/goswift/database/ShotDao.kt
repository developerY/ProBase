package com.zoewave.probase.goswift.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShotDao {
    @Query("SELECT * FROM shots ORDER BY timestamp DESC")
    fun getAllShots(): Flow<List<ShotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShot(shot: ShotEntity)

    @Query("DELETE FROM shots WHERE id = :id")
    suspend fun deleteShot(id: String)
}
