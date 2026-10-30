# KoColor: Google Play Deployment Guide

This guide outlines the critical skills, checklists, and procedures required to successfully deploy KoColor to the Google Play Store. It covers pre-release checks, building the Android App Bundle (AAB), Play Console compliance, and rollout strategies.

---

## 1. Pre-Release Validation & Quality Checks
Before generating a release build, ensure the application passes all automated and manual quality gates.

*   **Static Analysis & Linting:** Run `./gradlew lintRelease` to catch accessibility, performance, and internationalization issues.
*   **Unit & Instrumentation Tests:** Execute `./gradlew testReleaseUnitTest` and connected Android tests to verify core logic (e.g., Color Science engine, Data Connect).
*   **Play Policy Insights:** Run policy audits to ensure no undeclared data collection is occurring. KoColor handles biometric (facial/body) inferences entirely on-device, which must be clearly stated in the Privacy Policy.

## 2. R8 Shrinking & Obfuscation
KoColor utilizes R8 to minimize APK size and obfuscate code.
*   **Configuration:** Ensure `isMinifyEnabled = true` and `isShrinkResources = true` in the `build.gradle.kts` release build type.
*   **Proguard Rules:** Verify that all Dagger Hilt, Retrofit, and Room Database rules are correctly preserved in `proguard-rules.pro`.
*   **Crashlytics Mapping:** Firebase Crashlytics requires de-obfuscation mapping files. Ensure the Firebase Crashlytics Gradle plugin is applied so `mapping.txt` is automatically uploaded upon release build generation.

## 3. App Signing & Keystore Management
Google Play uses **Play App Signing**. 
*   **Upload Key:** Sign the local build with the `upload-keystore.jks`. Keep the keystore file, alias, and passwords secure (do not commit them to version control).
*   **Play Console Key:** Google Play will strip your upload signature and re-sign the APKs generated from your AAB with the official App Signing Key.
*   *Note:* Ensure the SHA-1/SHA-256 fingerprints of the Google Play App Signing key are registered in the Firebase Console (for Firebase Auth/AppCheck to function correctly in production).

## 4. Building the Android App Bundle (AAB)
Google Play requires an `.aab` file instead of an `.apk`.
1.  In Android Studio, go to **Build > Generate Signed Bundle / APK...**
2.  Select **Android App Bundle**.
3.  Choose the `upload-keystore.jks` and enter the credentials.
4.  Select the `release` build variant and click **Finish**.
5.  *CLI Alternative:* `./gradlew :applications:kocolor:apps:mobile:bundleRelease`

## 5. Google Play Console Compliance
Before submitting for review, the Store Listing must be fully configured:

*   **Data Safety Form:** Accurately declare all data collected. KoColor is "On-Device First," meaning most data (like camera feeds) never leaves the device. Cloud AI fallbacks must be documented.
*   **Health Apps Policy:** If KoColor requests Health Connect permissions (e.g., Hydration, Sleep), provide the required video demonstrating the use case and justification in the App Content section.
*   **Content Rating:** Complete the IARC questionnaire.
*   **Store Assets:** Ensure high-res icons (512x512), feature graphics (1024x500), and phone/tablet screenshots are uploaded.

## 6. Release Tracks & Rollout Strategy
Do not push directly to Production. Follow a staged rollout strategy:

1.  **Internal Testing:** Distribute to up to 100 internal QA testers for immediate feedback.
2.  **Closed Testing (Alpha/Beta):** Roll out to a broader group of invited users to test production infrastructure (Firebase, Databases, APIs).
3.  **Production (Staged Rollout):** When pushing to production, use a staged rollout (e.g., 10% -> 50% -> 100%) to monitor Firebase Crashlytics for unexpected regressions before full deployment.

## 7. Post-Launch Monitoring
*   **Firebase Crashlytics:** Monitor for fatal and non-fatal crashes.
*   **Play Console Vitals:** Keep ANR (Application Not Responding) and crash rates below the "bad behavior" threshold (1.09%).
*   **User Feedback:** Respond to reviews and adapt the roadmap based on feature requests (e.g., new Starter Packs in the Sync Hub).