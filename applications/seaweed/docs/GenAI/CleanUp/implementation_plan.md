# Seaweed: MAD Gold Standard Refactor

This plan outlines the steps to refactor the Seaweed app to follow Modern Android Development (MAD) best practices. Every top-level composable will be standardized to a consistent signature, and high-fidelity previews will be added for every screen.

## Goals
- **Standardized Signatures**: All Screen composables will take `(uiState, onEvent, navTo)`.
- **Stateless Routes**: `UiRoute` composables will act as stateless wrappers that connect ViewModels to Screens.
- **High-Fidelity Previews**: Every screen will have at least one `@Preview` with realistic mock data.
- **Improved Maintainability**: Decouple UI from ViewModel and Navigation logic for better testability.

## Proposed Changes

### [Home Feature]

#### [HomeUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/home/src/main/java/com/zoewave/probase/seaweed/mobile/home/ui/HomeUiRoute.kt)
- Standardize `HomeScreen` signature to `(uiState, onEvent, navTo)`.
- Ensure all sub-components follow consistent naming.
- Add comprehensive previews for Loading and Success states.

---

### [Transaction Feature]

#### [TransactionsUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/TransactionsUiRoute.kt)
- Standardize `TransactionsScreen` signature.
- Move navigation logic out of the Screen and into the `UiRoute` or `onEvent` handler.
- Add previews for different tabs (Recent vs Cyclic).

#### [AddTransactionUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/AddTransactionUiRoute.kt)
- Standardize `AddTransactionScreen` signature.
- Ensure the AI intervention flow is cleanly integrated into the standardized pattern.
- Add previews for standard entry and intervention states.

---

### [Bills & Budget Features]

#### [BillsUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/bills/src/main/java/com/zoewave/probase/seaweed/mobile/bills/ui/BillsUiRoute.kt)
- Refactor to match the `(uiState, onEvent, navTo)` pattern.
- Improve embedded layout handling.

#### [BudgetUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/budget/src/main/java/com/zoewave/probase/seaweed/mobile/budget/ui/BudgetUiRoute.kt)
- Standardize signature and add previews.

---

### [Spending Control Feature]

#### [EnvelopeManagementScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/features/spendingcontrol/src/main/java/com/zoewave/probase/seaweed/features/spendingcontrol/ui/EnvelopeManagementScreen.kt)
- Rename to `EnvelopeManagementUiRoute.kt` for consistency.
- Implement the `(uiState, onEvent, navTo)` pattern.
- Add previews for active envelopes and philosophy sections.

## Verification Plan

### Automated Tests
- Run existing unit tests for ViewModels to ensure no regressions in state logic.
- Verify Compose Previews render correctly in the IDE.

### Manual Verification
- Deploy the app and navigate through all refactored screens.
- Verify that state updates (e.g., adding a transaction, overriding an intervention) still function as expected.
- Check that the responsive layouts (Expanded vs Compact) are still working correctly on Home and Transaction screens.
