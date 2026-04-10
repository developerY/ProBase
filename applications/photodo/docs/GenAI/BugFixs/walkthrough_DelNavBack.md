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
- **Side Effect Channel**: Added a `TasksSideEffect` channel to `TasksViewModel` and `TaskDetailViewModel` to decouple business logic from navigation.

## Dark Code: Time Tracking & Budgeting
- **Reusable Feature Module**: Created a new `:applications:photodo:features:timebudgeting` module to house the UI and logic for time management. This module is currently "**dark code**" and is not linked to the rest of the application.
- **Isolated Schema**: Integrated "**Time Tracking**" and "**Time Budgeting**" in the database layer.
- **Architectural Isolation**:
    - Created new sub-packages: `entity/time` and `repo/time` in the `db` module.
    - Implemented `TimeBudgetScreen` and `TimeBudgetViewModel` within the new feature module, using `internal` visibility to keep them encapsulated.
- **AutoMigrations**: Incremented database version to **3** and implemented Room `AutoMigrations` (from 1 to 2, and 2 to 3).
- **New Budgeting Features**:
    - Added `estimatedTimeMillis` to `TaskEntity`.
    - Introduced `TimeBudgetEntity` for category-level goals.
    - Created a reactive `TimeBudgetViewModel` that aggregates category data and spending limits.

## Verification Summary

### Automated Tests
- Successfully ran builds for `:applications:photodo:apps:mobile:features:home`, `:applications:photodo:apps:mobile:features:tasks`, `:applications:photodo:db`, and the new `:applications:photodo:features:timebudgeting`.
- Verified that Room generated the `2.json` and `3.json` schema files.

### Manual Verification
- Verified the dynamic UI of the `QuickProjectBottomSheet` through Compose Previews, confirming it correctly switches between "Quick Project" and "Home Project" based on the context.
- Confirmed project thumbnail rendering and fallback logic in `ProjectRow` and `ProjectCard`.

![Quick Project Bottom Sheet](file:///Users/developer/Library/Caches/Google/AndroidStudio2025.3.4/projects/probase.459da513/.artifacts/20260408-121845-f050a219-5417-4b19-9eb3-6b3efdb80dfe/QuickProjectBottomSheetPreview.png)
