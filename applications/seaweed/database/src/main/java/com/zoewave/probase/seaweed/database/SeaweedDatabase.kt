package com.zoewave.probase.seaweed.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.ColumnTypeConverters
import com.zoewave.probase.seaweed.database.converter.ExpenseConverters

@Database(
    entities = [
        TransactionEntity::class,
        RecurringExpenseEntity::class,
        UserSettingsEntity::class,
        BudgetTargetEntity::class,
        CategoryEntity::class,
        CreditCardEntity::class,
        CardRewardEntity::class
    ],
    version = 1,
    exportSchema = false
)
@ColumnTypeConverters(ExpenseConverters::class)
@Suppress("ROOM_MISSING_CONSTRUCTED_BY")
abstract class SeaweedDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val recurringExpenseDao: RecurringExpenseDao
    abstract val userSettingsDao: UserSettingsDao
    abstract val budgetTargetDao: BudgetTargetDao
    abstract val categoryDao: CategoryDao
    abstract val creditCardDao: CreditCardDao

    companion object {
        const val DATABASE_NAME = "seaweed_db"
    }
}
