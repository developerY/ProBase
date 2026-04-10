package com.zoewave.probase.applications.photodo.db.repo.time

import com.zoewave.probase.applications.photodo.db.TimeTrackingDao
import com.zoewave.probase.applications.photodo.db.entity.time.TimeBudgetEntity
import com.zoewave.probase.applications.photodo.db.entity.time.TimeLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of [TimeTrackingRepository].
 */
class TimeTrackingRepositoryImpl @Inject constructor(
    private val timeTrackingDao: TimeTrackingDao
) : TimeTrackingRepository {

    override suspend fun saveTimeLog(log: TimeLogEntity): Long = withContext(Dispatchers.IO) {
        timeTrackingDao.upsertTimeLog(log)
    }

    override fun getTimeLogsForTask(taskId: Long): Flow<List<TimeLogEntity>> {
        return timeTrackingDao.getTimeLogsForTask(taskId)
    }

    override suspend fun deleteTimeLog(log: TimeLogEntity) = withContext(Dispatchers.IO) {
        timeTrackingDao.deleteTimeLog(log)
    }

    override suspend fun saveTimeBudget(budget: TimeBudgetEntity): Long = withContext(Dispatchers.IO) {
        timeTrackingDao.upsertTimeBudget(budget)
    }

    override fun getTimeBudgetForCategory(categoryId: Long): Flow<TimeBudgetEntity?> {
        return timeTrackingDao.getTimeBudgetForCategory(categoryId)
    }

    override fun getAllTimeBudgets(): Flow<List<TimeBudgetEntity>> {
        return timeTrackingDao.getAllTimeBudgets()
    }

    override suspend fun deleteTimeBudget(budget: TimeBudgetEntity) = withContext(Dispatchers.IO) {
        timeTrackingDao.deleteTimeBudget(budget)
    }
}
