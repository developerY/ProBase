# Navigate Back After Deleting Category or Project

The goal is to prevent the user from seeing a blank or "Not Found" screen after deleting the category or project they are currently viewing. We will implement a side-effect mechanism to trigger navigation back to the previous level.

## Proposed Changes

### Tasks Feature Module

#### [NEW] [TasksSideEffect.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/TasksSideEffect.kt)

- Define a `TasksSideEffect` sealed interface with a `NavigateBack` object.

#### [TasksViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/TasksViewModel.kt)

- Add a `Channel<TasksSideEffect>` to emit side effects.
- In `OnDeleteCategoryClicked`, emit `TasksSideEffect.NavigateBack` after successful deletion.

#### [TaskDetailViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/TaskDetailViewModel.kt)

- Add a `Channel<TasksSideEffect>` to emit side effects.
- In `OnDeleteTaskListClicked`, emit `TasksSideEffect.NavigateBack` after successful deletion.

### App Navigation

#### [photoTodoNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/navigation/photoTodoNavEntryProvider.kt)

- In the `NavEntry` blocks for `TasksList` and `TaskDetail`:
    - Use `LaunchedEffect` to collect effects from the ViewModel.
    - Call `navigateBack()` when `NavigateBack` effect is received.

## Verification Plan

### Automated Tests
- Run build to ensure compilation.

### Manual Verification
- **Scenario 1**: Go to a category's project list. Delete the category. Verify the app navigates back to the Home dashboard.
- **Scenario 2**: Go to a project's detail view. Delete the project. Verify the app navigates back to the Category's project list.
