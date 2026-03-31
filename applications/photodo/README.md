# PhotoDo 📸

A photo-centric task management application built with Modern Android Development (MAD) principles.

## Philosophy

**PhotoDo** is designed around a "photo-first" philosophy. We believe that visual documentation is key to effective task management. Whether you're documenting a completed cleaning task, keeping track of a shopping list, or managing a complex project, PhotoDo lets you capture and attach images directly to your checklists.

## Key Features

*   **Project-Based Organization**: Group your tasks into logical projects (e.g., "Home Improvement," "Weekly Shopping").
*   **Granular Checklists**: Every project can have multiple task items with easy-to-track completion states.
*   **Visual Documentation**: Attach photos to tasks or projects for easy reference and proof-of-work.
*   **Budgeting & Urgency**: Track project budgets and toggle urgency/favorite status for better task prioritization.
*   **Offline-First & Reactive**: Powered by a local Room database with Flow-based updates, ensuring a smooth and responsive experience.
*   **Adaptive Layouts**: Built using `Material 3 Adaptive` and `Nav3`, providing a tailored experience for phones, tablets, and foldables.

## Technical Highlights

*   **Architecture**: Multi-module Monorepo setup following Clean Architecture principles.
*   **UI Toolkit**: 100% Jetpack Compose with Material 3.
*   **Navigation**: Powered by `Nav3` for state-driven, type-safe navigation.
*   **Data Persistence**: Room Database for local storage and Hilt for dependency injection.
*   **ML Integration (Roadmap)**: OCR to convert photo text into notes.

## Roadmap

1.  **Phase 1 (Completed)**: Data foundation with Room entities and Repository.
2.  **Phase 2 (Completed)**: Initial task list and project management UI.
3.  **Phase 3 (In-Progress)**: Advanced navigation and detail views.
4.  **Phase 4**: CameraX integration for photo-to-task capture.
5.  **Phase 5**: OCR text extraction and Canvas mode for visual brainstorming.

## Local Development

### Prerequisites
* Android Studio (Koala or later recommended)
* JDK 17+

### Running the App
1.  Open the project in Android Studio.
2.  Select the `applications.photodo.apps.mobile` run configuration.
3.  Deploy to your emulator or physical device.

---

## License

This application is part of the ProBase repository and is protected by a **Source Available License**. Please refer to the root [LICENSE.md](file:///Users/developer/AndroidStudioProjects/ProBase/LICENSE.md) for details.
