# PhotoDo Mobile 📱

The mobile client for the PhotoDo ecosystem, built with the "Gold Standard" of Modern Android Development (MAD).

## Architectural Excellence

This module serves as the primary interface for PhotoDo, adhering to high-performance and high-resiliency standards:

*   **Reactive State Production**: ViewModels utilize the `stateIn` operator and `combine` to derive UI State reactively from underlying data streams. This ensures a "Single Source of Truth" and eliminates manual state fragmentation.
*   **Absolute Main-Safety**: All database and repository operations are explicitly offloaded to `Dispatchers.IO` within the repository implementation, ensuring the UI remains fluid and responsive even during heavy data processing.
*   **Lifecycle-Aware Navigation**: Integration with `SavedStateHandle` allows ViewModels to reactively retrieve navigation arguments (like `projectId` or `categoryId`) directly from the backstack. This makes the application resilient to process death and configuration changes.
*   **Adaptive UI**: Leverages Material 3 Adaptive layouts and `Nav3` to provide a seamless experience across compact (phone) and expanded (tablet/foldable) screen sizes.

## Key Technical Features

*   **100% Jetpack Compose**: Modern, declarative UI building.
*   **Hilt DI**: Clean dependency management across features.
*   **Flow-Driven Data**: Real-time updates from Room through the repository layer.
*   **Safe Navigation**: State-driven navigation via `Nav3`.

## Feature Modules

The app is divided into clean feature modules:
*   `:features:home`: The dashboard and category overview.
*   `:features:tasks`: Project lists, checklists, and the "Gold Standard" task detail experience.
*   `:features:settings`: Reactive app configuration and user preferences.

---
Part of the [PhotoDo](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/README.md) suite.
