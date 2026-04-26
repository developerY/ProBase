package com.zoewave.probase.seaweed.database

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards")
    fun getAllCards(): Flow<List<CreditCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCardEntity)

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteCard(id: String)
    
    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getCardById(id: String): CreditCardEntity?

    @Query("SELECT * FROM card_rewards WHERE cardId = :cardId")
    fun getRewardsForCard(cardId: String): Flow<List<CardRewardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: CardRewardEntity)
}
