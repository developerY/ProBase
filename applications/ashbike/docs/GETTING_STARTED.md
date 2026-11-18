# Getting Started with AshBike Development

Welcome to the AshBike project! This guide will help you set up your local development environment and run the app for the first time.

## Prerequisites

1. **Android Studio**: Use the latest stable version (or Ladybug/Meerkat if you need specific Compose previews).
2. **Java Development Kit (JDK)**: JDK 17+ is required.
3. **Devices**: 
   - A physical Android phone (recommended for BLE and GPS testing).
   - An Android Emulator (useful for GPX route simulation).
   - A Wear OS 4+ Emulator or physical watch (for Wear companion app testing).

## Building the Project

The AshBike project consists of multiple apps (`:applications:ashbike:apps:mobile` and `:applications:ashbike:apps:wear`).

### Debug Builds
Debug builds will work out of the box. Simply select the `apps.mobile` or `apps.wear` run configuration and hit **Run**.

### Release Builds
We enforce signed release builds for production readiness. However, for local release testing, the build script is designed to safely fall back to your local `debug.keystore` if the production keys are not found in your environment variables. 
To test a minified release build locally:
```bash
./gradlew :applications:ashbike:apps:mobile:assembleRelease
```

## Running Simulations
If you are testing at your desk and cannot ride a bike, you can simulate a ride using GPX files.
1. Open the Emulator Extended Controls -> Location.
2. Load one of the GPX files located in `applications/ashbike/docs/actions/`.
3. Set the playback speed.
For detailed instructions, see [operations/SIMULATION.md](operations/SIMULATION.md).

## Health Connect Setup
To test Health Connect sync:
1. Ensure the **Health Connect** app is installed on your testing device (pre-installed on Android 14+).
2. You will be prompted for permissions on your first ride. 
3. Verify data writes using the Health Connect Toolbox.

## Key Documentation to Read Next
* [ARCHITECTURE.md](architecture/ARCHITECTURE.md) - Understand the module boundaries.
* [TestPlan.md](testing/TestPlan.md) - Learn how we test UI and logic.
