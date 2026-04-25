package com.zoewave.probase.seaweed.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses ORDER BY categoryId ASC")
    fun getAllExpenses(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getExpense(id: String): RecurringExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: RecurringExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<RecurringExpenseEntity>)

    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    suspend fun deleteExpense(id: String)

    @Query("SELECT COUNT(*) FROM recurring_expenses")
    suspend fun getCount(): Int
}
