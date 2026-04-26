package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.database.BudgetTargetDao
import com.zoewave.probase.seaweed.database.toDomain
import com.zoewave.probase.seaweed.database.toEntity
import com.zoewave.probase.seaweed.model.BudgetTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetTargetRepositoryImpl @Inject constructor(
    private val dao: BudgetTargetDao
) : BudgetTargetRepository {

    override fun getAllBudgets(): Flow<List<BudgetTarget>> =
        dao.getAllBudgets().map { entities -> entities.map { it.toDomain() } }

    override fun getBudget(categoryId: String): Flow<BudgetTarget?> =
        dao.getBudget(categoryId).map { it?.toDomain() }

    override suspend fun saveBudget(budget: BudgetTarget) {
        dao.saveBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(categoryId: String) {
        dao.deleteBudget(categoryId)
    }

    override fun getTotalBudgetedAmountCents(): Flow<Long> =
        getAllBudgets().map { budgets -> budgets.sumOf { it.limitAmountCents } }
}
