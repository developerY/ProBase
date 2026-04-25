package com.zoewave.probase.seaweed.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.seaweed.model.BudgetTarget

@Entity(tableName = "budget_targets")
data class BudgetTargetEntity(
    @PrimaryKey val categoryName: String,
    val limitAmountCents: Long
)

fun BudgetTargetEntity.toDomain() = BudgetTarget(
    categoryName = categoryName,
    limitAmountCents = limitAmountCents
)

fun BudgetTarget.toEntity() = BudgetTargetEntity(
    categoryName = categoryName,
    limitAmountCents = limitAmountCents
)
