package com.zoewave.probase.core.model

interface FinancialContextProvider {
    suspend fun getFinancialContext(): String?
}
