# AshBike Comprehensive Testing Strategy

This document outlines the testing architecture, tools, and coverage strategies used across the AshBike monorepo to ensure maximum reliability and code quality.

---

## 1. Testing Technology Stack

AshBike relies on a modern, industry-standard Android testing stack:
*   **JUnit 4**: The core test runner for local JVM unit tests.
*   **MockK**: For mocking and spying on dependencies (Repositories, UseCases, System Services).
*   **Kotlinx Coroutines Test**: For testing suspend functions, StateFlows, and time-based emissions (`runTest`, `UnconfinedTestDispatcher`, `StandardTestDispatcher`).
*   **Truth**: Fluent assertion library for readable and maintainable assertions (e.g., `assertThat(value).isEqualTo(expected)`).
*   **Robolectric**: For running tests that require Android framework components (like Room Databases or Context) directly on the local JVM without an emulator.

---

## 2. Test Coverage by Layer

### A. Database & Persistence Layer (`:database`)
*   **Target**: DAOs, Room Entities, DataStore Repositories, and Mappers.
*   **Tools**: Robolectric (for in-memory Room DBs), MockK.
*   **Key Test Suites**: 
    *   `BikeRideDaoTest`: Verifies entity insertion, flow retrieval, and constraint enforcement.
    *   `DataStoreAppSettingsRepositoryTest`: Verifies DataStore reads/writes for user settings.
    *   `RideMappersTest`: Guarantees mathematically safe conversions between UI models, Domain models, and Database entities.

### B. Data & Hardware Integration Layer (`:data`)
*   **Target**: Repositories managing hardware state (Gears, Suspension, BLE Connections).
*   **Tools**: MockK, Coroutines Test.
*   **Key Test Suites**:
    *   `BikeRepositoryImplTest`: Exclusively tests business rules for hardware simulation (e.g., enforcing gear limits between 1 and 12, cycling suspension states, guarding projected display connections).

### C. Core Business Logic (`:features:main`)
*   **Target**: Mathematical algorithms, telemetry processing, and UseCases.
*   **Tools**: JUnit, Coroutines Test (virtual time control).
*   **Key Test Suites**:
    *   `CalculateCaloriesUseCaseTest`: Uses virtual time advancement to guarantee the MET curve algorithm does not double-count stationary time, handles GPS bounce gracefully, and scales per user weight.
    *   `RideStatsUseCaseTest`: Validates the accumulation flows for average speed and max speed, proving zero division guards.

### D. Presentation & UI State (`:apps:mobile`, `:apps:wear`)
*   **Target**: ViewModels powering the MVI architecture.
*   **Tools**: MockK, Coroutines Test.
*   **Key Test Suites**:
    *   `HomeViewModelTest`, `RidesViewModelTest`, `SettingsViewModelTest`, `GlassViewModelTest`: These suites mock out the underlying repositories and verify that dispatching specific Intents (`Events`) correctly mutates the `StateFlow` consumed by Jetpack Compose.
    *   `WearBikeViewModelTest`: Ensures Wear OS UI state translates correctly to commands sent via the Wearable DataLayer.

---

## 3. Running the Tests

Because AshBike is a heavily modularized monorepo, tests can be run either specifically by module or globally.

### Running Globally via Gradle (CLI)
To execute every unit test across the entire project simultaneously:
```bash
./gradlew :applications:ashbike:database:testDebugUnitTest \
          :applications:ashbike:data:testDebugUnitTest \
          :applications:ashbike:features:main:testDebugUnitTest \
          :applications:ashbike:apps:mobile:features:rides:testDebugUnitTest \
          :applications:ashbike:apps:mobile:features:home:testDebugUnitTest \
          :applications:ashbike:apps:mobile:features:settings:testDebugUnitTest \
          :applications:ashbike:apps:mobile:features:glass:testDebugUnitTest \
          :applications:ashbike:apps:wear:features:rides:testDebugUnitTest \
          :applications:ashbike:apps:wear:data:testDebugUnitTest
```

### Running Locally via Android Studio
1. Open the **Project View**.
2. Right-click the `applications/ashbike` root directory.
3. Select **Run 'Tests in ashbike'**. Android Studio will compile a matrix of all tests and display the results visually.

---

## 4. Future Test Implementations

*   **Jetpack Compose UI Tests**: Implementing `createComposeRule()` instrumented tests to verify button clicks, navigation events, and visual states.
*   **Screenshot Golden Testing**: Utilizing Robolectric or Paparazzi to snapshot Compose UI states to prevent visual regressions during styling changes.
*   **End-to-End (E2E) Journey Tests**: Orchestrating full ride simulations via `.gpx` files injected into an emulator to test the `BikeForegroundService` end-to-end integration.
