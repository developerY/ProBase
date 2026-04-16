As a Senior Architect, you are preparing to release **PhotoDo** and **Seaweed** into a regulatory environment that has changed fundamentally. In 2026, the **App Store Accountability Acts (ASAA)** in states like Texas and Utah—alongside California’s new safety mandates—require a "verifiable handshake" between your app and the platform identity.

Here is the master functional specification for your `:features:compliance` module, designed to satisfy the **0.0.3 SDK** requirements and your **"Zero-Footprint"** philosophy.

---

### **1. The Compliance Mandate (2026 Reality)**
Starting January 1, 2026, apps targeting **13+** users in regulated regions must prove they are providing "age-appropriate experiences".
* **Authoritative Identity**: You are no longer allowed to simply "trust" a user’s self-reported age in your own UI. You must treat the signal from the **Play Age Signals API** as the primary source of truth.
* **The 18+ Shift**: If the signal is **18+ (VERIFIED)**, you gain maximum model utility (Standard Gemini Filters). If the signal is **Teen (13-17)**, you must apply restrictive safety filters and, in some cases, verify parental approval for "Significant Changes" like Tier 3 Cloud AI.

---

### **2. Feature Logic: The "Compliance Handshake"**
Your `AgeSignalsManager` must execute this logic flow every time a user attempts to activate **Tier 3 (Cloud)** features:

#### **Step A: The Real-Time Query**
The app calls `getAgeSignal()`. In 2026, you are **prohibited from caching** this result to any local or remote database.
* **Zero-Footprint Rule**: The signal is transient. Once the AI extraction (coffee bill or project task) is complete, the signal should be dropped from memory.

#### **Step B: Status Mapping & Gating**
The `AgeVerificationStatus` from your model dictates the UI state:
* **`VERIFIED`**: Full access to all Tier 3 features with standard professional filters.
* **`DECLARED`**: Accessible, but restricted to **Strict Teen Filters** (`BLOCK_LOW_AND_ABOVE`) to minimize liability for unverified accounts.
* **`SUPERVISED`**: Requires an additional check of the `mostRecentApprovalDate`.

---

### **3. The "Significant Change" (Parental Approval)**
Because Tier 3 Cloud AI is a "Significant Change" in functionality for 2026, parents of supervised users must explicitly approve it.

1.  **The Console Trigger**: You must log into the Play Console and register a **"Significant Change"** for your April 2026 release.
2.  **Verbatim Description**: You provide a < 500-character description (e.g., *"Introducing AI Smart Capture for secure task extraction"*) that the parent sees on their device.
3.  **Code-Level Verification**:
    * Your code compares the `mostRecentApprovalDate` from the API against your internal `const val AI_LAUNCH_DATE = "2026-04-16"`.
    * **IF Date < Launch Date**: Status becomes `SUPERVISED_APPROVAL_PENDING`. The UI should show a "Parental Approval Required" message with a button to trigger the system-level consent prompt.

---

### **4. Safety Filter Configuration (Gemini API)**
Your `:features:compliance` module should provide a helper to generate the `SafetySetting` list based on the `AgeRange`:

| Age Bracket | Model Capability | Safety Threshold |
| :--- | :--- | :--- |
| **18+ (Verified)** | High Creativity / Advisor | `BLOCK_MEDIUM_AND_ABOVE` (Default) |
| **13-17 (Teen)** | Utility Only / Restricted | `BLOCK_LOW_AND_ABOVE` (Strict) |
| **0-12 (Child)** | **Blocked** | Cloud Features Disabled (13+ Policy) |

---

### **5. Zero-Footprint Record Keeping**
For your **ZoeWave LLC** audit trail, you do not need a server. Your "records" are the **Play Console logs** of your declarations and the **Digital Signature** of your `:features:compliance` module.
* **Legal Defense**: If an auditor asks how you protected a minor, you demonstrate that your code *cannot* initialize the `GeminiClient` without a successful `AgeSignal` handshake that passes the safety threshold.

### **Summary of App Specifics**
* **PhotoDo**: Needs the **Full Advisor** logic; if age is `< 18`, the Advisor should limit responses to purely factual task breakdowns to avoid "teen-safety" refusal errors.
* **Seaweed**: Needs the **Financial Features** declaration; since it's just "Photo-to-Ledger," it has a lower risk profile and will likely have a higher parental approval rate for supervised users.

Does this comprehensive writeup give your developer enough detail to finalize the `:features:compliance` module for your **Open Testing** submission this week?