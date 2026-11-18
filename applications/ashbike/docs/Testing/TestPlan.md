# 🧪 ProBase Repository Test Plan

**Project Name:** ProBase Monorepo (AshBike, PhotoDo)

**Version:** 1.0 (Alpha/Dev)

**Scope:** Android Mobile (Phone), Wear OS, and Shared Feature Modules.

---

## 1. Introduction

This document outlines the testing strategy for the **ProBase** repository. This monorepo contains multiple applications and modularized features. The testing goal is to ensure the stability of the core architecture while verifying the functional requirements of the flagship **AshBike** application and the **PhotoDo** utility app.

### 1.1 Scope of Testing

* **Applications**:
* AshBike Mobile (`applications/ashbike/apps/mobile`)
* AshBike Wear OS (`applications/ashbike/apps/wear`)
* PhotoDo (`applications/photodo/apps/mobile`)


* **Feature Modules**: BLE, NFC, Health, Weather, Places, QR Scanner, ML.
* **Core Libraries**: Network, Database, Data, UI, Model.

---

## 2. Test Strategy

We follow the **Testing Pyramid** approach, prioritizing fast, reliable unit tests, followed by integration tests, and finally UI/E2E tests.

### 2.1 Testing Levels

| Level | Type | Tools | Target Coverage |
| --- | --- | --- | --- |
| **L1** | **Unit Tests** | JUnit 4/5, Mockk, Coroutines Test | **80%+** (Business Logic, ViewModels, Repositories) |
| **L2** | **Integration Tests** | Hilt Testing, Room (In-Memory) | **Core Paths** (Database <-> Repo, Network <-> Repo) |
| **L3** | **UI Tests** | Compose UI Test, Espresso | **Critical Flows** (Navigation, Key Screens) |
| **L4** | **Manual/Field Tests** | Physical Devices | **Hardware Features** (GPS, BLE, NFC, Wear OS Sync) |

---

## 3. Automated Testing Plan

### 3.1 AshBike Application (Mobile & Wear)

#### 🚴 **Ride Tracking Core (Critical)**

* **Unit Tests**:
* Verify `RideSessionUseCase` correctly accumulates distance, duration, and calories.
* Test `Pause` and `Resume` state logic in ViewModels.
* Validate GPX file generation logic.


* **Integration Tests**:
* Simulate sensor data streams (Heart Rate, Speed) and verify database insertion.


* **Field Testing (Manual)**:
* **GPS Accuracy**: Ride a known distance; verify recorded path matches reality.
* **Battery Drain**: Monitor consumption during a 1-hour active ride session.



#### 👓 **Smart Glass Feature**

* **UI Tests**:
* Verify `AshGlassLayout` renders correctly on low-resolution/high-contrast emulator settings.
* Test projection state updates (e.g., updating speed/distance text).



#### ⌚ **Wear OS Companion**

* **Manual**:
* Test "Start Ride" on Watch triggers Mobile app.
* Test Heart Rate syncing from Watch to Mobile.



### 3.2 Feature Modules

#### 📡 **Feature: BLE (Bluetooth Low Energy)**

* **Unit**: Test packet parsing logic for standard BLE profiles (Heart Rate, Cycling Speed & Cadence).
* **Manual**:
* Connect to real BLE peripherals.
* Test auto-reconnect scenarios (move out of range, move back in).



#### ❤️ **Feature: Health (Health Connect)**

* **Integration**:
* Verify permissions flows (Grant/Deny).
* Test `ExerciseSession` writing to the sandbox Health Connect environment.


* **Manual**:
* Verify data written by AshBike appears in the official Google Fit / Samsung Health app.



#### 🌤️ **Feature: Weather**

* **Unit**:
* Mock API responses (Success, 404, Network Error) and verify `WeatherRepository` mapping.
* Test `WeatherUiState` transitions (Loading -> Success/Error).



#### 🏷️ **Feature: NFC**

* **Manual**:
* Scan various tag types (Type 2, Type 4).
* Verify NDEF message parsing.



### 3.3 PhotoDo Application

* **Unit**: Test Task management logic (Create, Read, Update, Delete).
* **UI**: Verify CameraX preview stream starts and captures image successfully.

---

## 4. Manual Test Cases (Smoke Test)

| ID | Application | Scenario | Expected Result |
| --- | --- | --- | --- |
| **TC-001** | AshBike | **App Launch** | App opens to Dashboard; Permissions (Location, BT) requested. |
| **TC-002** | AshBike | **Sensor Connection** | "Scan" finds BLE devices; Connection status turns Green. |
| **TC-003** | AshBike | **Record Ride** | Timer starts; Speed/Distance increment; Map updates position. |
| **TC-004** | AshBike | **End Ride** | Ride summary shown; Session saved to History & Health Connect. |
| **TC-005** | AshBike | **Wear OS Sync** | Launching Wear app shows status matches Phone app. |
| **TC-006** | PhotoDo | **Capture Task** | Taking a photo creates a new Task entry in the list. |
| **TC-007** | General | **Dark Mode** | All UI elements remain visible and legible in System Dark Mode. |

---

## 5. Tools & Environment

### 5.1 CI/CD (Continuous Integration)

* **Trigger**: On every Pull Request (PR) to `main`.
* **Jobs**:
1. `./gradlew lintDebug` (Code style & potential bugs).
2. `./gradlew testDebugUnitTest` (Run all unit tests).
3. `./gradlew :applications:ashbike:apps:mobile:assembleDebug` (Verify build success).



### 5.2 Device Matrix

Due to the hardware-dependent nature of this repo (Bluetooth, GPS, NFC), testing requires specific environments:

1. **Emulators**:
* Pixel 6 API 34 (General UI Testing).
* Wear OS 4 Emulator (AshBike Wear).
* *Note: Bluetooth/NFC cannot be fully tested on standard emulators.*


2. **Physical Devices (Required for Releases)**:
* **Tier A**: Google Pixel 7/8 (Reference Android Experience).
* **Tier B**: Samsung Galaxy S23/S24 (Custom OEM behavior check).
* **Wearable**: Pixel Watch or Galaxy Watch.



---

## 6. Risk Assessment

* **BLE Stability**: Bluetooth stack varies significantly between Android manufacturers. *Mitigation: Extensive manual testing on Samsung & Pixel devices.*
* **Background Location**: Android OS aggressively kills background processes. *Mitigation: Test "Long Ride" scenarios (2+ hours) with the screen off.*
* **API Limits**: Weather and Maps APIs have rate limits. *Mitigation: Use mock repositories for automated tests.*