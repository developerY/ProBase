package com.zoewave.probase.seaweed.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.TypeConverters
import com.zoewave.probase.seaweed.database.converter.ExpenseConverters

@Database(
    entities = [
        TransactionEntity::class,
        RecurringExpenseEntity::class,
        UserSettingsEntity::class,
        BudgetTargetEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(ExpenseConverters::class)
@Suppress("ROOM_MISSING_CONSTRUCTED_BY")
abstract class SeaweedDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val recurringExpenseDao: RecurringExpenseDao
    abstract val userSettingsDao: UserSettingsDao
    abstract val budgetTargetDao: BudgetTargetDao

    companion object {
        const val DATABASE_NAME = "seaweed_db"
    }
}
