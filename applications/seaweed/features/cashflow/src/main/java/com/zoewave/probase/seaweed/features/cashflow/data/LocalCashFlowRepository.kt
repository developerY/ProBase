package com.zoewave.probase.seaweed.features.cashflow.data

import com.zoewave.probase.seaweed.data.TransactionRepository
import com.zoewave.probase.seaweed.data.UserSettingsRepository
import com.zoewave.probase.seaweed.features.cashflow.domain.CashFlowEngine
import com.zoewave.probase.seaweed.features.cashflow.domain.CashFlowRepository
import com.zoewave.probase.seaweed.features.cashflow.domain.CashFlowSummary
import com.zoewave.probase.seaweed.features.cashflow.domain.CashFlowTrend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime
import javax.inject.Inject

class LocalCashFlowRepository @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val engine: CashFlowEngine
) : CashFlowRepository {

    override fun getCurrentMonthSummary(): Flow<CashFlowSummary> {
        return combine(
            transactionRepository.getAllTransactions(),
            userSettingsRepository.getUserSettings()
        ) { transactions, settings ->
            val now = LocalDateTime.now()
            val currentMonthTransactions = transactions.filter { 
                val dt = Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
                dt.monthNumber == now.monthValue && dt.year == now.year 
            }
            
            val incomeCents = (settings.monthlyIncome * 100).toLong()
            val expenseCents = currentMonthTransactions.filter { it.amountCents < 0 }.sumOf { -it.amountCents }
            
            engine.calculateSummary(
                incomeCents = incomeCents,
                expensesCents = expenseCents,
                monthDays = now.month.length(now.toLocalDate().isLeapYear),
                currentDay = now.dayOfMonth
            )
        }
    }

    override fun getHistoricalTrends(): Flow<CashFlowTrend> {
        // Placeholder for now
        return combine(getCurrentMonthSummary()) { summaries ->
            CashFlowTrend(summaries.toList())
        }
    }
}
