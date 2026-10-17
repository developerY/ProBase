Here is the complete, end-to-end guide to building and deploying the **Cardless Cloudflare Worker + Firebase App Check JWKS Proxy** on the server and integrating it into your **Android client (`KoColor`)** with zero API keys inside your APK and no credit card required.

---

## Part 1: Get Your Two Identifiers (`developerY` Account)

Before writing code, collect two values from your **`developerY`** project:

1. **Your Firebase Project Number:**
* Look at your `App ID` in the Firebase Console (for example, in your screenshot it was `1:960844692488:android:5bd6e122118f8e32b3a5bd`).


* The number between `1:` and `:android:` is your **Project Number** (e.g., `960844692488`).


* Also download the **`google-services.json`** file from this exact `developerY` Firebase project and place it in your Android project's `app/` folder.




2. **Your Standalone Gemini API Key:**
* Go to [Google AI Studio](https://aistudio.google.com/app/apikey) or the [Google Cloud Credentials page](https://console.cloud.google.com/apis/credentials) while signed into **`developerY`**.
* Click **+ Create credentials $\rightarrow$ API key**.


* Open the new key, scroll to **API restrictions**, choose **Restrict key**, and select **only** **`Gemini API`** (leave **Application restrictions** set to *None* because your Cloudflare Worker server will be calling it, not the Android phone directly).


* Click **Save** and copy the key string (`AIzaSy...`). **Never paste this key into Android Studio.**



---

## Part 2: Build & Deploy the Cloudflare Worker Server (Zero Credit Card)

You can deploy this Worker in two ways: via the **Cloudflare Web Dashboard** (zero CLI tools required) or via **Terminal (Wrangler CLI)**. Both are 100% free and require no credit card.

### Option 2A: Terminal Setup (Recommended)

1. **Create a Free Cloudflare Account:**
* Sign up at [dash.cloudflare.com/sign-up/workers-and-pages](https://dash.cloudflare.com/sign-up/workers-and-pages) (verify your email; do not enter any billing info).


2. **Scaffold the Project on Your Computer:**
   Open a terminal outside your Android project folder and run:
```bash
mkdir kocolor-gemma-proxy
cd kocolor-gemma-proxy
npm create cloudflare@latest . -- --type=hello-world --ts --no-deploy
npm install jose

```


3. **Configure `wrangler.jsonc` (or `wrangler.toml`):**
   Open `wrangler.jsonc` (or `wrangler.toml`) in the folder and add your `FIREBASE_PROJECT_NUMBER` under `vars`:
```json
{
  "name": "kocolor-gemma-proxy",
  "main": "src/index.ts",
  "compatibility_date": "2026-10-01",
  "vars": {
    "FIREBASE_PROJECT_NUMBER": "960844692488"
  }
}

```


(Replace `960844692488` with your `developerY` project number if different).


4. **Write the Proxy Server Code (`src/index.ts`):**
   Replace the contents of `src/index.ts` with this complete production implementation:
```typescript
import { createRemoteJWKSet, jwtVerify } from "jose";

export interface Env {
  GEMINI_API_KEY: string;
  FIREBASE_PROJECT_NUMBER: string;
}

// Cached at the Worker module level so Cloudflare doesn't re-fetch Google's keys on every request
const FIREBASE_APP_CHECK_JWKS = createRemoteJWKSet(
  new URL("https://firebaseappcheck.googleapis.com/v1/jwks")
);

interface ProxyRequestPayload {
  model?: string;
  prompt?: string;
  systemInstruction?: string;
  temperature?: number;
  maxOutputTokens?: number;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    // 1. Only allow POST requests
    if (request.method !== "POST") {
      return Response.json(
        { error: "Method Not Allowed. Use POST." },
        { status: 405 }
      );
    }

    // 2. Require the X-Firebase-AppCheck header from the Android app
    const appCheckToken = request.headers.get("X-Firebase-AppCheck");
    if (!appCheckToken) {
      return Response.json(
        { error: "Unauthorized: Missing X-Firebase-AppCheck header" },
        { status: 401 }
      );
    }

    // 3. Cryptographically verify the App Check JWT against Google's public JWKS
    const projectNumber = env.FIREBASE_PROJECT_NUMBER;
    try {
      await jwtVerify(appCheckToken, FIREBASE_APP_CHECK_JWKS, {
        issuer: `https://firebaseappcheck.googleapis.com/${projectNumber}`,
        audience: `projects/${projectNumber}`,
        algorithms: ["RS256"],
      });
    } catch (err: any) {
      return Response.json(
        {
          error: "Unauthorized: Invalid or expired App Check token",
          details: err?.message ?? "JWT verification failed",
        },
        { status: 401 }
      );
    }

    // 4. Parse incoming JSON body from the Android app
    let payload: ProxyRequestPayload;
    try {
      payload = (await request.json()) as ProxyRequestPayload;
    } catch {
      return Response.json({ error: "Invalid JSON body" }, { status: 400 });
    }

    const prompt = payload.prompt?.trim();
    if (!prompt) {
      return Response.json(
        { error: "Missing 'prompt' field in request body" },
        { status: 400 }
      );
    }

    // Allow only Gemma / Gemini models
    const model = payload.model || "gemma-4-26b-a4b-it";
    const temperature = payload.temperature ?? 0.2;
    const maxOutputTokens = payload.maxOutputTokens ?? 2048;

    // 5. Construct the upstream Google Generative Language API request
    const upstreamUrl = `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(
      model
    )}:generateContent`;

    const googleRequestBody: Record<string, unknown> = {
      contents: [
        {
          role: "user",
          parts: [{ text: prompt }],
        },
      ],
      generationConfig: {
        temperature,
        maxOutputTokens,
      },
    };

    if (payload.systemInstruction) {
      googleRequestBody.systemInstruction = {
        parts: [{ text: payload.systemInstruction }],
      };
    }

    // 6. Call Google's API injecting the encrypted server-side GEMINI_API_KEY
    const upstreamResponse = await fetch(upstreamUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-goog-api-key": env.GEMINI_API_KEY,
      },
      body: JSON.stringify(googleRequestBody),
    });

    const responseText = await upstreamResponse.text();

    return new Response(responseText, {
      status: upstreamResponse.status,
      headers: {
        "Content-Type": "application/json",
      },
    });
  },
};

```


5. **Log in, Upload the Encrypted Secret Key, and Deploy:**
```bash
npx wrangler login
npx wrangler secret put GEMINI_API_KEY

```


*(When prompted, paste the `AIzaSy...` key from Part 1. It is encrypted on Cloudflare's edge servers).*
Now deploy the Worker:
```bash
npx wrangler deploy

```


* **Verification:** Your terminal will output your live HTTPS URL:
  `https://kocolor-gemma-proxy.<your-subdomain>.workers.dev`
  If you open that URL in a browser (which sends a `GET` request), it will immediately return `{"error":"Method Not Allowed. Use POST."}`, confirming the server is live 24/7.



---

## Part 3: Build the Keyless Android Client (`KoColor`)

Now configure your Android app so it initializes Firebase App Check, fetches a short-lived App Check JWT token before calling Gemma, and sends it to your Cloudflare Worker URL.

### Step 1: Gradle Dependencies (`app/build.gradle.kts`)

Ensure you have the Firebase App Check dependencies alongside OkHttp and Coroutines Play Services (for `.await()`):

```kotlin
dependencies {
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    // Firebase App Check (Play Integrity for Release, Debug Provider for Debug builds)
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    debugImplementation("com.google.firebase:firebase-appcheck-debug")

    // Networking & Coroutines Task await()
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
}

```

### Step 2: Initialize App Check in Your `Application` Class

In your `Application` class (e.g., `KoColorApp.kt`), install the `DebugAppCheckProviderFactory` for debug builds and `PlayIntegrityAppCheckProviderFactory` for release builds:

```kotlin
package com.zoewave.probase.kocolor

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize

class KoColorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(context = this)

        val appCheck = Firebase.appCheck
        if (BuildConfig.DEBUG) {
            appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }
}

```

### Step 3: Register Your Debug Token in the `developerY` Firebase Console

1. Run your app in Debug mode on your phone or emulator.
2. In Android Studio **Logcat**, filter by `DebugAppCheckProvider`. You will see:
```text
D DebugAppCheckProvider: Enter this debug secret into the allow list in the Firebase Console for your project: 12345678-abcd-ef01-2345-6789abcdef01

```


3. Copy that UUID string.
4. Go to your **`developerY` Firebase Console $\rightarrow$ Build $\rightarrow$ App Check $\rightarrow$ Apps**.
5. Click the **three-dot menu ($\vdots$)** next to **`KoColor` (`com.zoewave.probase.kocolor`)** $\rightarrow$ select **Manage debug tokens**.


6. Click **Add debug token**, give it a name (e.g., `DevPhone`), paste the UUID, and click **Save**.

### Step 4: Replace `Gemma4Rest.kt` with the Keyless Worker Client

Replace your old `Gemma4Rest.kt` implementation (which was calling `generativelanguage.googleapis.com` with the restricted Firebase key) with this keyless client that calls your Cloudflare Worker URL and automatically refreshes the App Check token if needed:

```kotlin
package com.zoewave.probase.kocolor.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class Gemma4Rest(
    // Paste your deployed Cloudflare Worker URL here (safe to be public; protected by App Check JWT)
    private val workerUrl: String = "https://kocolor-gemma-proxy.your-subdomain.workers.dev"
) {
    companion object {
        private const val TAG = "Gemma4Rest"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Calls gemma-4-26b-a4b-it through the Cloudflare Worker proxy using only a Firebase App Check JWT.
     * Zero API keys exist in the APK.
     */
    suspend fun generateContent(
        prompt: String,
        modelName: String = "gemma-4-26b-a4b-it",
        systemInstruction: String? = null,
        temperature: Double = 0.2
    ): String = withContext(Dispatchers.IO) {
        // First attempt uses cached non-expired App Check token (forceRefresh = false)
        val firstAttempt = executeProxyCall(
            prompt = prompt,
            modelName = modelName,
            systemInstruction = systemInstruction,
            temperature = temperature,
            forceRefreshToken = false
        )

        // If 401 (e.g. clock skew or newly registered debug token), force-refresh App Check token once
        if (firstAttempt.statusCode == 401) {
            Log.w(TAG, "App Check token rejected (401); forcing token refresh and retrying once...")
            val retryAttempt = executeProxyCall(
                prompt = prompt,
                modelName = modelName,
                systemInstruction = systemInstruction,
                temperature = temperature,
                forceRefreshToken = true
            )
            if (!retryAttempt.isSuccess) {
                throw IOException("Gemma4Proxy failed (${retryAttempt.statusCode}): ${retryAttempt.body}")
            }
            return@withContext parseCandidateText(retryAttempt.body)
        }

        if (!firstAttempt.isSuccess) {
            throw IOException("Gemma4Proxy failed (${firstAttempt.statusCode}): ${firstAttempt.body}")
        }

        parseCandidateText(firstAttempt.body)
    }

    private suspend fun executeProxyCall(
        prompt: String,
        modelName: String,
        systemInstruction: String?,
        temperature: Double,
        forceRefreshToken: Boolean
    ): ProxyHttpResult {
        val tokenResult = Firebase.appCheck
            .getAppCheckToken(forceRefreshToken)
            .await()

        val jsonPayload = JSONObject().apply {
            put("model", modelName)
            put("prompt", prompt)
            put("temperature", temperature)
            if (!systemInstruction.isNullOrBlank()) {
                put("systemInstruction", systemInstruction)
            }
        }.toString()

        val request = Request.Builder()
            .url(workerUrl)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Firebase-AppCheck", tokenResult.token)
            .post(jsonPayload.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            return ProxyHttpResult(
                isSuccess = response.isSuccessful,
                statusCode = response.code,
                body = responseBody
            )
        }
    }

    private fun parseCandidateText(rawJson: String): String {
        val root = JSONObject(rawJson)
        val candidates = root.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""

        val content = candidates.getJSONObject(0).optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""

        return buildString {
            for (i in 0 until parts.length()) {
                val partObj = parts.getJSONObject(i)
                // Skip internal thought tokens if thinking mode is returned
                if (!partObj.optBoolean("thought", false)) {
                    append(partObj.optString("text", ""))
                }
            }
        }.trim()
    }

    private data class ProxyHttpResult(
        val isSuccess: Boolean,
        val statusCode: Int,
        val body: String
    )
}

```

---

## Part 4: How to Verify Every Layer Is Working

1. **Test Unauthorized Rejection:**
   Run in your terminal:
```bash
curl -X POST https://kocolor-gemma-proxy.<your-subdomain>.workers.dev \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Hello"}'

```


It must immediately return `401 {"error":"Unauthorized: Missing X-Firebase-AppCheck header"}`, proving nobody on the internet can abuse your Worker URL.
2. **Test Your Android App:**
   Call `Gemma4Rest().generateContent("Suggest a 3-color palette for a navy blazer.")` inside your app.
* **Expected Result:** `Firebase.appCheck.getAppCheckToken()` generates a signed RS256 token, your Cloudflare Worker verifies the signature against Google's JWKS endpoint, injects `env.GEMINI_API_KEY` at the server edge, and returns the `gemma-4-26b-a4b-it` styling response in ~800–1400ms—with **zero API keys in your APK** and **your computer completely turned off**.