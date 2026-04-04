# PhotoDo Wear OS ⌚

A high-performance companion app for PhotoDo, optimized for the "Gold Standard" of wearable computing.

## "Gold Standard" Architecture

The Wear OS app mirrors the architectural excellence of the mobile suite, tailored for the constraints and unique interactions of a watch:

*   **Reactive & Declarative**: Like the mobile app, Wear ViewModels use `SavedStateHandle` to observe navigation arguments reactively. This enables a purely declarative navigation provider and ensures state persistence across transitions.
*   **Optimized Sync Engine**: The synchronization process between phone and watch is performance-tuned. Heavy CPU tasks like bitmap grayscale transformations are offloaded to `Dispatchers.Default`, while data broadcasts use `Dispatchers.IO`, protecting the host device's UI fluidity.
*   **Main-Safe Data Flow**: All data operations are main-safe, ensuring the small-screen UI remains responsive and "magic" in its real-time updates.
*   **Wear OS Best Practices**: Implements standard Wear OS patterns including `AppScaffold`, `SwipeToDismissBox`, and `ScalingLazyColumn` for a native, intuitive experience.

## Key Features

*   **Real-Time Sync**: View categories, projects, and tasks synced directly from your phone.
*   **Visual Thumbnails**: Contextual thumbnails for categories and projects are synced as high-efficiency grayscale assets.
*   **Read-Only Workspace**: Designed for quick context checks on the go—see your checklists and photo documentation at a glance.
*   **Proactive Refresh**: Features a proactive "Sync Now" mechanism to ensure data is always current.

## Technical Highlights

*   **Compose for Wear OS**: Using the latest Material 3 components for Wear.
*   **Nav3**: Shared navigation philosophy with the mobile app for consistency.
*   **Hilt**: Dependency injection tailored for Wear features.

---
Part of the [PhotoDo](file:///Users/developer/AndroidStudioProjects/ProBase/applications/photodo/README.md) suite.
