package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.model.BudgetTarget
import kotlinx.coroutines.flow.Flow

interface BudgetTargetRepository {
    fun getAllBudgets(): Flow<List<BudgetTarget>>
    fun getBudget(categoryName: String): Flow<BudgetTarget?>
    suspend fun saveBudget(budget: BudgetTarget)
    suspend fun deleteBudget(categoryName: String)
    fun getTotalBudgetedAmountCents(): Flow<Long>
}
