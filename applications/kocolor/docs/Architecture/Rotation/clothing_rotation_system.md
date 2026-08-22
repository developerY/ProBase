# Architecture Specification: Intelligent Category-Aware Clothing Rotation (V1 Locked)

This document details the architecture and implementation strategy for the KoColor Clothing Rotation System. This system tracks user-specific garment usage history and transforms these metrics into a dynamic scoring factor for the AI styling engine, encouraging diverse wardrobe rotation.

## 1. System Goals
*   **Encourage Variety**: Systematically penalize frequently or recently used items in AI recommendations.
*   **Category Awareness**: Calculate usage share relative to specific rotation boundaries (e.g., "TOPS").
*   **Cold Start Protection**: Prevent AI erraticism by suppressing penalties until a baseline of usage history exists.
*   **Data Integrity**: Maintain strict separation between canonical catalog data and user personalization history.
*   **Atomic Persistence**: Ensure all multi-table updates (global metrics + individual item stats) are transacted safely.

## 2. Data Layer Design

### Entities

#### `GlobalRotationMetricsEntity`
Tracks the global state of the user's outfit lifecycle.
*   **Enforcement**: Single-row table (`metricsId = 0`).
*   **Primary Metric**: `totalOutfitsCommitted` (increments every time a user finalizes an outfit).

#### `ClothingUsageEntity`
Stores the personalization metadata for a specific product.
*   **Crucial**: Separates personalization state from the canonical `ClothingItemEntity`.
*   **Linkage**: Maps to catalog items via `productId`.
*   **Metrics**: `useCount` (frequency) and `lastUsedTimestamp` (recency).
*   **Note**: This entity does NOT store `categoryId`. Category metadata is retrieved by joining with `ClothingItemEntity`.

### Data Access Object (DAO)
The `GarmentRotationDao` provides low-level SQL primitives.
*   `getGlobalMetrics()`: Retrieves the single-row global state.
*   `getUsageForCategory(categoryId)`: Fetches usage statistics by joining `ClothingUsageEntity` with canonical `ClothingItemEntity` category metadata.
*   `updateGlobalMetrics()` / `updateGarmentUsages()`: Transactional primitives for the repository.

## 3. Repository Orchestration
The `KoColorRotationRepository` manages the business logic of "committing" an outfit.

### The Mandatory Atomic Transaction
Updating wardrobe history is a non-trivial multi-step process that must be wrapped in a database transaction boundary to prevent data corruption if the app crashes mid-operation.

1.  **Fetch & Increment Global State**: Update `totalOutfitsCommitted` and `lastOutfitTimestamp`.
2.  **Deduplicate Selection**: Ensure unique product IDs are processed once per commitment event.
3.  **Synchronous Update**: Fetch existing `ClothingUsageEntity` records, apply increments/timestamps, and persist.

## 4. Domain Layer: AI Rotation Scoring
The `RotationScoringUseCase` transforms raw database counts into a **Rotation Penalty** used by the styling engine.

### Mathematical Formulation
The "Rotation Penalty" is derived from three primary factors:

1.  **Cold Start Rule**: If `totalOutfitsCommitted < 5` (configurable threshold), return a penalty of `0.0`.
2.  **Category Usage Share**: 
    `Share = (Item Use Count) / (Total Category Use Count)`
    *   *Note*: Category usage is defined as committed garment-selection events within that specific category.
    *   *Threshold*: `highFrequencyShare = 0.35` (35% selection rate).
3.  **Recency Decay**:
    *   *Window*: `48h` (48 * 60 * 60 * 1000ms).
    *   Items used within this window receive a maximum recency penalty.

### Final AI Input
The system produces a normalized modifier [0.0 - 1.0].
`Final Selection Score = StyleCompatibility - (RotationPenalty * WeightingFactor)`

## 5. UI Layer Integration
The `OutfitGenerationViewModel` acts as the UI orchestration boundary.
*   **Responsibility**: It invokes the outfit-generation/use-case layer and observes the resulting state. 
*   **Scoring Boundary**: Rotation mathematics and penalty calculation remain entirely within `RotationScoringUseCase`. The ViewModel does NOT perform scoring.
*   **Action**: `onCommitCurrentOutfit(ids)` triggers the repository transaction and refreshes the UI state.

## 6. Verification Requirements

### Case 1: Atomic Rollback Rigor
Force an exception after the global count update but before garment updates. Assert that **all** values (global and individual) are restored to pre-transaction states.

### Case 2: Deterministic Usage Verification
Verify that 1 committed outfit with 4 garments increments the global counter by 1 and each unique garment counter by 1. Ensure category share is derived correctly (e.g., 4 uses in 10 total = 40%).

### Case 3: Recency Integration
Verify that a garment used 24 hours ago receives a significantly larger penalty than one used 20 days ago, despite having identical usage counts.

### Case 4: Cold Start Suppressing
Verify that if `totalOutfitsCommitted < 5`, the penalty returned is exactly `0.0`, regardless of individual item usage spikes.
