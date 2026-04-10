package com.zoewave.probase.photodo.features.timebudgeting.data.repo

import com.zoewave.probase.photodo.features.timebudgeting.data.db.dao.TimeBudgetDao
import com.zoewave.probase.photodo.features.timebudgeting.data.db.entity.TimeBudgetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class TimeBudgetRepository @Inject constructor(
    private val timeBudgetDao: TimeBudgetDao
) {
    suspend fun saveTimeBudget(budget: TimeBudgetEntity): Long = withContext(Dispatchers.IO) {
        timeBudgetDao.upsertTimeBudget(budget)
    }

    fun getTimeBudgetForCategory(categoryId: Long): Flow<TimeBudgetEntity?> {
        return timeBudgetDao.getTimeBudgetForCategory(categoryId)
    }

    fun getAllTimeBudgets(): Flow<List<TimeBudgetEntity>> {
        return timeBudgetDao.getAllTimeBudgets()
    }

    suspend fun deleteTimeBudget(budget: TimeBudgetEntity) = withContext(Dispatchers.IO) {
        timeBudgetDao.deleteTimeBudget(budget)
    }
}
