package com.zoewave.probase.seaweed.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.seaweed.model.Transaction

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val category: String,
    val description: String,
    val date: Long
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amount = amount,
    category = category,
    description = description,
    date = date
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    category = category,
    description = description,
    date = date
)
