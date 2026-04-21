# Category-Based Filtering for Spending Analytics

Implement cross-component filtering on the Spending Analytics page. Clicking on a "Spending Habit" (category) will filter the Trends chart, the Heatmap, and the Transaction details to show only data for that specific category.

## Proposed Changes

### [Transaction Feature]

#### [AnalyticsViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/AnalyticsViewModel.kt)

- Update `AnalyticsUiState` to include `categoryHeatmapData: Map<String, Map<LocalDate, Double>>` and `categoryTrends: Map<String, Map<SpendingPeriod, List<TrendPoint>>>`.
- This pre-calculates filtered data for each category to ensure smooth UI transitions.

#### [AnalyticsUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/AnalyticsUiRoute.kt)

- Add local state `selectedCategory: String?` to track the active filter.
- Update `HabitInsightCard` to be clickable and show a selection state (e.g., border).
- Pass the filtered data (based on `selectedCategory`) to `SimpleBarChart` and `SpendingHeatmap`.
- Update the daily transaction list logic to filter by `selectedCategory` when a day is selected in the heatmap.
- Add a way to clear the filter (e.g., clicking the same habit again or a "Clear" button).

## Verification Plan

### Automated Tests
- Run Gradle assemble to verify compilation.

### Manual Verification
- Deploy the app and navigate to **Spending Analytics**.
- Click on a "Spending Habit" card (e.g., "Food").
- Verify that:
    - The **Spending Trends** chart updates to show only "Food" spending.
    - The **Spending Heatmap** updates its intensity to show only "Food" transactions.
    - Clicking a day in the filtered heatmap shows only "Food" transactions in the list.
- Click the habit again to clear the filter and verify all components return to showing "All" data.
