# Real-time Money Profile & Cyclic Bill Management for Seaweed

Implement a "Real-time Money Profile" system that empowers users by showing their true flexible spending power. The app automatically pre-fills common cyclic bills (Housing, Utilities, etc.) to jumpstart the user's financial setup. By deducting all fixed costs from monthly income upfront, we establish a "Real Starting Balance" for daily tracking.

## User Review Required

- [ ] **Default Bill List**: I've pre-selected the following common bills:
    - **Housing**: Rent/Mortgage, Home Insurance.
    - **Utilities**: Electricity, Water, Gas/Heating.
    - **Communication**: Mobile Phone, Internet.
    - **Transportation**: Car Payment, Auto Insurance.
    - **Subscriptions**: Streaming (Netflix/Spotify), Gym.
- [ ] **Setup Wizard**: Should the user be prompted to fill these in on their first app launch, or just see them in the "Bills" tab? (Proposing a guided "Setup Your Real Profile" flow).

## Proposed Changes

### [Data] The Financial Foundation
Establish the data structures and pre-filling logic.

#### [NEW] [RecurringExpense.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/model/src/main/java/com/zoewave/probase/seaweed/model/RecurringExpense.kt)
- **Model**: `id`, `name`, `amount`, `frequency`, `category` (Housing, Utilities, etc.), `isDefault` (to handle system vs user added).
- **Logic**: Universal amortization to monthly impact.

#### [NEW] [RecurringExpenseEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/database/src/main/java/com/zoewave/probase/seaweed/database/RecurringExpenseEntity.kt)
- Room entity with `category` and `isDefault` flags.

#### [NEW] [RecurringExpenseDao.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/database/src/main/java/com/zoewave/probase/seaweed/database/RecurringExpenseDao.kt)
- CRUD + Initialization logic to insert default bills if the table is empty.

---

### [Mobile] Guided Setup & Bills Dashboard
Help users transition to a "Real Money" mindset.

#### [NEW] [:applications:seaweed:apps:mobile:features:bills](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/bills)
- **Bills Dashboard**: Grouped by Category (e.g., all Housing costs together).
- **The Pre-filled View**: Shows the default bills with "Action Required" status until an amount is entered.
- **Easy Deletion**: One-tap removal of pre-filled bills that don't apply (e.g., "I don't have a car").

#### [Home Refactor](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/HomeUiRoute.kt)
- **The "Real Money" Profile**: Prominently display remaining flexible money.
- **Fixed Cost Breakdown**: A summary card that expandable to show Housing vs. Utilities vs. Subscriptions.

---

### [WearOS] Fast Financial Reality
- **Remaining Money Glance**: Always see how much "Real Money" is left for the month.
- **Next Bill Card**: Displays the next pre-filled or user-added bill coming due.

## Verification Plan

### Automated Tests
- **Initialization Test**: Verify that default bills are correctly populated on first database creation.
- **Aggregation Test**: Verify that costs are correctly summed within their "Housing", "Utilities", etc. categories.

### Manual Verification
- Launch the app fresh and verify the "Bills" tab contains the pre-filled list.
- Enter a Rent amount and verify the Home screen "Real Money" updates.
- Delete the "Car Payment" pre-filled bill and verify it's removed from calculations.
