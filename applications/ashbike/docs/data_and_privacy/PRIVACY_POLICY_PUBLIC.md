# Privacy Policy for AshBike

**Last Updated:** November 2026

Welcome to **AshBike**! This Privacy Policy applies to the Android application developed under package identifier `com.zoewave.probase.ashbike` by **ZoeWave** ("we", "us", or "our").

We built AshBike to be a powerful, privacy-focused companion for your rides. This policy explains how your data is accessed, processed, stored, and managed on your device.

---

## 1. Data Collection, Access & In-App Usage

AshBike accesses specific device permissions solely to deliver core cycling fitness and telemetry features:

### A. Location Data (`ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION`)
* **In-App Usage:** Location access is used during active cycling workouts to calculate live speed, record ride distance, measure elevation changes, and display your route map.
* **Ride Service Processing:** Location data is processed while a ride is active using an Android Foreground Service with a persistent status bar notification, allowing tracking to continue seamlessly when your screen is off.
* **Privacy Guarantee:** Location data is saved exclusively in the app's local database on your device. It is **never sold, shared with third parties, or transmitted to external servers**.

### B. Google Health Connect Data
If you explicitly grant permission, AshBike interacts with Android Health Connect to read and write the following fitness data:
* **Distance (`READ_DISTANCE` / `WRITE_DISTANCE`):** Used by the Ride Tracker and Ride History screens to calculate total mileage per session and sync completed bike rides with Health Connect.
* **Exercise Sessions (`READ_EXERCISE` / `WRITE_EXERCISE`):** Used by the Bike Computer and Ride Summary features to log structured cycling workouts to Health Connect.
* **Total Calories Burned (`READ_TOTAL_CALORIES_BURNED` / `WRITE_TOTAL_CALORIES_BURNED`):** Used by the Ride Summary screen to calculate and save estimated energy expenditure for tracked rides.
* **Heart Rate (`READ_HEART_RATE` / `WRITE_HEART_RATE`):** Used to display real-time cardiac intensity during rides and log heart rate samples for synced workout sessions.

*Note: We do not request or access Steps, Cadence, Sleep, Hydration, or Weight data from Health Connect.*

### C. Bluetooth (BLE)
Used solely on-device to discover and connect to external heart rate monitors, e-bike hardware controllers, or smart glasses (XR) telemetry displays.

---

## 2. Telemetry, Crash Reporting & Analytics

To ensure application stability, diagnose bugs, and improve ride features, AshBike includes third-party SDKs provided by Google Firebase:

* **Firebase Crashlytics:** Collects anonymized crash logs, stack traces, and device diagnostic state when an unexpected crash occurs.
* **Firebase Analytics:** Collects high-level feature interaction metrics and performance insights to help us optimize app responsiveness.
* **Advertising ID Collection Disabled:** We explicitly disable Google Advertising ID (`google_analytics_adid_collection_enabled = false`). We do not track you across other applications, nor do we serve ads.
* **Enabled by Default & User Opt-Out Control:** Both Crashlytics and Analytics are enabled by default on app launch to ensure diagnostic reliability out of the box. You have complete control to independently turn off **Crash Reporting** or **Usage Analytics** at any time in the app **Settings**.

---

## 3. Local-First Storage & Data Control

* **No Cloud Accounts Required:** You are not required to create an account or log in to use AshBike.
* **Local Storage & Backup Exclusion:** Your ride history, routes, and performance statistics are stored in an app-private Room Database on your device. Ride data is explicitly excluded from cloud backup and device transfer rules to safeguard your privacy.
* **In-App Deletion:** You can delete individual rides or clear your entire ride history at any time from the Ride History screen.
* **App Uninstall:** Uninstalling the app permanently removes all local databases and stored ride routes from your device.
* **Health Connect Data Management:** Data synced to Google Health Connect can be viewed, modified, or deleted at any time through system settings (`Settings -> Security & Privacy -> Privacy -> Health Connect`).

---

## 4. Developer Contact Information

If you have any questions, feedback, or inquiries regarding this Privacy Policy or data handling practices, please contact us:

* **Application Name:** AshBike
* **Package Identifier:** `com.zoewave.probase.ashbike`
* **Developer Name:** ZoeWave
* **Developer Contact Email:** developer@zoewave.com
