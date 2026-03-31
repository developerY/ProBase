# PhotoDo Performance Audit

This audit evaluates the **PhotoDo** application for potential performance bottlenecks in UI rendering and data processing.

## 🔴 Critical Issues

### 1. Inefficient UI State Mapping (Heavy ViewModel Logic)
**Location:** `HomeViewModel.kt`
**Problem:** The `uiState` is derived by iterating over a list of `CategoryWithProjects`, where each entry contains another list of `ProjectEntity`. For each project, it performs filtering and multiple object transformations on every database update.
- **Impact:** As the number of categories and projects grows, this mapping logic (O(N*M)) runs on the main thread (or the thread the flow is collected on), potentially causing frame drops during rapid data updates.
- **Recommendation:** Move heavy mapping to a background thread using `flowOn(Dispatchers.Default)` before `stateIn`.

### 2. Lack of Database Indexing
**Location:** `ProjectEntity.kt`, `TaskEntity.kt`, `PhotoEntity.kt`, `ExpenseEntity.kt`
**Problem:** While foreign keys are indexed, other common filter/sort columns are not.
- **Impact:** `getAllCategories` sorts by `name ASC`, and `getAllProjects` sorts by `creationDate DESC`. Without indices on `name` and `creationDate`, Room must perform a full table scan and sort in memory.
- **Recommendation:** Add indices to `name` in `CategoryEntity` and `creationDate` in `ProjectEntity`.

## 🟡 Optimization Opportunities

### 1. Recomposition Risk (Unstable Types)
**Location:** `TasksListScreen.kt`, `ProjectCard.kt`
**Problem:** The `TasksUiState` and `ProjectListUiModel` are standard data classes. Compose might not always infer them as stable if they contain certain types (like `List`).
- **Impact:** Passing these models to child composables may trigger unnecessary recompositions even if the specific data the child needs hasn't changed.
- **Recommendation:** Use `@Immutable` or `@Stable` annotations on UI state/models to explicitly signal stability to the Compose compiler.

### 2. Redundant Computations in UI (ProjectCard)
**Location:** `ProjectCard.kt`
**Problem:** `ProjectCard` performs currency formatting (`NumberFormat.getCurrencyInstance`) during every recomposition.
- **Impact:** While relatively fast, this is a redundant computation that adds up in a `LazyColumn`.
- **Recommendation:** Pre-format the currency strings in the `ViewModel` or `UiModel`, or `remember` the formatter.

### 3. Derived State Opportunity
**Location:** `TasksListScreen.kt` (FAB icon logic)
**Problem:** The FAB icon uses `derivedStateOf` for the icon vector, which is good, but the `checkedProgress` comes from `ToggleFloatingActionButton`.
- **Recommendation:** Ensure all complex UI logic that depends on other state is wrapped in `remember(key) { derivedStateOf { ... } }` to avoid recalculating during every frame of an animation.

## 🟢 Best Practices Found
- **Nav3 Implementation:** Excellent use of state-driven navigation, which reduces the overhead of string-based route parsing.
- **Room Flow Usage:** Good use of observable queries to keep the UI in sync with the database automatically.
- **Multi-module Architecture:** Proper separation of concerns helps isolate build times and potentially limits the scope of recompilation.
