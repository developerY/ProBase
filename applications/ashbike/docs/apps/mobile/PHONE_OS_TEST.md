# Master Test Plan: AshBike Mobile App

**Version:** 1.0
**Project:** AshBike Multi-Platform Application (Android & iOS)
**Focus Area:** Android Client & Wear OS Companion

---

## 1. Objective

To verify the stability, accuracy, and user experience of the AshBike mobile application, ensuring reliable GPS tracking, accurate health data synchronization, and a fluid Jetpack Compose UI across both mobile and wearable form factors.

## 2. Test Environment setup

Testing must be divided into simulated environments and physical field tests due to the nature of GPS and hardware sensors.

* **Emulators (Android Studio):**
* Use **Extended Controls > Location > Routes** to play back GPX tracks for simulated movement.
* Use Virtual Sensors to mock Heart Rate data.


* **Physical Devices (Mandatory):**
* At least one physical Android device running Android 14+ (API 34+) for native Health Connect testing.
* A physical device is strictly required to test the Fused Location Provider (FLP) "Anchor Logic" and real-world multipath GPS noise (Urban Canyon effect).


* **Wear OS:** Physical watch preferred to monitor true battery drain during DB queries.

---

## 3. Core Feature Test Suite

### 3.1. Location Tracking & Foreground Service (`BikeForegroundService`)

This module handles the core physics calculations and GPS filtering.

| Test ID | Scenario | Steps to Execute | Expected Result | Status |
| --- | --- | --- | --- | --- |
| **LOC-01** | **Emulator Teleport Guard** | 1. Start emulator.<br>

<br>2. Open AshBike and start a ride.<br>

<br>3. Instantly change location by 500m in Extended Controls. | App ignores the jump. Speed remains 0 km/h. Distance does not spike. | Unassigned |
| **LOC-02** | **Anchor Logic (Physical)** | 1. Start a ride on a physical phone.<br>

<br>2. Leave phone perfectly still on a table for 10 minutes. | GPS drift is filtered out. Total distance remains exactly 0.0 km. | Unassigned |
| **LOC-03** | **Elevation Hysteresis** | 1. Start ride.<br>

<br>2. Walk up a small incline (< 3m) and back down. | Elevation gain/loss remains 0 (filtered by the 3m threshold). | Unassigned |
| **LOC-04** | **Foreground WakeLock** | 1. Start ride.<br>

<br>2. Turn off phone screen and ride for 15 minutes. | OS does not kill the service. Route is mapped completely without straight-line gaps. | Unassigned |

### 3.2. Data Persistence (`BikeRideDao`)

Verifying the SQLite/Room implementation and query optimization.

| Test ID | Scenario | Steps to Execute | Expected Result | Status |
| --- | --- | --- | --- | --- |
| **DB-01** | **Mobile Flow Reactivity** | 1. Open Ride History on phone.<br>

<br>2. Delete a ride. | The UI instantly removes the card without requiring a manual screen refresh. | Unassigned |
| **DB-02** | **Wear OS One-Shot Fetch** | 1. Open Ride History on watch.<br>

<br>2. Monitor CPU/Battery profiling. | `getAllRidesBasicOnce()` fires once. CPU drops to idle while viewing the list. | Unassigned |
| **DB-03** | **Order Verification** | 1. View Ride History list. | Rides are strictly ordered by `startTime DESC` (newest at the top). | Unassigned |
| **DB-04** | **Delete Cascade** | 1. Delete a ride via UI.<br>

<br>2. Inspect Database via Android Studio. | Both the `bike_rides_table` entry and all associated `RideLocationEntity` points are removed. | Unassigned |

### 3.3. Health Connect Integration (`SyncRideUseCase`)

Ensuring fitness data is accurately written and formatted for Google Fit/Health Connect.

| Test ID | Scenario | Steps to Execute | Expected Result | Status |
| --- | --- | --- | --- | --- |
| **HC-01** | **Permission Flow** | 1. Install app fresh.<br>

<br>2. Navigate to Health sync settings. | User is prompted for Read/Write permissions. Graceful fallback if denied. | Unassigned |
| **HC-02** | **Session Sync** | 1. Finish a ride.<br>

<br>2. Sync to Health Connect.<br>

<br>3. Open Google Fit. | Session appears as "Biking" with the correct start/end times and AshBike metadata. | Unassigned |
| **HC-03** | **Calories Accuracy** | 1. Ride burns 450 kcal.<br>

<br>2. Sync data.<br>

<br>3. Check Google Fit/Health Connect. | Total calories burned reads exactly 450 kcal (verifying `Energy.kilocalories` fix is active). | Unassigned |
| **HC-04** | **Distance & HR Sync** | 1. Complete ride with simulated HR.<br>

<br>2. Sync data. | Distance matches AshBike DB. HR shows starting and ending BPM boundaries. | Unassigned |

### 3.4. UI & Navigation (Jetpack Compose)

Testing the visual components and user flow.

| Test ID | Scenario | Steps to Execute | Expected Result | Status |
| --- | --- | --- | --- | --- |
| **UI-01** | **Top-Level Navigation** | 1. Navigate to Home, History, and Settings via Bottom Navigation. | No "Back Arrow" is present in the TopAppBar on any of these three screens. | Unassigned |
| **UI-02** | **Sparkline Rendering** | 1. Complete an interval ride (fast/slow).<br>

<br>2. View the Ride Card. | The `RideSparkLine` draws a smooth, non-crashing curve. Fits entirely within the card bounds. | Unassigned |
| **UI-03** | **Map vs. Path View** | 1. Open Map View.<br>

<br>2. Toggle `showMapContent` to false. | UI smoothly transitions from Google Map to the custom `MapPathScreen` drawing. | Unassigned |
| **UI-04** | **Collapsible Summary** | 1. Tap the Weekly Health Summary card. | Card expands/collapses smoothly. Content reads from Health Connect aggregated data. | Unassigned |

---

## 4. Defect Management & Priority

When logging bugs during this test cycle, use the following classifications:

* **P0 (Blocker):** Service crashes, database corruption, or `TransactionTooLargeException` when passing intents.
* **P1 (High):** GPS tracking inaccuracies (zig-zagging > 5% error), Health Connect sync failures, or Watch battery drain.
* **P2 (Medium):** UI glitches, Sparkline drawing out of bounds, incorrect sort orders.
* **P3 (Low):** Typographical errors in UI, slight animation stutters.

---

Would you like me to create a mock-up of an automated UI test (using Espresso or Compose Test rules) for one of your specific components, like the `RideSparkLine` or the Top-Level Navigation rules?