package com.zoewave.probase.gotmind.data.repository

import com.zoewave.probase.gotmind.database.dao.ScoreDao
import com.zoewave.probase.gotmind.database.ScoreEntity
import com.zoewave.probase.gotmind.model.Score
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface GotMindRepository {
    fun getTopScores(): Flow<List<Score>>
    suspend fun saveScore(value: Int)
}

@Singleton
class GotMindRepositoryImpl @Inject constructor(
    private val scoreDao: ScoreDao
) : GotMindRepository {
    override fun getTopScores(): Flow<List<Score>> =
        scoreDao.getTopScores().map { entities ->
            entities.map { Score(id = it.id, value = it.value, timestamp = it.timestamp) }
        }

    override suspend fun saveScore(value: Int) {
        val entity = ScoreEntity(value = value, timestamp = System.currentTimeMillis())
        scoreDao.insertScore(entity)
    }
}
