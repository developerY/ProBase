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

## Dark Code: Time Tracking & Budgeting
- **Isolated Schema**: Integrated "**Time Tracking**" and "**Time Budgeting**" as dark code in the database layer. This ensures the database is ready for future features without exposing them to the UI yet.
- **Architectural Isolation**:
    - Created new sub-packages: `entity/time` and `repo/time`.
    - Used Kotlin's `internal` visibility for all new classes (`TimeLogEntity`, `TimeBudgetEntity`, `TimeTrackingDao`, `TimeTrackingRepository`), ensuring they are only accessible within the `db` module.
- **AutoMigrations**: Incremented database version to **3** and implemented Room `AutoMigrations` (from 1 to 2, and 2 to 3) to automatically handle the schema updates for all users.
- **New Budgeting Fields**: Added `estimatedTimeMillis` to `TaskEntity` to support future per-task time planning.
- **Category-Level Goals**: Introduced `TimeBudgetEntity` to allow setting time targets for entire categories (e.g., "Goal: 10 hours/week for Research").
- **No UI Exposure**: Purposely omitted Hilt providers for the new repository and DAO to prevent accidental usage in other modules.

## Verification Summary

### Automated Tests
- Successfully ran builds for `:applications:photodo:apps:mobile:features:home`, `:applications:photodo:apps:mobile:features:tasks`, and `:applications:photodo:db`.
- Verified that Room generated the `2.json` and `3.json` schema files, confirming that the migrations are correctly configured.

### Manual Verification
- Verified the dynamic UI of the `QuickProjectBottomSheet` through Compose Previews, confirming it correctly switches between "Quick Project" and "Home Project" based on the context.
- Confirmed project thumbnail rendering and fallback logic in `ProjectRow` and `ProjectCard`.

![Quick Project Bottom Sheet](file:///Users/developer/Library/Caches/Google/AndroidStudio2025.3.4/projects/probase.459da513/.artifacts/20260408-121845-f050a219-5417-4b19-9eb3-6b3efdb80dfe/QuickProjectBottomSheetPreview.png)
