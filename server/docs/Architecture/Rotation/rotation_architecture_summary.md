# KoColor Rotation System Architecture Summary (V1)

The KoColor Clothing Rotation System is a feedback-driven engine designed to encourage wardrobe diversity by tracking usage and applying intelligent penalties to frequently worn items.

## 1. System Components & Functions

| Item | Function |
| :--- | :--- |
| **GlobalRotationMetricsEntity** | Tracks the global state of the user's outfit history, specifically `totalOutfitsCommitted`. |
| **ClothingUsageEntity** | Stores personalization metadata for a specific product, including `useCount` and `lastUsedTimestamp`. |
| **RotationScoringUseCase** | The domain-level logic that calculates a **Rotation Penalty** [0.0 - 1.0] for the AI styling engine. |
| **Rotation Penalty** | A scoring modifier: Cold Start (score = 0.0 if < 5 outfits), Recency (1.0 if < 48h), and Frequency (0.85 if share > 35%). Final penalty is `max(Recency, Frequency)`. |
| **GarmentWithUsage** | A joined Room relation model combining canonical item data (`ClothingItemEntity`) with user-specific personalization (`ClothingUsageEntity`). |

## 2. Screen Content Specifications

### Curated Closet (Wardrobe Landing)
*   **Glow Score (Wardrobe Utilization)**: A 0–100% metric showing the percentage of the owned wardrobe that has been worn. Displays "∞" or "CALCULATING" during the Cold Start phase (< 5 outfits).
*   **Diversity Index (Wardrobe Entropy)**: A qualitative label (e.g., "Strategic", "Eclectic") measuring how broadly usage is distributed across categories.
*   **Item Count & Value**: High-level summaries of total pieces (Editorial Serif font) and total portfolio investment (Dark Green Serif font).
*   **Actions**: Dual-path navigation: "VIEW INTELLIGENCE" (Holographic action) and "VIEW INVENTORY" (Forest Green action).

### Wardrobe Surface (Garment List)
*   **Dynamic Freshness Indicators**: Garment cards use visual states based on rotation logic:
    *   **RESTING**: Used < 48h (max penalty applied).
    *   **FRESH**: Never used or high potential for rotation.
    *   **IN ROTATION**: Standard state for regularly used items.
*   **Wear Count Badges**: Visual count of how many times an item has been worn.

### Style Simulator (Outfit Generation)
*   **AI Proposals**: Outfits generated with `RotationScoringUseCase` penalties applied to the selection algorithm.
*   **Commit Action**: An atomic transaction (`commitOutfitUsage`) that increments global and individual metrics simultaneously.

## 3. Key Design Principles
- **Atomic Transactions**: All metrics are updated in a single `database.withTransaction {}` block to ensure `totalOutfitsCommitted` stays in sync with individual `useCount` increments.
- **Data Separation**: Product metadata (catalog) is immutable; personalization (usage) is stored in a separate table linked by `productId`.
- **Mathematical Safety**: CPW (Cost Per Wear) logic handles zero-wear items by yielding a null state, rendered in the UI as **"NOT DEPLOYED"**.
