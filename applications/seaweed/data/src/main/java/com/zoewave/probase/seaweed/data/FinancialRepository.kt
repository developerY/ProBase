package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.model.CategoryOverview
import com.zoewave.probase.seaweed.model.FinancialProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
            //recurringExpenseRepository.getTotalMonthlyImpact(),
            recurringExpenseRepository.getTotalMonthlyImpactCents(),
            transactionRepository.getAllTransactions(),
            //budgetTargetRepository.getTotalBudgetedAmountCents()
            budgetTargetRepository.getTotalBudgetedAmountCents(),
            getCategoryOverviews()
        ) { settings, fixedCosts, transactions, budgeted, categories ->
            val income = (settings.monthlyIncome * 100).toLong()
            val fixed = (fixedCosts * 100).toLong()
            val realStarting = income - fixed
            
            val now = Calendar.getInstance()
            val startOfMonth = now.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val monthlyVariable = transactions
                .filter { it.timestamp >= startOfMonth && it.amountCents < 0 }
                .sumOf { it.amountCents }
                .absoluteValue

            val budgetedCents = (budgeted * 100).toLong()

            FinancialProfile(
                monthlyIncomeCents = income,
                totalFixedCostsCents = fixed,
                realStartingBalanceCents = realStarting,
                monthlyVariableSpendingCents = monthlyVariable,
                flexibleMoneyRemainingCents = realStarting - monthlyVariable,
                totalBudgetedAmountCents = budgetedCents,
                unallocatedMoneyCents = realStarting - budgetedCents,
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

            val monthlyTransactions = transactions.filter { it.timestamp >= startOfMonth && it.amountCents < 0 }
            val budgetsMap = budgets.associateBy { it.categoryName }
            
            // This is still using category strings from transactions, need to bridge with categoryId later
            val allCategoryNames = (monthlyTransactions.map { it.categoryId } + budgets.map { it.categoryName }).distinct()

            allCategoryNames.map { categoryName ->
                val spent = monthlyTransactions.filter { it.categoryId == categoryName }.sumOf { it.amountCents }.absoluteValue
                val count = monthlyTransactions.count { it.categoryId == categoryName }
                val limit = budgetsMap[categoryName]?.limitAmountCents
                
                CategoryOverview(
                    name = categoryName,
                    totalAmountCents = spent,
                    transactionCount = count,
                    limitAmountCents = limit,
                    remainingAmountCents = limit?.let { it - spent },
                    progressPercentage = limit?.let { (spent.toFloat() / it).coerceIn(0f, 2f) } ?: 0f
                )
            }.sortedByDescending { it.totalAmountCents }
        }

    fun getMonthProgress(): Float {
        val now = Calendar.getInstance()
        val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = now.get(Calendar.DAY_OF_MONTH)
        return currentDay.toFloat() / daysInMonth
    }
}
