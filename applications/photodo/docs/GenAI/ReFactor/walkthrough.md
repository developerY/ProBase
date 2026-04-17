# Smart Capture Improvements: ML Logs, Retake Navigation, and Privacy Enforcement

I have updated the Smart Capture feature to provide more transparency during AI analysis, fixed the "Retake" button navigation, and strictly enforced user privacy by removing gallery access.

## Key Changes

### 1. ML Logs during AI Processing
The AI engines now provide real-time feedback during the multimodal extraction and OCR processes.
- **[CloudCaptureEngineImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/data/CloudCaptureEngineImpl.kt)**: Added logs for prompt preparation, request sending, and response parsing for the Gemini API.
- **[LocalCaptureEngineImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/data/LocalCaptureEngineImpl.kt)**: Added logs for ML Kit OCR initialization and regex extraction steps.
- **[SmartCaptureScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/ui/SmartCaptureScreen.kt)**: Increased the number of visible logs in the loading indicator from 3 to 5 for better visibility.

### 2. Privacy Enforcement: No Gallery Access
In accordance with the privacy requirement that the app never touches the user's personal data (camera roll):
- **[SmartCaptureScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/ui/SmartCaptureScreen.kt)**: Removed all "Upload Photo" functionality, including the image picker and the upload button in the empty state.

### 3. Fixed Retake Button Navigation
The "Retake" button now correctly routes the user back to the camera flow.
- **[SmartCaptureUiRoute](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/ui/SmartCaptureScreen.kt)**: Added an `onRetakeRequest` callback.
- **[photoTodoNavEntryProvider.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/src/main/java/com/zoewave/probase/photodo/mobile/ui/navigation/photoTodoNavEntryProvider.kt)**: Implemented the callback to pop the current screen and navigate directly to the camera.

### 4. Cloud Fallback Diagnostics UI
Added transparency when the cloud analysis falls back to local AI due to errors or missing configuration.
- **[SmartCaptureScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/ui/SmartCaptureScreen.kt)**: Integrated an `IconButton` with a diagnostic icon directly into the "Cloud analysis unavailable" warning banner.
- **[DiagnosticsDialog](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/ui/SmartCaptureScreen.kt)**: Added a dialog that displays the underlying logs and errors (e.g., "Orchestrator: Cloud failed...", "Tier 1: Cloud AI requested...") in a scrollable list when the icon is clicked.

### 6. Text-Only AI Task Extraction
Added the ability to generate tasks using Cloud AI from simple text commands, even without an image.
- **[SmartCaptureScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/ui/SmartCaptureScreen.kt)**: Updated the `EmptyState` to include a task command input field and an "Analyze Text" button.
- **[CloudCaptureEngineImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/data/CloudCaptureEngineImpl.kt)**: Implemented text-only prompt processing when no image is provided, correctly generating structured task drafts from user commands like "clean the car".
- **[SmartCaptureOrchestrator.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/data/SmartCaptureOrchestrator.kt)**: Updated to allow text-only extraction when Cloud AI is enabled, providing a clear error message if attempted without a valid Cloud setup.

### 7. Direct Access to AI Input
Added a new entry point to navigate directly to the text-only AI input flow without taking a photo.
- **[PhotoTodoRoute.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/model/src/main/java/com/zoewave/probase/photodo/model/navigation/PhotoTodoRoute.kt)**: Updated `SmartCapture` route to make the photo URI optional, allowing for direct navigation.
- **[HomeOverviewScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/categories/HomeOverviewScreen.kt)**: Integrated an **"AI Task"** button into the dashboard's Floating Action Button (FAB) menu, providing instant access to the text-only task extraction feature. The icon is tinted **Gold** (`#FFD700`) for visual distinction and **automatically hides** if Cloud AI is disabled in settings.

### 8. Global Task Search in "Jump Back In" Section
Relocated and implemented a powerful global search feature directly within the "Jump Back In" section of the Dashboard.
- **[TaskSearchBar](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/HomeSearchComponents.kt)**: A clean, modular search component positioned at the start of the task listing section.
- **[TaskSearchResultsList](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/HomeSearchComponents.kt)**: Replaces the urgent/favorite list when searching, displaying results grouped by project. Clicking on any task result navigates the user directly to the project detail screen.
- **[HomeViewModel](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/HomeViewModel.kt)**: Reactive search logic that automatically filters tasks as the user types (2+ characters), providing an instant, contextual search experience.

### 10. Independent Task and Category Search
Decoupled the search queries for tasks and categories, allowing users to search for each independently without affecting the other.
- **[HomeUiState](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/HomeUiState.kt)**: Split the single `searchQuery` into `categorySearchQuery` and `taskSearchQuery`.
- **[HomeEvent](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/HomeEvent.kt)**: Added separate events `OnCategorySearchQueryChanged` and `OnTaskSearchQueryChanged`.
- **[HomeViewModel](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/HomeViewModel.kt)**: Updated the reactive logic to independently handle and filter both task and category searches.
- **[UI Variants](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/HomeScreen.kt)**: Updated all home screen variants to use their respective search queries and handlers, ensuring a smooth and intuitive user experience.

### 9. Collapsible Category Summary Section
Made the "Total PhotoDo Categories" section collapsible to allow more vertical space for tasks when needed.
- **[OverviewSummaryCard](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/OverviewSummaryCard.kt)**: Updated with a clickable header and an expand/collapse icon. The donut chart and detailed legend are now wrapped in an `AnimatedVisibility` block.
- **[HomeUiState](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/HomeUiState.kt)**: Added `isCategoriesSummaryExpanded` to track the toggle state.
- **[HomeViewModel](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/HomeViewModel.kt)**: Implemented `OnToggleCategoriesSummary` event handling to update the expansion state reactively.
- **[Adaptive/Compact Layouts](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/home/src/main/java/com/zoewave/probase/photodo/mobile/features/home/ui/components/home/HomeScreen.kt)**: Ensured consistent search behavior and UI placement across all device sizes and screen modes.

### 11. Refactor to MAD Best Practices ("Gold Standard")
Refactored all Photodo mobile top-level screen composables to strictly follow Modern Android Development best practices.
- **Strict Signature**: Every screen now only takes exactly `(uiState, onEvent, navTo)`. This simplifies testing and separates UI from logic.
- **Hoisted Side Effects**: Moved all `LaunchedEffect` and complex navigation decisions (e.g., auto-navigation on save, data loading on entry) from the screens to the `NavEntry` provider level.
- **Standardized Navigation**: Unified all external navigation through a single `navTo` callback, improving predictability and code readability.
- **Centralized UI State**: Moved UI-specific state (like dialog visibility and FAB expansion) into the respective ViewModels and `UiState` data classes, making the entire screen state observable and reproducible.

### 8. Conditional Smart Advice Icon
Ensured the Smart Advice icon (Question Mark) only appears when relevant and enabled.
- **[TaskDetailScreen.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/TaskDetailScreen.kt)**: Updated the top bar actions to conditionally show the AI help icon. It now only appears if:
    1.  **AI features are enabled** in settings.
    2.  The project has **at least one task or photo** to provide context to the AI.
- **[TaskDetailViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/apps/mobile/features/tasks/src/main/java/com/zoewave/probase/photodo/mobile/features/tasks/ui/detail/TaskDetailViewModel.kt)**: Updated to reactively track the AI enabled state from the settings repository and expose it to the UI.

### 5. Fixed AI Model Synchronization and Log Preservation
Ensured the correct AI model from settings is always used and preserved detailed logs for failed attempts.
- **[SmartCaptureViewModel.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/ui/SmartCaptureViewModel.kt)**: Updated to fetch the latest model preference from the settings repository right before starting any image analysis.
- **[SmartCaptureOrchestrator.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/data/SmartCaptureOrchestrator.kt)**: Updated to preserve and merge logs from the Cloud AI engine even when it fails, ensuring the model used and specific error messages are visible in the diagnostics.
- **[CloudCaptureEngineImpl.kt](file:///Users/developer/AndroidStudioProjects/ProBase/features/ai/capture/src/main/java/com/zoewave/probase/features/ai/capture/data/CloudCaptureEngineImpl.kt)**: Updated model discovery to keep the `models/` prefix for better SDK compatibility and improved error handling to return logs instead of throwing exceptions.

> [!NOTE]
> The issues with Gemini API key validation were identified as device-specific and have been rolled back to maintain the original codebase state for AI configuration.

## Verification Summary

### Build Verification
- Successfully ran `gradle_build("app:assembleDebug")` to ensure the project remains stable after cleaning up the rollback state.

### Manual Verification Steps (Performed via Code Analysis)
- Verified that `SmartCaptureViewModel` uses `_uiState.update { ... }` to thread-safely append logs as they arrive from the orchestrator.
- Verified that all call sites of `SmartCaptureUiRoute` were updated to handle the new callback.
- Verified that `EmptyState` in `SmartCaptureScreen` no longer accepts or displays an upload button.
