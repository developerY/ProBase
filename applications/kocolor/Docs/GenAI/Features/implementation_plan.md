# KoColor Core Features Implementation Plan

Implementing the foundational pillars of the KoColor Digital Beauty Ecosystem based on the `Features.md` architectural design. This plan focuses on professional inventory, chronobiological routines, ingredient intelligence, and holistic health integration.

## User Review Required

> [!IMPORTANT]
> - **Ingredient Database**: Initially, ingredient analysis will rely on AI-assisted decoding and local taxonomies. Integration with specific external APIs (EWG/SkinSAFE) will be planned as a follow-up.
> - **Health Integration**: Core logic for "Gut-Skin Axis" will be implemented, but real-time wearable syncing (Google Fit) will be added in the next phase.

## Proposed Changes

### [Core Model] Data Layer Expansion
Expanding the data models to support professional logistics and biological rhythms.

#### [CosmeticItem.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/model/src/main/java/com/zoewave/probase/kocolor/model/CosmeticItem.kt)
- Add professional inventory fields: `batchCode`, `openedDate`, `paoMonths`, `expiryDate`, `price`, `volume`.
- Add state properties: `isOpened`, `isFinished`, `isArchived`.

#### [BeautyRoutine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/model/src/main/java/com/zoewave/probase/kocolor/model/BeautyRoutine.kt)
- Enhance routine logic: `objective` (Defense vs Repair), `minWaitMinutes`, `layeringOrder`.
- Link routines to biological markers (UV index, sleep quality).

---

### [Features: Inventory] Professional Logistics & Project Pan
Implementing the "Professional Logistics" and "Mindful Consumption" features.

#### [CosmeticDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/dao/CosmeticDao.kt)
- Add queries for FEFO (First Expired, First Out) sorting.
- Add queries for usage velocity tracking.

#### [InventoryAnalyticsEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/inventory/src/main/java/com/zoewave/probase/kocolor/features/inventory/data/InventoryAnalyticsEngine.kt) [NEW]
- Implement Cost-Per-Use (CPU) calculations.
- Implement demand forecasting and replenishment alerts.

---

### [Features: Analyzer] Ingredient & Interaction Engine
Decoding chemical taxonomies and layer safety.

#### [IngredientIntelligence.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/data/IngredientIntelligence.kt) [NEW]
- AI-powered INCI decoder (via Gemini).
- Risk detection logic (Allergens, Irritants, Pregnancy-safe).

#### [InteractionEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/data/InteractionEngine.kt) [NEW]
- Layering compatibility checker (e.g., Vitamin C + Retinol warnings).

---

### [Features: Routines] Chronobiology & UX
Aligning behaviors with circadian rhythms.

#### [RoutineChronobiologyService.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/routines/src/main/java/com/zoewave/probase/kocolor/features/routines/data/RoutineChronobiologyService.kt) [NEW]
- Dynamic scheduling based on time of day and biological needs.
- Layering enforcement logic (Cleansing -> Toning -> Actives -> Protection).

#### [RoutinesScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/routines/src/main/java/com/zoewave/probase/kocolor/features/routines/ui/RoutinesScreen.kt)
- Update UI to reflect AM/PM biological objectives.
- Add "Wait Timer" notifications for PM routines.

---

### [Features: Health] Holistic Nexus [NEW]
Connecting wellness data to skin performance.

#### [WellnessCorrelationEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/health/src/main/java/com/zoewave/probase/features/health/WellnessCorrelationEngine.kt) [NEW]
- Identify triggers (Sugar -> Acne, Poor Sleep -> Puffiness).
- Recommend "Recovery Routines" based on wellness markers.

## Verification Plan

### Automated Tests
- `InventoryAnalyticsTest.kt`: Verify CPU and usage velocity logic.
- `InteractionEngineTest.kt`: Test layering compatibility rules.
- `ChronobiologyServiceTest.kt`: Validate AM/PM sequence enforcement.

### Manual Verification
- **Inventory FEFO**: Verify that products nearing expiration are highlighted first in the inventory list.
- **Ingredient Scan**: Take a photo of an ingredient list and verify AI decoding and risk flagging.
- **Routine Progress**: Complete an AM routine and verify consistency streak gamification.
