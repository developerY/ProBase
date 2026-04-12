# 🔐 Security Architecture: BYOK & API Key Protection

**Context:** The PhotoDo Smart Capture feature utilizes a "Bring Your Own Key" (BYOK) architecture for Google's Gemini API. Because users are trusting our application with their private Google AI Studio API keys, **protecting this secret is our highest security priority.**

This document outlines the cryptographic standards and architectural patterns used to ensure that a user's API key is never leaked, exposed to malicious apps, or held vulnerably in memory.

---

## 🚫 The Threat Model
We explicitly **DO NOT** use standard Android `SharedPreferences` or Jetpack `DataStore` (Preferences/Proto) for storing the API key. 
* Standard local storage saves data as plain-text XML or unencrypted protobuf files.
* If a user's device is rooted, or if a malicious app gains filesystem access, plain-text API keys can be instantly scraped and exploited.

## 🛡️ Our Standard: Hardware-Backed Crypto
To protect user secrets, we utilize the official **AndroidX Security Crypto** library. 

We implement `EncryptedSharedPreferences`, which wraps standard local storage in military-grade encryption backed by the Android device's physical hardware.

### How it Works:
1. **The Hardware Enclave:** When the user first enters their key, the app generates a `MasterKey`. This key is generated and permanently stored inside the Android device's physical **Trusted Execution Environment (TEE)** or **StrongBox** hardware chip. It cannot be extracted, even on a rooted device.
2. **Two-Way Encryption:**
   * The actual API Key (the value) is encrypted using **AES256-GCM**.
   * The reference name (the dictionary key, e.g., "GEMINI_API_KEY") is encrypted using **AES256-SIV**.
3. **The Result:** If a hacker opens the preferences file on the device, they will only see randomized, useless ciphertext for both the key and the value.

---

## 🏗️ Implementation Guidelines

### 1. Dependency Injection (Hilt)
The encrypted storage is isolated and provided as a Singleton via Hilt in the `:core:data` module. Standard ViewModels should never interact with the `EncryptedSharedPreferences` directly.

### 2. The Secure Repository Pattern
Access to the encrypted file is strictly mediated by the `SecureApiKeyRepository` in `:core:data`. 

---

## 🧠 Memory & UI Safety Rules (Strict Mandates)

Encrypting the key on the disk is only half the battle. Developers working on this feature must adhere to the following UI and Memory constraints:

### Rule 1: Just-In-Time (JIT) Memory Access
**NEVER** load the API key into a ViewModel `StateFlow` or `LiveData` where it sits in the device's RAM for the duration of the user's session. 

The Orchestrator fetches the key from the `SecureApiKeyRepository` at the exact millisecond the network request is made. Once the `GenerativeModel` finishes its network call, the local variable holding the API key must fall out of scope so the Android Garbage Collector can scrub it from RAM.

### Rule 2: Masked UI Inputs
When building the Settings screen where the user pastes their API key, the Jetpack Compose `OutlinedTextField` **MUST** use a password visual transformation.

### Rule 3: Do Not Log
**NEVER** print the API key or the raw `GenerativeModel` configuration block to Logcat or Crashlytics. Ensure all network logging interceptors redact any headers containing the key.
