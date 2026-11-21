# Background Ride Tracking & Service Lifecycle

This document explains how background tracking operates in AshBike and how ride sessions start, run in the background, and stop cleanly.

---

## 🚴‍♂️ Background Ride Tracking Behavior

When you lock your phone, turn off the screen, or switch to another app (like Spotify, Google Maps, or Camera), **AshBike continues tracking your ride route, speed, distance, elevation, and calories without interruption.**

### How It Works
Android handles background tracking via an official **Foreground Service** (`BikeForegroundService`):

1. **Persistent Status Bar Notification:**
   When you tap **Start Ride**, AshBike launches a Foreground Service that displays an ongoing status bar notification with live ride stats (Speed, Distance, Duration).
2. **Foreground-Equivalent Location Access:**
   Because the service is explicitly registered with `foregroundServiceType="location|connectedDevice"`, Android grants foreground-equivalent location access to the process—even if the UI is minimized, in a pocket, or the screen is locked.
3. **Local-First Retention:**
   Your GPS coordinates, speed telemetry, and calorie deltas record continuously into the local Room database.

---

## 🛑 How the Service Stops (No Manual Kill Required!)

**The user does NOT need to force-kill or swipe away the service.** The app handles the entire lifecycle cleanly and automatically:

### 1. Stopping / Saving a Ride
When the user taps the **Stop (Red Square)** button on the live Dashboard or the **Stop** action button directly on the notification:
* **Finalize & Persist:** The service calculates the final workout summary (Total Distance, Duration, Avg/Max Speed, Calories, Elevation) and writes the `BikeRideEntity` and location points to the local Room database for the Ride History screen.
* **Stop Foreground State:** The service removes the status bar notification via `stopForeground(STOP_FOREGROUND_REMOVE)` and immediately calls `stopSelf()` to completely shut down the background execution process.
* **Release Resources:** Releases any CPU/Screen `WakeLock` held during tracking, allowing the phone to return to normal deep sleep.
* **Complete Exit:** Because `stopSelf()` is called, the background service does not linger in the Android 13/14 "Active Apps" / FGS Task Manager. The app fully exits the background, saving battery life.

