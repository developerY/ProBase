package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.model.RecurringExpense
import kotlinx.coroutines.flow.Flow

interface RecurringExpenseRepository {
    fun getAllExpenses(): Flow<List<RecurringExpense>>
    suspend fun saveExpense(expense: RecurringExpense)
    suspend fun deleteExpense(id: String)
    fun getTotalMonthlyImpact(): Flow<Double>
    suspend fun initializeDefaultExpenses()
}
