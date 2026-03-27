package com.zoewave.probase.seaweed.data

import com.zoewave.probase.seaweed.database.TransactionDao
import com.zoewave.probase.seaweed.database.toDomain
import com.zoewave.probase.seaweed.database.toEntity
import com.zoewave.probase.seaweed.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {
    override fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: String) {
        transactionDao.deleteTransaction(id)
    }
}
