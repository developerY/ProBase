package com.zoewave.probase.gotmind.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.zoewave.probase.gotmind.database.MindWaveScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MindWaveScoreDao {
    @Query("SELECT * FROM mindwave_scores ORDER BY score DESC LIMIT 7")
    fun getTopScores(): Flow<List<MindWaveScoreEntity>>

    @Insert
    suspend fun insertScore(score: MindWaveScoreEntity)

    @Query("DELETE FROM mindwave_scores")
    suspend fun clearAllScores()
}
