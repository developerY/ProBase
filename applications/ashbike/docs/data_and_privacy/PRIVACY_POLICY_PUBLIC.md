# Privacy Policy for YLabzBike (AshBike)

**Last Updated:** March 30, 2026

Welcome to **YLabzBike: Experimental Dev** (also referred to as **AshBike** within the application). This Privacy Policy applies to the Android applications developed under package identifiers `com.ylabz.basepro.applications.bike` and `com.zoewave.probase.ashbike` by **ZoeWave** / **YLabz** ("we", "us", or "our").

We built YLabzBike (AshBike) to be a powerful, local-first companion for your rides. **We do not track you across apps, we do not serve advertisements, and we do not upload your personal ride data to external servers.**

This policy explains how your data is accessed, processed, and stored on your device.

---

## 1. Data Collection, Access & In-App Usage

YLabzBike (AshBike) accesses specific device permissions solely to deliver core cycling fitness and telemetry features:

### A. Location Data (`ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION`)
* **In-App Usage:** Location access is used during active cycling workouts to calculate live speed, record ride distance, measure elevation changes, and display your route map.
* **Ride Service Processing:** Location data is processed while a ride is active using an Android Foreground Service with a persistent status bar notification, allowing tracking to continue seamlessly when your screen is off.
* **Privacy Guarantee:** Location data is saved exclusively in the app's local database on your device. It is **never sold, shared with third parties, or transmitted to external servers**.

### B. Google Health Connect Data
If you explicitly grant permission, YLabzBike (AshBike) interacts with Android Health Connect to read and write the following fitness data:
* **Distance (`READ_DISTANCE` / `WRITE_DISTANCE`):** Used by the Ride Tracker and Ride History screens to calculate total mileage per session and sync completed bike rides with Health Connect.
* **Exercise Sessions (`READ_EXERCISE` / `WRITE_EXERCISE`):** Used by the Bike Computer and Ride Summary features to log structured cycling workouts to Health Connect.
* **Total Calories Burned (`READ_TOTAL_CALORIES_BURNED` / `WRITE_TOTAL_CALORIES_BURNED`):** Used by the Ride Summary screen to calculate and save estimated energy expenditure for tracked rides.
* **Heart Rate (`READ_HEART_RATE` / `WRITE_HEART_RATE`):** Used to display real-time cardiac intensity during rides and log heart rate samples for synced workout sessions.
* **Speed & Elevation (`READ_SPEED`, `READ_ELEVATION_GAINED`):** Used to display speed and elevation profiles on the ride dashboard and history summary screens.

*Note: We do not request or access Steps, Cadence, Sleep, Hydration, or Weight data from Health Connect.*

### C. Bluetooth (BLE)
Used solely on-device to discover and connect to external heart rate monitors, e-bike hardware controllers, or smart glasses (XR) telemetry displays.

---

## 2. Local-First Data Retention & No Third-Party Analytics

* **No Cloud Accounts:** You are not required to create an account or log in to use YLabzBike (AshBike).
* **Local Storage Only:** Your ride history, routes, and performance statistics are stored in an encrypted/local Room Database on your device.
* **No Analytics or Trackers:** We do not include third-party tracking SDKs, advertising SDKs (e.g. AdMob, AdID), or external crash reporting engines that automatically collect telemetry.

---

## 3. Data Control & Deletion

You have full control over your data:
* **In-App Deletion:** You can delete individual rides or clear your entire ride history at any time from the Ride History screen.
* **App Uninstall:** Uninstalling the app permanently removes all local databases and stored ride routes from your device.
* **Health Connect Data Management:** Data synced to Google Health Connect can be viewed, modified, or deleted at any time through the system Android Health Connect settings menu (`Settings -> Security & Privacy -> Privacy -> Health Connect`).

---

## 4. Developer Contact Information

If you have any questions, feedback, or inquiries regarding this Privacy Policy or data handling practices, please contact us:

* **Application Name:** YLabzBike: Experimental Dev (AshBike)
* **Package Identifiers:** `com.ylabz.basepro.applications.bike` / `com.zoewave.probase.ashbike`
* **Developer Name:** ZoeWave / YLabz
* **Developer Contact Email:** support@zoewave.com
