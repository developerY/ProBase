# Walkthrough: Add Project Photo and Enhanced Quick Project Logic

I've completed the implementation of project photo support and the differentiated logic for "Quick Project" and "Home Project".

## Key Changes

### Database & UI Models
- **Project Photos**: Updated [ProjectWithTasks.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/entity/ProjectWithTasks.kt) and [CategoryWithProjectsAndTasks.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/entity/CategoryWithProjectsAndTasks.kt) to support fetching project photos.
- **Thumbnail Support**: Added `thumbnailUri` to [ProjectListUiModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/state/ProjectListUiModel.kt) and updated ViewModels to populate it.

### UI Components
- **Project Thumbnails**: Modified [ProjectRow.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/components/ProjectRow.kt) and [ProjectCard.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/components/ProjectCard.kt) to display photo thumbnails using Coil's `AsyncImage`.
- **Quick Project Bottom Sheet**: Created [QuickProjectBottomSheet.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/components/QuickProjectBottomSheet.kt) with templates for Quick Fix, Quick Buy, and Quick Find.

### Project Creation Logic
- **Differentiated Category Assignment**:
    - **Quick Project** (from project list screen) adds projects to the **current category**. If no categories exist yet, it defaults to a "**Default**" category.
    - **Home Project** (from home screen) always adds projects to the "**Home**" category (creating it if it doesn't exist).
- **Fixed Empty State Visibility**: Refactored `HomeUiState` to ensure the "**Home Project**" menu can be opened even when no categories exist yet.
- **Default Task Creation**: Projects created via these templates automatically include a default task (e.g., "fix quick task").
- **Name Collision Handling**: Automatically appends a number (e.g., "fix 1", "fix 2") if a project with the same name already exists.

## Navigation Enhancements
- **Automatic Back Navigation**: Implemented a side-effect mechanism to automatically navigate the user back to the previous screen level after a successful deletion.
    - Deleting a **Category** takes you back to the **Home** dashboard.
    - Deleting a **Project** takes you back to the **Category's project list**.
- **Empty Tasks Tab Redirection**: Added logic to automatically navigate to the Home screen if the user is on the Tasks tab and no categories exist in the database.
- **Side Effect Channel**: Added a `TasksSideEffect` channel to `TasksViewModel` and `TaskDetailViewModel` to decouple business logic from navigation.

## Crash Fixes
- **NavDisplay Stability**: Fixed a `java.lang.IllegalArgumentException: NavDisplay backstack cannot be empty` crash by refactoring the navigation backstack to use atomic updates. This ensures the backstack always contains at least one route during transitions.

## Complete Feature Isolation
- **Architectural Independence**: Achieved 100% isolation for the "**Time Budgeting**" and "**Calendar Integration**" features. Each now has its own standalone persistence layer (Database, DAO, Entities) located directly within its feature module.
- **Time Budgeting Module**:
    - Created `TimeBudgetDatabase` and `TimeBudgetDao` inside `:applications:photodo:features:timebudgeting`.
    - Features its own isolated repository and ViewModel logic.
- **Calendar Module**:
    - Created `CalendarSyncDatabase` and `CalendarSyncDao` inside `:features:calendar` for reusable system calendar logic.
    - Added a dedicated `:applications:photodo:features:calendar` module to handle PhotoDo-specific calendar integration and UI.
    - **Feature Inventory Integration**: Linked the core calendar feature to the main app's feature inventory, allowing for isolated testing and development.
- **Database Cleanup**: Removed all budgeting and calendar-specific code from the main `:applications:photodo:db` module.
- **AutoMigrations**: Maintained schema compatibility in the main database while offloading feature-specific tables to their respective modules.

## Verification Summary
- Successfully ran builds for all affected modules, including the new `:applications:photodo:features:calendar`.
- Verified that Room generated independent schema files for each persistence-enabled module.

### Manual Verification
- Verified the dynamic UI of the `QuickProjectBottomSheet` through Compose Previews, confirming it correctly switches between "Quick Project" and "Home Project" based on the context.
- Confirmed project thumbnail rendering and fallback logic in `ProjectRow` and `ProjectCard`.

![Quick Project Bottom Sheet](file:///Users/developer/Library/Caches/Google/AndroidStudio2025.3.4/projects/probase.459da513/.artifacts/20260408-121845-f050a219-5417-4b19-9eb3-6b3efdb80dfe/QuickProjectBottomSheetPreview.png)
