# 📸 Feature: Smart Capture

**Module:** `:features:smartcapture`
**App:** PhotoDo (Mobile)

## 📖 Overview
The Smart Capture module provides intelligent parsing of physical project briefs, whiteboards, and handwritten notes. It takes an image input and autonomously extracts actionable data to formulate a `TaskDraftState` (Category, Project Name, Task Name, Budget, Due Date, etc.). 

To ensure global scalability, zero infrastructure cost, and maximum user privacy, this module implements a **BYOK (Bring Your Own Key) + Local Fallback Architecture**.

---

## 🏛️ Architecture: The 3-Tier System

This module is designed to never fail. It degrades gracefully based on user configuration and network availability.

### 1. The Pro Engine (Cloud AI)
If a user has provided their own free Gemini API key in the app settings, the module uses the `generativeai` Cloud SDK to make a multimodal call to **Gemini 1.5 Flash**. 
* **Pros:** Highly accurate, contextual reasoning, flawless JSON structuring.
* **Cost/Scale:** $0 for the organization. Rate limits (1,500/day) are tied to the individual user's Google account, allowing infinite global scaling.

### 2. The Local Engine (ML Kit + Regex)
If the user has not provided an API key, or if the device is completely offline, the module gracefully falls back to Google ML Kit (`text-recognition`).
* **Execution:** Extracts raw text on-device, applies Regex to locate currencies (`budget`) and dates (`dueDate`), and assumes the first line is the `taskName`.
* **Pros:** 100% on-device (Zero-Footprint), lightning-fast, works without internet.

### 3. The Bedrock (Manual Entry)
If an image is completely illegible or OCR fails entirely, the module safely returns an empty or partially filled `TaskDraftState`, allowing the user to seamlessly complete the data entry manually in the host UI.

---

## 🔒 Security Standards

**User API Keys are NEVER stored in plain text.** This module utilizes `androidx.security:security-crypto-ktx`. User-provided Gemini keys are encrypted using **AES256-GCM** and secured via a Master Key in the Android device's hardware-backed Keystore (`EncryptedSharedPreferences`). 

Keys are fetched Just-In-Time (JIT) at the exact millisecond of the network request and immediately drop out of memory scope.

---

## 🧩 Module Interface

This module is strictly isolated and exposes only two primary components to the broader application:

### 1. Data Model
```kotlin
@Serializable 
data class TaskDraftState(
    val category: String? = null,
    val projectName: String? = null,
    val taskName: String? = null,
    val duration: String? = null,
    val dueDate: String? = null,
    val budget: Double? = null,
    val subTasks: List<String> = emptyList()
)
```

### 2. The Orchestrator
The host application interacts purely with the `SmartCaptureOrchestrator` via Hilt Dependency Injection. The Orchestrator safely abstracts the complexity of attempting the Cloud Engine, handling exceptions, and routing to the Local Engine.

```kotlin
interface SmartCaptureEngine {
    suspend fun processImage(bitmap: Bitmap, apiKey: String?): TaskDraftState
}
```

---

## 📦 Dependencies
This module is self-contained and utilizes:
* `com.google.mlkit:text-recognition` (Local OCR)
* `com.google.ai.client.generativeai` (Cloud Multimodal LLM)
* `androidx.security:security-crypto-ktx` (Hardware Encryption)
* `kotlinx.serialization.json` (Structured AI Parsing)
* `Hilt` (Dependency Injection)
