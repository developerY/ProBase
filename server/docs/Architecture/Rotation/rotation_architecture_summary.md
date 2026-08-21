# KoColor Rotation System Architecture Summary

The KoColor Clothing Rotation System is a feedback-driven engine designed to encourage wardrobe diversity by tracking usage and applying intelligent penalties to frequently worn items.

## 1. System Components & Functions

| Item | Function |
| :--- | :--- |
| **GlobalRotationMetricsEntity** | Tracks the global state of the user's outfit history, specifically `totalOutfitsCommitted`. |
| **ClothingUsageEntity** | Stores personalization metadata for a specific product, including `useCount` and `lastUsedTimestamp`. |
| **RotationScoringUseCase** | The domain-level logic that calculates a **Rotation Penalty** [0.0 - 1.0] for the AI styling engine. |
| **Rotation Penalty** | A scoring modifier based on three rules: Cold Start (min 5 outfits), Category Share (max 35% usage), and Recency (48-hour cooldown). |
| **ClothingWithUsage** | A joined domain model combining canonical item data (`ClothingItem`) with user-specific personalization (`ClothingUsageEntity`). |

## 2. Screen Content Specifications

### Wardrobe Dashboard (Analytics & Intelligence)
*   **Glow Score (Wardrobe Utilization)**: A 0–100% circular progress gauge showing the percentage of the owned wardrobe that has been worn (`Unique garments with useCount > 0 / Total garments owned`).
*   **Diversity Index (Wardrobe Entropy)**: A textual rating or metric measuring how evenly usage is distributed across categories.
*   **Cooldown Ticker**: A scrolling list of items currently in the 48-hour recency penalty window, displaying the hours remaining until "freshness" is restored.
*   **Item Count & Value**: High-level summaries of the total number of items owned and the calculated total portfolio value.

### Wardrobe Surface (Garment List)
*   **Dynamic Freshness Indicators**: Garment cards (`WardrobeItemCard`) use visual states based on rotation logic:
    *   **RESTING**: Used < 48h or high category share (Plum/Muted Border).
    *   **FRESH**: Never used or not used in 10+ days (Cyan/Pulsing Dot).
    *   **IN ROTATION**: Standard state for regularly used items (Gold/Solid Border).
*   **Wear Count Badges**: Visual count of how many times an item has been worn.

### Style Simulator (Outfit Generation)
*   **AI Proposals**: Displays outfits generated with rotation penalties applied.
*   **Commit Action**: A "SAVE TO PALETTE" or similar button that triggers an atomic database transaction to update global and individual usage metrics.
*   **Visual Feedback**: Post-save animations like the "Data Beam" and haptic feedback to confirm the data commitment.

## 3. Key Design Principles
- **Atomic Transactions**: All metrics (global and individual) are updated in a single `database.withTransaction {}` block to ensure data integrity.
- **Data Separation**: Personalization data (usage) is strictly separated from canonical product data (catalog).
- **Reactive Updates**: UI components observe Room database streams via `StateFlow`, ensuring immediate updates upon data commitment.
