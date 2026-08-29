Data Deletion Policy for KoColor
Developer: Developer@ZoeWave.com
At KoColor, we take your privacy and data autonomy seriously. KoColor is designed with a privacy-first, local-first architecture: all core color science, facial skin calibration, wardrobe management, and on-device AI styling execute directly on your device. We do not maintain a centralized cloud database containing your personal identity, wardrobe images, or biometric profiles.
Below are the pathways and guidelines for managing and deleting your data.
1. How to Delete Your Personal Content (Local Data)
   All of your personal wardrobe inventory, cosmetic vanity items, facial color calibration profiles, daily style plans, hydration logs, and uploaded/captured portrait images are stored entirely on your device's internal storage using local databases (Room & DataStore). We do not have access to this data and cannot delete it remotely for you.
   Steps to delete your local content:
1.
In-App Deletion: You can delete individual clothing items, cosmetics, or saved style advice entries directly within the KoColor app interface (e.g., in the Wardrobe, Vanity, or Collection Detail screens).
2.
Reset App Data: Navigating to Android System Settings > Apps > KoColor > Storage & Cache > Clear Data will instantly purge all local databases, color profiles, and cached portraits.
3.
Complete Removal: Uninstalling the KoColor app from your Android device will automatically wipe the local database, image cache, and all associated application data from your device storage.
2. On-Device AI & Biometric Image Privacy
   •
   Local NPU Execution: On-device AI features (Gemini Nano) process texture, drape, and facial color calibration 100% locally on your device's NPU/GPU. Your raw portrait photos and wardrobe images are never transmitted to external AI servers.
   •
   Cloud AI Text Bifurcation: When using optional Cloud AI fallback tiers, KoColor strictly transmits anonymized text manifests and numerical color vectors (L*C*h^∘). Raw pixel images are compile-time blocked from cloud transmission.
3. How to Request Deletion of Anonymized Analytics Data
   While we do not collect personal identifiers (such as names, phone numbers, or email addresses), KoColor uses Firebase Analytics and Crashlytics to monitor application stability and performance. These services identify app instances using an anonymous App Instance ID.
   Steps to request deletion of analytics data:
1.
Open KoColor and navigate to Settings > About.
2.
Locate your App Instance ID (a unique string of alphanumeric characters).
3.
Email your deletion request to [Developer@ZoeWave.com] with your App Instance ID.
4.
Upon receipt, we will trigger a deletion request within the Google Firebase Console to scrub all telemetry and crash records associated with your App Instance ID.
4. Data Types and Retention Periods
   Data Type
   Storage Location
   Deletion Status & Retention Policy
   Wardrobe & Cosmetic Inventory
   On-Device Storage
   User-Controlled: Deleted immediately upon in-app deletion, clearing app storage, or app uninstallation.
   Facial & Color Calibration Profiles
   On-Device Storage
   User-Controlled: Stored locally in Room DB. Wiped immediately upon app uninstallation.
   Portrait Photos & Camera Captures
   On-Device Storage
   User-Controlled: Stored locally on internal storage. Deleted immediately upon app uninstallation.
   Health Connect Wellness Logs (Optional)
   On-Device Memory
   User-Controlled: Read synchronously from local Health Connect API; never stored externally.
   Crash Logs & Performance Metrics
   Firebase Crashlytics
   Automated Cleanup: Scrubbed from Firebase servers within 90 days of the crash event.
   Analytics Identifiers
   Firebase Analytics
   By Request: Kept for up to 14 months unless a manual deletion request is submitted via App Instance ID.
5. Contact for Privacy & Data Inquiries
   If you have any questions regarding your data, privacy rights, or this policy, please reach out to us:
   •
   Developer: ZoeWave Development Team
   •
   Email: Developer@ZoeWave.com
   •
   Privacy Policy: Accessible within the app at Settings > About > Privacy Policy