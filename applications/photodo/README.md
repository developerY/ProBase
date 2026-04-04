# PhotoDo 📸

A photo-centric task management application built with Modern Android Development (MAD) principles.

## Philosophy

**PhotoDo** is designed around a "photo-first" philosophy. We believe that visual documentation is key to effective task management. Whether you're documenting a completed cleaning task, keeping track of a shopping list, or managing a complex project, PhotoDo lets you capture and attach images directly to your checklists.

## Key Features

*   **Project-Based Organization**: Group your tasks into logical projects (e.g., "Home Improvement," "Weekly Shopping").
*   **Granular Checklists**: Every project can have multiple task items with easy-to-track completion states.
*   **Visual Documentation**: Attach photos to tasks for easy reference and proof-of-work.
*   **Budgeting & Urgency**: Track project budgets and toggle urgency/favorite status for better task prioritization.
*   **Offline-First & Reactive**: Powered by a local Room database with Flow-based updates, ensuring a smooth and responsive experience.
*   **Adaptive Layouts**: Built using `Material 3 Adaptive` and `Nav3`, providing a tailored experience for phones, tablets, and foldables.

# 📸 PhotoDo Ecosystem

Below is the architectural and feature documentation for the PhotoDo companion suite, built adhering to modern Android development best practices.

---

## ⌚ PhotoDo Wear OS
**A high-performance companion app for PhotoDo, optimized for the "Gold Standard" of wearable computing.**

### 🏛️ "Gold Standard" Architecture
The Wear OS app mirrors the architectural excellence of the mobile suite, tailored specifically for the constraints and unique interactions of a smartwatch:

* **Reactive & Declarative:** Like the mobile app, Wear ViewModels use `SavedStateHandle` to observe navigation arguments reactively. This enables a purely declarative navigation provider and ensures state persistence across transitions.
* **Optimized Sync Engine:** The synchronization process between the phone and watch is performance-tuned. Heavy CPU tasks (like bitmap grayscale transformations) are offloaded to `Dispatchers.Default`, while data broadcasts use `Dispatchers.IO`, protecting the host device's UI fluidity.
* **Main-Safe Data Flow:** All data operations are strictly main-safe, ensuring the small-screen UI remains responsive and "magical" in its real-time updates.
* **Wear OS Best Practices:** Implements standard Wear OS patterns including `AppScaffold`, `SwipeToDismissBox`, and `ScalingLazyColumn` for a native, intuitive experience.

### ✨ Key Features
* **Real-Time Sync:** View categories, projects, and tasks synced directly from your phone.
* **Visual Thumbnails:** Contextual thumbnails for categories and projects are synced as high-efficiency, grayscale `Asset` objects to preserve bandwidth.
* **Read-Only Workspace:** Designed for quick context checks on the go—see your checklists and photo documentation at a glance without risking sync conflicts.
* **Proactive Refresh:** Features a proactive "Sync Now" mechanism to ensure data is always current when moving out of Bluetooth range.

### 🛠️ Technical Highlights
* **Compose for Wear OS:** Built using the latest Material 3 components for Wear.
* **Nav3 Architecture:** Shares a cohesive navigation philosophy with the mobile app for ecosystem consistency.
* **Hilt Integration:** Scoped dependency injection tailored for Wear OS feature modules.

---

## 📱 PhotoDo Mobile
**The mobile client for the PhotoDo ecosystem, built with the "Gold Standard" of Modern Android Development (MAD).**

### 🏛️ Architectural Excellence
This module serves as the primary interface for PhotoDo, adhering to high-performance and high-resiliency standards:

* **Reactive State Production:** ViewModels utilize the `stateIn` operator and `combine` to derive UI State reactively from underlying data streams. This ensures a strict "Single Source of Truth" and eliminates manual state fragmentation.
* **Absolute Main-Safety:** All database and repository operations are explicitly offloaded to `Dispatchers.IO` within the repository implementation, ensuring the UI remains fluid and responsive even during heavy data processing or image compression.
* **Lifecycle-Aware Navigation:** Integration with `SavedStateHandle` allows ViewModels to reactively retrieve navigation arguments (like `projectId` or `categoryId`) directly from the backstack. This makes the application highly resilient to process death and configuration changes.
* **Adaptive UI:** Leverages Material 3 Adaptive layouts and Nav3 to provide a seamless, premium experience across compact (phone) and expanded (tablet/foldable) screen sizes.

### ✨ Key Technical Features
* **100% Jetpack Compose:** Modern, purely declarative UI construction.
* **Hilt DI:** Clean, predictable dependency management across all features.
* **Flow-Driven Data:** Real-time, reactive updates from Room through the repository layer to the UI.
* **Safe Navigation:** Type-safe, state-driven routing via the Nav3 framework.

### 📦 Feature Modules
The application is strictly decoupled into clean, isolated feature modules:

* `:features:home` — The main dashboard and category overview layer.
* `:features:tasks` — Project lists, checklists, and the "Gold Standard" task detail and photo capture experience.
* `:features:settings` — Reactive app configuration, theming, and user preferences.

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
