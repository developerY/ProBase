# AI-Powered Transaction Population

This plan outlines the implementation of full AI population for the Transaction form in Seaweed. It leverages Gemini (multimodal) to extract merchant, amount, category, date, and importance (WANT/NEED) from receipt images, providing a high-automation experience similar to Photodo.

## Proposed Changes

### [AI Vision Module]

#### [ReceiptEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/vision/src/main/java/com/zoewave/probase/features/ai/vision/receipt/ReceiptEngine.kt)

- Add `importance: String?` to `ReceiptDiagnosticResult` data class.

#### [CloudReceiptEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/vision/src/main/java/com/zoewave/probase/features/ai/vision/receipt/data/CloudReceiptEngine.kt)

- Update `GeminiReceiptDraft` to include `importance: String?`.
- Update the Gemini prompt to explicitly ask for `importance` with values `WANT` or `NEED`.
- Map the extracted `importance` to the `ReceiptDiagnosticResult`.

---

### [Transaction Feature]

#### [AddTransactionViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/AddTransactionViewModel.kt)

- Inject `CategoryRepository` to help map AI-suggested category names to internal IDs.
- Update `processReceiptImage` to:
    - Parse the `date` string from AI into a timestamp (if we decide to add date field to UI, otherwise just for description or future use).
    - Map AI `importance` string to `SpendingType` enum.
    - Implement category mapping: search for existing category by name, otherwise fallback to the name string.
    - Populate `amount`, `description`, `category`, and `importance` in the UI state.

---

### [Data Layer]

#### [CategoryRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/data/src/main/java/com/zoewave/probase/seaweed/data/CategoryRepository.kt)

- Add `suspend fun getCategoryByName(name: String): Category?`.

#### [CategoryRepositoryImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/data/src/main/java/com/zoewave/probase/seaweed/data/CategoryRepositoryImpl.kt)

- Implement `getCategoryByName` using the DAO.

#### [CategoryDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/database/src/main/java/com/zoewave/probase/seaweed/database/CategoryDao.kt)

- Add `@Query("SELECT * FROM categories WHERE name = :name COLLATE NOCASE LIMIT 1")` to find category by name.

## Verification Plan

### Automated Tests
- Run existing `ReceiptOrchestratorTest` if it exists.
- Add a unit test for `AddTransactionViewModel` verifying that `processReceiptImage` correctly updates the UI state from a mock `ReceiptDiagnosticResult`.

### Manual Verification
- Deploy the app and use the "Add Transaction" feature with a receipt image.
- Verify that fields (Amount, Description, Category, Importance) are correctly populated from the AI response.
- Inspect logs to confirm AI extraction details.
