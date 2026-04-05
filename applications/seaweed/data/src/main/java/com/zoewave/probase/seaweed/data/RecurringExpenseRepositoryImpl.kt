package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.database.RecurringExpenseDao
import com.zoewave.probase.seaweed.database.toDomain
import com.zoewave.probase.seaweed.database.toEntity
import com.zoewave.probase.seaweed.model.ExpenseCategory
import com.zoewave.probase.seaweed.model.ExpenseFrequency
import com.zoewave.probase.seaweed.model.RecurringExpense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class RecurringExpenseRepositoryImpl @Inject constructor(
    private val dao: RecurringExpenseDao
) : RecurringExpenseRepository {

    override fun getAllExpenses(): Flow<List<RecurringExpense>> =
        dao.getAllExpenses().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveExpense(expense: RecurringExpense) {
        dao.insertExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(id: String) {
        dao.deleteExpense(id)
    }

    override fun getTotalMonthlyImpact(): Flow<Double> =
        getAllExpenses().map { expenses -> expenses.sumOf { it.monthlyImpact } }

    override suspend fun initializeDefaultExpenses() {
        if (dao.getCount() == 0) {
            val defaults = listOf(
                RecurringExpense(UUID.randomUUID().toString(), "Rent/Mortgage", 0.0, ExpenseFrequency.MONTHLY, ExpenseCategory.HOUSING, true),
                RecurringExpense(UUID.randomUUID().toString(), "Home Insurance", 0.0, ExpenseFrequency.YEARLY, ExpenseCategory.HOUSING, true),
                RecurringExpense(UUID.randomUUID().toString(), "Electricity", 0.0, ExpenseFrequency.MONTHLY, ExpenseCategory.UTILITIES, true),
                RecurringExpense(UUID.randomUUID().toString(), "Water", 0.0, ExpenseFrequency.MONTHLY, ExpenseCategory.UTILITIES, true),
                RecurringExpense(UUID.randomUUID().toString(), "Gas/Heating", 0.0, ExpenseFrequency.MONTHLY, ExpenseCategory.UTILITIES, true),
                RecurringExpense(UUID.randomUUID().toString(), "Mobile Phone", 0.0, ExpenseFrequency.MONTHLY, ExpenseCategory.COMMUNICATION, true),
                RecurringExpense(UUID.randomUUID().toString(), "Internet", 0.0, ExpenseFrequency.MONTHLY, ExpenseCategory.COMMUNICATION, true),
                RecurringExpense(UUID.randomUUID().toString(), "Car Payment", 0.0, ExpenseFrequency.MONTHLY, ExpenseCategory.TRANSPORTATION, true),
                RecurringExpense(UUID.randomUUID().toString(), "Auto Insurance", 0.0, ExpenseFrequency.YEARLY, ExpenseCategory.TRANSPORTATION, true),
                RecurringExpense(UUID.randomUUID().toString(), "Netflix", 0.0, ExpenseFrequency.MONTHLY, ExpenseCategory.SUBSCRIPTIONS, true),
                RecurringExpense(UUID.randomUUID().toString(), "Spotify", 0.0, ExpenseFrequency.MONTHLY, ExpenseCategory.SUBSCRIPTIONS, true),
                RecurringExpense(UUID.randomUUID().toString(), "Gym", 0.0, ExpenseFrequency.MONTHLY, ExpenseCategory.SUBSCRIPTIONS, true)
            )
            dao.insertExpenses(defaults.map { it.toEntity() })
        }
    }
}
