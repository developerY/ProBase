package com.zoewave.probase.seaweed.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.zoewave.probase.seaweed.model.CardReward
import com.zoewave.probase.seaweed.model.CreditCard

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey val id: String,
    val name: String,
    val bankName: String,
    val lastFourDigits: String,
    val creditLimitCents: Long,
    val interestRateApr: Double,
    val statementDay: Int,
    val dueDay: Int,
    val isFrozen: Boolean,
    val colorHex: String?,
    val cardPurposeContext: String?,
    val dailyTransactionLimitCents: Long?,
    val maxSingleTransactionCents: Long?
)

@Entity(tableName = "card_rewards", primaryKeys = ["cardId", "categoryId"])
data class CardRewardEntity(
    val cardId: String,
    val categoryId: String,
    val multiplier: Double,
    val rewardType: String?
)

fun CreditCardEntity.toDomain() = CreditCard(
    id = id,
    name = name,
    bankName = bankName,
    lastFourDigits = lastFourDigits,
    creditLimitCents = creditLimitCents,
    interestRateApr = interestRateApr,
    statementDay = statementDay,
    dueDay = dueDay,
    isFrozen = isFrozen,
    colorHex = colorHex,
    cardPurposeContext = cardPurposeContext,
    dailyTransactionLimitCents = dailyTransactionLimitCents,
    maxSingleTransactionCents = maxSingleTransactionCents
)

fun CreditCard.toEntity() = CreditCardEntity(
    id = id,
    name = name,
    bankName = bankName,
    lastFourDigits = lastFourDigits,
    creditLimitCents = creditLimitCents,
    interestRateApr = interestRateApr,
    statementDay = statementDay,
    dueDay = dueDay,
    isFrozen = isFrozen,
    colorHex = colorHex,
    cardPurposeContext = cardPurposeContext,
    dailyTransactionLimitCents = dailyTransactionLimitCents,
    maxSingleTransactionCents = maxSingleTransactionCents
)

fun CardRewardEntity.toDomain() = CardReward(
    cardId = cardId,
    categoryId = categoryId,
    multiplier = multiplier,
    rewardType = rewardType
)

fun CardReward.toEntity() = CardRewardEntity(
    cardId = cardId,
    categoryId = categoryId,
    multiplier = multiplier,
    rewardType = rewardType
)
