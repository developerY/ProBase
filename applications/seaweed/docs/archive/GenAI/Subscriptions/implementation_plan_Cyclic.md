# Real-time Money Profile & Cyclic Bill Management for Seaweed

Implement a "Real-time Money Profile" system that empowers users by showing their true flexible spending power. By deducting all cyclic bills (Rent, Car, Insurance, Subscriptions) from their monthly income upfront, the app establishes a "Real Starting Balance." Daily expenses are then tracked against this balance in real-time.

## User Review Required

- [ ] **Default Income**: I'll set a default monthly income of $5000 in a new `UserSettings` table, which users can adjust in the Settings tab.
- [ ] **Spending Pace**: Should we include a "Daily Allowance" calculation? (e.g., `Flexible Money / Days Left in Month`).

## Proposed Changes

### [Data] The Financial Foundation
Establish the data structures needed for real-time profile calculations.

#### [NEW] [RecurringExpense.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/model/src/main/java/com/zoewave/probase/seaweed/model/RecurringExpense.kt)
- **Model**: `id`, `name`, `amount`, `frequency`, `type` (Rent, Car, Insurance, Subscription, Utility, Other).
- **Logic**: Amortizes all frequencies to a monthly impact.

#### [NEW] [UserSettings.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/model/src/main/java/com/zoewave/probase/seaweed/model/UserSettings.kt)
- **Model**: `monthlyIncome`, `currency`.

#### [Database & Repository]
- **RecurringExpenseEntity/Dao**: Store and aggregate fixed costs.
- **UserSettingsEntity/Dao**: Persist user-defined financial baseline.
- **FinancialRepository**: A high-level repository that combines `Income`, `Fixed Costs`, and `Daily Transactions` to provide the `Real-time Money Profile` flow.

---

### [Mobile] Real-time Money Profile UI
Transform the Home screen into a dynamic dashboard of financial reality.

#### [Home Refactor](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/HomeUiRoute.kt)
- **The "Real Money" Hero**: A large, prominent display of "Flexible Money Remaining" for the current month.
- **Visual Breakdown**:
    - `Income` (The Source)
    - `- Fixed Costs` (The Foundation - Deducted Upfront)
    - `- Daily Spending` (The Variable)
    - `= Real-time Remaining`
- **Spending Pace Indicator**: A progress bar showing how much of the "Flexible Money" has been used vs. how much of the month has passed.

#### [NEW] [:applications:seaweed:apps:mobile:features:bills](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/bills)
- **Categorized Bill Manager**: Easy management of Rent, Car, Insurance, and Subscriptions.
- **Impact visualization**: Show how each individual bill affects the "Real Starting Balance."

---

### [WearOS] Wrist-based Money Profile
- **Hero Metric**: Current "Flexible Money" available.
- **Quick Glance**: Circular progress showing monthly spending pace.

## Verification Plan

### Automated Tests
- **Budget Calculator Tests**: Verify the math: `Income - Sum(Fixed) - Sum(Variable) == Profile`.
- **Amortization Tests**: Verify Weekly/Yearly to Monthly conversion accuracy.

### Manual Verification
- Set Income to $4000. Add Rent ($1500) and Netflix ($15). Verify "Real Starting Balance" is $2485.
- Add a $10 coffee transaction and verify the "Flexible Money" drops to $2475 in real-time.
- Check WearOS to ensure it reflects the $2475 balance immediately.
