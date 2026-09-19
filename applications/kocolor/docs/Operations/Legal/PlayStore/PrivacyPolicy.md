# Privacy Policy for KoColor

**Effective Date:** April 21, 2026

**ZoeWave LLC** ("we", "us", or "our") provides the KoColor application as a Commercial service. This application is intended for use as is.

---

### 1. Information Collection and Use: Local-First Privacy Architecture
KoColor is engineered with a **local-first, privacy-by-design** architecture.
* **Local Storage Only:** All personal content created within KoColor—including wardrobe clothing inventory, vanity cosmetics, personal fashion profiles, facial skin color calibration profiles, daily style plans, hydration logs, and captured portrait photos—is stored exclusively in a secure local database (Room & DataStore) on your device.
* **No Central User Database:** We do not own, operate, or utilize central servers to store your personal wardrobe items or facial biometric profiles. We have no access to your personal information, and your data is never synchronized to external servers by ZoeWave LLC.
* **No Account Required:** You are not required to create an account, log in, or provide an email address to use the core application.

---

### 2. Artificial Intelligence & Data Processing Boundaries
KoColor utilizes a dual-tier AI architecture designed to enforce strict privacy boundaries:

* **Local On-Device AI (Gemini Nano):** Features running on local AI engines execute 100% on your device's NPU/GPU. Images, texture analysis, and color geometry remain on your hardware; raw pixel data is never transmitted across the network.
* **Cloud AI (Firebase AI Logic / Cloud Tier):** When Cloud AI features are utilized, KoColor strictly transmits anonymized text manifests and non-identifiable categorical attributes (e.g., `Temperature: Cool`, `Depth: Medium`, `Contrast: High`).
* **Compile-Time Image Protection:** Our Cloud AI client is compile-time restricted to accept `TextOnly` inputs. Raw facial images, portrait bitmaps, skin patch samples, and biometric face-mesh vectors are **strictly blocked** from cloud transmission.

---

### 3. Device Permissions & Optional Data Integrations
To provide core fashion, skin-defense, and wellness correlation features, KoColor requests the following permissions, all handled with explicit user consent and choice:

* **Camera & Storage:** Used solely to capture or select portrait photos for facial color calibration and wardrobe item photography locally on your device.
* **Location & Weather Parameters (Optional):** Used to retrieve ambient temperature (°C) and UV Index from weather services to calibrate environmental garment and sunscreen recommendations. Location query is optional; users can deny permission, opt out in app settings, or rely on ambient defaults.
* **Google Health Connect (Bio-Data Integration):** Users may optionally grant permission to synchronize local Health Connect bio-markers:
  * **Activity & Fitness:** Steps, distance, total calories burned, exercise sessions, and heart rate records.
  * **Nutrition & Hydration:** Hydration logs, daily volume targets, and weight metrics.
  * **Strict On-Device Processing:** All Health Connect bio-data is processed **100% locally on-device** (in-memory & local DB) to generate personal skin and style insights. ZoeWave LLC **never stores, transmits, or shares** Health Connect bio-data on external cloud servers or with third parties.

---

### 4. Analytics, Performance & Tracking
* **Google Analytics & Advertising ID (`AD_ID`):** KoColor uses **Google Analytics for Firebase** and **Firebase Crashlytics** for anonymized usage metrics, performance monitoring, and app stability. Google Analytics utilizes the Advertising ID (`AD_ID`) and anonymous App Instance ID to measure install attribution, app usage trends, and crash diagnostics.
* **No Third-Party Ads:** We do not display third-party advertisements or sell user data to data brokers.

---

### 5. Third-Party Services and API Integration
KoColor utilizes a limited number of industry-standard third-party services to ensure application stability and performance:

#### **A. Stability and Analytics (Standard)**
The app uses SDKs to monitor performance, ANRs, and anonymized usage trends. These services use an anonymous **App Instance ID** to distinguish device software versions:
* [Google Play Services](https://policies.google.com/privacy)
* [Firebase Crashlytics](https://firebase.google.com/support/privacy/) (For crash reporting and stability debugging)
* [Google Analytics for Firebase](https://firebase.google.com/policies/analytics) (For anonymized usage metrics)

#### **B. Optional Developer Intelligence (BYOK Model)**
Users may optionally provide their own **Google Gemini API Key** in settings for custom AI quota allocation:
* **Local Storage of Keys:** If provided, your API Key is stored exclusively in your device's secure local DataStore. ZoeWave LLC does not transmit, store, or access this key on external servers.
* **Governing Policies:** Interactions using custom API keys are subject to the [Google API Services User Data Policy](https://developers.google.com/terms/api-services-user-data-policy) and [Google Privacy Policy](https://policies.google.com/privacy).

---

### 6. Children’s Privacy
KoColor is a personal style and wellness utility rated **13+**. We do not knowingly collect personally identifiable information from children. Because the application operates locally without social feeds, public profile pages, or peer-to-peer messaging, it provides a secure, private environment.

---

### 7. Data Deletion and User Control
Because your data resides locally on your device, you maintain absolute sovereignty over your information:
* **In-App & Storage Deletion:** Deleting the KoColor application or clearing app storage in Android Settings instantly and permanently removes all local databases, color profiles, and stored images. ZoeWave LLC cannot recover this data once uninstalled.
* **Analytics Deletion Request:** Users can locate their anonymous **App Instance ID** in **Settings > About** and email **Developer@ZoeWave.com** to trigger a manual deletion request within the Google Firebase console.

---

### 8. Changes to This Privacy Policy
We may update our Privacy Policy periodically to reflect technical, operational, or regulatory updates. You are encouraged to review this page for updates.

---

### 9. Contact Us
If you have questions regarding our privacy architecture or this policy, please contact us at: **Developer@ZoeWave.com**.
