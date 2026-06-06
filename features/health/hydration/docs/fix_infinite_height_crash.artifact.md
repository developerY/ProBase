# Implementation Plan - Fix Infinite Height Crash in Health Dashboard

I will resolve the `java.lang.IllegalStateException: Vertically scrollable component was measured with an infinity maximum height constraints` crash caused by nested `verticalScroll` modifiers in the Health & Wellness dashboard.

## 1. Research & Analysis
- **Root Cause**: Double `verticalScroll` modifiers.
    - Parent: `HealthUiRoute.kt` applies `Modifier.verticalScroll` to the `HealthContent` container.
    - Child: `StyleHealthDashboard.kt` applies `Modifier.verticalScroll` to its root `Column`.
- **Constraint Violation**: When a scrolling container is nested inside another, the inner one receives infinite height constraints.

## 2. Technical Steps

### UI Refactoring (`StyleHealthDashboard.kt`)
- [ ] **Remove `verticalScroll`**: Delete `.verticalScroll(rememberScrollState())` from the root `Column` in `StyleHealthDashboard`.
- [ ] **Adjust Modifiers**:
    - Remove `fillMaxSize()` from the root `Column` to allow it to be measured by its contents within the scrolling parent.
    - Ensure it uses `fillMaxWidth()` and appropriate padding.
- [ ] **Clean Up Imports**: Remove unused `rememberScrollState` and `verticalScroll` imports.

## 3. Verification
- [ ] **Build Check**: Run `:applications:kocolor:apps:mobile:assembleDebug` to ensure compilation.
- [ ] **Visual Check**: Verify in the Layout Editor that the Health dashboard remains scrollable via its parent container.
- [ ] **Runtime Check**: Confirm that navigating to the Health page no longer triggers a crash.

---
<!-- feedback_request -->
I've pinpointed the double-scrolling issue that's causing your crash. I'll remove the redundant scroll modifier from the dashboard component so it can be correctly hosted within the screen's main scrollable container.

**Should I proceed with the fix?**
