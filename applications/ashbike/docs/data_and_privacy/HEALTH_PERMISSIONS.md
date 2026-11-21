# Health Connect Permission Architecture (AshBike & KoColor)

This document details how Google Health Connect permissions are architected, scoped, and resolved per-application within the monorepo (`AshBike` vs. `KoColor`).

---

## 🎯 Overview & Motivation

Google Play imposes strict Play Store Data Safety guidelines regarding Health Connect permissions: **apps must only request the minimal set of permissions strictly necessary for their features.**

Previously, health permissions were hardcoded in the shared `:features:health:core` module. This created a permission leakage risk—updating permissions for `AshBike` (which requires both READ and WRITE permissions for rides) would accidentally expand the permission footprint of `KoColor` (which only needs baseline wellness reads).

To solve this, a **Per-App Health Permission Policy** architecture was introduced, mirroring the `ResolvedDiagnosticDelegate` pattern.

---

## 🏗️ Architecture Design (`:features:health:core`)

The shared health module specifies an optional-binding pattern using Hilt:

```
                  ┌─────────────────────────────────────────┐
                  │        AppHealthPermissionPolicy        │
                  │               (Interface)               │
                  └────────────────────┬────────────────────┘
                                       │
            ┌──────────────────────────┴──────────────────────────┐
            ▼                                                     ▼
┌───────────────────────┐                             ┌───────────────────────┐
│ AshBikeHealthPolicy   │                             │  KoColorHealthPolicy  │
│ (13 Permissions:      │                             │ (6 Baseline          │
│  READ + WRITE Sync)   │                             │  Permissions)         │
└───────────────────────┘                             └───────────────────────┘
            │                                                     │
            └──────────────────────────┬──────────────────────────┘
                                       ▼
                     ┌───────────────────────────────────┐
                     │  DefaultHealthPermissionPolicy    │
                     │  (Fallback if app binds nothing)  │
                     └─────────────────┬─────────────────┘
                                       │
                                       ▼
                     ┌───────────────────────────────────┐
                     │ ResolvedHealthPermissionPolicy    │
                     │ (Injected into ViewModels/Cases)  │
                     └───────────────────────────────────┘
```

### 1. `AppHealthPermissionPolicy` Interface
Located in `:features:health:core:di`. Defines the contract:
```kotlin
interface AppHealthPermissionPolicy {
    val permissions: Set<String>
}
```

### 2. Optional Hilt Resolution
Using Hilt's `@BindsOptionalOf`, the system checks whether the active application module provides an `AppHealthPermissionPolicy` override:
* **If bound**: The app's specific policy is used.
* **If not bound**: The system automatically falls back to `DefaultHealthPermissionPolicy` (the 6-permission wellness baseline).

### 3. Injected Consumers
`HealthViewModel` and `GetHealthSummaryUseCase` inject `ResolvedHealthPermissionPolicy`. Neither component has hardcoded permissions anymore; they resolve the list dynamically at runtime.

---

## 🚴 AshBike Policy Setup

* **Module**: `apps/mobile/.../di/AshBikeHealthPermissionModule.kt`
* **Scope**: **13 Permissions** (Full Sync Writes + Dashboard Telemetry Reads)
* **Permissions Included**:
  * `WRITE_EXERCISE`
  * `READ_EXERCISE`
  * `READ_HEART_RATE`
  * `READ_DISTANCE`
  * `READ_ACTIVE_CALORIES_BURNED`
  * `READ_SPEED`
  * `READ_ELEVATION_GAINED`
  * *(Plus additional telemetry read/write types required for ride synchronization)*

### Safeguard & Testing
AshBike includes an automated test (`AshBikeHealthPermissionPolicyTest`) that executes `SyncRideUseCase` (with and without heart-rate sensor data) to guarantee that every `Record` type emitted by the ride sync engine is explicitly declared in `AshBikeHealthPermissionModule`.

---

## 🎨 KoColor Policy Setup

* **Module**: `applications/kocolor/apps/mobile/.../di/KoColorHealthPermissionModule.kt`
* **Scope**: **6 Permissions** (Minimum Baseline Wellness Reads)
* **Permissions Included**:
  * `READ_EXERCISE`
  * `READ_HEART_RATE`
  * `READ_ACTIVE_CALORIES_BURNED`
  * `READ_DISTANCE`
  * `READ_SPEED`
  * `READ_ELEVATION_GAINED`

KoColor binds an explicit minimal policy so that its Google Play Store submission can declare a minimal health data footprint, completely decoupled from AshBike's write requirements.

---

## ➕ Adding Health Permissions to a New App

If you create a new application in the monorepo that interacts with Health Connect:

1. Create a Hilt module in your app's mobile module:
   ```kotlin
   @Module
   @InstallIn(SingletonComponent::class)
   abstract class MyNewAppHealthPermissionModule {
       @Binds
       abstract fun bindPermissionPolicy(
           policy: MyNewAppHealthPermissionPolicy
       ): AppHealthPermissionPolicy
   }
   ```
2. Implement `AppHealthPermissionPolicy` returning only the permissions your app requires.
3. If you omit this module, Hilt will automatically fall back to `DefaultHealthPermissionPolicy`.
