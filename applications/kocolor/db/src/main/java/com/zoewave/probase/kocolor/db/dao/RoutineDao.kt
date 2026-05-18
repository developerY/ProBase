package com.zoewave.probase.kocolor.db.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import com.zoewave.probase.kocolor.db.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM beauty_routines ORDER BY date DESC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM beauty_routines WHERE date >= :startOfDay AND date < :endOfDay")
    fun getRoutinesForDay(startOfDay: Long, endOfDay: Long): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM beauty_routines WHERE id = :id")
    fun getRoutineById(id: Long): Flow<RoutineEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Query("DELETE FROM beauty_routines WHERE id = :id")
    suspend fun deleteRoutine(id: Long)
}
