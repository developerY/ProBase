# Categories Screen Enhancements

Improve the Categories screen with interactive management features, synchronized data, and a modern UI/UX.

## Proposed Changes

### [Home Feature]

#### [HomeUiState.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/HomeUiState.kt)

- Add `DeleteCategory` to `HomeUiEvent`.
- Add `Refresh` to `HomeUiEvent`.

#### [HomeViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/HomeViewModel.kt)

- Implement `DeleteCategory` logic:
    - This will need to delete all transactions associated with the category from `TransactionRepository`.
    - Also delete the `BudgetTarget` for that category from `BudgetTargetRepository`.
- Ensure data is refreshed correctly using the `financialRepository`.

#### [CategoryGridRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/CategoryGridRoute.kt)

- Update `CategoryGridScreen`:
    - Add a `FloatingActionButton` (FAB) with a menu (using `Box` and `DropdownMenu`).
    - Menu options: "Add Transaction" (linked to data generator for now) and "Refresh".
    - Update `CategoryQuickJumpCard` usage to support a "Long Press" or a small "Trash" icon for deletion.
    - Implement a `DeleteCategoryDialog` (AlertDialog) to confirm deletion.
    - Ensure the layout matches the provided visual reference (consistent spacing and colors).

---

### [Seaweed Data]

#### [TransactionRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/data/src/main/java/com/zoewave/probase/seaweed/data/TransactionRepository.kt)

- Add `deleteTransactionsByCategory(category: String)` method.

#### [TransactionRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/data/src/main/java/com/zoewave/probase/seaweed/data/TransactionRepositoryImpl.kt)

- Implement `deleteTransactionsByCategory`.

## Verification Plan

### Manual Verification
- Navigate to the **All Categories** screen.
- Verify the **FAB** is present and the menu opens.
- Click a category card to navigate to its transactions.
- Long-press or click the trash icon on a category card.
- Confirm the **Delete Alert** appears.
- Delete a category and verify that all associated transactions and budgets are removed.
- Verify that the totals on the main page and budget page reflect the changes.
