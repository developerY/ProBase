# Final Locked Specification: KoColor Rotation V1

This document represents the final, implemented state of the KoColor Rotation and Inventory system as of V1.

## 1. Domain & Data Contracts

### Persistent Entities
- **`GlobalRotationMetricsEntity`**: Primary key `id=1`. Tracks `totalOutfitsCommitted`.
- **`ClothingUsageEntity`**: Joined to `ClothingItemEntity` by `productId`. Tracks `useCount` (Int) and `lastUsedTimestamp` (Long).

### Business Logic (`RotationScoringUseCase`)
The system calculates a `RotationPenalty` [0.0 - 1.0] used by the AI engine to deprioritize items.
- **Cold Start Rule**: If `totalOutfitsCommitted < 5`, `penalty = 0.0` (System is learning).
- **Recency Rule (Hard Cooldown)**: If `currentTime - lastUsed < 48h`, `penalty = 1.0`.
- **Frequency Rule (Category Share)**: If an item's share of category usage `> 35%`, `penalty = 0.85`.
- **Combination Logic**: `RotationPenalty = max(RecencyPenalty, FrequencyPenalty)`.

## 2. Presentation Layer

### Visual Standards
- **Corners**: `RoundedCornerShape(24.dp)`.
- **Background**: `0xFFF9F9F9` (Off-white).
- **Typography**: `FontFamily.Serif` for piece counts and currency values.

### The 4-Screen Navigation Model
1. **Curated Closet (Landing)**: High-level dashboard hub.
2. **Strategic Diversity**: Wardrobe architecture and concentration.
3. **Usage Metrics**: Behavioral history and frequency distribution.
4. **Style Intelligence**: CPW analysis and "Chromatic Core" palette.

## 3. Implementation Verification
- **Atomic Sync**: Increments to `totalOutfitsCommitted` and `useCount` are handled in a single Room `@Transaction`.
- **Division Safety**: CPW logic uses a nullable state for `useCount == 0`, rendering as **"NOT DEPLOYED"** in the UI.
- **Cold Start UI**: Glow Score renders as "∞" when the global outfit count is below the threshold.
