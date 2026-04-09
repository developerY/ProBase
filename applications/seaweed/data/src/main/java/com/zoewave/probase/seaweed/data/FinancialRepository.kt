package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.FinancialProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

@Singleton
class FinancialRepository @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val budgetTargetRepository: BudgetTargetRepository
) {
    fun getFinancialProfile(): Flow<FinancialProfile> =
        combine(
            userSettingsRepository.getUserSettings(),
            recurringExpenseRepository.getTotalMonthlyImpact(),
            transactionRepository.getAllTransactions(),
            budgetTargetRepository.getTotalBudgetedAmount(),
            getCategoryOverviews()
        ) { settings, fixedCosts, transactions, budgeted, categories ->
            val income = settings.monthlyIncome
            val realStarting = income - fixedCosts
            
            val now = Calendar.getInstance()
            val startOfMonth = now.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val monthlyVariable = transactions
                .filter { it.date >= startOfMonth && it.amount < 0 }
                .sumOf { it.amount }
                .absoluteValue

            FinancialProfile(
                monthlyIncome = income,
                totalFixedCosts = fixedCosts,
                realStartingBalance = realStarting,
                monthlyVariableSpending = monthlyVariable,
                flexibleMoneyRemaining = realStarting - monthlyVariable,
                totalBudgetedAmount = budgeted,
                unallocatedMoney = realStarting - budgeted,
                categoryOverviews = categories,
                monthProgress = getMonthProgress()
            )
        }

    fun getCategoryOverviews(): Flow<List<CategoryOverview>> =
        combine(
            transactionRepository.getAllTransactions(),
            budgetTargetRepository.getAllBudgets()
        ) { transactions, budgets ->
            val now = Calendar.getInstance()
            val startOfMonth = now.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val monthlyTransactions = transactions.filter { it.date >= startOfMonth && it.amount < 0 }
            val budgetsMap = budgets.associateBy { it.categoryName }
            
            val allCategoryNames = (monthlyTransactions.map { it.category } + budgets.map { it.categoryName }).distinct()

            allCategoryNames.map { categoryName ->
                val spent = monthlyTransactions.filter { it.category == categoryName }.sumOf { it.amount }.absoluteValue
                val count = monthlyTransactions.count { it.category == categoryName }
                val limit = budgetsMap[categoryName]?.limitAmount
                
                CategoryOverview(
                    name = categoryName,
                    totalAmount = spent,
                    transactionCount = count,
                    limitAmount = limit,
                    remainingAmount = limit?.let { it - spent },
                    progressPercentage = limit?.let { (spent / it).toFloat() } ?: 0f
                )
            }.sortedByDescending { it.totalAmount }
        }

    fun getMonthProgress(): Float {
        val now = Calendar.getInstance()
        val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = now.get(Calendar.DAY_OF_MONTH)
        return currentDay.toFloat() / daysInMonth
    }
    
    // Helper methods for individual metrics
    fun getMonthlyIncome(): Flow<Double> = userSettingsRepository.getUserSettings().map { it.monthlyIncome }
    fun getTotalMonthlyFixedCosts(): Flow<Double> = recurringExpenseRepository.getTotalMonthlyImpact()
    fun getFlexibleMoneyRemaining(): Flow<Double> = getFinancialProfile().map { it.flexibleMoneyRemaining }
}
