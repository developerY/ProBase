# Tabbed Analytics Insights

Reorganize the Spending Analytics screen to use a tabbed layout for the main insights (Trends and Heatmap), while maintaining the Spending Habits at the top as a global filter.

## Proposed Changes

### [Transaction Feature]

#### [AnalyticsUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/AnalyticsUiRoute.kt)

- Introduce a new state `selectedTabIndex` to manage the active tab (0 for Trends, 1 for Heatmap).
- Add a `TabRow` below the Spending Habits section.
- Define two tabs: "Trends" and "Heatmap".
- Reorganize the `LazyColumn`:
    - Spending Habits remain as items at the top.
    - Add a `TabRow` as a sticky item or regular item.
    - Show either Trends or Heatmap content based on the `selectedTabIndex`.
- Remove the individual collapsible logic for Trends and Heatmap as the tabs provide the necessary organization.

## Verification Plan

### Automated Tests
- Run Gradle assemble: `:applications:seaweed:apps:mobile:features:transaction:assembleDebug`.

### Manual Verification
- Deploy the app and navigate to **Spending Analytics**.
- Verify that **Spending Habits** filter is still functional.
- Switch between **Trends** and **Heatmap** tabs.
- Confirm filtering works correctly in both tabs.
