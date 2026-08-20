# Architecture Specification: Category-Aware Clothing Rotation (V1 Locked)

This document represents the final, locked architectural specification for the KoColor Clothing Rotation System.

## 1. Data Layer: Strict Separation of State

To ensure scalability and data integrity, user-specific personalization is decoupled from the canonical product catalog.

### `ClothingItemEntity` (Canonical)
*   **Source**: Signed KCPS Vaults.
*   **Role**: Defines the "Truth" of the garment (Name, Brand, Category, Color DNA).
*   **Update**: Only during release cycles.

### `ClothingUsageEntity` (Personalization)
*   **Source**: Local User Behavior.
*   **Role**: Tracks "Usage" metadata (Frequency, Recency).
*   **Linkage**: Joined with `ClothingItemEntity` via `productId`.
*   **Note**: Does NOT duplicate category data; category is retrieved via a database join.

---

## 2. Mathematical Formulation: Proportional Rotation

The rotation engine produces a normalized **Rotation Penalty [0.0 - 1.0]** derived from two primary signals:

### A. Category Usage Share (Frequency)
The system calculates the item's share of selections within its specific rotation boundary (e.g., "TOPS").
`Share = (Item Use Count) / (Total Category Use Count)`
*   **Threshold**: A share > 35% triggers a high penalty.

### B. Recency Decay (48-Hour Hard Window)
Any item used within the last 48 hours receives a maximum penalty of `1.0`. This ensures that even if an item has a low overall share, it won't be recommended two days in a row.

### C. Cold-Start Protection
If the user has committed fewer than **5 total outfits**, all rotation penalties are suppressed (`0.0`). This prevents the AI from behaving erratically before a statistically significant baseline is established.

---

## 3. Implementation Orchestration

### Domain Layer (`RotationScoringUseCase`)
All rotation mathematics and penalty calculations reside here. It is independently testable and isolated from UI logic.

### Repository Layer (`KoColorRotationRepository`)
Handles the **Atomic Transaction**. When an outfit is committed:
1.  Apply `.distinct()` filter to input IDs to ensure accurate counting.
2.  Wrapped in `database.withTransaction { ... }`.
3.  Synchronously update `GlobalRotationMetrics` and individual `ClothingUsage` stats.

### UI Layer (`OutfitGenerationViewModel`)
Acts as the **UI Orchestrator**. 
1.  Invokes the Use Case to generate candidate scoring.
2.  Observes the database via `StateFlow`.
3.  **Reactive Feedback**: UI badges (e.g., "Wear Count") update instantly upon database commit without manual invalidation.

---

## 4. AI Engine Integration: The Selection Priority Rule

The AI Style Architect receives a minified matrix containing the `RotationPenalty`. The system prompt enforces the following rules:
*   **PRIORITIZE**: Items with **LOW** RotationPenalty (0.0 = "Never Worn").
*   **AVOID**: Items with **HIGH** RotationPenalty (> 0.70 = "Exhausted") unless they are an absolute perfect match for user intent.
