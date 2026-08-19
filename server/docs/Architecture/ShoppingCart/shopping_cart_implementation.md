# Implementation Plan: KoColor Shopping Cart & Inventory Integration

This document outlines the architectural changes required to transform the "BUY" button into a state-aware shopping cart interaction that seamlessly transitions items into the user's scientific inventory via atomic database transactions.

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

    @Query("SELECT productId FROM shopping_cart_items")
    fun getCartProductIds(): Flow<List<String>>

    @Query("DELETE FROM shopping_cart_items WHERE productId = :productId")
    suspend fun deleteByProductId(productId: String)
}
```

### [MODIFY] `InventoryDao.kt`
Add an atomic transaction to handle the "Checkout" logic, ensuring data consistency if the app crashes mid-purchase.
```kotlin
@Transaction
suspend fun purchaseItem(
    cosmetic: CosmeticItemEntity?,
    clothing: ClothingItemEntity?,
    productId: String
) {
    // 1. Add to appropriate inventory table
    cosmetic?.let { insertCosmetic(it) }
    clothing?.let { insertClothing(it) }

    // 2. Remove from cart atomically
    shoppingCartDao.deleteByProductId(productId)
}
```

## 2. Repository Layer Integration

### `PackSyncRepository.kt` [MODIFY]
Acts as the single source of truth for the ViewModel. Direct DAO access in the ViewModel is prohibited to maintain Clean Architecture.
- **New Flow**: `val cartProductIds: Flow<Set<String>>` (Mapping from `ShoppingCartDao`)
- **New Method**: `addToCart(productId, packId)`
- **New Method**: `purchaseInstant(productId)` -> Triggers the atomic DAO transaction.

## 3. ViewModel Logic

### `PackPreviewViewModel.kt` [MODIFY]
Uses `stateIn` to convert cold repository flows into a hot, lifecycle-aware UI state.

```kotlin
val uiState: StateFlow<PackPreviewUiState> = combine(
    repository.getPackItemsFlow(), // Cold flow of items in current pack
    syncRepository.cartProductIds, // Flow from ShoppingCartRepository
    syncRepository.ownedProductIds // Flow from InventoryRepository
) { items, cartIds, ownedIds ->
    baseState.copy(
        items = items,
        cartProductIds = cartIds,
        ownedProductIds = ownedIds,
        isLoading = false
    )
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = PackPreviewUiState(isLoading = true)
)
```

## 4. UI Polish (ProductEditorialNotesDialog)

### State-Aware Buy Button [MODIFY]
The button implements an **"Instant Buy"** pattern. Clicking "IN CART" immediately finalizes the item into the inventory without navigating to a separate screen.

| State | Label | Icon | Action |
| :--- | :--- | :--- | :--- |
| **Idle** | BUY | None | `onAddToCart()` |
| **In Cart** | IN CART | `Icons.Default.Check` | `onConfirmPurchase()` |
| **Owned** | OWNED | `Icons.Default.Inventory` | Disabled |

> [!NOTE]
> The "IN CART" state uses a secondary high-contrast color (e.g., Material Green) to signal that the item is staged and ready for immediate deployment to the inventory.

## 5. Verification Plan

### Automated Tests
- **Atomic Transaction Test**: Unit test `purchaseItem` in the DAO. Force a simulated crash/exception after the inventory insertion to verify the cart item is *not* deleted (ensuring rollback).
- **Repository Mapping**: Verify `cartProductIds` flow correctly updates when the underlying table changes.

### Manual Verification
1. **The Handshake**: Open a product, click BUY. Verify the button immediately shifts to "IN CART" with a checkmark.
2. **The Purchase**: Click IN CART. Verify the dialog closes (or shifts to OWNED) and the item appears in the main "Collection" Hub.
3. **App Restart**: Add an item to cart, kill the app, and reopen. Verify the item remains "IN CART".
