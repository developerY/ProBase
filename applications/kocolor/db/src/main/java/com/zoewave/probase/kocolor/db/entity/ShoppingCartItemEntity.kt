package com.zoewave.probase.kocolor.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Represents an item in the user's shopping cart before official acquisition.
 */
@Entity(tableName = "shopping_cart_items")
data class ShoppingCartItemEntity(
    @PrimaryKey val productId: String, // globally unique KCPS product ID
    val packId: String,               // provenance of the acquisition
    val timestamp: Long = System.currentTimeMillis()
)
