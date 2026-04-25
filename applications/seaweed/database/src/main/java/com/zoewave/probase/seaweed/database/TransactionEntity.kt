package com.zoewave.probase.seaweed.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.seaweed.model.SpendingType
import com.zoewave.probase.seaweed.model.Transaction

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amountCents: Long,
    val categoryId: String,
    val description: String,
    val timestamp: Long,
    val receiptUri: String? = null,
    val defaultType: SpendingType,
    val userOverrideType: SpendingType? = null,
    val recurringId: String? = null
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amountCents = amountCents,
    categoryId = categoryId,
    description = description,
    timestamp = timestamp,
    receiptUri = receiptUri,
    defaultType = defaultType,
    userOverrideType = userOverrideType,
    recurringId = recurringId
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amountCents = amountCents,
    categoryId = categoryId,
    description = description,
    timestamp = timestamp,
    receiptUri = receiptUri,
    defaultType = defaultType,
    userOverrideType = userOverrideType,
    recurringId = recurringId
)
