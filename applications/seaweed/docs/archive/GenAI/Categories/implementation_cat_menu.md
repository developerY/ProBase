# Category Management: Add & Combine

Enhance the Categories screen with the ability to add new categories and combine existing ones. Combining categories will merge all transactions from one category into another.

## Proposed Changes

### [Data Module]

#### [TransactionDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/database/src/main/java/com/zoewave/probase/seaweed/database/TransactionDao.kt)

- Add `@Query("UPDATE transactions SET category = :toCategory WHERE category = :fromCategory")` to handle transaction merging.

#### [TransactionRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/data/src/main/java/com/zoewave/probase/seaweed/data/TransactionRepository.kt)

- Add `updateCategory(fromCategory: String, toCategory: String)` method.

#### [TransactionRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/data/src/main/java/com/zoewave/probase/seaweed/data/TransactionRepositoryImpl.kt)

- Implement `updateCategory`.

---

### [Home Feature]

#### [HomeUiState.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/HomeUiState.kt)

- Add `AddCategory(name: String)` and `CombineCategories(from: String, to: String)` events.
- Remove `Refresh` and `AddRandomTransaction` (as requested).

#### [HomeViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/HomeViewModel.kt)

- Implement `AddCategory`:
    - This just needs to save a `BudgetTarget` with the new category name (limit 0) to ensure the category appears in lists.
- Implement `CombineCategories`:
    - Call `transactionRepository.updateCategory(from, to)`.
    - Delete the `BudgetTarget` for the `from` category.

#### [CategoryGridRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/CategoryGridRoute.kt)

- Update FAB menu:
    - Remove "Add Random Data" and "Refresh".
    - Add "Add Category" and "Combine Categories".
- Implement `AddCategoryDialog`:
    - Simple text input for the new category name.
- Implement `CombineCategoriesBottomSheet`:
    - Use `ModalBottomSheet`.
    - Provide two pickers (Dropdowns or similar) to select the source and destination categories.
    - Confirm button to trigger the merge.

## Verification Plan

### Manual Verification
- Navigate to **All Categories**.
- Click FAB > **Add Category**.
- Verify new category appears in the grid.
- Click FAB > **Combine Categories**.
- Select "Coffee" to merge into "Food".
- Verify "Coffee" category disappears.
- Verify "Food" category total now includes the previous "Coffee" transactions.
- Check **Transactions** tab to confirm all "Coffee" items are now labeled "Food".
