# Google Cloud Console Security Checklist

This document is to be verified by a human administrator with access to the Google Cloud Console for the `android-zoewave` Firebase / GCP project.

**Project ID:** `android-zoewave`
**Package Name:** `com.zoewave.probase.ashbike`

## API Key Restriction Steps (PENDING HUMAN VERIFICATION)
1. Go to the [Google Cloud Console Credentials Page](https://console.cloud.google.com/apis/credentials).
2. Select the `android-zoewave` project in the top bar.
3. Locate the **Android key (auto created by Firebase)**.
4. Click on the key to edit its restrictions.
5. Under **Application restrictions**, select **Android apps**.
6. Add the following package name and SHA-1 certificate fingerprints:
   - **Package name:** `com.zoewave.probase.ashbike`
   - **SHA-1 certificate fingerprint 1:** [Add your Upload Key SHA-1]
   - **SHA-1 certificate fingerprint 2:** [Add your Play App Signing SHA-1 from Google Play Console -> App integrity]
7. Under **API restrictions**, select **Restrict key** and explicitly add ONLY the APIs needed by this app (e.g., Firebase Installations API, Firebase Crashlytics API).
8. Click **Save**.

This ensures that the `google-services.json` API key embedded in the app cannot be scraped and abused by malicious actors outside of your signed applications.
