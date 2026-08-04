package com.zoewave.probase.kocolor.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.zoewave.probase.kocolor.db.entity.InstalledPackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalledPackDao {
    @Query("SELECT * FROM installed_packs ORDER BY timestamp DESC")
    fun getAllInstalledPacks(): Flow<List<InstalledPackEntity>>

    @Query("SELECT * FROM installed_packs WHERE packId = :packId")
    suspend fun getPackById(packId: String): InstalledPackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPack(pack: InstalledPackEntity)

    @Query("DELETE FROM installed_packs WHERE packId = :packId")
    suspend fun deletePackRecord(packId: String)
}
