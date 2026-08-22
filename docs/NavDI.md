Here is the complete architectural record for your documentation. It explains exactly why the KoColor dependency stack is structured the way it is, serving as a definitive guide for any future engineers joining the project.

---

# KoColor Core Architecture: Navigation & Dependency Injection Stack (2026)

**Document Path:** `server/docs/Architecture/Core/Dependency_Stack_2026.md`

## 1. Executive Summary

The KoColor Android application is built on the absolute bleeding edge of modern Jetpack architecture (as of mid-2026). The dependency stack strictly avoids legacy Android frameworks, favoring fully declarative, type-safe, and Kotlin-native solutions.

This document outlines the structural decisions behind our two most critical foundational layers: **Navigation** and **Dependency Injection**.

---

## 2. Navigation Architecture: Jetpack Navigation 3 (Nav3)

KoColor utilizes **Jetpack Navigation 3 (`androidx.navigation3`)**, a ground-up rewrite of the navigation system designed specifically for Jetpack Compose. We do not use the legacy `androidx.navigation:navigation-compose` (Nav2) library.

There is no "Nav4"; Nav3 represents the highest tier of modern Android navigation architecture.

### 2.1 Core Paradigms of Nav3

* **State-Driven Backstack:** Instead of fighting a black-box `NavController`, the application manages a `SnapshotStateList` of states. The UI observes this state directly, adhering to the pure unidirectional data flow of Compose.
* **Absolute Type Safety:** Nav3 entirely eliminates string-based routing and XML graphs. Every destination is a type-safe object or class, preventing runtime route-parsing crashes.
* **The Scenes API & Adaptive Layouts:** We utilize `material3AdaptiveNav3` to support polymorphic layouts natively. This allows KoColor to display multiple destinations side-by-side (e.g., List-Detail panes on foldables and tablets) without rewriting navigation logic.
* **Wear OS Integration:** Through `wearComposeNav3`, the navigation graph natively handles Wear OS-specific hardware gestures, such as swipe-to-dismiss.

### 2.2 Nav3 Version Catalog Implementation

```toml
nav3Core = "1.1.6" # Latest stable Nav3 runtime
lifecycleViewmodelNav3 = "2.11.0"
material3AdaptiveNav3 = "1.3.0-rc01"

```

---

## 3. Dependency Injection: Hilt + KSP

KoColor uses **Dagger-Hilt** for dependency injection. The architecture eliminates the legacy KAPT (Kotlin Annotation Processing Tool) entirely, running Hilt natively through **KSP (Kotlin Symbol Processing)**.

### 3.1 The Dual-Track Versioning System

Our `libs.versions.toml` reflects two distinct versioning tracks for Hilt. This is intentional and architecturally correct:

1. **Core Dagger-Hilt (`2.60.1`):** Maintained by the core Dagger team. This is the primary dependency injection engine.
2. **AndroidX Hilt Extensions (`1.4.0`):** Maintained by the Jetpack team. These are the bridges that natively connect Hilt to our Compose lifecycles, Navigation nodes, and WorkManager.

### 3.2 The KSP & K2 Compiler Advantage

By pairing Hilt `2.60.1` with KSP `2.3.9` and Kotlin `2.4.10`, the KoColor build pipeline bypasses the Java stub-generation phase entirely.

**Architectural Benefits:**

* **Fully Kotlin-Native Analysis:** Hilt analyzes the Kotlin AST directly.
* **Build Speed:** Compiling the multi-module dependency graph is 30-50% faster than legacy KAPT setups.
* **K2 Ready:** The DI graph is fully compatible with the K2 compiler's performance enhancements.

### 3.3 Hilt Version Catalog Implementation

```toml
# Core Engine
hilt = "2.60.1" 

# AndroidX Compose/Lifecycle Bridges
hiltLifecycleViewModel = "1.4.0"
hiltLifecycleViewModelCompose = "1.4.0"
hiltNavigationCompose = "1.4.0"
hiltWork = "1.4.0"

```

---

## 4. Multi-Module Enforcement

Because we rely on KSP and Nav3, our multi-module boundaries must remain strict.

* **Feature Modules** may depend on `androidx-navigation3-runtime` to define their screens.
* **App Module** orchestrates the `SnapshotStateList` and wires the Hilt `@AndroidEntryPoint`.
* **Domain/Model Modules** remain pure Kotlin and contain zero Hilt or Navigation dependencies, ensuring they can be unit-tested instantly without the Android framework.