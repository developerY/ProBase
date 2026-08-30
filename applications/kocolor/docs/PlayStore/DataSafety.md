# Google Play Console Data Safety Form Guide for KoColor

This document provides the exact responses and field mappings required for completing the **Data Safety** section in Google Play Console for **KoColor**.

---

## 1. Executive Summary & Core Declarations

* **Does your app collect or share any of the required user data types?**  
  👉 **Yes** (App Activity, App Info and Performance, Device Identifiers, and Environmental Weather Parameters for atmospheric color gating).
* **Is all of the user data collected by your app encrypted in transit?**  
  👉 **Yes** (All network traffic uses HTTPS/TLS 1.3).
* **Do you provide a way for users to request that their data be deleted?**  
  👉 **Yes** (Local data is deleted in-app or upon uninstallation; anonymized Firebase Analytics records are deleted upon request via App Instance ID in **Settings > About**).

---

## 2. On-Device Local Processing Exceptions (Google Play Guidelines)

> [!NOTE]
> Under Google Play Data Safety policy, data processed **strictly locally on-device** without leaving the user's hardware does **NOT** count as "Collection" for Play Console forms.

1. **Photos & Facial Portraits (Skin & Color Calibration)**:
   * **Handling:** Processed **100% locally on-device** via ML Kit Face Detection and local NPU color science algorithms.
   * **Cloud Bifurcation Invariant:** Raw pixel images, bitmaps, and facial landmark coordinates are **compile-time blocked** from leaving your device. Cloud AI requests accept strictly `TextOnly` manifests.
   * **Play Console Declaration:** Mark as **Not Collected** (or "Processed locally on-device") because raw biometric pixels and photos are never transmitted off the device.
2. **Categorical Feature Descriptors vs. Raw Biometrics**:
   * **What IS sent to Cloud LLM:** Only non-identifiable, categorical text labels (e.g., `Temperature: Cool`, `Depth: Medium`, `Contrast: High`) and wardrobe text IDs (`w_101`).
   * **What IS NOT sent to Cloud LLM:** No facial images, no biometric face-mesh vectors, no skin patches, and no personal identifiers.
3. **Google Health Connect (Sleep & Circadian Wellness Data)**:
   * **Handling:** Read **100% locally on-device** via Google Health Connect API to calibrate circadian skin defense context.
   * **Play Console Declaration:** Mark as **Not Collected** off-device.

---

## 3. Detailed Data Type Declarations (Field-by-Field Mappings)

### A. Location & Environmental Weather Data
* **Data Type:** Coarse Location / Environmental Parameters (Postal Code / Weather query).
* **Collected?** Yes.
* **Shared?** No.
* **Purpose:** **App Functionality** (Provides ambient temperature and UV index for hard-gating heavy outerwear/cosmetic recommendations).
* **Ephemerally Processed?** Yes (Transmitted over HTTPS to retrieve atmospheric state, then discarded without persistent cloud storage).

---

### B. App Activity
* **Data Type:** App interactions (e.g., screen views, simulator runs, palette locks).
* **Collected?** Yes (via Firebase Analytics).
* **Shared?** No.
* **Purpose:** **Analytics** & **App Functionality**.
* **Optional or Required?** Optional (User can opt out or request deletion via App Instance ID).

---

### C. App Info and Performance
* **Data Type 1: Crash Logs**
  * **Collected?** Yes (via Firebase Crashlytics).
  * **Shared?** No.
  * **Purpose:** **App Functionality** & **Analytics** (Identifying ANRs, NPU execution crashes, and stack traces).
  * **Linked to User?** No (Anonymous).
* **Data Type 2: Diagnostics & Performance Data**
  * **Collected?** Yes (App load times, memory consumption).
  * **Shared?** No.
  * **Purpose:** **Analytics**.

---

### D. Device or Other Identifiers
* **Data Type:** Device or other IDs (Firebase App Instance ID, Advertising ID `AD_ID`).
* **Collected?** Yes (via Firebase Analytics).
* **Shared?** No.
* **Purpose:** **Analytics** & **App Functionality** (Provides install attribution, app usage metrics, and enables manual analytics deletion routing upon user request).
* **Linked to User?** No (Anonymized).

---

## 4. Play Console Form Section Answers

### Section: Data Collection and Security
| Question | Selection |
| :--- | :--- |
| **Does your app collect or share any user data?** | **Yes** |
| **Is all data collected by your app encrypted in transit?** | **Yes** |
| **Do you provide a way for users to request data deletion?** | **Yes** |
| **Is data processing required for app functionality?** | **Yes** |

### Section: Data Types Table Mappings

| Data Category | Specific Data Type | Collected | Shared | Ephemeral | Purpose | Linked to User |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Location** | Coarse Location / Weather Query | Yes | No | Yes | App Functionality | No |
| **Photos & Videos** | Personal Photos (Portraits) | **No** (Local Only) | No | N/A | Local NPU Analysis | No |
| **Health & Fitness** | Sleep / Wellness Metrics | **No** (Local Only) | No | N/A | Local Skin Calibration | No |
| **App Activity** | App Interactions | Yes | No | No | Analytics | No |
| **App Info & Performance** | Crash Logs | Yes | No | No | Analytics & Functionality | No |
| **App Info & Performance** | Performance Diagnostics | Yes | No | No | Analytics | No |
| **Device Identifiers** | App Instance ID | Yes | No | No | Analytics & Functionality | No |

---

## 5. Google Play Console Health Feature Declarations

KoColor integrates with **Google Health Connect** to synchronize local bio-markers for skin defense and wellness correlation. In Google Play Console, declare the following under **Tell us about the health features in your app**:

| Play Console Health Feature Category | Selection Status | Data Types Read via Health Connect API | Reason / Justification |
| :--- | :--- | :--- | :--- |
| **Activity and fitness** | **CHECK THIS BOX** | Steps, distance, active calories, exercise sessions, heart rate | Calculates physical activity and vital bio-marker correlations for skin/style insights. |
| **Nutrition and weight management** | **CHECK THIS BOX** | Hydration records, weight metrics, daily fluid intake goals | Calibrates daily hydration targets and skin moisture defense. |
| **Period tracking** | **UNCHECK** | N/A | Not used / Stripped from manifest. |
| **Sleep management** | **UNCHECK** | N/A | **Not used / Stripped from manifest.** |
| **Stress management, relaxation, mental acuity** | **UNCHECK** | N/A | Not used / Stripped from manifest. |

> [!IMPORTANT]
> **Manifest Stripping Policy**: KoColor's `AndroidManifest.xml` explicitly strips all unneeded Health Connect permission declarations (such as `READ_SLEEP`, `READ_SEXUAL_ACTIVITY`, `READ_BLOOD_GLUCOSE`, `READ_CERVICAL_MUCUS`, etc.) using `tools:node="remove"`. KoColor strictly requests permissions only for **Activity & Fitness** and **Nutrition & Weight Management**.

---

## 6. Summary of Developer Commitments

1. **No Data Sold:** KoColor does not sell user data or Health Connect bio-markers to data brokers or third parties.
2. **No Advertising Tracking:** KoColor does not use third-party advertising SDKs or cross-app tracking identifiers (like GAID) for targeted advertising.
3. **Local-First Privacy Guarantee:** Raw pixel images, Health Connect bio-markers (vitals, steps, hydration), and facial features never leave the user's Android hardware.
