# Envelope-Based Spending Control Implementation

This plan details the implementation of an isolated, reusable module for real-time spending enforcement in Seaweed.

## Proposed Changes

### [New Module] :applications:seaweed:features:spendingcontrol

Created a new library module to house the envelope logic, keeping it decoupled from the main application.

#### [Envelope.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/features/spendingcontrol/src/main/java/com/zoewave/probase/seaweed/features/spendingcontrol/domain/Envelope.kt)
- Defined the core `Envelope` data class and `EnvelopePriority` enum.

#### [DecisionEngine.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/features/spendingcontrol/src/main/java/com/zoewave/probase/seaweed/features/spendingcontrol/domain/DecisionEngine.kt)
- Interface for evaluating transactions against envelopes.

#### [InterventionFlowOrchestrator.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/features/spendingcontrol/src/main/java/com/zoewave/probase/seaweed/features/spendingcontrol/domain/InterventionFlowOrchestrator.kt)
- Manages the state and logic for intercepting and resolving declined transactions.

#### [RulesBasedClassifier.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/features/spendingcontrol/src/main/java/com/zoewave/probase/seaweed/features/spendingcontrol/domain/RulesBasedClassifier.kt)
- Simple rules-based implementation of transaction classification for the POC.

#### [InterventionDialog.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/features/spendingcontrol/src/main/java/com/zoewave/probase/seaweed/features/spendingcontrol/ui/InterventionDialog.kt)
- Reusable UI component for the "Decline & Recover" flow.

---

### [Application Integration]

#### [AddTransactionViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/AddTransactionViewModel.kt)
- Injected the `InterventionFlowOrchestrator`.
- Added an interception step in `saveTransaction` to check limits before persisting data.

#### [AddTransactionUiRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/seaweed/apps/mobile/features/transaction/src/main/java/com/zoewave/probase/seaweed/mobile/transaction/ui/AddTransactionUiRoute.kt)
- Integrated `InterventionDialog` to show up when the orchestrator's state is non-null.

## Verification Plan

### Manual Verification
- **Test Case 1: Within Limit**
    - Add a transaction for $10 in "Dining".
    - It should save instantly.
- **Test Case 2: Exceed Limit**
    - Add a transaction for $60 in "Dining" (since limit is set to $50 in POC).
    - An "Intervention Dialog" should appear.
    - Click "Approve Anyway" to verify the override flow.
