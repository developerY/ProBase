# Implementation Plan - KoColor Style Brain (Inventory Module)

Create a standalone and reusable `:applications:kocolor:features:inventory` module that manages the four-pillar style inventory (Face, Hair, Garments, Footwear) with advanced metadata, AI tagging, and privacy-first local storage.

## User Review Required

- **Privacy Policy**: We will explicitly mark the module as "Local-Only Storage" with no server upload.
- **Segmentation**: We'll use MediaPipe for background removal (clipping) to create professional assets.

## Proposed Changes

### Build Configuration

#### [settings.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/settings.gradle.kts)
- Include `:applications:kocolor:features:inventory`.

#### [NEW] [features/inventory/build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/inventory/build.gradle.kts)
- Add dependencies for MediaPipe (Image Segmenter), Room, and encryption libraries.

---

### Data Models & Schema

#### [InventoryItemEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/InventoryItemEntity.kt)
- Expand schema to include `clippedUri` (background removed) and `metadata` (JSON blob for AI tags).
- Metadata structure for each pillar:
    - **Face**: skinTone (Hex), eyeColor, hairColor (at capture).
    - **Garments**: material, silhouette, styleCategory.
    - **Footwear**: heelHeight, material, primaryColor.

#### [NEW] [LookEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/LookEntity.kt)
- Represents a "Mix-and-Match" set (links 4 inventory items).

---

### Inventory Engine (Style Brain)

#### [NEW] [InventoryManager.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/inventory/src/main/java/com/zoewave/probase/kocolor/features/inventory/data/InventoryManager.kt)
- **Clipping Pipeline**: Uses MediaPipe to remove backgrounds from garment and shoe photos.
- **Tagging Pipeline**: Orchestrates Gemini calls to generate metadata tags from images.
- **Encryption**: Uses Android Keystore and EncryptedFile to store images securely.

---

### UI Components

#### [NEW] [InventoryScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/inventory/src/main/java/com/zoewave/probase/kocolor/features/inventory/ui/InventoryScreen.kt)
- Replaces the current "Vault" view in the Color tab.
- Categorized grid with filterable metadata (e.g., "Show all Boho shirts").

#### [NEW] [SimulatorScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/inventory/src/main/java/com/zoewave/probase/kocolor/features/inventory/ui/SimulatorScreen.kt)
- Drag-and-drop interface to select items from each pillar and get a virtual AI analysis.

---

## Verification Plan

### Automated Tests
- Unit tests for `MetadataParser` (ensures Gemini JSON tags are correctly mapped).
- Integration test for `InventoryManager` background removal (using local bitmap assets).

### Manual Verification
1. Capture a new item in the Analyzer.
2. Verify it appears in the Inventory with the background removed.
3. Check that AI tags (material, style) are correctly populated.
4. Use the Simulator to combine a historical Hair profile with a new Shirt and check the Gemini report.
