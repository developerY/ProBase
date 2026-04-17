# Refactor Photodo Mobile Composables to MAD "Gold Standard"

Refactor all top-level screen composables in `applications/photodo/apps/mobile` to strictly adhere to Modern Android Development (MAD) best practices. This ensures scalability, testability, and a clean separation of concerns.

## User Review Required

> [!IMPORTANT]
> This refactor strictly enforces the `(UiState, onEvent, navTo)` pattern for all top-level composables. Any existing logic (like side-effect handling or complex navigation logic) will be hoisted to the `NavEntry` level or encapsulated within `onEvent`.

## Proposed Changes

### Screens to Refactor

| Screen | File Path | Status |
| :--- | :--- | :--- |
| **Home** | `features/home/.../HomeScreen.kt` | To be updated |
| **Adaptive Home** | `features/home/.../AdaptiveHomeScreen.kt` | To be updated |
| **Home Overview** | `features/home/.../HomeOverviewScreen.kt` | To be updated |
| **Tasks List** | `features/tasks/.../TasksListScreen.kt` | To be updated |
| **Task Detail** | `features/tasks/.../TaskDetailScreen.kt` | To be updated |
| **Settings** | `features/settings/.../SettingsScreen.kt` | To be updated |

---

### Refactoring Pattern

For each screen, I will:
1.  **Standardize Signature**: Ensure the composable takes exactly `uiState`, `onEvent: (Event) -> Unit`, and `navTo: (PhotoTodoRoute?) -> Unit`.
2.  **Hoist Side Effects**: Move `LaunchedEffect` and complex navigation logic (e.g., handling ViewModel effects) to the `NavEntry` provider.
3.  **Encapsulate State**: Ensure all UI-only state (e.g., dialog visibility) is managed cleanly within the composable or passed via the `UiState`.
4.  **Simplify Navigation**: Use the `navTo` callback for all external navigation, passing `null` to indicate "navigate back".

### Example: TaskDetailScreen

#### [TaskDetailScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/TaskDetailScreen.kt)

- Update `TaskDetailScreen` signature.
- Remove redundant navigation logic and hoist to `NavEntry`.
- Ensure all interactions dispatch `TaskDetailEvent`.

---

## Verification Plan

### Automated Tests
- Run `gradle_build("app:assembleDebug")` to ensure compilation success.
- Verify that Hilt DI is unaffected by the refactor.

### Manual Verification
- Deploy the app and walk through all screens:
    - Home -> Category -> Task Detail.
    - Settings updates.
    - Camera flow and Smart Capture.
- Verify that "back" navigation works correctly on all screens.
- Ensure all dialogs (Add Task, Add Expense, Delete) still function perfectly.
