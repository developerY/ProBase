package com.zoewave.probase.seaweed.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.seaweed.model.BudgetTarget

@Entity(tableName = "budget_targets")
data class BudgetTargetEntity(
    @PrimaryKey val categoryId: String,
    val limitAmountCents: Long
)

fun BudgetTargetEntity.toDomain() = BudgetTarget(
    categoryId = categoryId,
    limitAmountCents = limitAmountCents
)

fun BudgetTarget.toEntity() = BudgetTargetEntity(
    categoryId = categoryId,
    limitAmountCents = limitAmountCents
)
