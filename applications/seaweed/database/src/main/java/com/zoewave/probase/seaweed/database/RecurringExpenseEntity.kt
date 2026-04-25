package com.zoewave.probase.seaweed.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.seaweed.model.ExpenseCategory
import com.zoewave.probase.seaweed.model.ExpenseFrequency
import com.zoewave.probase.seaweed.model.RecurringExpense
import com.zoewave.probase.seaweed.model.SpendingImportance

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amount: Double,
    val frequency: ExpenseFrequency,
    val category: ExpenseCategory,
    val isDefault: Boolean,
    val nextBillingDate: Long?,
    val importance: SpendingImportance = SpendingImportance.REQUIRED
)

fun RecurringExpenseEntity.toDomain() = RecurringExpense(
    id = id,
    name = name,
    amount = amount,
    frequency = frequency,
    category = category,
    isDefault = isDefault,
    nextBillingDate = nextBillingDate,
    importance = importance
)

fun RecurringExpense.toEntity() = RecurringExpenseEntity(
    id = id,
    name = name,
    amount = amount,
    frequency = frequency,
    category = category,
    isDefault = isDefault,
    nextBillingDate = nextBillingDate,
    importance = importance
)
