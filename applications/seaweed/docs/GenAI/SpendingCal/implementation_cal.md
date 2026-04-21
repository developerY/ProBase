# Spending Heatmap Calendar Feature

Implement a calendar-looking UI that visualizes daily spending trends using a heatmap. Darker colors represent higher spending days, while lighter colors represent lower spending days.

## Proposed Changes

### [Transaction Feature]

#### [AnalyticsViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/AnalyticsViewModel.kt)

- Update `AnalyticsUiState` to include `heatmapData: Map<LocalDate, Double>`.
- Implement `calculateHeatmapData` to group transactions by day for the last 3 months.

#### [NEW] [SpendingHeatmap.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/components/SpendingHeatmap.kt)

- Create a new Composable that renders a grid of days for a given month.
- Each day will be colored based on the spending amount relative to the maximum spending in that period.

#### [AnalyticsUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/AnalyticsUiRoute.kt)

- Integrate the `SpendingHeatmap` into the `AnalyticsContent` LazyColumn.

## Verification Plan

### Automated Tests
- Add a unit test to `AnalyticsViewModelTest` (if it exists) or create one to verify the heatmap data calculation.

### Manual Verification
- Deploy the app and navigate to the "Spending Analytics" screen.
- Verify that the heatmap calendar is displayed correctly.
- Verify that days with higher spending are darker and days with lower/no spending are lighter.
- Check if it helps identify patterns (e.g., higher spending on weekends or lunch M-F as mentioned by the user).
