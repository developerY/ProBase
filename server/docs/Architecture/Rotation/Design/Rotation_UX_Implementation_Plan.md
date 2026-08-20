# Implementation Plan: Rotation Visualization & UX (The Glow Score)

This plan outlines the UI/UX enhancements required to showcase the **Clothing Rotation System** to the user. We will transform raw usage data into a premium, "gamified" scientific experience.

## 1. Wardrobe "Freshness" Visualization

### [MOD] Garment Cards (`WardrobeItemCard`)
Add a dynamic **"Freshness Badge"** to the corner of each garment card.
*   **Status: FRESH (Cyan/Green)**
    *   *Logic*: `useCount == 0` OR `lastUsed > 10 days ago`.
    *   *UI*: Pulsing circular dot.
*   **Status: IN ROTATION (Yellow/Gold)**
    *   *Logic*: `useCount > 0` AND `lastUsed` between 3-10 days.
    *   *UI*: Solid gold border.
*   **Status: COOLING DOWN (Plum/Red)**
    *   *Logic*: `lastUsed < 48 hours` OR `CategoryShare > 35%`.
    *   *UI*: Grayscale overlay or a "Lock" icon indicating the item is "stale" for AI suggestions.

## 2. The Rotation Dashboard (New Component)

Place a summary surface at the top of the Wardrobe landing page:
*   **Glow Score (Total)**: A circular progress gauge showing `Unique Items Worn / Total Items`.
*   **Diversity Index**: A textual rating (e.g., "Architectural", "Monotonous", "Vibrant") based on the entropy of categories.
*   **Cooldown Ticker**: A small scrolling list of items currently in the 48h recency window.

## 3. Immersive Outfit Commitment UX

### [MOD] "Save to Palette" Action
When the user clicks "SAVE", the app must visualize the data loop:
1.  **Sync Animation**: A "Data Beam" animation moving from the outfit preview into the navigation bar (Collection tab).
2.  **Haptic Confirmation**: A "Heavy" impact vibration to signify the commitment.
3.  **Real-time Badge Increment**: The "Wear Count" text on the items should perform a number-scroll animation from `X` to `X+1`.

## 4. AI Insight Integration

### [MOD] `ProductEditorialNotesDialog`
Update the "Scientific Overview" section to include user-specific rotation facts:
*   "**Rotation Status**: This item is currently at 12% category share (Fresh)."
*   "**Last Deployment**: 8 days ago."
*   "**Stylist Note**: This piece hasn't been worn in a while—deploy it today to maximize your wardrobe ROI."

---

## 5. Technical Requirements (Mobile)

### `RotationRepository` [MODIFY]
Add a way to observe all personalization data reactively.
```kotlin
fun observeAllUsages(): Flow<List<ClothingUsageEntity>>
```

### `WardrobeViewModel` [MODIFY]
Combine the canonical catalog flow with the usage flow.
```kotlin
val uiState = combine(
    wardrobeRepository.getAllClothing(),
    rotationRepository.observeAllUsages()
) { items, usages ->
    items.map { item ->
        val usage = usages.find { it.productId == item.remoteId }
        item.copy(
            usageCount = usage?.useCount ?: 0,
            lastWorn = usage?.lastUsedTimestamp
        )
    }
}
```

## 6. Verification Plan
*   **Visual Check**: Verify the "Freshness Badge" changes color immediately after clicking "SAVE" in the simulator.
*   **Dashboard Accuracy**: Ensure the "Glow Score" increments correctly after committing a multi-garment outfit.
*   **Cooldown Verification**: Manually set system time forward 48h and verify "Cooling Down" items return to "Fresh/In Rotation" status.
