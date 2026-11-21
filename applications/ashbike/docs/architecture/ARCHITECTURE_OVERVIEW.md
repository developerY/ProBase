# AshBike Architectural Overview

This document outlines the core architectural principles, design patterns, and technology stack that power the AshBike application. It is designed to ensure scalability, testability, and a strict separation of concerns across Mobile, Wear OS, and Smart Glasses.

---

## 🛠️ Core Technologies & Architecture

AshBike is built exclusively using a modern, Google-recommended Android tech stack:

*   **UI (User Interface)**:
    *   **Jetpack Compose**: The entire UI is built with this modern, declarative toolkit.
    *   **Navigation Compose**: Manages all in-app and screen navigation.
    *   **Wear Compose & Horologist**: Powers the companion smartwatch application.
    *   **Projected XR / Glimmer**: Powers the projected UI for smart glasses.
*   **Architecture & State Management**:
    *   **MVI (Model-View-Intent)**: A clean and scalable architectural pattern ensuring unidirectional data flow.
    *   **StateFlow & SharedFlow**: Manages reactive UI state and hardware telemetry streams in a lifecycle-aware manner.
    *   **Hilt**: Handles Dependency Injection (DI) throughout the application.
*   **Data & Background**:
    *   **Room**: For robust, offline-first local database storage.
    *   **DataStore**: For modern, safe storage of key-value preferences.
    *   **Foreground Services (FGS)**: Manages continuous location and BLE tracking while the app is backgrounded.
*   **Performance Optimization**:
    *   **Baseline Profiles**: Pre-compiles the app for significantly faster startup times.
    *   **Coroutines**: Manages all asynchronous operations to ensure a smooth, non-blocking UI.

---

## ✅ Separation of Concerns

The architecture correctly separates the app into distinct, independent layers. This is the most important principle of a good architecture, allowing the codebase to grow without becoming a tangled mess.

1.  **UI Layer (The "View")**: 
    *   Handled entirely by **Jetpack Compose**. 
    *   Its only job is to display the state given to it and send user intents (events like button clicks) to the ViewModel. 
    *   It is strictly declarative; it contains no business logic or database queries.
2.  **State & Logic Layer (The "ViewModel" & "UseCases")**: 
    *   The **ViewModel** acts as the brain for each screen. It takes user events, invokes UseCases, and prepares the data for the UI using `StateFlow`.
    *   **UseCases** (e.g., `RideStatsUseCase`, `CalculateCaloriesUseCase`) encapsulate complex, reusable business logic and mathematical algorithms (like calorie or speed calculations) outside of the ViewModel.
3.  **Data Layer (The "Model")**: 
    *   This layer handles all data operations. It's made up of **Repositories** that abstract away the data sources (Room database, Health Connect, BLE sensors). 
    *   The UI and ViewModels never know *where* the data comes from, making the data layer completely interchangeable and easy to test.

---

## 🧱 Modular Design

AshBike is structured as a multi-module monorepo. This allows different parts of the app to be developed, compiled, and tested independently.

*   **`:apps:mobile` / `:apps:wear`**: The application entry points. They bind the features together but contain minimal logic.
*   **`:features:*`**: Independent modules containing isolated functionality (e.g., `:features:home`, `:features:rides`, `:features:glass`).
*   **`:core:*`**: Foundational modules shared across the entire project (e.g., `:core:ui` for the AshBike theme, `:core:model` for domain objects).
*   **`:database`**: The isolated Room database schema and entity models.

It's like building a house with prefabricated rooms—it's faster, cleaner, and much easier to manage than building everything on-site from scratch.

---

## 💉 Dependency Injection (DI)

We use **Hilt** for dependency injection. 
*   Instead of manually instantiating classes (and heavily coupling them), Hilt acts as a system that automatically provides the right dependencies (like Repositories or UseCases) whenever they are needed. 
*   This makes it incredibly easy to add new features or mock out components during testing without breaking the entire structure.

---

## 🧪 Testability

A direct result of the strict separation of concerns, modular design, and Hilt DI is that AshBike is highly testable. Because each layer is independent:

*   **UseCases & Algorithms**: Complex math (like the `CalculateCaloriesUseCase`) can be tested entirely in isolation using pure JUnit tests without needing an Android emulator.
*   **ViewModels**: State transformations can be tested by passing in mock Repositories.
*   **Repositories**: Data fetching and local caching can be tested without needing a network connection.
*   **UI**: Automated UI tests and Screenshot tests can be run using Espresso and Compose UI Testing.

---

## 🚀 Modern and Future-Proof

By exclusively using modern, Google-recommended libraries from the Jetpack suite (Compose, Hilt, Room, Coroutines, Health Connect), AshBike is:

*   **Highly Efficient**: These libraries are optimized for Android performance, battery life, and memory management.
*   **Easier to Onboard**: Developers want to work with modern, idiomatic Kotlin tools.
*   **Ready for the Future**: The app is perfectly positioned to adopt new Android OS features (like Edge-to-Edge, Predictive Back, and GenAI) and will be vastly easier to maintain for years to come.
