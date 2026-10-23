# Health Connect Integration & Monorepo Architecture

## Executive Overview

This document outlines the architecture, data flow, and permission handling models for **Google Health Connect** across the ProBase monorepo.

The repository follows a **Clean Multi-Module Architecture** that balances:
1. **Centralized Platform Interop (`:features:health:core`)**: A shared, robust foundation for Google Health Connect SDK integration, permission handling, and health data queries.
2. **Product-Specific App Features (`applications/<app>/...`)**: Custom UI routes and ViewModels tailored to individual product domains (e.g., caffeine kinetics in **GoSwift**, skin/hydration analysis in **KoColor**, ride telemetry in **AshBike**).

---

## High-Level Architecture Diagram

```
                       ┌──────────────────────────────────────────┐
                       │     Google Health Connect SDK (Android)  │
                       └────────────────────┬─────────────────────┘
                                            │
                       ┌────────────────────▼─────────────────────┐
                       │  Shared Core Feature (:features:health)  │
                       │  • Permission Controller & Intents       │
                       │  • HealthDataRepository / Use Cases       │
                       │  • Common Models (Sleep, HRV, Nutrition) │
                       └──────────────┬─────────────┬─────────────┘
                                      │             │
              ┌───────────────────────┴──┐       ┌──┴───────────────────────┐
              │   GoSwift Application    │       │   KoColor Application    │
              │  • Hummingbird Fuel Theme│       │  • Skin & Hydration Theme│
              │  • PK Microdosing Limits │       │  • Ritual Correlation    │
              └──────────────────────────┘       └──────────────────────────┘
```

---

## Architectural Layers

### Layer 1: Centralized Health Core Library (`:features:health:core`)

The shared library at `features/health/core` (`com.zoewave.probase.features.health.core`) acts as the single source of truth for Health Connect interactions across all monorepo applications.

* **Key Components**:
  * **`HealthDataRepository`**: Defines standard suspend methods for fetching `SleepSummary`, `HrvSample`, `HeartRateSample`, and `DailyActivitySummary`.
  * **`HealthRoute` & `HealthViewModel`**: Generic Compose routes managing permission state checking and side effects.
  * **`HealthConnectionStatus`**: Standard UI card rendering **`✔ Access Granted`** badges and **Manage Permissions** buttons.
  * **Intent Contracts**: Centralizes launching `Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)`.

* **Benefits**:
  * **Single Source of Truth**: SDK availability checks, permission contracts, and data mapping logic are implemented once and tested thoroughly.
  * **Unified Security & Compliance**: Ensures all monorepo apps adhere strictly to Google Play Data Safety and Health Connect policy requirements.

---

### Layer 2: Product-Specific App Features (`applications/<app>/...`)

Each app in `applications/` consumes the core health domain while delivering custom UX tailored to its target audience:

| Application | Product Focus | UI Route & Theme |
| :--- | :--- | :--- |
| **GoSwift** | Caffeine microdosing & PK clearance | `SettingsUiRoute.kt` (Dark "Hummingbird Fuel" theme) |
| **KoColor** | Skin analysis, hydration & wellness rituals | `HealthUiRoute.kt` (Light / Pastel theme) |
| **AshBike** | Cycling ride strain & heart rate zones | `RidesUIRoute.kt` (High-contrast outdoor theme) |

---

## Permission Management & User Experience Flow

The permission lifecycle handles initial authorization as well as ongoing permission management:

```
                      [User Taps "Manage Permissions"]
                                     │
                    Check HealthConnectClient.getSdkStatus()
                                     │
           ┌─────────────────────────┼────────────────────────┐
           ▼                         ▼                        ▼
    [SDK_UNAVAILABLE]     [SDK_UPDATE_REQUIRED]        [SDK_AVAILABLE]
           │                         │                        │
  Show Toast Warning       Prompt Play Store Update     Check Granted
                                                        Permissions
                                                              │
                                            ┌─────────────────┴────────────────┐
                                            ▼                                  ▼
                                    [Not Yet Granted]                  [Already Granted]
                                            │                                  │
                                 Launch Request Dialog               Launch App Settings
                              (PermissionController)          (ACTION_HEALTH_CONNECT_SETTINGS)
```

### 1. Initial Permission Request (Not Granted)
When permissions have not yet been granted, tapping **Manage Permissions** launches:
```kotlin
permissionLauncher.launch(healthConnectPermissions)
```
This displays the standard Google Health Connect system bottom sheet asking the user to authorize specific data streams (e.g., Activity, Sleep, Heart Rate, Nutrition).

### 2. Managing Granted Permissions (Access Granted)
Once permissions are granted:
* The UI displays an **`✔ Access Granted`** badge.
* Tapping **Manage Permissions** opens the Health Connect App Settings page for the app:
```kotlin
val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
context.startActivity(intent)
```
This opens Android's Health Connect permissions settings page specifically for the app, allowing users to toggle individual data types on or off or revoke access altogether.

---

## Required Manifest Declarations

Every application consuming Health Connect must declare the necessary permissions and intent rationale filters in its `AndroidManifest.xml`:

```xml
<!-- Health Connect Permissions -->
<uses-permission android:name="android.permission.health.READ_STEPS" />
<uses-permission android:name="android.permission.health.READ_SLEEP" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE_VARIABILITY" />
<uses-permission android:name="android.permission.health.READ_NUTRITION" />
<uses-permission android:name="android.permission.health.WRITE_NUTRITION" />
<uses-permission android:name="android.permission.health.READ_HYDRATION" />
<uses-permission android:name="android.permission.health.WRITE_HYDRATION" />

<queries>
    <package android:name="com.google.android.apps.healthdata" />
</queries>

<application ...>
    <activity
        android:name=".MainActivity"
        android:exported="true">
        <!-- Rationale & Permission Usage Intents -->
        <intent-filter>
            <action android:name="androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE" />
        </intent-filter>
        <intent-filter>
            <action android:name="android.intent.action.VIEW_PERMISSION_USAGE" />
            <category android:name="android.intent.category.HEALTH_PERMISSIONS" />
        </intent-filter>
    </activity>
</application>
```

---

## Developer Guidelines & Best Practices

1. **SDK Availability Check**: Always verify `HealthConnectClient.getSdkStatus(context)` before attempting to create a client or launch permission intents.
2. **Graceful Fallbacks**:
   * If `SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED`, prompt the user to update Health Connect via Play Store.
   * If `SDK_UNAVAILABLE`, inform the user via Toast or status card rather than crashing.
3. **Resumed Permission Syncing**: Implement `Lifecycle.State.RESUMED` observers in Compose routes so permission status updates automatically when the user returns from Health Connect settings.
