package com.zoewave.probase.seaweed.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.seaweed.model.BudgetTarget

@Entity(tableName = "budget_targets")
data class BudgetTargetEntity(
    @PrimaryKey val categoryName: String,
    val limitAmount: Double
)

fun BudgetTargetEntity.toDomain() = BudgetTarget(
    categoryName = categoryName,
    limitAmount = limitAmount
)

fun BudgetTarget.toEntity() = BudgetTargetEntity(
    categoryName = categoryName,
    limitAmount = limitAmount
)
