# Implementation Walkthrough: KoColor Clothing Rotation System (V1)

This document provides a detailed end-to-end walkthrough of the implemented Category-Aware Clothing Rotation System, tracing the flow of data from the database layer to the AI-driven UI.

---

## 1. The Data Foundation (Room Persistence)

We utilize a decoupled schema to ensure that user-specific behavioral data (frequency, recency) survives canonical catalog updates.

### Persistent Entities
*   **[`GlobalRotationMetricsEntity`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/GlobalRotationMetricsEntity.kt)**: Tracks the "Total Outfits Committed." This acts as the baseline for the **Cold Start Rule**.
*   **[`ClothingUsageEntity`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/db/src/main/java/com/zoewave/probase/kocolor/db/entity/ClothingUsageEntity.kt)**: A high-performance table containing only `productId`, `useCount`, and `lastUsedTimestamp`.

### The "Joined" Model (`GarmentWithUsage`)
To avoid N+1 query performance traps, the `GarmentWithUsage` POJO uses Room's **`@Relation`** capability to observe canonical garment data and personalization usage data as a single joined model.

```kotlin
data class GarmentWithUsage(
    @Embedded val garment: ClothingItemEntity,
    @Relation(parentColumns = ["remoteId"], entityColumns = ["productId"])
    val usage: ClothingUsageEntity?
)
```

---

## 2. The Atomic Transaction Loop

Updating wardrobe history is a multi-table operation that must be 100% rollback-safe.

### Orchestration (`commitOutfit`)
Implemented in **[`RotationRepositoryImpl`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/repository/RotationRepositoryImpl.kt)** (utilizing `KoColorDatabase.withTransaction`):
1.  **Deduplication**: The repository applies a `.distinct()` filter to the input list prior to the Room update sequence.
2.  **Transaction Start**: Opens an atomic boundary on the database.
3.  **Global Increment**: `totalOutfitsCommitted` is incremented in the global metrics table.
4.  **Individual Update**: Existing `useCount` is incremented, and `lastUsedTimestamp` is set to `System.currentTimeMillis()`.
5.  **Commit**: The transaction finishes, triggering a reactive emission to all active `StateFlow` observers.

---

## 3. The Scoring Domain (Business Logic)

The **[`RotationScoringUseCase`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RotationScoringUseCase.kt)** transforms raw database counts into a normalized penalty factor **[0.0 to 1.0]**.

### Mathematical Derivation
1.  **Cold Start Check**: If global commits < 5, penalty is always `0.0`.
2.  **Frequency Penalty**: 
    `Category Share = (Item Use Count) / (Total Category Selection Events)`
    *   *Threshold*: If share > 35%, a full frequency penalty is applied.
3.  **Recency Penalty**: 
    *   *48-Hour Hard Cooldown*: If `currentTime - lastUsed < 48 hours`, penalty = `1.0`. Otherwise, 0.0.
4.  **Normalization**: The result is a `Double` value consumed by the AI.

---

## 4. AI Engine Integration (The Architect)

The **[`StyleSimulatorEngine`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/features/analyzer/src/main/java/com/zoewave/probase/kocolor/features/analyzer/simulator/data/StyleSimulatorEngine.kt)** receives the penalty scores in its minified matrix manifest.

### The System Prompt Rule
The LLM is explicitly instructed:
*   **PRIORITIZE** "Fresh" items (Penalty = 0.0).
*   **AVOID** "Exhausted" items (Penalty > 0.70) unless the user intent demands a specific "Signature Look."

---

## 5. High-Fidelity UI Experience

### Reactive Card States (`WardrobeViewModel`)
The ViewModel observes the joined database stream. Garment cards reactively display their **Freshness Pulse**:
*   **RESTING**: Subtle Muted Plum border (Last used < 48h).
*   **FRESH**: Pulsing Cyan dot (New or long-unused).
*   **IN ROTATION**: Solid Gold ring (Regularly cycled).

### Truthful Animations
Animations only trigger upon a **Success Callback** from the database:
1.  **Data Beam**: A custom Canvas animation syncs the selection.
2.  **Badge Bounce**: The wear-count text scrolls up from `X` to `X+1` using `AnimatedContent`.

---

## 6. Verification & Test Suite
The implementation is verified through automated tests:
*   **[`RotationScoringUseCaseTest`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/test/java/com/zoewave/probase/kocolor/data/usecase/RotationScoringUseCaseTest.kt)**: Validates cold-start behavior, category-share calculations, frequency penalties, and recency rules.
*   **[`RotationRepositoryTest`](file:///Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/data/src/test/java/com/zoewave/probase/kocolor/data/repository/RotationRepositoryTest.kt)**: Validates atomic transaction behavior and input deduplication.
