# 📋 Foreground Service & Hardware Permissions Audit

## 1. Foreground Service Permissions

| Permission | Mobile App Status | Wear OS App Status | Purpose / Justification |
|---|---|---|---|
| `FOREGROUND_SERVICE_LOCATION` | YES (Used) | YES (Used) | **Required.** Powers live GPS route tracking, speed, elevation, and distance while riding, including when the screen is off or the device is in a pocket. |
| `FOREGROUND_SERVICE_CONNECTED_DEVICE` | REMOVED | REMOVED | **Removed from both apps.** Reduces Play Store submission review complexity. Standard Bluetooth runtime permissions (`BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`) still manage BLE hardware connections while active on-screen. |
| `FOREGROUND_SERVICE_HEALTH` | REMOVED | YES (Used) | **Required on Wear OS.** Powers on-watch sensor batching via Android Health Services (`ExerciseClient`). Removed from Mobile because mobile synchronization runs through the Health Connect client, not a foreground service. |
| `FOREGROUND_SERVICE_DATA_SYNC` | REMOVED | REMOVED | **Removed from both apps.** Health Connect ride synchronization (`SyncRideUseCase`) and Wearable Data Layer synchronization run as instant background suspend workers, not as ongoing foreground services. |

---

## 2. Hardware Features (`<uses-feature>`) Declaration Audit

Google Play Store uses `<uses-feature>` declarations to filter device compatibility for app distribution. If permissions like Camera or Audio are merged transitively from feature modules, Google Play automatically assumes those hardware features are *mandatory* unless explicitly marked `android:required="false"`.

| Hardware Feature | Required Status | Purpose & Play Store Protection |
|---|---|---|
| `android.hardware.location` | **`required="true"`** | Primary capability for GPS ride tracking, speed, and distance calculations. |
| `android.hardware.location.gps` | **`required="true"`** | Hardware GPS sensor for high-accuracy route mapping. |
| `android.hardware.camera` | **`required="false"`** | Optional feature used for station QR code scanning or bike serials. Marked optional so non-camera tablets or displays can install the app. |
| `android.hardware.microphone` | **`required="false"`** | Optional feature used for Smart Glasses (XR) voice gear commands (`RECORD_AUDIO`). Marked optional so devices without microphones can install the app. |
| `android.hardware.faketouch` | **`required="false"`** | Touchscreen emulation for trackpads, Chromebooks, or projected smart glass HUD displays. |
| `android.hardware.screen.portrait` | **`required="false"`** | Portrait orientation preference. Marked optional to support landscape head-units or tablets. |

---

## 🛡️ Summary of Manifest Cleanups

### 1. 1:1 Foreground Service Permission Matching
Every `FOREGROUND_SERVICE_*` permission declared in `<uses-permission>` strictly 1:1 matches the `foregroundServiceType` declared on the service:
```xml
<service
    android:name="com.zoewave.probase.ashbike.features.main.service.BikeForegroundService"
    android:exported="false"
    android:foregroundServiceType="location" />
```

### 2. Background Location Removed
Removed `ACCESS_BACKGROUND_LOCATION` from both Mobile and Wear OS manifests to eliminate Google's strictest background location permission review tier.

### 3. ARCore Set to Optional
Added the following metadata declaration so the app can be installed on devices that do not support ARCore:
```xml
<meta-data
    android:name="com.google.ar.core"
    android:value="optional"
    tools:replace="android:value" />
```

---

## ✅ Build Verification

| Build Target | Result |
|---|---|
| `:applications:ashbike:apps:mobile:assembleDebug` | SUCCESS |
| `:applications:ashbike:apps:wear:assembleDebug` | SUCCESS |

**Build Status: 100% Success**
