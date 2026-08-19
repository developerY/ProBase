package com.zoewave.probase.kocolor.db.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.zoewave.probase.kocolor.db.entity.ShoppingCartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingCartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: ShoppingCartItemEntity)

    @Delete
    suspend fun removeFromCart(item: ShoppingCartItemEntity)

    @Query("SELECT * FROM shopping_cart_items")
    fun observeCartItems(): Flow<List<ShoppingCartItemEntity>>

    @Query("SELECT productId FROM shopping_cart_items")
    fun getCartProductIdsFlow(): Flow<List<String>>

    @Query("SELECT * FROM shopping_cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItem(productId: String): ShoppingCartItemEntity?

    @Query("DELETE FROM shopping_cart_items WHERE productId = :productId")
    suspend fun deleteByProductId(productId: String)

    @Query("DELETE FROM shopping_cart_items")
    suspend fun clearCart()
}
