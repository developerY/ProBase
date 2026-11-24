# ROLE & OPERATING MODE
You are a disciplined Android NFC + BLE systems engineer adding **NFC Forum BLE Connection Handover (`application/vnd.bluetooth.le.oob`, Bluetooth SIG BTSSP 1.3 / NFC Forum Connection Handover)** to **AshBike Mobile** (`:applications:ashbike:apps:mobile` and `:applications:ashbike:features:ble`).

This builds directly on the existing v1 **Multi-Sensor BLE Hub** (`BleSensorHub`, `CyclingBleUuids`, `MobileSensorModule`). You MUST obey every byte-level specification, Android 14+ API rule, scope fence, and hard stop below without exception.

---

## HARD SCOPE FENCE (NEVER VIOLATE)
1. **IN SCOPE:**
   - `applications/ashbike/features/ble` (pure Kotlin OOB codec, NFC handover manager/abstraction, `BleSensorHub` direct OOB connect entry point, unit tests)
   - `applications/ashbike/apps/mobile` (manifest `android.hardware.nfc required="false"`, Activity reader-mode wiring, Settings BLE Sensor UI for Tap-to-Pair & Write NFC Tag, `PRIVACY_POLICY_PUBLIC.md` if needed, `BLE_HUB_HARDWARE_TEST.md`)
2. **STRICTLY OUT OF SCOPE (DO NOT EDIT OR BREAK):**
   - Wear OS (`applications/ashbike/apps/wear/**`)
   - XR / Glass (`applications/ashbike/apps/mobile/features/glass/**`, `features/xr/**`)
   - Sibling apps (KoColor, SeaWeed, PhotoDo, GoSwift, GotMind)
   - Do NOT add `FOREGROUND_SERVICE_CONNECTED_DEVICE`, `BLUETOOTH_ADVERTISE`, `ACCESS_BACKGROUND_LOCATION`, or remove `neverForLocation` from `BLUETOOTH_SCAN`.
   - Do NOT add `lint-baseline.xml` or `abortOnError = false`.
   - Do NOT claim proprietary MIFARE e-bike unlock emulation or cloud BikeID accounts—this is strictly offline-first **NFC Forum BLE Connection Handover**.
3. **ONE TASK AT A TIME:** Work strictly in order: **Phase 0 → Task 1 → Task 2 → Task 3 → Task 4**.
   - **NEVER** start the next task until the user explicitly replies `APPROVED`.

---

## AUTHORITATIVE NFC FORUM BTSSP 1.3 / BLE OOB SPECIFICATION (DO NOT DEVIATE)

### NDEF Record Structure
- **MIME Type:** `"application/vnd.bluetooth.le.oob"` (`NdefRecord.TNF_MIME_MEDIA`, ASCII bytes)
- **Compatibility Rule (READ):** Support **BOTH**:
  1. **NFC Forum Static Handover NDEF Message:** Record 0 is Well-Known Handover Select (`TNF_WELL_KNOWN`, type `"Hs"`), followed by one or more `TNF_MIME_MEDIA` `"application/vnd.bluetooth.le.oob"` carrier configuration records.
  2. **Direct Simplified BLE OOB NDEF Message:** A single `TNF_MIME_MEDIA` `"application/vnd.bluetooth.le.oob"` record (common on ST25 / DIY NTAG215 stickers).
  - Always iterate all records in the NDEF message and parse every `application/vnd.bluetooth.le.oob` record found!
- **Compatibility Rule (WRITE):** When writing an NTAG215/NDEF tag from AshBike, write a standard `NdefRecord.createMime("application/vnd.bluetooth.le.oob", payload)` (clean, compact, universally readable by Android, nRF Connect, and TagInfo; stays well inside NTAG213/NTAG215 capacity).

### Exact Binary TLV Payload Inside `application/vnd.bluetooth.le.oob`
Payload consists of contiguous Bluetooth GAP AD Structures: `[length: uint8] [adType: uint8] [data: (length - 1) bytes]`.
- Validate `length >= 1` and `offset + 1 + length <= payload.size` on EVERY iteration. If `length == 0` or truncated, stop/fail safely (NEVER throw `IndexOutOfBoundsException`).
- **AD Type `0x1B` — LE Bluetooth Device Address (MANDATORY, `data.size == 7`, so `length == 8`):**
  - Bytes `0..5`: 48-bit MAC address in **LITTLE-ENDIAN** byte order!
    - Example: bytes `FF EE DD CC BB AA` decode to `"AA:BB:CC:DD:EE:FF"` (UPPERCASE, `Locale.ROOT`).
  - Byte `6`: Address Type:
    - `0x00` = `PUBLIC` (`BluetoothDevice.ADDRESS_TYPE_PUBLIC`)
    - `0x01` = `RANDOM` (`BluetoothDevice.ADDRESS_TYPE_RANDOM`)
    - Any other byte value → invalid/reject safely.
- **AD Type `0x1C` — LE Role (`data.size == 1`, `length == 2`):**
  - `0x00` = Peripheral Only (supported)
  - `0x01` = Central Only (**REJECT / mark incompatible** — AshBike is the Central connecting to a bike Peripheral!)
  - `0x02` = Peripheral & Central, Peripheral preferred (supported)
  - `0x03` = Peripheral & Central, Central preferred (supported)
  - *(If `0x1C` is omitted on a minimal DIY tag, default to `PERIPHERAL_ONLY` rather than failing hard, but ALWAYS write `0x02, 0x1C, 0x00` when AshBike encodes a tag.)*
- **AD Type `0x08` / `0x09` — Shortened / Complete Local Name (Optional):** UTF-8 string.
- **AD Type `0x02` / `0x03` — Incomplete / Complete List of 16-bit Service Class UUIDs (Optional):**
  - Sequence of Little-Endian `uint16` values (e.g., `16 18` → `0x1816` CSC, `18 18` → `0x1818` CPS, `0D 18` → `0x180D` HRS, `26 18` → `0x1826` FTMS). Map to standard Bluetooth Base UUID `0000XXXX-0000-1000-8000-00805F9B34FB`.
- **AD Type `0x06` / `0x07` — Incomplete / Complete List of 128-bit Service Class UUIDs (Optional):**
  - Sequence of 16-byte **LITTLE-ENDIAN** (byte-reversed!) UUIDs (e.g., OpenBikeControl `d273f680-d548-419d-b9d1-fa0472345229` is stored on the wire as `29 52 34 72 04 FA D1 B9 9D 41 48 D5 80 F6 73 D2`).
- **AD Type `0x19` — Appearance (Optional):** `uint16` LE (2 bytes).
- **AD Types `0x10` (SM TK Value), `0x22` (LESC Confirmation), `0x23` (LESC Random):** Optional 16-byte security blobs; parse/retain as `ByteArray?` cleanly without crashing.
- **Unknown AD Types:** Skip cleanly by advancing `1 + length` bytes.

---

## ANDROID 14+ (`minSdk 34`) NFC & BLE RULES (ZERO TOLERANCE)
1. **Optional Hardware Feature:** Confirm `AndroidManifest.xml` declares BOTH:
   - `<uses-permission android:name="android.permission.NFC" />`
   - `<uses-feature android:name="android.hardware.nfc" android:required="false" />`
   *(CRITICAL: `required="false"` so devices without NFC hardware stay eligible on Google Play!)*
2. **Use `NfcAdapter.enableReaderMode` (NOT deprecated Android Beam / `setNdefPushMessage`):**
   - Enable reader mode ONLY while the user is actively on the BLE Sensors screen (or taps "Tap NFC Tag to Pair" / "Write NFC Tag"), and ALWAYS call `disableReaderMode(activity)` in `onPause` / `DisposableEffect.onDispose`.
   - Use flags `FLAG_READER_NFC_A or FLAG_READER_NFC_B or FLAG_READER_NFC_F or FLAG_READER_NFC_V` (do NOT enable P2P).
   - Graceful degradation: If `NfcAdapter.getDefaultAdapter(context) == null` (phone has no NFC hardware), hide NFC tap/write buttons cleanly with zero crash. If NFC is turned off, show a clear inline prompt with an action to open `Settings.ACTION_NFC_SETTINGS`.
3. **API 33+ Direct LE Connection (`BluetoothAdapter.getRemoteLeDevice`):**
   - Use `bluetoothAdapter.getRemoteLeDevice(record.macAddress.uppercase(Locale.ROOT), addressType)` (`BluetoothDevice.ADDRESS_TYPE_PUBLIC` / `BluetoothDevice.ADDRESS_TYPE_RANDOM`) so both Public and Random static BLE MACs from NFC OOB tags connect directly via `BleSensorHub` without requiring a prior BLE scan!
   - Guard with explicit `BLUETOOTH_CONNECT` runtime permission check and `SecurityException` handling.
4. **No Vacuous Tests & No Deprecated APIs:** Never add `@Ignore`, never mock away the codec under test, and paste verbatim Gradle command + exit code + test counts at every checkpoint.
5. **UI Conventions:** Standard **Jetpack Compose Material 3 Expressive** components with the default theme (NO custom theme); 100% of user-facing strings in `strings.xml`.

---

## REQUIRED CHECKPOINT FORMAT
End EVERY phase/task with this exact block and **STOP**:

```text
### CHECKPOINT: [Phase / Task Name]
1. Files Read (with exact paths):
2. Files Created / Modified (git diff --stat):
3. What Changed (bulleted, factual, citing exact symbols and line numbers):
4. Automated Verification Run (exact command + exit code + verbatim output):
5. Tests Added / Updated (exact test class + test method names + assertions proven):
6. Risks / Uncertainties / Manual Hardware Checks Remaining:
7. STATUS: WAITING FOR USER APPROVAL (Reply "APPROVED" to proceed to next task)
```

---

## PHASE 0 — DISCOVERY & BASELINE (NO CODE EDITS ALLOWED)
1. Run `git status` and inspect:
   - `applications/ashbike/apps/mobile/src/main/AndroidManifest.xml` (verify current `NFC` permission & whether `android.hardware.nfc required="false"` is present)
   - `applications/ashbike/features/ble/src/main/java/com/zoewave/ashbike/features/ble/BleSensorHub.kt` & `CyclingBleUuids.kt`
   - `applications/ashbike/apps/mobile/src/main/java/com/zoewave/probase/ashbike/mobile/MainActivity.kt`
   - `applications/ashbike/apps/mobile/features/settings/` (locate the BLE Sensors screen & ViewModel)
   - `applications/ashbike/apps/mobile/src/main/java/com/zoewave/probase/ashbike/mobile/di/MobileSensorModule.kt`
2. Output **CHECKPOINT: Phase 0** with the exact integration plan. **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 1 — Pure Kotlin NFC Forum BLE OOB Codec (`BleOobHandoverCodec.kt`) + Ground-Truth Unit Tests
**Objective:** Build a pure, Android-framework-free encoder/decoder for `application/vnd.bluetooth.le.oob` payloads inside `applications/ashbike/features/ble`.

### Step 1A: Implementation
Create `BleOobHandoverCodec.kt` in `applications/ashbike/features/ble/src/main/java/com/zoewave/ashbike/features/ble/`:
- Constant `const val MIME_TYPE_BLE_OOB = "application/vnd.bluetooth.le.oob"`.
- Data models:
  - `enum class BleAddressType(val wireValue: Int) { PUBLIC(0x00), RANDOM(0x01) }`
  - `enum class BleLeRole(val wireValue: Int)` (`PERIPHERAL_ONLY(0x00)`, `CENTRAL_ONLY(0x01)`, `BOTH_PERIPHERAL_PREFERRED(0x02)`, `BOTH_CENTRAL_PREFERRED(0x03)`)
  - `data class BleOobHandoverData(val macAddress: String, val addressType: BleAddressType, val leRole: BleLeRole = BleLeRole.PERIPHERAL_ONLY, val localName: String? = null, val serviceUuids: List<UUID> = emptyList(), val appearance: Int? = null, val smTkValue: ByteArray? = null, val lescConfirmation: ByteArray? = null, val lescRandom: ByteArray? = null)`
  - `sealed interface BleOobParseResult { data class Valid(val data: BleOobHandoverData) : BleOobParseResult; data class Invalid(val reason: String) : BleOobParseResult }`
- Functions:
  - `decodeOobPayload(payload: ByteArray): BleOobParseResult` — strictly validates bounds, decodes Little-Endian MAC (`0x1B`), LE Role (`0x1C`, returning `Invalid` if `CENTRAL_ONLY`), UTF-8 name (`0x08`/`0x09`), 16-bit (`0x02`/`0x03`) & Little-Endian 128-bit (`0x06`/`0x07`) UUIDs, Appearance (`0x19`), and optional security fields (`0x10`/`0x22`/`0x23`), skipping unknown AD types safely.
  - `encodeOobPayload(data: BleOobHandoverData): ByteArray` — encodes `0x1B`, `0x1C`, optional `0x09` (UTF-8), optional `0x03` (for 16-bit standard SIG UUIDs), and optional `0x07` (for 128-bit UUIDs like OpenBikeControl in Little-Endian byte order).

### Step 1B: Mandatory Ground-Truth Unit Tests (`BleOobHandoverCodecTest.kt`)
Prove ALL exact vectors:
1. **Random Address + Peripheral Role + Name + 16-bit CSC & CPS UUIDs:**
   - Input bytes: `0x08, 0x1B, 0xFF, 0xEE, 0xDD, 0xCC, 0xBB, 0xAA, 0x01, 0x02, 0x1C, 0x00, 0x0A, 0x09, 0x57, 0x61, 0x68, 0x6F, 0x6F, 0x20, 0x43, 0x41, 0x44, 0x05, 0x03, 0x16, 0x18, 0x18, 0x18`
   - Asserts: `macAddress == "AA:BB:CC:DD:EE:FF"`, `addressType == RANDOM`, `leRole == PERIPHERAL_ONLY`, `localName == "Wahoo CAD"`, `serviceUuids` contains exact `CyclingBleUuids.CSC_SERVICE` and `CyclingBleUuids.CPS_SERVICE`.
2. **Public Address + 128-bit Little-Endian OpenBikeControl UUID:**
   - Input bytes: `0x08, 0x1B, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00, 0x02, 0x1C, 0x00, 0x11, 0x07, 0x29, 0x52, 0x34, 0x72, 0x04, 0xFA, 0xD1, 0xB9, 0x9D, 0x41, 0x48, 0xD5, 0x80, 0xF6, 0x73, 0xD2`
   - Asserts: `macAddress == "01:02:03:04:05:06"`, `addressType == PUBLIC`, `serviceUuids` contains `d273f680-d548-419d-b9d1-fa0472345229`.
3. **Central-Only Role Rejection:**
   - Input bytes: `0x08, 0x1B, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00, 0x02, 0x1C, 0x01`
   - Asserts: returns `BleOobParseResult.Invalid`.
4. **Round-Trip Encode → Decode:** Encoding a `BleOobHandoverData` (including MAC, address type, name, 16-bit HRS/CSC/FTMS UUIDs, and 128-bit OpenBikeControl UUID) and decoding back produces an equal `BleOobHandoverData`.
5. **Malformed / Edge Payloads:** Empty array, missing `0x1B`, truncated `0x1B` (length 5), invalid address type `0x05`, zero-length AD entry, unknown AD type `0xFE` before valid `0x1B` (unknown skipped cleanly, valid MAC still parsed), zero exceptions.

### Step 1C: Verification
- Run `./gradlew :applications:ashbike:features:ble:testDebugUnitTest` and paste verbatim output.
- Output **CHECKPOINT: Task 1** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 2 — `NfcBleHandoverManager` & Direct `BleSensorHub` OOB Connection
**Objective:** Bridge Android NDEF tag reading/writing to `BleSensorHub` cleanly with testable transport abstractions and full unit test coverage.

### Step 2A: Implementation
1. **Direct OOB Connect in `BleSensorHub` / Transport:**
   - Add `connectFromOob(oob: BleOobHandoverData)` (or overload `connect(address: String, addressType: BleAddressType, displayName: String?)`) that resolves the remote `BluetoothDevice` using `bluetoothAdapter.getRemoteLeDevice(oob.macAddress.uppercase(Locale.ROOT), androidAddressType)` on Android API 33+, guards `BLUETOOTH_CONNECT`, persists the sensor to the local saved roster on connect, and runs the exact same per-device serialized GATT discovery/subscription queue (`HRS`, `CPS`, `CSC`, `FTMS`, `BAS`, `OpenBikeControl`).
2. **Create `NfcBleHandoverManager.kt`** in `applications/ashbike/features/ble/src/main/java/com/zoewave/ashbike/features/ble/`:
   - Pure/testable NDEF extraction helper: `extractBleOobRecords(records: List<NdefRecordSnapshot>): List<BleOobHandoverData>` (handles both single-record `application/vnd.bluetooth.le.oob` and NFC Forum Static Handover `Hs` + `application/vnd.bluetooth.le.oob` multi-record messages!).
   - Android `NfcAdapter` foreground reader controller (`enableReadMode(activity, onResult)` / `enableWriteMode(activity, data, onWriteResult)` / `disableReaderMode(activity)`):
     - Handles `NfcAvailability`: `UNAVAILABLE` (no NFC chip), `DISABLED` (NFC off in Settings), `READY`.
     - Write path safely checks `Ndef.get(tag)` / `NdefFormatable.get(tag)`, verifies `isWritable` and `maxSize >= messageBytes.size`, writes cleanly on `Dispatchers.IO`, closes the tag in `finally`, and emits typed `NfcWriteResult` (`Success`, `ReadOnlyTag`, `TagTooSmall`, `IoError`).
     - Zero hardcoded mocks, zero main-thread I/O.

### Step 2B: Required Unit Tests (`NfcBleHandoverManagerTest.kt`)
Prove with real unit tests (no tautological mocks):
1. Extracting BLE OOB data from a **Static Handover multi-record message** (`Hs` record + `application/vnd.bluetooth.le.oob` record) succeeds.
2. Extracting BLE OOB data from a **Direct single-record message** (`application/vnd.bluetooth.le.oob`) succeeds.
3. Unrelated NDEF tags (e.g., plain text/URL record with no `application/vnd.bluetooth.le.oob`) return empty / structured `NotABleHandoverTag` result without crashing.
4. `connectFromOob` passes uppercase MAC + exact `BleAddressType` (`PUBLIC` vs `RANDOM`) into `BleSensorHub` and refuses `CENTRAL_ONLY` payloads cleanly.

### Step 2C: Verification
- Run `./gradlew :applications:ashbike:features:ble:testDebugUnitTest`.
- Output **CHECKPOINT: Task 2** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 3 — Manifest, DI & Material 3 Settings UI (Tap-to-Pair & Write NFC Tag)
**Objective:** Ensure Play-safe manifest declaration and wire intuitive Tap-to-Pair + Write NFC Tag flows into AshBike's BLE Sensors Settings screen.

### Step 3A: Implementation
1. **Manifest (`applications/ashbike/apps/mobile/src/main/AndroidManifest.xml`):**
   - Verify `<uses-permission android:name="android.permission.NFC" />` is present.
   - Add/verify `<uses-feature android:name="android.hardware.nfc" android:required="false" />`.
2. **Settings → Bluetooth LE Sensors UI (Standard Material 3 Expressive, Default Theme):**
   - When NFC hardware is available (`NfcAvailability != UNAVAILABLE`):
     - Add a **"Tap NFC Sensor to Pair"** card/button (activates foreground reader mode while sheet/dialog is open; if NFC is disabled, offers button to open Android NFC Settings). When a valid `application/vnd.bluetooth.le.oob` tag is tapped, prompt for `BLUETOOTH_CONNECT` if not yet granted, then connect immediately via `BleSensorHub.connectFromOob(...)` and show live connection feedback!
     - On each **Saved / Connected Sensor** card, add a **"Write NFC Tag"** action that encodes that sensor's MAC address, address type, name, and discovered cycling profile UUIDs (`HRS`/`CPS`/`CSC`/`FTMS`/`OpenBikeControl`) into a standard `application/vnd.bluetooth.le.oob` NDEF message and prompts the user to hold a blank NTAG215 sticker against the back of the phone.
   - When NFC hardware is unavailable on the device, keep the UI completely clean (hide NFC actions).
   - Extract 100% of new strings to `strings.xml`.
3. **Privacy Policy (`applications/ashbike/docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md`):**
   - Add a brief, accurate note under optional hardware permissions stating that NFC is used solely on-device for standard NFC Forum BLE Connection Handover (tap-to-pair / writing local cycling sensor tags) with zero cloud transmission.

### Step 3B: Required Tests
- Add/update ViewModel unit tests verifying:
  - Tapping a valid BLE OOB record triggers `connectFromOob` and updates UI state cleanly.
  - Unavailable NFC hides NFC actions; disabled NFC surfaces the enable-NFC state.
  - Writing a tag builds valid `BleOobHandoverData` from a saved sensor and surfaces `Success` / `ReadOnlyTag` / `TagTooSmall` states.
- Run `./gradlew :applications:ashbike:apps:mobile:testDebugUnitTest :applications:ashbike:apps:mobile:features:settings:testDebugUnitTest :applications:ashbike:features:ble:testDebugUnitTest`.
- Output **CHECKPOINT: Task 3** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 4 — Full E2E Regression, R8/Manifest Audit & Desk Test Protocol
1. Update `applications/ashbike/docs/release/BLE_HUB_HARDWARE_TEST.md` to add an **NFC Tap-to-Pair & Write NTAG215 Tag** test section (testing both writing a tag for an existing sensor / nRF52840 peripheral and tapping it back to auto-connect).
2. Run the full end-to-end verification script:
   - `./scripts/test_ashbike_e2e.sh`
   - Paste verbatim scoreboard and confirm **`FAIL : 0`**, including:
     - `android.permission.NFC` present & `android.hardware.nfc required="false"`
     - `BleOobHandoverCodec` & `NfcBleHandoverManager` compiled into release AAB (`mapping.txt`)
     - `BleOobHandoverCodecTest` & `NfcBleHandoverManagerTest` executed with 0 failures
     - `lintRelease` 0 Fatal / 0 Error & `bundleRelease` green
3. Confirm `git diff --stat` has zero edits in Wear OS, XR/Glass, or sibling apps.
4. Output **CHECKPOINT: Task 4 (NFC FORUM BLE CONNECTION HANDOVER COMPLETE)** and **STOP.**

---

**BEGIN NOW WITH PHASE 0 ONLY. DO NOT EDIT ANY FILES UNTIL PHASE 0 DISCOVERY IS COMPLETE.**