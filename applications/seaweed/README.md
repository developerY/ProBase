# Seaweed 🌿

A finance application focused on "Real-time Money Profile" and "Honest Budgeting" built with Modern Android Development (MAD) principles.

## Philosophy

**Seaweed** is designed to provide a realistic view of a user's financial situation. Unlike traditional trackers, Seaweed deducts all **Cyclic Bills** (Rent, Insurance, Subscriptions) from monthly income upfront. This establishes a **Real Starting Balance**, showing the user exactly how much "Flexible Money" they have left for the rest of the month.

## Key Features

*   **Real-time Money Profile**: Prominent display of "Flexible Money Remaining" after all fixed costs and daily spending are accounted for.
*   **Cyclic Bill Management**: Categorized tracking of recurring expenses (Housing, Utilities, Transportation, Subscriptions).
*   **Guided Setup**: Automatically pre-fills common bills to jumpstart the user's financial profile.
*   **Spending Pace**: Visual indicators showing money spent versus time passed in the month.
*   **Offline-First & Reactive**: Powered by Room and Flow for a responsive, real-time experience.
*   **Adaptive Layouts**: Built with `Material 3 Adaptive` for a premium experience on phones, tablets, and foldables.

# 🌿 Seaweed Ecosystem

---

## ⌚ Seaweed Wear OS
**A glanceable companion for Seaweed, keeping your financial reality on your wrist.**

### 🏛️ "Gold Standard" Architecture
The Wear OS app is modularized for performance and follows modern best practices:
*   **Reactive UI**: ViewModels reactively derive state from repository flows.
*   **Modular Features**: Decoupled into `home`, `transactions`, and `bills` modules for better isolation and testing.
*   **Wear OS Best Practices**: Uses `ScalingLazyColumn` and optimized Material 3 components for wearables.

### ✨ Key Features
*   **Real Money Glance**: Instantly see your remaining flexible money.
*   **Fixed Cost List**: Quick view of all upcoming recurring bills and their monthly impact.
*   **Recent Spend**: View latest transactions synced from your phone.

---

## 📱 Seaweed Mobile
**The primary financial dashboard, providing deep insights and management tools.**

### 🏛️ Architectural Excellence
*   **Reactive State Production**: Derives UI state using `stateIn` and `combine` for a true Single Source of Truth.
*   **Main-Safety**: All data operations are offloaded to `Dispatchers.IO`.
*   **Adaptive UI**: Uses `ListDetailPaneScaffold` to support various device configurations.
*   **Type-Safe Navigation**: Driven by the `Nav3` framework with centralized route definitions.

### 📦 Feature Modules
*   `:features:home` — The "Real Money" dashboard and overview.
*   `:features:bills` — Management of all recurring and cyclic expenses.
*   `:features:transaction` — Daily expense tracking and category management.
*   `:features:settings` — Theme configuration and profile setup.

---

## License

This application is part of the ProBase repository and is protected by a **Source Available License**.
