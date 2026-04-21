# Spending Analytics UI Enhancements

Improve the Spending Analytics screen by reordering components and adding interactive details to the Spending Heatmap.

## Proposed Changes

### [Transaction Feature]

#### [AnalyticsViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/AnalyticsViewModel.kt)

- Update `AnalyticsUiState` to include `allTransactions: List<Transaction>`.
- This will allow the UI to filter transactions for a specific day when clicked in the heatmap.

#### [SpendingHeatmap.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/components/SpendingHeatmap.kt)

- Add `onDayClick: (LocalDate) -> Unit` and `selectedDate: LocalDate?` parameters to `SpendingHeatmap`.
- Make `DayBox` clickable and highlight the selected day.

#### [AnalyticsUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/AnalyticsUiRoute.kt)

- Reorder the `LazyColumn` content to show **Spending Habits** at the top.
- Add local state `selectedHeatmapDate` to track the clicked day in the heatmap.
- Add an `AnimatedVisibility` block below the heatmap to show transactions for the selected day using `TransactionItem`.

## Verification Plan

### Automated Tests
- Run Gradle assemble to verify compilation.

### Manual Verification
- Deploy the app and navigate to **Spending Analytics**.
- Verify that **Spending Habits** are now at the top.
- Click on a day in the **Spending Heatmap**.
- Verify that the day is highlighted and a list of transactions for that day appears below the heatmap.
- Click a different day and verify the list updates.
