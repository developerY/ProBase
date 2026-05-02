package com.zoewave.probase.gotmind.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.zoewave.probase.gotmind.database.MemBloxScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemBloxScoreDao {
    @Query("SELECT * FROM memblox_scores ORDER BY score DESC LIMIT 7")
    fun getTopScores(): Flow<List<MemBloxScoreEntity>>

    @Query("SELECT * FROM memblox_scores ORDER BY score DESC LIMIT 7")
    fun getAllTopScores(): Flow<List<MemBloxScoreEntity>>

    @Insert
    suspend fun insertScore(score: MemBloxScoreEntity)

    @Query("DELETE FROM memblox_scores")
    suspend fun clearAllScores()

    @Query("DELETE FROM memblox_scores WHERE id = :id")
    suspend fun deleteScoreById(id: String)
}
