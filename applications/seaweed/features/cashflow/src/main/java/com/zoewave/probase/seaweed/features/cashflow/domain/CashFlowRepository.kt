package com.zoewave.probase.seaweed.features.cashflow.domain

import kotlinx.coroutines.flow.Flow

interface CashFlowRepository {
    fun getCurrentMonthSummary(): Flow<CashFlowSummary>
    fun getHistoricalTrends(): Flow<CashFlowTrend>
}
