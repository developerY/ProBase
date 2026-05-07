# Implementation Plan - KoColor Style Inventories

Implement a persistent inventory system for Face, Hair, Shoes, and Clothes images, allowing users and Gemini to reuse them for future style explorations.

## Proposed Changes

### Database Layer

#### [NEW] [InventoryItemEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/InventoryItemEntity.kt)
- Define `InventoryItemEntity`: `id`, `type` (Enum: FACE, HAIR, SHOES, CLOTHES), `uri`, `timestamp`.

#### [NEW] [InventoryDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/dao/InventoryDao.kt)
- Add methods to insert, delete, and fetch items by type.

#### [KoColorDatabase.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/KoColorDatabase.kt)
- Register `InventoryItemEntity` and `InventoryDao`.

---

### Data Layer

#### [FashionRepository.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/FashionRepository.kt)
- Add methods to manage `InventoryItem` domain models.

---

### Feature: Analyzer

#### [AnalyzerViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/ui/AnalyzerViewModel.kt)
- Update `saveAnalysis` to automatically add all captured/picked URIs to their respective inventories.

---

### Feature: Color (History & Vault)

#### [ColorScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/color/ui/ColorScreen.kt)
- Redesign the "Color" tab to include a `TabRow` with two main sections:
    1. **"Looks"**: The existing chronological history of analyses.
    2. **"Vault"**: A categorized view of the 4 inventories (Face, Hair, Shoes, Clothes).
- Implement a grid view for inventory items.

#### [ColorViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/color/ui/ColorViewModel.kt)
- Update `ColorUiState` to hold both `savedSuggestions` (History) and `inventoryItems`.
- Add filtering logic for the Vault view.

---

## Verification Plan

### Automated Tests
- Run `:applications:kocolor:apps:mobile:assembleDebug` to verify build.

### Manual Verification
1. Perform a style analysis with multiple images.
2. Save the analysis.
3. Navigate to the "Color" tab.
4. Verify the new analysis appears in the "Looks" history.
5. Switch to the "Vault" tab and verify that the captured images appear in their respective inventories (Face, Hair, Shoes, Clothes).
6. Verify that deleting an item from the Vault works (if implemented).
