# Secure Firebase Integration for Gemini (Firebase AI Logic)

This document outlines the secure implementation plan for integrating Gemini using the **Firebase AI Logic** SDK and **Firebase App Check**. This architecture ensures that the Gemini API secret key is never exposed on the device or in configuration files.

## Architectural Requirements

> [!CAUTION]
> **No Secret Keys**: We are explicitly **NOT** storing the Gemini API key in Remote Config or the APK. Remote Config is public and insecure for secrets.
> **Firebase AI Logic**: We will use the modern `firebase-ai` SDK, which acts as a secure proxy. It routes requests through Firebase's backend using the public Firebase API key and enforces client attestation before the request ever reaches the Gemini provider.

### The Production Security Stack

To achieve maximum security in production, the architecture relies on a strictly layered defense:

1. **Firebase API-Key Restrictions**: The Firebase API key included in `google-services.json` is not a secret. It is restricted to the Firebase AI Logic API and only the Firebase APIs required by the application, with appropriate application restrictions configured in Google Cloud Console.
2. **Authenticated-Users Mode**: Firebase AI Logic is configured to require Firebase Authentication, ensuring only logged-in users can trigger requests.
3. **App Check / Play Integrity**: Validates that the request comes from a genuine, untampered Android device running your authorized app.
4. **Replay Protection**: Production environments should strongly consider enforcing limited-use App Check tokens. This consumes the token on its first use, rejecting any subsequent network replay attacks.
5. **Firebase AI Logic Proxy**: Acts as the secure gateway, attaching the attestation and routing the verified request.
6. **Gemini Developer API**: The underlying generative model provider processing the verified prompt.
7. **Quotas & Monitoring**: Strict budget caps, AI monitoring, and rate limits are enforced in the Firebase Console to prevent runaway spend from authenticated users.

*(Note: AI Logic App Check enforcement became part of the guided setup in July 2026, and baseline enforcement will be strictly required for Firebase AI Logic starting November 2, 2026).*

---

## Proposed Changes

### 1. Build Configuration

#### `gradle/libs.versions.toml`

Add the modern Firebase AI and App Check libraries. The project's current Firebase BoM `34.16.0` easily satisfies the `34.14.0+` requirement needed for limited-use App Check tokens. (Note: Firebase removed `-ktx` extensions entirely starting in BoM v34.0.0, so they are not used here).

```toml
[libraries]
firebase-ai = { group = "com.google.firebase", name = "firebase-ai" }
firebase-appcheck-playintegrity = { group = "com.google.firebase", name = "firebase-appcheck-playintegrity" }
firebase-appcheck-debug = { group = "com.google.firebase", name = "firebase-appcheck-debug" }
```

### 2. AI Feature Module (`features/ai`)

#### `features/ai/firebase/build.gradle.kts`

```kotlin
plugins {
    id("composetemplate.android.library")
    id("composetemplate.android.hilt")
}
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.ai)
    implementation(libs.firebase.appcheck.playintegrity)
    // Included as standard implementation so we can toggle it in code for debug builds
    implementation(libs.firebase.appcheck.debug) 
}
```

---

### 3. Implementation Details

#### `FirebaseAiClient.kt` (Secure Client Usage)

Provides access to `GenerativeModel` instances via the Firebase SDK without requiring a secret key parameter. We explicitly define the backend, and—crucially—enable `useLimitedUseAppCheckTokens` to support our replay protection architecture.

```kotlin
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.GenerativeBackend
import com.google.firebase.ai.type.GenerativeModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAiClient @Inject constructor() {
    
    // Utilizing a current, supported Gemini model (Gemini 3.5 Flash-Lite)
    fun getModel(modelName: String = "gemini-3.5-flash-lite"): GenerativeModel {
        
        // Explicitly route to the Gemini Developer API and enable limited-use tokens for replay protection
        return Firebase.ai(
            backend = GenerativeBackend.googleAI(),
            useLimitedUseAppCheckTokens = true
        ).generativeModel(modelName = modelName)
    }
}
```

#### `AppCheckInitializer.kt` (Debug & Release Routing)

Handles the initialization of Firebase App Check. This dynamically routes between the `DebugAppCheckProviderFactory` for local development and Play Integrity for production releases.

```kotlin
import android.content.Context
import com.zoewave.probase.features.ai.BuildConfig 
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize

object AppCheckInitializer {
    fun initialize(context: Context) {
        Firebase.initialize(context)
        
        val providerFactory = if (BuildConfig.DEBUG) {
            // Generates a debug token for local testing without Play Integrity requirements
            DebugAppCheckProviderFactory.getInstance()
        } else {
            // Enforces Play Integrity device attestation for production releases
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }

        Firebase.appCheck.installAppCheckProviderFactory(providerFactory)
    }
}
```

---

## Next Steps for Local Development

1. **Sync Gradle** and run the app locally on your emulator or test device.
2. **Check Logcat:** Because you are running a `DEBUG` build, the `DebugAppCheckProviderFactory` will print a debug secret token to Logcat (search for `DebugAppCheckProvider` or `Enter this debug secret`).
3. **Register Token:** Copy that token, go to the **Firebase Console > App Check > Apps > Manage debug tokens**, and paste it in.

This eliminates the need to ship a Gemini Developer API key in the app while allowing local development to exercise the same Firebase AI Logic proxy and App Check verification architecture used in production.

for background read
features/ai/Docs/StepsPlan.md
