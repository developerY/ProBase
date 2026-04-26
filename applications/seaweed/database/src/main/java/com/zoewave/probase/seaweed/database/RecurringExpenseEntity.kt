package com.zoewave.probase.seaweed.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.seaweed.model.ExpenseFrequency
import com.zoewave.probase.seaweed.model.RecurringExpense
import com.zoewave.probase.seaweed.model.SpendingType

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val averageAmountCents: Long,
    val frequency: ExpenseFrequency,
    val categoryId: String,
    val isDefault: Boolean,
    val nextBillingDate: Long?,
    val defaultType: SpendingType = SpendingType.NEED,
    val cardId: String? = null
)

fun RecurringExpenseEntity.toDomain() = RecurringExpense(
    id = id,
    name = name,
    averageAmountCents = averageAmountCents,
    frequency = frequency,
    categoryId = categoryId,
    isDefault = isDefault,
    nextBillingDate = nextBillingDate,
    defaultType = defaultType,
    cardId = cardId
)

fun RecurringExpense.toEntity() = RecurringExpenseEntity(
    id = id,
    name = name,
    averageAmountCents = averageAmountCents,
    frequency = frequency,
    categoryId = categoryId,
    isDefault = isDefault,
    nextBillingDate = nextBillingDate,
    defaultType = defaultType,
    cardId = cardId
)
