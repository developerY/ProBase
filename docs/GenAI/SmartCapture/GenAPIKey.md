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
- **Project Duration**: Added a `durationMillis` field to [ProjectEntity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/db/src/main/java/com/zoewave/probase/applications/photodo/db/entity/ProjectEntity.kt) to support tracking planned project lengths alongside due dates.
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
- **Architectural Independence**: Achieved 100% isolation for the "**Time Budgeting**", "**Calendar Integration**", and "**Camera/Save Photo**" features. Each now has its own standalone persistence layer (where applicable) or logic located directly within its feature module.
- **Time Budgeting Module**:
    - Created `TimeBudgetDatabase` and `TimeBudgetDao` inside `:applications:photodo:features:timebudgeting`.
    - Features its own isolated repository and ViewModel logic.
- **Calendar Module**:
    - Created `CalendarSyncDatabase` and `CalendarSyncDao` inside `:features:calendar` for reusable system calendar logic.
    - **Functional UI**: Implemented a functional `CalendarScreen` and `CalendarViewModel` that query the system calendar for events within a 60-day range.
    - **Runtime Permissions**: Integrated full permission checking (`READ_CALENDAR`, `WRITE_CALENDAR`) with a dedicated rationale UI, ensuring the feature complies with Android's security model.
    - **Feature Inventory Integration**: Linked the core calendar feature to the main app's feature inventory, allowing developers to test querying and deleting system events.
    - Added a dedicated `:applications:photodo:features:calendar` module to handle PhotoDo-specific calendar integration and UI.
- **Camera Feature Module**:
    - Created a new `:applications:photodo:features:camera` module to house PhotoDo-specific camera capture and "**Save Photo**" logic.
    - Relocated `SavePhotoViewModel`, `SavePhotoBottomSheet`, and `AddPhotoToTaskUseCase` to this module.
    - Refactored navigation to decouple camera result handling and destination selection from the main application flow.
- **Database Cleanup**: Removed all budgeting and calendar-specific code from the main `:applications:photodo:db` module.
- **AutoMigrations**: Maintained schema compatibility in the main database while offloading feature-specific tables to their respective modules.

## UX Improvements
- **Contextual Delete Actions**: Improved the Top App Bar by hiding the delete (trashcan) icon when there is nothing to delete. This reduces visual noise and prevents "broken button" confusion in empty states.
- **Quick Pick Categories**: Added a "**Quick Pick**" section to the new category sheet. Users can now create common categories (Work, Personal, Home, Shopping, Travel) with a single tap, eliminating the need for manual typing.

## Dark Code: Smart Capture (AI)
- **New Feature Module**: Created `:features:smartcapture` to handle AI-powered task extraction from photos. This module is isolated and ready for integration into any ProBase app.
- **BYOK + Local Fallback Architecture**:
    - **Tier 1 (Pro Engine)**: Uses the **Gemini 1.5 Flash Cloud SDK** for high-fidelity multimodal parsing if a user provides their own API key.
    - **Tier 2 (Local Engine)**: Automatically falls back to **ML Kit Text Recognition** and a local Regex/Heuristic engine to extract core task details (titles, dates, budgets) offline.
- **Clean Orchestration**: Implemented `SmartCaptureOrchestrator` to manage tiered execution and ensure zero technical debt by abstracting engines behind a common interface.
- **Reactive UI**: Built a Material 3 screen that supports photo uploads via `PickVisualMedia`, displays a "Verify Extracted Task" pane, and allows users to confirm the structured result.
- **Feature Inventory Integration**: Fully integrated into the main app's feature inventory for end-to-end testing of the AI pipeline.

## Verification Summary
- Successfully ran builds for all affected modules, including the new `:applications:photodo:features:calendar`.
- Verified that Room generated independent schema files for each persistence-enabled module.

### Manual Verification
- Verified the dynamic UI of the `QuickProjectBottomSheet` through Compose Previews, confirming it correctly switches between "Quick Project" and "Home Project" based on the context.
- Confirmed project thumbnail rendering and fallback logic in `ProjectRow` and `ProjectCard`.

![Quick Project Bottom Sheet](file:///Users/developer/Library/Caches/Google/AndroidStudio2025.3.4/projects/probase.459da513/.artifacts/20260408-121845-f050a219-5417-4b19-9eb3-6b3efdb80dfe/QuickProjectBottomSheetPreview.png)
