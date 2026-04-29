package com.zoewave.probase.gotmind.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.zoewave.probase.gotmind.database.ScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores ORDER BY value DESC LIMIT 10")
    fun getTopScores(): Flow<List<ScoreEntity>>

    @Insert
    suspend fun insertScore(score: ScoreEntity)
}
