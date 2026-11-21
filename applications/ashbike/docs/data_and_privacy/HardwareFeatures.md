# 📱 Hardware Features Audit

| Hardware Feature | Status in AshBike | Purpose & Play Store Protection |
|---|---|---|
| `android.hardware.location` / `location.gps` | Required (`required="true"`) | **Core Feature.** Required for live GPS ride tracking, speedometer, distance calculation, and route mapping. |
| `android.hardware.camera` | Optional (`required="false"`) | **Optional.** Used transitively for bike-share QR code station scanning or serial number scanning. Marked as optional so devices without cameras, including tablets, can still install the app. |
| `android.hardware.microphone` | Optional (`required="false"`) | **Optional.** Used with Smart Glasses (XR) for hands-free voice gear commands (`RECORD_AUDIO`). Marked as optional so devices without microphones can still install the app. |
| `android.hardware.faketouch` | Optional (`required="false"`) | **Optional.** Allows touchscreen emulation so the app can run on non-touch displays, including Chromebooks, trackpads, and projected Smart Glasses HUDs. |
| `android.hardware.screen.portrait` | Optional (`required="false"`) | **Optional.** Portrait is the primary screen orientation. Marked as optional so landscape bike head units and tablets can install the app. |

## 🛡️ Why Declaring `required="false"` Is Important for Google Play

When an app merges libraries such as CameraX, QR Scanner, or Accompanist, Google Play may automatically infer that hardware features like `android.hardware.camera` and `android.hardware.microphone` are mandatory.

If a device does not have a camera or microphone, Google Play Console can silently exclude that device from being able to download the app.

By explicitly declaring these features with `android:required="false"` in:

`applications/ashbike/apps/mobile/src/main/AndroidManifest.xml`

We ensure:

1. Google Play Console does not unnecessarily block devices without cameras or microphones.
2. AshBike remains installable across a broader range of Android devices, including tablets, Chromebooks, and compatible non-touch devices.

## ✅ Implementation & Build Verification

The following updates have been completed:

- Added optional hardware feature declarations to `AndroidManifest.xml`.
- Updated `Permissions.md` to document the hardware requirements.
- Verified both Mobile debug and release manifest processing builds.

| Build Target | Result |
|---|---|
| `:applications:ashbike:apps:mobile:assembleDebug` | SUCCESS |
| `:applications:ashbike:apps:mobile:processReleaseMainManifest` | SUCCESS |

**Build Status: 100% Success**

All hardware feature declarations have been applied, and both debug compilation and release manifest processing completed successfully.