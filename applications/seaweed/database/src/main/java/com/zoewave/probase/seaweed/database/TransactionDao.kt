package com.zoewave.probase.seaweed.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransaction(id: String): Flow<TransactionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("DELETE FROM transactions WHERE categoryId = :categoryId")
    suspend fun deleteTransactionsByCategory(categoryId: String)

    @Query("UPDATE transactions SET categoryId = :toCategory WHERE categoryId = :fromCategory")
    suspend fun updateTransactionsCategory(fromCategory: String, toCategory: String)
}
