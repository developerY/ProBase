# 📸 PhotoDo

PhotoDo is a modern, photo-first task management Android application. Designed for visual organizers, it allows users to group task lists into high-level categories and attach photographic context to individual projects and checklists.

Built entirely with modern Android Development (MAD) principles, PhotoDo leverages Jetpack Compose, Room Database, Kotlin Coroutines/Flow, and the cutting-edge Compose Navigation 3 (Nav3) API.

## 🛠️ Tech Stack

* **UI:** Jetpack Compose (Material 3 Expressive)
* **Architecture:** MVVM with strict Unidirectional Data Flow (UDF)
* **Database:** Room (SQLite) with highly optimized `@Relation` queries
* **Navigation:** Compose Navigation 3 (`androidx.navigation3`)
* **Dependency Injection:** Dagger Hilt
* **Concurrency:** Kotlin Coroutines & Flow

---

## 🏗️ Architecture Overview

PhotoDo strictly adheres to a **Unidirectional Data Flow (UDF)** architecture. 

1.  **State Down:** ViewModels expose a single source of truth via `StateFlow` (e.g., `TasksUiState`). The UI is completely stateless and only renders what the ViewModel dictates.
2.  **Events Up:** User interactions (clicks, text input, swipes) are passed back to the ViewModel via sealed interfaces (e.g., `TasksEvent.OnAddCategoryClicked`).

### The Navigation Philosophy (Nav3)
PhotoDo utilizes the bleeding-edge **Compose Navigation 3**. Instead of relying on a black-box `NavController` in the UI layer, navigation is treated as pure state. 
* The backstack is managed as a standard Kotlin `List`.
* Screens are scoped dynamically using `NavEntry`.
* ViewModels are automatically scoped and cleared based on their presence in the backstack.

---

## 📂 Package Structure

The codebase is organized by **Feature**, not by layer. This ensures that everything related to a specific domain (like Tasks or Home) lives together.

```text
com.zoewave.probase.photodo.mobile
│
├── 📁 db                   # Room Database, DAOs, and Entities
│
├── 📁 navigation           # Global Nav3 entry providers and sealed routes
│
└── 📁 features             # Feature modules
    ├── 📁 home             # The Global Dashboard (Progress calculations)
    │
    └── 📁 tasks            # Task management pipeline
        ├── 📁 navigation   # Task-specific route arguments
        └── 📁 ui           
            ├── TasksListScreen.kt       # The Global "Buckets" View
            ├── TasksViewModel.kt        
            │
            ├── 📁 detail                # Contextual Task Views
            │   ├── TaskDetailScreen.kt
            │   └── TaskDetailViewModel.kt
            │
            └── 📁 components            # Reusable, stateless UI widgets
                ├── ProjectRow.kt
                ├── AddCategorySheet.kt
                └── AddListSheet.kt
```

### The `components` Rule
To maintain a clean architecture, the `components` package is strictly reserved for "dumb," stateless UI widgets. Screens (`*Screen.kt`), Routes (`*UiRoute.kt`), and ViewModels belong directly under their respective context (e.g., `ui/` or `ui/detail/`).

---

## 🗄️ Database Schema & Relationships

PhotoDo's Room database is heavily relational, designed to prevent orphaned data using strict SQLite Foreign Key constraints (`CASCADE`).

* **`CategoryEntity` (The Super Bucket):** The highest level of organization (e.g., "Real Estate").
* **`TaskListEntity` (The Project):** Belongs to a Category (e.g., "PreFab Home Build"). Deleting a Category cascades and deletes all its Task Lists.
* **`TaskItemEntity` (The Checklist):** Individual actionable items belonging to a Task List.
* **`PhotoEntity` (The Visual Context):** Photos attached directly to a Task List. Deleting a Task List cascades and deletes all associated photos and checklist items.

### Optimized Querying
Instead of manually combining Kotlin Flows in memory, PhotoDo leverages Room's `@Relation` annotation (e.g., `CategoryWithTaskLists` and `TaskListWithPhotos`). This pushes the heavy lifting of joining tables down to the C/C++ SQLite engine, resulting in massive performance gains and preventing UI stutter.

---

## 📱 UI/UX Patterns

### Contextual FABs (Floating Action Buttons)
To prevent users from accidentally creating orphaned data, the FAB menus are strictly contextual:
1.  **Global Screens (Home/Tasks):** The FAB only allows the creation of high-level containers ("New Category", "New List").
2.  **Detail Screens:** Once inside a specific list, the FAB transforms to only allow contextual additions ("Add Task", "Take Photo"), automatically injecting the correct `listId` under the hood.

### Material 3 Expressive
The application utilizes the latest `ExperimentalMaterial3ExpressiveApi` components, including the `FloatingActionButtonMenu` (Speed Dial) and expressive container coloring for dashboards.

---

## 🚀 Roadmap
* **v1.0 (Current):** Highly polished mobile experience for standard smartphone form factors.
* **v2.0 (Planned):** Adaptive Layouts. Implementing `ListDetailPaneScaffold` and `NavigationSuiteScaffold` to fully support foldables and tablets without rewriting underlying ViewModel logic.