# 🚴‍♂️ AshBike Release Notes

---

## 📱 Google Play Store "What's New" (Copy-Paste for Play Console)

```text
What's new in AshBike v0.0.3:

• Enhanced GPS Accuracy: Added stationary noise filtering so distance and average speed stay precise while stopped.
• Improved Calorie Engine: Overhauled calorie calculations with aerodynamic MET curves tailored to your riding pace and weight.
• Health Connect Sync: Seamlessly sync completed rides, route distance, heart rate, and active calories with Google Health Connect.
• Smooth Navigation: Refreshed full-screen ride dashboard and ride history experience.
• Battery & Performance: Optimized foreground tracking service for long rides.

Happy riding!
```

---

## 🚀 Full Release Summary

### 🎯 Highlights & Features
* **Aerodynamic MET Calorie Engine:** Completely redesigned the energy expenditure algorithm using speed-dependent MET curves. Added stoplight and stationary GPS bounce guards to prevent false calorie inflation during pauses or red lights.
* **Google Health Connect Integration:** Easily sync your completed cycling workouts, distance, total calories burned, and heart rate samples to Google Health Connect with a privacy-first, Play Policy-compliant permission model.
* **Navigation 3 Architecture:** Upgraded the mobile app to AndroidX Navigation 3, offering fluid, full-screen transitions between the Dashboard, Ride History, and Settings.
* **Standalone Wear OS Companion:** Autonomous watch ride tracking with automatic background sync to your mobile device via the Wearable Data Layer.
* **Smart Glasses (XR) HUD Integration:** Live telemetry streaming and hands-free voice control options for projected smart glass displays.

### 🔧 Performance & Stability Improvements
* **GPS Noise Filtering:** Fixed average speed division artifacts and distance drifting while standing still. Average speed now requires 30 meters of true movement before calculating.
* **Battery & Lifecycle Management:** Updated the foreground service lifecycle so GPS location hardware and background threads power down immediately when a ride stops or when the app is closed.
* **In-App Location Transparency:** Introduced a prominent in-app location disclosure modal that explains how location data is used for ride mapping prior to launching system permission prompts.
* **Refreshed App Icons:** Added high-resolution adaptive launcher icons with matching background vectors.

---

## 📄 File Location
Saved in project repository: [`applications/ashbike/docs/release/RELEASE_NOTES.md`](RELEASE_NOTES.md)
