# AshBike 🚴‍♂️

Next-generation ride tracking built with Kotlin, Jetpack Compose, Navigation 3, and a modular MVI architecture.

---

## 🌟 Overview

AshBike is a modern health and cycling fitness platform designed to deliver a **truly standalone Wear OS experience**, a feature-packed companion **Android Mobile app**, and projected **Smart Glasses (XR)** telemetry displays.

### Our Engineering Philosophy
1. **Wear OS Autonomy:** The watch can do everything untethered—GPS tracking, local Room persistence, and direct cloud sync.
2. **Local-First & Privacy-Focused:** Data remains on-device first, with strictly-scoped Health Connect synchronization.
3. **Reactive & Deterministic:** Jetpack Compose UI state is powered by pure `StateFlow` streams with strict feedback-loop protections (`distinctUntilChanged`).

---

## 🚀 Key Features

* **Standalone Wear OS Tracking:** Track cycling workouts using watch hardware sensors via Android Health Services, independent of the phone.
* **Smart Health Connect Sync:** Seamlessly bridge completed rides into Google Health Connect using a Play-Policy compliant **13-permission set** tailored strictly for AshBike (Exercise, Distance, Heart Rate, and Calories).
* **Accurate Caloric Burn Engine:** Uses an aerodynamic MET-curve algorithm emitting **per-tick delta calories**, guarded by:
  * *Stoplight Guard:* Zeroes calorie burn when stationary.
  * *Phantom Speed Guard:* Ignores GPS bounce while holding time deltas in reserve.
  * *Pause Guard:* Prevents large jumps when resuming tracking after long pauses.
* **Nav3 Type-Safe Navigation:** Built on AndroidX Navigation 3 (`NavEntry`, `NavDisplay`, state lists), delivering smooth, full-screen top-level destination switching.
* **Projected Smart Glasses (Glass) Telemetry:** Real-time HUD integration for gear selection, electronic suspension toggles, and live speed/elevation profiles.

---

## 🏗️ Architecture & Module Breakdown

AshBike uses a multi-module feature structure to maximize code sharing across Mobile, Wear, and XR targets:

```
applications/ashbike/
├── model/                # Form-factor agnostic domain models (BikeRide, LocationPoint)
├── database/             # Room DB, DAOs, DataStore Preferences (AppSettings, UserProfile)
├── data/                 # BikeRepositoryImpl, BLE location engines, Sync Engine
├── features/
│   ├── main/             # BikeForegroundService, CalculateCaloriesUseCase, RideStatsUseCase
│   └── places/           # Coffee shop & route landmark discovery
└── apps/
    ├── mobile/           # Mobile App entry point, Nav3 NavDisplay, and feature modules:
    │   ├── features/home/      # Main Dashboard & Live Speedometer
    │   ├── features/rides/     # Ride History List & Detailed Ride Analytics
    │   ├── features/settings/  # Units, GPS accuracy, and Profile configuration
    │   └── features/glass/     # Projected XR HUD panels & Gear controls
    └── wear/             # Wear OS App entry point, Wear Compose UI, and standalone features
```

---

## 🧪 Testing & Quality Assurance

AshBike is backed by a comprehensive unit testing infrastructure powered by **JUnit 4**, **MockK**, **kotlinx-coroutines-test**, and **Truth**.

### Key Test Coverage
* **UseCases & Calculations:** `CalculateCaloriesUseCaseTest`, `RideStatsUseCaseTest`, `CalculationTests` (100% logic validation with mockable virtual clocks).
* **Persistence & Mappers:** `BikeRideDaoTest` (In-memory Room DB), `RideMappersTest`, `DataStoreAppSettingsRepositoryTest`, `DataStoreUserProfileRepositoryTest`.
* **Repositories & Services:** `BikeRepositoryImplTest`, `AshBikeHealthPermissionPolicyTest`.
* **ViewModels:** `HomeViewModelTest`, `RidesViewModelTest`, `SettingsViewModelTest`, `GlassViewModelTest`, `WearBikeViewModelTest`, `WearRidesViewModelTest`.

### Running Tests
To run all AshBike unit tests without compiling unrelated monorepo modules:
```bash
./gradlew \
    :applications:ashbike:database:testDebugUnitTest \
    :applications:ashbike:data:testDebugUnitTest \
    :applications:ashbike:model:testDebugUnitTest \
    :applications:ashbike:features:main:testDebugUnitTest \
    :applications:ashbike:apps:mobile:testDebugUnitTest \
    :applications:ashbike:apps:wear:testDebugUnitTest \
    --continue
```
*(Or right-click the `applications/ashbike` directory in Android Studio and select **Run 'Tests in ashbike'**).*

---

## 📚 Documentation Directory

Detailed technical guides, architectural decision records, and Play Store release documents are organized in `docs/`:

* **Getting Started:** [`docs/GETTING_STARTED.md`](docs/GETTING_STARTED.md)
* **Architecture Overview:** [`docs/architecture/ARCHITECTURE_OVERVIEW.md`](docs/architecture/ARCHITECTURE_OVERVIEW.md)
* **Mathematical Calculations:** [`docs/architecture/Bike_Calculations.md`](docs/architecture/Bike_Calculations.md)
* **State Management & Flow Loops:** [`docs/architecture/STATE_MANAGEMENT.md`](docs/architecture/STATE_MANAGEMENT.md)
* **Health Permissions Architecture:** [`docs/data_and_privacy/HEALTH_PERMISSIONS.md`](docs/data_and_privacy/HEALTH_PERMISSIONS.md)
* **Testing Strategy:** [`docs/testing/TESTING_STRATEGY.md`](docs/testing/TESTING_STRATEGY.md)
* **Test Execution Guide:** [`docs/testing/TEST_EXECUTION.md`](docs/testing/TEST_EXECUTION.md)
* **Production Readiness Checklist:** [`docs/release/PRODUCTION_READINESS.md`](docs/release/PRODUCTION_READINESS.md)
* **Full Documentation Hub:** [`docs/README.md`](docs/README.md)
