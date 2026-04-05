package com.zoewave.probase.seaweed.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinancialRepository @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val userSettingsRepository: UserSettingsRepository
) {
    /**
     * The absolute baseline income before any deductions.
     */
    fun getMonthlyIncome(): Flow<Double> =
        userSettingsRepository.getUserSettings().combine(userSettingsRepository.getUserSettings()) { settings, _ ->
            settings.monthlyIncome
        }

    /**
     * Total amortized monthly cost of all recurring bills.
     */
    fun getTotalMonthlyFixedCosts(): Flow<Double> =
        recurringExpenseRepository.getTotalMonthlyImpact()

    /**
     * Real Starting Balance = Income - Fixed Costs.
     * This is the "honest" starting point for the month.
     */
    fun getRealStartingBalance(): Flow<Double> =
        combine(getMonthlyIncome(), getTotalMonthlyFixedCosts()) { income, fixedCosts ->
            income - fixedCosts
        }

    /**
     * Total spent on daily transactions so far this month.
     */
    fun getMonthlyVariableSpending(): Flow<Double> =
        transactionRepository.getAllTransactions().combine(transactionRepository.getAllTransactions()) { transactions, _ ->
            val now = Calendar.getInstance()
            val startOfMonth = now.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            transactions.filter { it.date >= startOfMonth }.sumOf { it.amount }
        }

    /**
     * Real-time Money Remaining = Real Starting Balance - Variable Spending.
     */
    fun getFlexibleMoneyRemaining(): Flow<Double> =
        combine(getRealStartingBalance(), getMonthlyVariableSpending()) { realStarting, variableSpending ->
            realStarting - variableSpending
        }

    /**
     * Progress through the current month (0.0 to 1.0).
     */
    fun getMonthProgress(): Float {
        val now = Calendar.getInstance()
        val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = now.get(Calendar.DAY_OF_MONTH)
        return currentDay.toFloat() / daysInMonth
    }
}
