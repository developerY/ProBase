package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.database.RecurringExpenseDao
import com.zoewave.probase.seaweed.database.toDomain
import com.zoewave.probase.seaweed.database.toEntity
import com.zoewave.probase.seaweed.model.ExpenseFrequency
import com.zoewave.probase.seaweed.model.RecurringExpense
import com.zoewave.probase.seaweed.model.SpendingType
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

    override fun getTotalMonthlyImpactCents(): Flow<Long> =
        getAllExpenses().map { expenses -> expenses.sumOf { it.monthlyImpactCents } }

    override suspend fun initializeDefaultExpenses() {
        if (dao.getCount() == 0) {
            val defaults = listOf(
                RecurringExpense(UUID.randomUUID().toString(), "Rent/Mortgage", 0L, ExpenseFrequency.MONTHLY, "housing_id", true, defaultType = SpendingType.NEED),
                RecurringExpense(UUID.randomUUID().toString(), "Electricity", 0L, ExpenseFrequency.MONTHLY, "utilities_id", true, defaultType = SpendingType.NEED),
                RecurringExpense(UUID.randomUUID().toString(), "Mobile Phone", 0L, ExpenseFrequency.MONTHLY, "comm_id", true, defaultType = SpendingType.NEED),
                RecurringExpense(UUID.randomUUID().toString(), "Internet", 0L, ExpenseFrequency.MONTHLY, "comm_id", true, defaultType = SpendingType.NEED),
                RecurringExpense(UUID.randomUUID().toString(), "Netflix", 0L, ExpenseFrequency.MONTHLY, "entertainment_id", true, defaultType = SpendingType.WANT),
                RecurringExpense(UUID.randomUUID().toString(), "Spotify", 0L, ExpenseFrequency.MONTHLY, "entertainment_id", true, defaultType = SpendingType.WANT),
                RecurringExpense(UUID.randomUUID().toString(), "Gym", 0L, ExpenseFrequency.MONTHLY, "sub_id", true, defaultType = SpendingType.WANT)
            )
            dao.insertExpenses(defaults.map { it.toEntity() })
        }
    }
}
