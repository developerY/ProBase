# Getting Started with AshBike Development

Welcome to the AshBike project! This guide will help you set up your local development environment and run the app for the first time.

## 📋 Prerequisites

1. **Android Studio**: Use the latest stable version (Ladybug/Meerkat recommended for Compose previews and Nav3 tool support).
2. **Java Development Kit (JDK)**: JDK 17+ is required.
3. **Devices**: 
   - A physical Android phone (recommended for BLE and GPS testing).
   - An Android Emulator (useful for GPX route simulation).
   - A Wear OS 4+ Emulator or physical watch (for Wear companion app testing).

## 🛠️ Building the Project

The AshBike project consists of multiple app entry points (`:applications:ashbike:apps:mobile` and `:applications:ashbike:apps:wear`).

### Debug Builds
Debug builds work out of the box. Simply select the `apps.mobile` or `apps.wear` run configuration in Android Studio and click **Run**.

### Release Builds
We enforce signed release builds for production readiness. For local release testing, the build script automatically falls back to your local `debug.keystore` if production signing keys are omitted from environment variables. 
To build a release bundle locally:
```bash
./gradlew :applications:ashbike:apps:mobile:bundleRelease
```

## 🧪 Running Unit Tests

To run unit tests specifically for AshBike without building unrelated monorepo modules:
```bash
./gradlew :applications:ashbike:apps:mobile:testDebugUnitTest \
          :applications:ashbike:features:main:testDebugUnitTest \
          :applications:ashbike:database:testDebugUnitTest \
          :applications:ashbike:data:testDebugUnitTest \
          --continue
```
For detailed execution commands and IDE options, see [`testing/TEST_EXECUTION.md`](testing/TEST_EXECUTION.md).

## 🚲 Running Simulations
If you are testing at your desk and cannot ride a bike, you can simulate a ride using GPX files:
1. Open the Emulator Extended Controls -> Location.
2. Load one of the `.gpx` route files located in `applications/ashbike/docs/actions/` (e.g. `central_park_loop.gpx`).
3. Set the playback speed.
For detailed instructions, see [`operations/SIMULATION.md`](operations/SIMULATION.md).

## 🏥 Health Connect Setup
To test Health Connect sync:
1. Ensure the **Health Connect** app is installed on your testing device (pre-installed on Android 14+).
2. AshBike utilizes a Play-policy compliant **13-permission set** (Exercise, Distance, Heart Rate, Calories).
3. Grant permissions on first ride or through the Settings tab.
For details on per-app permission scoping, see [`data_and_privacy/HEALTH_PERMISSIONS.md`](data_and_privacy/HEALTH_PERMISSIONS.md).

## 📖 Key Documentation to Read Next
* [ARCHITECTURE_OVERVIEW.md](architecture/ARCHITECTURE_OVERVIEW.md) - System architecture and module dependencies.
* [Bike_Calculations.md](architecture/Bike_Calculations.md) - Aerodynamic MET calorie engine & speed/distance math.
* [STATE_MANAGEMENT.md](architecture/STATE_MANAGEMENT.md) - MVI pattern & Flow loop prevention.
* [TESTING_STRATEGY.md](testing/TESTING_STRATEGY.md) - Comprehensive testing framework setup.
* [FINAL_MAD_AUDIT_v0.0.4.md](release/FINAL_MAD_AUDIT_v0.0.4.md) - Master release audit & readiness verification.
