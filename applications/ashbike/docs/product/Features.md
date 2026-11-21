# AshBike Feature Matrix

A detailed breakdown of all features currently implemented, those actively in development, and future planned capabilities for the AshBike ecosystem across Mobile, Wear OS, and Smart Glasses.

---

## ✅ Current Features (Implemented)

These features are fully built and deployed in the current codebase.

### Core Telemetry & Tracking
* **Live GPS Ride Tracking**: Real-time tracking of distance, speed, max speed, and elevation using device location services.
* **Smart Route Pathing**: Continuous plotting of the current ride path on a map using Google Maps Compose.
* **Caloric Burn Calculation**: A custom algorithm using a multi-tier MET curve based on current speed and user weight.
* **Jitter & Phantom Guard**: Intelligent distance filtering that ignores micro-GPS bounce when the user is waiting at a stoplight.
* **Elapsed Real-time Engine**: Prevents massive time jumps from timezone or cellular network clock syncs mid-ride.

### Connectivity & Hardware
* **BLE (Bluetooth Low Energy)**: Connects seamlessly to external e-bikes and fitness sensors.
* **Wear OS Companion App**: A fully standalone Wear OS 4+ companion app.
* **Dual-Tracking Hardware Handoff**: If the phone loses connection, the Wear app tracks the ride via Health Services and syncs orphaned data back to the phone upon reconnection.
* **Live Heart Rate Zones**: Displays dynamic, color-coded heart rate zones (Warm up, Fat Burn, Cardio, Peak) from connected sensors.
* **Battery Monitoring**: Segmented battery indicator showing the e-bike's remaining charge.

### Health & Privacy
* **Health Connect Integration**: Granular, permission-gated exporting of `ExerciseSessionRecord`, distance, speed, and calories directly into Google Health Connect.
* **Local-First Persistence**: All ride data is stored entirely on-device using a Room database. There is no cloud backend, ensuring absolute privacy.

### Wearables & Glasses
* **Projected XR / Smart Glasses (Glass Feature)**: A secondary Compose-based UI designed specifically for display glasses (using Glimmer components).
* **Wear OS Complications**: At-a-glance shortcuts on the watch face.

---

## 🚧 In Development (Active Branch)

These features are currently being built or are in final testing phases before release.

* **Google Maps "Find Cafes" Integration**: Using the Yelp API and Maps Compose to dynamically find and display coffee shops near the rider's active route.
* **Voice Control (Gen AI)**: Integrating the ML Kit GenAI Prompt API for on-device natural language voice controls ("End my ride", "How far have I gone?").
* **Edge-to-Edge Adaptive Layouts**: Optimizing the dashboard and map views to fully utilize large screens (Tablets/Foldables) using `WindowSizeClass` and `adaptive-navigation-suite`.
* **GPX Route Simulation**: Loading pre-recorded GPX files for automated emulator testing (e.g., Central Park Loop, Manhattan ZigZag).

---

## 🔮 Future Roadmap (Planned)

Features planned for subsequent releases.

* **Wear OS Tiles**: Adding a quick-access swipeable Tile on Wear OS to start a ride without opening the app.
* **NFC Quick-Pair**: Tap-to-pair functionality for quickly connecting the phone to the e-bike.
* **Smart Routing & Turn-by-Turn Navigation**: Upgrading the map from a simple tracker to providing active routing instructions.
* **ML Kit Text Recognition**: Scanning bike-share station IDs or serial numbers using the camera.
* **Post-Ride Analysis & Charts**: Generating Sparklines and detailed performance charts (Elevation over Time, Speed over Distance) for historical rides in the ride history tab.
