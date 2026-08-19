# Implementation Plan: KoColor Shopping Cart & Inventory Integration

This document outlines the architectural changes required to transform the "BUY" button into a state-aware shopping cart interaction that seamlessly transitions items into the user's scientific inventory.

## 1. Data Layer Enhancements

### [NEW] `ShoppingCartEntity.kt`
Create a new Room entity to persist the user's current selection.
```kotlin
@Entity(tableName = "shopping_cart_items")
data class ShoppingCartItemEntity(
    @PrimaryKey val productId: String,
    val packId: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

### [NEW] `ShoppingCartDao.kt`
Add standard CRUD operations and a Flow to observe cart changes.
```kotlin
@Dao
interface ShoppingCartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: ShoppingCartItemEntity)

    @Delete
    suspend fun removeFromCart(item: ShoppingCartItemEntity)

    @Query("SELECT * FROM shopping_cart_items")
    fun getCartItems(): Flow<List<ShoppingCartItemEntity>>

    @Query("DELETE FROM shopping_cart_items")
    suspend fun clearCart()
}
```

## 2. Repository Layer Integration

### `PackSyncRepository.kt` [MODIFY]
Inject the new `ShoppingCartDao` and expose a unified flow of "Owned" vs "In Cart" IDs.
- **New Method**: `toggleCartItem(productId, packId)`
- **New Method**: `purchaseItems(productIds)` -> Moves items from `shopping_cart_items` to `cosmetic_items`/`clothing_items`.

## 3. ViewModel Logic

### `PackPreviewViewModel.kt` [MODIFY]
Track the state of every item in the current pack relative to the user's database.

```kotlin
data class PackPreviewUiState(
    // ... existing fields ...
    val cartProductIds: Set<String> = emptySet(),
    val ownedProductIds: Set<String> = emptySet()
)

// Inside ViewModel
init {
    viewModelScope.launch {
        combine(
            shoppingCartDao.getCartItems(),
            inventoryDao.getAllProductIds()
        ) { cart, owned ->
            _uiState.update { it.copy(
                cartProductIds = cart.map { it.productId }.toSet(),
                ownedProductIds = owned.toSet()
            )}
        }.collect()
    }
}
```

## 4. UI Polish (ProductEditorialNotesDialog)

### State-Aware Buy Button [MODIFY]
The button will now dynamically shift based on the item's lifecycle:

| State | Label | Icon | Action |
| :--- | :--- | :--- | :--- |
| **Idle** | BUY | None | Add to Cart |
| **In Cart** | IN CART | `Icons.Default.Check` | Add to Inventory (Purchase) |
| **Owned** | OWNED | `Icons.Default.Inventory` | Disabled / View Inventory |

```kotlin
val buttonState = when {
    isOwned -> ButtonState.OWNED
    isInCart -> ButtonState.IN_CART
    else -> ButtonState.BUY
}

Button(
    onClick = {
        when(buttonState) {
            BUY -> onAddToCart()
            IN_CART -> onConfirmPurchase()
            OWNED -> {} // Already in inventory
        }
    },
    colors = if (isInCart) ButtonDefaults.success() else ButtonDefaults.primary()
) {
    if (isInCart) Icon(Icons.Default.Check, null)
    Text(buttonState.label)
}
```

## 5. Verification Plan
- **Unit Test**: Verify `toggleCartItem` correctly inserts and deletes from Room.
- **Integration Test**: Verify that "purchasing" an item removes it from the cart and it appears in the main "My Collection" hub.
- **Manual QA**: Open a product dialog, click BUY (Verify checkmark), click IN CART (Verify item added to local inventory).
