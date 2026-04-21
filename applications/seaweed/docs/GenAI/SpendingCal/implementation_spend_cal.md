# Heatmap Selection Visibility Enhancement

Improve the visual feedback for the selected day in the Spending Heatmap to ensure it is easily identifiable at a glance, even when scrolling.

## Proposed Changes

### [Transaction Feature]

#### [SpendingHeatmap.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/components/SpendingHeatmap.kt)

- Fix duplicate imports for `LazyRow` and `items`.
- Update `DayBox` selection visuals:
    - Change selection border color from `primary` to `secondary` (or `tertiary` for even more contrast).
    - Increase selection border width from `2.dp` to `3.dp`.
    - Apply a slight `scale(1.1f)` or `z-index` to the selected box to make it "float" above others if possible, or simply use the thick contrasting border.
    - Ensure the text color within the selected box is high-contrast.

## Verification Plan

### Automated Tests
- Run Gradle assemble to verify compilation.

### Manual Verification
- Deploy the app and navigate to **Spending Analytics**.
- Click on various days in the heatmap.
- Verify that the selected day is highly visible (thick contrasting border).
- Scroll down to the transactions and glance back up to confirm the selected day remains easily identifiable.
