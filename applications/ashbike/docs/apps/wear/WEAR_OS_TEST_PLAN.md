# AshBike Wear OS: Quality Assurance & Test Plan

## 1. Overview and Scope

This document outlines the testing strategy for the AshBike Wear OS application. The focus is on verifying standalone functionality, ensuring memory safety on constrained hardware, validating the `ExerciseClient` sensor fusion, and confirming seamless Bluetooth synchronization with the mobile companion app.

## 2. Test Environments

Testing must be conducted across both simulated and physical hardware to account for real-world sensor behaviors.

* **Physical Hardware:** Wear OS 3+ devices (e.g., Pixel Watch, Galaxy Watch) for real-world GPS locking, battery throttling, and heart rate validation.
* **Wear OS Emulator:** Used for simulated GPS routes, forced memory constraints, and synthetic heart rate injection via Extended Controls.

---

## 3. Permission & Onboarding Testing

Wear OS has aggressive permission models. The app must handle grant and denial states gracefully without crashing.

* [ ] **Cold Start Permissions:** Verify the app requests `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `POST_NOTIFICATIONS`, and `BODY_SENSORS` before allowing a ride to start.
* [ ] **Android 14 Foreground Rules:** Verify the app does not crash when initiating the `location|health` foreground service on API 34+ devices.
* [ ] **Denied Location:** Deny location permissions and start a ride. Verify the app still records time and heart rate, and saves the ride gracefully without crashing the Map UI later.
* [ ] **Denied Heart Rate:** Deny `BODY_SENSORS`. Verify the live tracking UI displays `-- bpm` and does not crash the `ExerciseClient`.

---

## 4. Ride Tracking Engine (`ExerciseClient`)

Testing the core tracking loop inside the `BikeForegroundService`.

* [ ] **Cold GPS Start:** Start a ride immediately after leaving a building. Verify the UI clearly indicates "Acquiring GPS" and does not map anomalous jumps (e.g., the 118 km/h speed bug).
* [ ] **Sensor Batching Validation:** Ride for 5 minutes and verify the `ExerciseClient` successfully delivers batched `LocationPoint` updates to the UI StateFlow.
* [ ] **Ongoing Activity Indicator:** Navigate to the watch home screen during an active ride. Verify the animated AshBike icon appears at the bottom of the watch face and correctly deep-links back into the active tracking screen.
* [ ] **Micro-Ride Edge Case:** Start a ride and stop it under 30 seconds. Verify the ride saves successfully even if the hardware batching has not delivered the first array of GPS coordinates.

---

## 5. Database & Memory Safety (Room DAO)

Smartwatches have severe RAM limitations. Testing must ensure the split-load architecture prevents Out-Of-Memory (OOM) crashes.

* [ ] **Lightweight History List:** Populate the database with 50+ historical rides. Swipe to the History Pager. Verify the app calls `getAllRidesBasic()` and renders the list instantly without UI stutter or memory crashes.
* [ ] **Heavy Map Fetch:** Tap on a specific historical ride. Verify `getRideWithLocationsSuspend(id)` correctly fetches the `@Relation` class and passes the populated coordinate array to the Canvas.
* [ ] **Empty Map Fallback:** Select a ride with 0 recorded locations. Verify the Map screen correctly catches `locations.isEmpty()` and renders the "No GPS data for this ride" fallback text instead of attempting to draw a 0-point polyline.
* [ ] **Deletions:** Delete a ride from the watch UI. Verify the Room Foreign Key cascade successfully wipes the associated thousands of rows in `ride_locations` to free up storage space.

---

## 6. Wearable Data Layer (Watch -> Phone Sync)

Testing the background payload transfer using `PutDataMapRequest`.

* [ ] **Immediate Post-Ride Sync:** Finish and save a ride on the watch while paired via Bluetooth. Verify the mobile app successfully receives the payload and writes it to the phone's local Room database.
* [ ] **Disconnected Ride (Queueing):** Disconnect Bluetooth/Wi-Fi on the watch. Complete a 10-minute ride and save it. Reconnect to the phone. Verify the Wearable Data Layer correctly caches the payload and delivers it once the connection is restored.
* [ ] **Payload Size Limits:** Complete a long 3-hour ride with thousands of GPS points. Verify the serialization process does not exceed Data Layer payload limits (typically 100KB per asset). *Note: If this fails, transition the payload sync to `ChannelClient` or transfer the locations array as an `Asset`.*

---

## 7. Lifecycle & Battery Throttling Edge Cases

Simulating extreme watch conditions.

* [ ] **Wrist-Down (Ambient Mode):** Start a ride, lower the wrist to trigger ambient mode (screen dims). Wait 5 minutes, raise the wrist. Verify the UI instantly updates with the elapsed time and distance, proving the Foreground Service kept the process alive.
* [ ] **Low Battery Throttle:** Trigger the watch's built-in "Battery Saver" mode during an active ride. Verify the app handles reduced GPS polling rates without crashing.
* [ ] **Heart Rate Interruption:** During an active ride, take the watch off the wrist. Verify the `ExerciseClient` registers the `Availability` change and the UI correctly defaults back to `-- bpm` until worn again.