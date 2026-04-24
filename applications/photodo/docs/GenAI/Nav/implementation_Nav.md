# Seaweed Navigation Refactoring Plan

Consolidate Seaweed mobile app navigation to three main bottom tabs and create clear entry points for secondary features.

## Proposed Changes

### [Seaweed Model]

#### [SeaweedDestination.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/model/src/main/java/com/zoewave/probase/seaweed/model/navigation/SeaweedDestination.kt)

- Update `topLevelDestinations` to only include:
  1. `Home`
  2. `Transactions()`
  3. `Settings`
- Remove `Budget` from `topLevelDestinations`.

### [Mobile App Components]

#### [SeaweedMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/src/main/java/com/zoewave/probase/seaweed/mobile/ui/components/SeaweedMainScreen.kt)

- Refine bottom bar logic to ensure it only shows the three primary tabs.
- Ensure backstack handling works correctly with the reduced tab set.

#### [SeaweedBottomBar.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/src/main/java/com/zoewave/probase/seaweed/mobile/ui/components/SeaweedBottomBar.kt)

- Update selection logic to correctly highlight the active tab even when on secondary screens.
  - `Home` tab selected for: `Home`, `Budget`, `CategoryGrid`.
  - `Transactions` tab selected for: `Transactions`, `Analytics`, `Bills`.
  - `Settings` tab selected for: `Settings`.

### [Home Feature]

#### [HomeUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/HomeUiRoute.kt)

- (Visual Only) Ensure "Manage Budgets" and "All Categories" buttons are prominent as they are now primary entry points.

### [Transaction Feature]

#### [TransactionsUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/TransactionsUiRoute.kt)

- (Visual Only) Ensure the `Analytics` and `Budget` icons in the TopAppBar provide clear secondary navigation.

## Verification Plan

### Manual Verification
- Launch the app and verify only 3 tabs are in the bottom bar.
- Click **Home** -> Navigate to **Budget** -> Verify **Home** tab remains selected.
- Click **Home** -> Navigate to **Categories** -> Verify **Home** tab remains selected.
- Click **Transactions** -> Navigate to **Analytics** (via TopAppBar) -> Verify **Transactions** tab remains selected.
- Navigate to **Settings** -> Verify **Settings** tab is selected.
- Test back button from all secondary screens to ensure it returns to the correct primary tab.
