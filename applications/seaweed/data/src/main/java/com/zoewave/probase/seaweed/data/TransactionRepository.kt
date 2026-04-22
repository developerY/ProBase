package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getTransaction(id: String): Flow<Transaction?>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
    suspend fun deleteTransactionsByCategory(category: String)
    suspend fun updateTransactionsCategory(fromCategory: String, toCategory: String)
}
