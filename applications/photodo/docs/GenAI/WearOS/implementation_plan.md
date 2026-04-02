# Photodo Wear OS Application

Create a Wear OS companion app for Photodo that allows users to view categories, projects, and tasks, and mark tasks as complete.

## User Review Required

- **Image Support**: I will attempt to display task/project images using Coil. Wear OS has limited memory, so I'll ensure they are downsampled.
- **Navigation**: Using Navigation 3 with `SwipeDismissableSceneStrategy` to match the Seaweed Wear implementation.
- **UI Design**: Using Material 3 for Wear OS (`androidx.wear.compose:compose-material3`).

## Proposed Changes

### Build & Project Configuration

#### [settings.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/settings.gradle.kts)
- Register `:applications:photodo:apps:wear`.

#### [NEW] [build.gradle.kts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/build.gradle.kts)
- Configure Wear OS application module with dependencies for Wear Compose Material 3, Navigation 3, Hilt, and Room.
- Include shared modules: `:applications:photodo:model`, `:applications:photodo:db`, `:core:ui`, `:core:util`.

---

### UI - Main Architecture

#### [NEW] [MainActivity.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/MainActivity.kt)
- Standard Wear OS Activity with Hilt and Splash Screen support.
- Sets content to `PhotoDoWearMainScreen`.

#### [NEW] [PhotoDoWearMainScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/ui/PhotoDoWearMainScreen.kt)
- Manages navigation backstack using `mutableStateListOf<PhotoTodoRoute>`.
- Uses `AppScaffold` and `SwipeToDismissBox`.
- Integrates `NavDisplay` with `SwipeDismissableSceneStrategy`.

#### [NEW] [PhotoDoWearNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/ui/navigation/PhotoDoWearNavEntryProvider.kt)
- Maps `PhotoTodoRoute` destinations to Wear OS screens.
- Supports `Home` (Categories), `TasksList` (Projects in Category), and `TaskDetail` (Tasks in Project).

---

### Features - Home (Categories)

#### [NEW] [HomeRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/features/home/HomeRoute.kt)
- Displays a `ScalingLazyColumn` of Categories.
- Each category shows its name and a progress indicator or task count.

#### [NEW] [HomeViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/features/home/HomeViewModel.kt)
- Uses `photoDoRepo.getCategoriesWithProjectsAndTasks()` to provide category overview data.

---

### Features - Projects (TasksList)

#### [NEW] [ProjectListRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/features/project/ProjectListRoute.kt)
- Displays a list of Projects within a selected Category.
- Each project card shows title, budget/date info, and progress.

#### [NEW] [ProjectListViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/features/project/ProjectListViewModel.kt)
- Provides projects for a specific `categoryId`.

---

### Features - Tasks (TaskDetail)

#### [NEW] [TaskDetailRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/features/task/TaskDetailRoute.kt)
- Displays tasks for a selected Project.
- Each task has a checkbox/toggle.
- Displays project images if available.

#### [NEW] [TaskDetailViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/features/task/TaskDetailViewModel.kt)
- Provides tasks for a `projectId`.
- Implements `onToggleTask` to mark tasks as complete via `photoDoRepo.updateTask`.

---

### Resources & Styling

#### [NEW] [PhotoDoWearTheme.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/java/com/zoewave/probase/photodo/wear/ui/theme/PhotoDoWearTheme.kt)
- Defines the Wear OS theme using Photodo's primary colors.

#### [NEW] [AndroidManifest.xml](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/wear/src/main/AndroidManifest.xml)
- Declares the Wear OS app, launcher activity, and hardware requirements.

## Verification Plan

### Automated Tests
- I'll try to run `./gradlew :applications:photodo:apps:wear:assembleDebug` to verify the module builds correctly.

### Manual Verification
- Deploy to a Wear OS emulator (if available/configured).
- Verify navigation flow: Category -> Project -> Task.
- Verify marking a task as complete updates the database (and is reflected if we go back).
- Verify images load in the Task Detail screen.
