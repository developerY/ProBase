# Seaweed Budgeting System

Build out the budgeting component of Seaweed to provide granular control over flexible spending. After cyclic bills are deducted, users can allocate their "Real Starting Balance" into categorical budgets (e.g., Food, Entertainment) and track their progress in real-time.

## User Review Required

- [ ] **Unallocated Money**: Should we show a "Buffer" or "Unallocated" category for any flexible money not assigned to a specific budget?
- [ ] **Budget Period**: Confirming that all category budgets reset on the 1st of every month, matching the cyclic bill logic.

## Proposed Changes

### [Data] Budget Targets & Tracking
Introduce persistence and logic for categorical spending limits.

#### [NEW] [BudgetTarget.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/model/src/main/java/com/zoewave/probase/seaweed/model/BudgetTarget.kt)
- **Model**: `categoryName`, `limitAmount`.

#### [CategoryOverview.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/model/src/main/java/com/zoewave/probase/seaweed/model/CategoryOverview.kt)
- Add `limitAmount: Double?` and `remainingAmount: Double?`.

#### [Database & Repository]
- **BudgetTargetEntity/Dao**: Standard CRUD for category limits.
- **FinancialRepository Expansion**:
    - `getBudgetHealth()`: A flow providing a list of categories with `Spent vs. Target` data.
    - `getTotalBudgetedAmount()`: Sum of all category targets.

---

### [Mobile] Budget Management & Visualization
Integrate budget progress into the existing dashboard and provide management tools.

#### [Home Refactor](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/HomeUiRoute.kt)
- **Category Progress Bars**: Update the category list to show linear progress indicators (Spent / Target).
- **Over-Budget Alerts**: Highlight categories in red if they exceed their set limit.

#### [NEW] [:applications:seaweed:apps:mobile:features:budget](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/budget)
- **Budget Setup Screen**: A dedicated view to manage category limits.
- **Allocation View**: Visual showing `Real Starting Balance` being "divided" into the different category buckets.

---

### [WearOS] Budget Status
- **Top Categories Glance**: Show the remaining balance for the user's top 3 most-used categories (e.g., "Food: $45 left").

## Verification Plan

### Automated Tests
- **Budget Progress Test**: Verify math for `progressPercentage` (e.g., Spent $50 of $100 = 50%).
- **Calculation Test**: Ensure "Unallocated" money correctly reflects `Real Starting Balance - Total Budgeted`.

### Manual Verification
- Set a $500 budget for "Food". Add a $50 grocery transaction. Verify the progress bar shows 10% usage.
- Exceed the "Food" budget and verify the UI changes to an alert state (e.g., red color).
- Check WearOS to see the updated "Food" budget status.
