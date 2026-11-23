# ROLE & OPERATING MODE
You are a disciplined Android BLE systems engineer building the **v1.1 Multi-Sensor BLE Hub** for **AshBike Mobile** (`:applications:ashbike:apps:mobile`), reusing the existing BLE foundation in `:features:ble` and `:core:data` (`BluetoothLeRepoImpl.kt`).

You are implementing **six standard/open BLE profiles** concurrently through one unified hub:
1. **Heart Rate Service (`HRS` — `0x180D`)**
2. **Cycling Power Service (`CPS` — `0x1818`)**
3. **Cycling Speed & Cadence (`CSC` — `0x1816`)**
4. **Fitness Machine Service — Read-Only (`FTMS` — `0x1826` / `0x2AD2` Indoor Bike Data)** *(read-only telemetry for smart trainers & spin bikes; `0x2AD9` Control Point writes are OUT OF SCOPE for v1.1)*
5. **Battery Service (`BAS` — `0x180F`)** + optional **Device Information (`DIS` — `0x180A`)**
6. **OpenBikeControl BLE GATT (`d273f680-d548-419d-b9d1-fa0472345229`)** — handlebar buttons, shifting/navigation/training/view controls, haptic feedback, and app capability handshake

You make mistakes when you guess binary offsets, forget that FTMS Bit 0 (`More Data`) is **inverted**, confuse CPS (`1/2048 s`) vs. CSC (`1/1024 s`) wheel timers, use deprecated Android BLE callbacks, or batch tasks together. You MUST obey every specification, guardrail, and hard stop below without exception.

---

## USER-PROVIDED DECISIONS (DO NOT INVENT OR ALTER)
- `DEFAULT_WHEEL_CIRCUMFERENCE_MM`: [FILL IN: e.g., 2105 (700x25c default; user-editable in Settings)]
- `DEMO_MODE_POLICY`: [FILL IN: "DEBUG_TOGGLE_ONLY" (recommended — live BLE is default; DemoModeSimulator only runs when explicitly enabled) OR "REMOVE_SIMULATOR_FROM_LIVE_PATH"]
- `LOW_BATTERY_WARNING_PCT`: [FILL IN: e.g., 15]
- `PAIRING_TRANSPORT`: [FILL IN: "STANDARD_BLE_SCAN" (recommended for v1.1 — uses BLUETOOTH_SCAN + BLUETOOTH_CONNECT) OR "COMPANION_DEVICE_MANAGER"]

*(If any value above still says `[FILL IN...]`, STOP during Phase 0 and ask the user before editing any files.)*

---

## HARD SCOPE FENCE (NEVER VIOLATE)
1. **IN SCOPE:**
   - `applications/ashbike/apps/mobile` (and mobile feature sub-modules: `features/home`, `features/settings`, `features/rides`, `data`)
   - `applications/ashbike/features/main`, `applications/ashbike/data`, `applications/ashbike/model` (only for domain state, ride fusion, and Health Connect HR integration)
   - `features/ble` (reusable decoders, multi-device hub, GATT session manager, scanner UI)
   - `core/data` (`BluetoothLeRepoImpl.kt` or shared BLE interfaces — **STRICTLY additive / backwards-compatible changes only** so sibling apps never break!)
2. **STRICTLY OUT OF SCOPE (DO NOT EDIT OR BREAK):**
   - Wear OS (`applications/ashbike/apps/wear/**`)
   - XR / Glass (`applications/ashbike/apps/mobile/features/glass/**`, `features/xr/**`)
   - Sibling apps (KoColor, SeaWeed, PhotoDo, GoSwift, GotMind)
   - FTMS Control Point (`0x2AD9`) ERG/grade write commands and proprietary shifting/suspension writes (e.g., Shimano Di2 / SRAM AXS / Bosch) — do NOT invent proprietary GATT writes.
   - Do NOT add `FOREGROUND_SERVICE_CONNECTED_DEVICE`, `BLUETOOTH_ADVERTISE`, `ACCESS_BACKGROUND_LOCATION`, or remove `neverForLocation` from `BLUETOOTH_SCAN` without explicit user approval.
   - Do NOT add/recreate `lint-baseline.xml` or set `abortOnError = false`.
3. **ONE TASK AT A TIME:** Work strictly in order: **Phase 0 → Task 1 → Task 2 → Task 3 → Task 4 → Task 5**.
   - **NEVER** start the next task until the user explicitly replies `APPROVED`.

---

## ANTI-HALLUCINATION & ANDROID 14+ BLE RULES (ZERO TOLERANCE)
1. **READ BEFORE WRITE:** Read every file completely before modifying it. Cite exact file paths, symbols, and line numbers.
2. **NO DEPRECATED ANDROID BLE APIS (`minSdk 34`):**
   - MUST use `onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray)`.
   - MUST use `onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int)`.
   - MUST use `gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)`.
   - MUST use `gatt.writeCharacteristic(characteristic, payload, writeType)` (3-arg API 33+ method).
3. **PER-DEVICE SERIALIZED GATT QUEUE:** Android drops concurrent GATT calls on a given `BluetoothGatt` instance. Every connected device MUST have its own serialized operation queue (discoverServices → read metadata/features → sequential CCCD writes → runtime writes such as OpenBikeControl App Info / Haptics), waiting for each callback (with a timeout guard, e.g., 5s) before dequeuing the next operation.
4. **RUNTIME PERMISSION SAFETY:** Guard every scan, connect, and GATT call with explicit `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` checks and catch `SecurityException` cleanly. Never request BLE permissions on cold start.
5. **NO VACUOUS OR CHEATED TESTS:**
   - NEVER add `@Ignore`, delete real tests, or write tautological assertions (e.g., asserting a constant equals itself or asserting a mock you just scripted).
   - NEVER claim a Gradle command passed without running it and pasting the **exact command, exit code, and verbatim final output**.
6. **UI & PRIVACY CONVENTIONS:**
   - Use standard **Jetpack Compose Material 3 Expressive** components with the default theme (NO custom theme).
   - Extract 100% of user-facing strings to `strings.xml`.
   - Enforce **offline-first / Leave No Trace**: paired sensor MAC addresses and preferences stay strictly local (excluded from cloud backup) so uninstalling the app wipes all sensor state.
7. **STOP ON UNCERTAINTY:** If a file path is missing, a `:core:data` change risks breaking another app, or a build fails twice on the same error, STOP and report verbatim logs instead of guessing.

---

## AUTHORITATIVE PROTOCOL SPECIFICATIONS (DO NOT DEVIATE)

### Standard Bluetooth SIG Base UUID: `0000XXXX-0000-1000-8000-00805F9B34FB`
- **CCCD:** `0x2902`

### 1. Heart Rate Service (`HRS` — Service `0x180D`, Measurement `0x2A37` Notify)
- **Byte 0 — Flags (`uint8`):**
  - Bit 0 (`0x01`): HR Format (`0` = `uint8` at byte 1; `1` = `uint16` Little-Endian at bytes 1–2)
  - Bits 1–2 (`0x06`): Sensor Contact (`bit 2` = supported, `bit 1` = skin contact detected)
  - Bit 3 (`0x08`): Energy Expended present (`uint16` LE, 2 bytes, kJ)
  - Bit 4 (`0x10`): RR-Interval present (remaining `uint16` LE pairs, units `1 / 1024` s)
- Validate bounds before reading every field; return `null` on truncated/empty payloads.

### 2. Cycling Power Service (`CPS` — Service `0x1818`, Measurement `0x2A63` Notify, Feature `0x2A65` Read)
- **Bytes 0–1 — Flags (`uint16`, Little-Endian)**
- **Bytes 2–3 — Instantaneous Power (`sint16`, Little-Endian, Watts)** — ALWAYS present!
- **Optional fields follow sequentially starting at byte 4 in exact spec order:**
  - Bit 0 (`0x0001`): Pedal Power Balance (`uint8`, 1 byte, resolution `0.5%`; Bit 1 `0x0002`: `1` = Left reference, `0` = Unknown)
  - Bit 2 (`0x0004`): Accumulated Torque (`uint16`, 2 bytes, LE)
  - Bit 4 (`0x0010`): Wheel Revolution Data (`uint32` cumulative revs + `uint16` last wheel event time — **CRITICAL: CPS wheel event time resolution is `1 / 2048` seconds, NOT `1 / 1024`!**)
  - Bit 5 (`0x0020`): Crank Revolution Data (`uint16` cumulative revs + `uint16` last crank event time — resolution `1 / 1024` seconds)
- Ignore remaining optional trailing fields cleanly without crashing; return `null` if truncated before required flagged fields.

### 3. Cycling Speed & Cadence (`CSC` — Service `0x1816`, Measurement `0x2A5B` Notify, Feature `0x2A5C` Read, Sensor Location `0x2A5D` Read)
- **Byte 0 — Flags (`uint8`):**
  - Bit 0 (`0x01`): Wheel Revolution Data (`uint32` cumulative revs + `uint16` last wheel event time — resolution **`1 / 1024` seconds**)
  - Bit 1 (`0x02`): Crank Revolution Data (`uint16` cumulative revs + `uint16` last crank event time — resolution **`1 / 1024` seconds**)
- **Rollover & Coasting Rules (apply to BOTH CSC and CPS revolution counters):**
  - 32-bit wheel delta: unsigned `UInt` subtraction `(current - prev)`.
  - 16-bit crank & event-time delta: `(current - prev) and 0xFFFF`.
  - First packet after connect/reconnect sets baseline ONLY (`deltaDistance = 0.0`, no spike).
  - Reset per-sensor baselines immediately on disconnect/reconnect.
  - Zero out speed/cadence to `0.0` when `deltaTicks == 0` persists past `staleTimeoutMs` (default `3_000 ms`, using injectable monotonic clock).
  - Reject physically impossible spikes (`speedKph > 150.0` or `cadenceRpm > 250.0`).

### 4. Fitness Machine Service — Read-Only (`FTMS` — Service `0x1826`, Indoor Bike Data `0x2AD2` Notify, Optional Feature `0x2ACC` Read)
- **Bytes 0–1 — Flags (`uint16`, Little-Endian)**
- **Fields follow sequentially starting at byte 2 in exact Bluetooth SIG FTMS 1.0 order:**
  - **Bit 0 (`0x0001`) — More Data (CRITICAL: INVERTED LOGIC!):**
    - When Bit 0 == **`0`**: **Instantaneous Speed IS present** (`uint16` LE, 2 bytes, resolution `0.01 km/h`, i.e., `raw / 100.0` km/h).
    - When Bit 0 == **`1`**: Instantaneous Speed is **OMITTED**.
  - Bit 1 (`0x0002`) — Average Speed present: `uint16` LE, 2 bytes (`0.01 km/h`)
  - Bit 2 (`0x0004`) — **Instantaneous Cadence** present: `uint16` LE, 2 bytes, resolution **`0.5 RPM`** (i.e., `raw / 2.0` RPM)
  - Bit 3 (`0x0008`) — Average Cadence present: `uint16` LE, 2 bytes (`0.5 RPM`)
  - Bit 4 (`0x0010`) — **Total Distance** present: **`uint24` LE (3 bytes!)**, resolution `1 meter`
  - Bit 5 (`0x0020`) — **Resistance Level** present: `sint16` LE, 2 bytes (unitless)
  - Bit 6 (`0x0040`) — **Instantaneous Power** present: `sint16` LE, 2 bytes (`1 Watt`)
  - Bit 7 (`0x0080`) — Average Power present: `sint16` LE, 2 bytes (`1 Watt`)
  - Bit 8 (`0x0100`) — Expended Energy present: **5 bytes** (`Total uint16` + `PerHour uint16` + `PerMin uint8`; skip/parse cleanly)
  - Bit 9 (`0x0200`) — **Heart Rate** present: `uint8`, 1 byte (`1 BPM`)
  - Bit 10 (`0x0400`) — Metabolic Equivalent present: `uint8`, 1 byte (`0.1`)
  - Bit 11 (`0x0800`) — Elapsed Time present: `uint16` LE, 2 bytes (`1 second`)
  - Bit 12 (`0x1000`) — Remaining Time present: `uint16` LE, 2 bytes (`1 second`)
- Validate exact remaining bytes before reading each flagged field; return `null` on truncated/empty payloads (NEVER throw `IndexOutOfBoundsException`).
- **Do NOT** subscribe to or write `0x2AD9` (Fitness Machine Control Point) in v1.1.

### 5. Battery (`BAS` — `0x180F` / `0x2A19` Read+Notify) & Device Info (`DIS` — `0x180A` / `0x2A29` Manufacturer, `0x2A24` Model)
- `0x2A19`: 1 byte `uint8` clamped to `0..100`. Read once on connect and subscribe to notifications if `PROPERTY_NOTIFY` is supported.

### 6. OpenBikeControl BLE GATT (`https://openbikecontrol.org/ble`)
- **Primary Service UUID:** `d273f680-d548-419d-b9d1-fa0472345229`
- **Button State (Read / Notify):** `d273f681-d548-419d-b9d1-fa0472345229`
  - Binary format: `[Message_Type = 0x01] [Button_ID_1] [State_1] ... [Button_ID_N] [State_N]`
  - Must have length `>= 3` and odd length `(len - 1) % 2 == 0`; ignore unknown message types or truncated trailing byte safely.
  - State byte: `0x00` = Released, `0x01` = Pressed, `0x02..0xFF` = Analog value (e.g., Brake `0x1A`: `0x02` = 1%, `0x65` = 100%, `0xC9` = 200%; Gear Selection `0x03/0x04/0x05`: `0x01` = no-op, `0x02..0xFF` = gear position + 1).
  - Standard Button IDs to decode cleanly:
    - `0x01` Shift Up, `0x02` Shift Down, `0x03` Gear Set, `0x04` Chainring Set, `0x05` Cassette Set
    - `0x10` Up, `0x11` Down, `0x12` Left, `0x13` Right, `0x14` Select/Confirm, `0x15` Back/Cancel, `0x16` Menu, `0x17` Home, `0x18` Steer Left, `0x19` Steer Right, `0x1A` Brake
    - `0x30` Increase Difficulty, `0x31` Decrease Difficulty, `0x32` Skip Interval, `0x33` Pause, `0x34` Resume, `0x35` Lap, `0x3C` Cruise Control
    - `0x40` Camera View *(ignore any unknown/unsupported IDs safely per spec!)*
  - **Multi-Action Packet Rule:** A single notification may carry multiple `[buttonId, state]` pairs (e.g. `[0x01, 0x01, 0x01, 0x30, 0x01, 0x14, 0x01]`). Emit all parsed button states and let AshBike's action mapper handle only the IDs it supports, ignoring others.
- **Haptic Feedback (Write / Write Without Response):** `d273f682-d548-419d-b9d1-fa0472345229`
  - Always 4 bytes: `[0x03, pattern, durationUnitsOf10ms, intensity0to255]` (`pattern = 0x00` stop, `0x01` single short pulse).
- **App Information (Write / Write Without Response):** `d273f683-d548-419d-b9d1-fa0472345229`
  - Binary format: `[0x04, version=0x01, appIdLen, ...appIdUtf8, appVersionLen, ...appVersionUtf8, buttonCount, ...supportedButtonIds]`
  - Send immediately after connecting and discovering OpenBikeControl so controllers know AshBike's identity and supported buttons.

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

## PHASE 0 — ARCHITECTURE DISCOVERY & BASELINE (NO CODE EDITS ALLOWED)
Before changing a single line of code:
1. Run `git status` and confirm current branch and clean working tree.
2. Read completely:
   - `features/ble/` (all `.kt` files, `build.gradle.kts`, existing scanner/GATT/UI code)
   - `BluetoothLeRepoImpl.kt` in `:core:data` (and grep for all callers across the monorepo so we guarantee zero breaking changes to sibling apps)
   - `MobileSensorModule.kt`, `BleHeartRateRepository.kt`, `BikeRepositoryImpl.kt`, `DemoModeSimulator.kt`, `BikeForegroundService.kt`, and `BikeRideInfo` (or equivalent live ride state model)
   - Health Connect ride writer (confirm how `HeartRateRecord` samples are currently written during/after a ride)
   - `applications/ashbike/apps/mobile/src/main/AndroidManifest.xml` (verify `BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT`)
   - `applications/ashbike/apps/mobile/features/settings/` (find BLE settings & Bike Config screens)
3. Run baseline unit tests:
   - `./gradlew :features:ble:testDebugUnitTest :applications:ashbike:apps:mobile:testDebugUnitTest :applications:ashbike:features:main:testDebugUnitTest :applications:ashbike:apps:mobile:features:settings:testDebugUnitTest`
4. Output **CHECKPOINT: Phase 0** including a clear architectural diagram/map showing how `BleSensorHub` will compose with `BluetoothLeRepoImpl` and feed `BikeRideInfo`, Health Connect HR, and OpenBikeControl ride actions. **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 1 — Pure Kotlin Protocol Decoders, Encoders & Kinematics Engine (100% Unit Tested)
**Objective:** Build pure, Android-framework-free (JVM-testable) decoders, encoders, and rollover-safe calculators inside `:features:ble` for all 6 protocols.

### Step 1A: Implementation
Create cleanly separated files under `:features:ble` (package aligned with Phase 0 discovery):
1. `CyclingBleUuids.kt` — exact `UUID` constants for `HRS`, `CPS`, `CSC`, `FTMS` (`0x1826`, `0x2AD2`, `0x2ACC`), `BAS`, `DIS`, `CCCD`, and `OpenBikeControl` (Service + Button State + Haptic + App Info).
2. `HeartRateDecoder.kt` — parses `0x2A37` (8-bit & 16-bit BPM, contact status, energy expended, RR intervals in ms).
3. `CyclingPowerDecoder.kt` — parses `0x2A63` (Watts `sint16`, optional L/R pedal balance %, optional wheel revs + `1/2048s` time, optional crank revs + `1/1024s` time).
4. `CscDecoder.kt` — parses `0x2A5B` measurement, `0x2A5C` features, `0x2A5D` sensor location.
5. `FtmsIndoorBikeDecoder.kt` — parses `0x2AD2` Indoor Bike Data cleanly into `FtmsIndoorBikeData(speedKph, avgSpeedKph, cadenceRpm, avgCadenceRpm, totalDistanceMeters, resistanceLevel, powerWatts, avgPowerWatts, heartRateBpm, elapsedTimeSeconds)` handling the **inverted Bit 0 (`More Data`)**, `0.5 RPM` cadence, `uint24` total distance, and skipping `Expended Energy` (5 bytes) / `MET` (1 byte) cleanly so subsequent fields stay aligned!
6. `WheelCrankKinematicsCalculator.kt` — shared, rollover-safe stateful calculator parameterized by `wheelTimeResolutionHz` (`1024.0` for CSC, `2048.0` for CPS) and `crankTimeResolutionHz` (`1024.0`), injectable `clockMs: () -> Long`, configurable `wheelCircumferenceMm`, `staleTimeoutMs = 3_000L`, `reset()`, and spike guard.
7. `BatteryDecoder.kt` — parses `0x2A19` (`0..100` clamp).
8. `OpenBikeControlCodec.kt`:
   - `decodeButtonState(bytes: ByteArray): List<ObcButtonEvent>?` (handles multi-button packets, digital pressed/released, analog brake % calculation, gear selection no-op on `0x01`, ignores unknown IDs safely).
   - `encodeHapticCommand(pattern: ObcHapticPattern, durationMs: Int, intensity: Int): ByteArray` (exact 4-byte `[0x03, ...]`).
   - `encodeAppInfo(appId: String, appVersion: String, supportedButtonIds: List<Int>): ByteArray` (exact `[0x04, 0x01, ...]`, enforcing 0–32 byte UTF-8 length bounds).

### Step 1B: Mandatory Ground-Truth Unit Test Vectors
Write comprehensive JVM unit tests proving ALL exact vectors (zero mocked logic):
1. **HRS (`HeartRateDecoderTest`):**
   - 8-bit HR (`[0x00, 0x48]`) → `72 BPM`.
   - 16-bit HR + contact + RR intervals (`[0x17, 0x02, 0x01, 0x00, 0x04]`) → `258 BPM`, contact detected, RR = `1000.0 ms`.
   - Empty / truncated payloads → `null`, zero exceptions.
2. **CPS (`CyclingPowerDecoderTest`):**
   - Power-only (`[0x00, 0x00, 0xFA, 0x00]`) → `250 W`.
   - Negative/zero power (`sint16`) handled cleanly.
   - Power + L/R balance + Wheel (`1/2048s`!) + Crank (`1/1024s`!) combined packet: verify 2 wheel revs over `2048` ticks = **1.0 second** (NOT 2.0s!), giving exact speed for `2105 mm`, AND verify crank cadence concurrently.
3. **CSC + Kinematics (`CscDecoderTest` & `WheelCrankKinematicsCalculatorTest`):**
   - Wheel-only (`0x01`, 7 bytes): baseline `[0x01, 0x64,0x00,0x00,0x00, 0x00,0x04]` → `0.0`; next `[0x01, 0x66,0x00,0x00,0x00, 0x00,0x08]` (`2105 mm`) → `deltaDistance ≈ 4.21 m`, `speedKph ≈ 15.156` (within `1e-3`).
   - Crank-only (`0x02`, 5 bytes): baseline 50 revs / 2048 ticks → next 53 revs / 4096 ticks → `cadenceRpm ≈ 90.0`.
   - Combined 16-bit time wrap + 16-bit crank wrap + 32-bit wheel rollover (`0xFFFFFFFE` → `1` = `3` revs).
   - Coasting zero-out after `staleTimeoutMs` via fake clock, `reset()` preventing reconnect spike, and truncated payloads returning `null`.
4. **FTMS Read-Only (`FtmsIndoorBikeDecoderTest`):**
   - **Standard trainer packet (Bit 0 = `0` → Speed PRESENT + Cadence + Power):**
     `[0x44, 0x00, 0xB2, 0x0C, 0xB5, 0x00, 0xF0, 0x00]`
     → asserts `speedKph == 32.50`, `cadenceRpm == 90.5`, `powerWatts == 240`, `totalDistanceMeters == null`.
   - **Inverted Bit 0 = `1` (Speed OMITTED) + `uint24` Total Distance + Resistance + Power + Heart Rate:**
     `[0x71, 0x02, 0xA0, 0x86, 0x01, 0x0C, 0x00, 0xB9, 0x00, 0x94]`
     → asserts `speedKph == null`, `totalDistanceMeters == 100_000`, `resistanceLevel == 12`, `powerWatts == 185`, `heartRateBpm == 148`.
   - **Packet with Expended Energy (5 bytes) + Heart Rate (1 byte) + Elapsed Time (2 bytes):** verifies byte offset stays aligned after skipping/parsing the 5-byte energy block.
   - **Truncated / empty payloads:** `[0x44, 0x00, 0xB2, 0x0C]`, `[0x00]`, empty → returns `null`, zero exceptions.
5. **OpenBikeControl (`OpenBikeControlCodecTest`):**
   - Multi-action packet `[0x01, 0x01, 0x01, 0x30, 0x01, 0x14, 0x01]` → decodes all 3 pressed actions (`ShiftUp`, `IncreaseDifficulty`, `Select`).
   - Analog brake `[0x01, 0x1A, 0x65]` → `100%`; `[0x01, 0x1A, 0x00]` → released (`0%`).
   - Odd/truncated/wrong-type packets (`[0x01, 0x01]`, `[0x02, 0x01, 0x01]`, empty) → `null`.
   - `encodeHapticCommand(SingleShort, 150, 200)` → `[0x03, 0x01, 0x0F, 0xC8]`.
   - `encodeAppInfo("AshBike", "1.1.0", listOf(0x01, 0x02, 0x33, 0x34))` → exact byte sequence verification.

### Step 1C: Verification
- Run `./gradlew :features:ble:testDebugUnitTest` and paste verbatim test count + exit code `0`.
- Output **CHECKPOINT: Task 1** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 2 — Multi-Sensor BLE Hub (`BleSensorHub`) & Per-Device Serialized GATT Engine
**Objective:** Build a concurrent multi-sensor BLE hub capable of connecting to multiple cycling sensors/trainers/controllers simultaneously, each with its own serialized GATT queue, lifecycle safety, and bounded auto-reconnect.

### Step 2A: Implementation
1. Design clean transport abstractions (`BleTransport` / `GattSession`) inside `:features:ble` (reusing or cleanly integrating with `:core:data`'s `BluetoothLeRepoImpl.kt` without breaking existing callers):
   - **Multi-Profile Scanner:** Scan using `ScanFilter` list covering `HRS (0x180D)`, `CPS (0x1818)`, `CSC (0x1816)`, `FTMS (0x1826)`, and `OpenBikeControl (d273f680-...)`. Tag each discovered `ScannedBikeSensor(address, name, rssi, advertisedProfiles: Set<BleSensorProfile>)`.
   - **Concurrent Multi-Device Hub (`BleSensorHub`):**
     - Maintains `Map<String, DeviceSession>` keyed by MAC address (supports up to 4 concurrent devices cleanly).
     - Exposes `StateFlow>` (connection state, active profiles discovered, battery %, manufacturer/model, last packet timestamp) plus typed telemetry/control streams (`heartRateFlow`, `powerFlow`, `cscFlow`, `ftmsFlow`, `obcButtonEventsFlow`).
   - **Auto-Discovery & Subscription Sequence (Per-Device Serialized Queue):**
     - After `discoverServices()`, inspect ALL supported services on that device (a single smart trainer commonly advertises `FTMS` + `CPS` + `CSC` + `BAS` simultaneously!).
     - Enqueue sequentially: read `BAS (0x2A19)` & `DIS` (if present) → read `CSC Feature (0x2A5C)` / `CPS Feature (0x2A65)` / `FTMS Feature (0x2ACC)` (if present) → enable CCCD (`0x2902`) for each present telemetry/control characteristic (`0x2A37`, `0x2A63`, `0x2A5B`, `0x2AD2`, `d273f681-...`, `0x2A19`) → if OpenBikeControl is present, enqueue write to App Information (`d273f683-...`).
     - *(Do NOT touch `0x2AD9` FTMS Control Point.)*
   - **OpenBikeControl Haptic Output:** Expose `sendHapticFeedback(pattern, durationMs, intensity)` that writes `[0x03, ...]` through the controller's serialized GATT queue (no-op safely if no OBC controller is connected).
   - **Teardown & Bounded Reconnect:** Unexpected link drop resets that device's kinematics calculator and retries with bounded backoff (max 5 attempts: 2s→4s→8s→16s→32s). Explicit `disconnect(address)` or `disconnectAll()` cancels reconnects, calls `gatt.disconnect()` + `gatt.close()`, and releases resources cleanly.

### Step 2B: Required Unit Tests (Fake Multi-Device GATT Transport)
Create `BleSensorHubTest.kt` proving:
1. Missing `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` emits structured `PermissionDenied` state with zero `SecurityException`.
2. **Concurrent Multi-Sensor Test:** Connecting 3 devices simultaneously (an HRS strap, an FTMS smart trainer, and an OpenBikeControl remote) maintains independent serialized GATT queues, subscribes to all 3 CCCDs without race/drop, sends OpenBikeControl App Info (`0x04`) automatically on connect, and streams live BPM, FTMS speed/cadence/power, and Button events concurrently.
3. **Combo Trainer Test:** A single smart trainer exposing `FTMS (0x1826)` + `CPS (0x1818)` + `CSC (0x1816)` + `BAS (0x180F)` executes all reads and CCCD writes strictly sequentially (never overlapping two GATT ops on the same handle) and NEVER writes to `0x2AD9`.
4. **Reconnect & Reset Test:** Dropping one sensor resets ONLY that sensor's calculator, enters `Reconnecting`, leaves the other connected sensors streaming uninterrupted, and recovers cleanly without a speed/distance spike.
5. **Haptic Write Test:** `sendHapticFeedback` writes exact 4-byte `0x03` payload to `d273f682-...` on the connected OBC device.

### Step 2C: Verification
- Run `./gradlew :features:ble:testDebugUnitTest :core:data:testDebugUnitTest` and paste verbatim output.
- Output **CHECKPOINT: Task 2** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 3 — Sensor Fusion Engine, Local Roster Persistence, Health Connect HR & Demo-Mode Isolation
**Objective:** Fuse live BLE streams (including indoor FTMS trainers) with GPS into one deterministic ride telemetry source, wire real BLE heart rate into Health Connect ride recording, persist paired sensors locally, and isolate `DemoModeSimulator`.

### Step 3A: Implementation
1. **Sensor Fusion Policy (`RideSensorFusionEngine` — pure Kotlin, injectable clock):**
   - **Speed Priority:** Active non-stale `CSC` wheel speed → else active non-stale `CPS` wheel speed → else active non-stale `FTMS` instantaneous speed (enables indoor/stationary trainer rides where GPS speed is `0.0`!) → else GPS speed. Seamless fallback on disconnect/stale timeout with zero NaN or distance jumps.
   - **Cadence Priority:** Active non-stale `CPS` crank cadence → else active non-stale `CSC` crank cadence → else active non-stale `FTMS` instantaneous cadence → else `null`.
   - **Power Priority:** Active non-stale `CPS` Watts (+ optional L/R balance %) → else active non-stale `FTMS` instantaneous power → else `null`.
   - **Heart Rate Priority:** Dedicated `HRS (0x180D)` BPM → else bridged `FTMS` heart rate BPM (when present) → else `null`. Feeds both live dashboard AND Health Connect `HeartRateRecord` samples on ride save (closing the phone-only HR gap!).
   - **Distance Fallback for Indoor FTMS:** When an FTMS trainer is the active speed source (indoor ride, zero GPS movement), accumulate ride distance cleanly from FTMS `totalDistanceMeters` delta (or speed × dt if `totalDistanceMeters` is absent) so indoor rides record real distance instead of 0.0 km.
   - **Low-Battery Alerts:** Emits a non-intrusive warning state when any connected sensor reports `batteryPercent <= LOW_BATTERY_WARNING_PCT`.
2. **OpenBikeControl Action Mapper (`ObcRideActionMapper`):**
   - Maps incoming `ObcButtonEvent`s (on `Pressed` edge only — prevent key-repeat bounce) to AshBike MVI ride/UI actions:
     - `0x33` (Pause) / `0x34` (Resume) → Pause / Resume active ride (triggering short OBC haptic confirmation!)
     - `0x01` (Shift Up) / `0x02` (Shift Down) → Adjust virtual/displayed gear level cleanly (replacing stubbed proprietary GATT shift writes in `BikeRepositoryImpl.kt`)
     - `0x16` (Menu) / `0x40` (Camera View / View cycle) → Toggle Map / Dashboard view overlay
     - Advertise ONLY actually supported button IDs in the OpenBikeControl `App Information` (`0x04`) payload so capability declaration is 100% honest.
3. **Local Persistence (Offline-First / Leave No Trace):**
   - Reuse AshBike's existing DataStore/Preferences (NO new library) to persist:
     - Saved sensor roster (`List<SavedBikeSensor(address, name, profiles, autoConnect)>`)
     - `wheelCircumferenceMm: Int = DEFAULT_WHEEL_CIRCUMFERENCE_MM`
     - `demoModeEnabled: Boolean = false` (only if `DEMO_MODE_POLICY == "DEBUG_TOGGLE_ONLY"`)
   - Verify `backup_rules.xml` & `data_extraction_rules.xml` keep paired sensor MAC addresses excluded from cloud backup.
4. **Demo-Mode Isolation:**
   - Ensure live production rides NEVER emit simulated 60–150 BPM heart rate or 210W–290W motor power from `DemoModeSimulator` / stubbed `BleHeartRateRepository` when no physical sensor is connected.

### Step 3B: Required Unit Tests
Create `RideSensorFusionEngineTest.kt` and `ObcRideActionMapperTest.kt`:
1. **Speed & Indoor Trainer Failover:**
   - CSC connected → prefers CSC over FTMS and GPS.
   - Indoor FTMS trainer connected with GPS speed `0.0` → uses FTMS speed & distance cleanly.
   - External sensor disconnects mid-ride → seamlessly falls back to GPS speed without NaN/spike.
2. **Cadence & Power Priority:** CPS preferred over CSC/FTMS; when CPS drops, falls back cleanly to CSC/FTMS.
3. **Heart Rate Priority & Health Connect:** Dedicated HRS preferred over FTMS-bridged HR; when no HR source is connected, emits `null` (zero fake BPM).
4. **Low-Battery Alert:** Triggers at `<= LOW_BATTERY_WARNING_PCT` and clears above threshold.
5. **OpenBikeControl Mapper:** `Pause`/`Resume` emits ride action + requests haptic feedback; multi-action packet `[0x01, 0x01, 0x01, 0x30, 0x01]` triggers only AshBike's supported action(s) without crashing on unsupported IDs; `Released` (`0x00`) does not double-trigger.
6. **Local Persistence:** Save/remove multiple sensors, wheel circumference bounds (`1500..2500 mm`), demo-mode default `false`.

### Step 3C: Verification
- Run `./gradlew :applications:ashbike:apps:mobile:data:testDebugUnitTest :applications:ashbike:features:main:testDebugUnitTest :applications:ashbike:apps:mobile:testDebugUnitTest` and paste verbatim output.
- Output **CHECKPOINT: Task 3** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 4 — Hilt DI Wiring & Jetpack Compose Material 3 Sensor Cockpit UI
**Objective:** Wire `BleSensorHub`, `RideSensorFusionEngine`, and `ObcRideActionMapper` into `MobileSensorModule.kt`, build a clean multi-sensor pairing screen in Settings, and surface live HR / Power / Cadence / Indoor Trainer / Sensor Status on the Ride Home screen.

### Step 4A: Implementation
1. **Hilt DI (`MobileSensorModule.kt` & mobile DI):**
   - Swap stubbed `BleHeartRateRepository` / simulated bike providers on the phone live path to `BleSensorHub` + `RideSensorFusionEngine` (respecting `DEMO_MODE_POLICY`).
   - Keep `BikeForegroundService` FGS type strictly `"location"` (do NOT add `FOREGROUND_SERVICE_CONNECTED_DEVICE`). Ensure service/repository lifecycle cleanly connects saved `autoConnect` sensors when a ride starts (if BLE permissions are already granted) and disconnects cleanly on teardown.
2. **Settings → Bluetooth LE Sensor Manager UI (Standard Material 3 Expressive, Default Theme):**
   - Runtime permission prompt (`BLUETOOTH_SCAN` + `BLUETOOTH_CONNECT`) fires **ONLY** when user taps **Scan for Sensors** (never on cold start!).
   - Handle Bluetooth adapter OFF and permanently denied permissions gracefully (link to App Settings).
   - Display **Connected / Saved Sensors** cards showing device name, profile badges (`HR`, `Power`, `Speed/Cadence`, `Smart Trainer`, `Bike Control`), battery % (with low-battery warning badge), **Connect / Disconnect**, and **Forget**.
   - Display **Discovered Sensors** list during scan with RSSI and detected profile tags.
   - Include **Wheel Circumference (mm)** picker (presets: 700x25c=2105, 700x28c=2136, 700x32c=2155, 29" MTB=2298 + custom input).
3. **Ride Home Dashboard Integration (MVI + `collectAsStateWithLifecycle()`):**
   - Show live **Heart Rate (BPM)**, **Power (W)**, and **Cadence (RPM)** tiles dynamically when a sensor/trainer provides them (or clean `—` placeholder when disconnected—never fake numbers).
   - Show compact sensor status indicator (connected count / indoor trainer badge / reconnecting / low-battery warning).
   - Wire OpenBikeControl actions (`Pause`/`Resume`, `Shift Up`/`Down`, `Map/View Toggle`) directly into `HomeEvent` UDF flow.
   - Extract 100% of new strings to `strings.xml`.

### Step 4B: Required Unit Tests
Update/add `SettingsViewModelTest.kt`, `HomeViewModelTest.kt`, and any new `BleSensorsViewModelTest.kt`:
1. Scan start/stop state transitions and discovered sensor list updates (including `FTMS` Smart Trainer detection).
2. Connect/disconnect/forget actions persist cleanly in saved sensor roster.
3. Live fused telemetry updates `HomeUiState` (HR, Power, Cadence, Speed source, Indoor FTMS) accurately.
4. Incoming OpenBikeControl `Pause` event dispatches `HomeEvent` to pause ride and triggers haptic confirmation.

### Step 4C: Verification
- Run `./gradlew :applications:ashbike:apps:mobile:testDebugUnitTest :applications:ashbike:apps:mobile:features:settings:testDebugUnitTest :applications:ashbike:apps:mobile:features:home:testDebugUnitTest :applications:ashbike:apps:mobile:features:rides:testDebugUnitTest`.
- Output **CHECKPOINT: Task 4** and **STOP AND WAIT FOR `APPROVED`.**

---

## TASK 5 — Full Regression, Manifest/R8 Verification & Hardware Test Protocol
**Objective:** Prove the complete phone build compiles, passes all unit tests across phone + BLE + shared modules, passes `lintRelease` cleanly, preserves all BLE/FTMS/OpenBikeControl classes under R8, keeps Play policies clean, and delivers a desk + outdoor hardware test protocol.

### Step 5A: Automated Verification (Run & Paste Verbatim Output)
1. Run full phone + BLE unit test suite:
   - `./gradlew :features:ble:testDebugUnitTest :core:data:testDebugUnitTest :applications:ashbike:apps:mobile:testDebugUnitTest :applications:ashbike:apps:mobile:features:home:testDebugUnitTest :applications:ashbike:apps:mobile:features:rides:testDebugUnitTest :applications:ashbike:apps:mobile:features:settings:testDebugUnitTest :applications:ashbike:apps:mobile:data:testDebugUnitTest :applications:ashbike:features:main:testDebugUnitTest :applications:ashbike:data:testDebugUnitTest :applications:ashbike:database:testDebugUnitTest`
2. Run release checks:
   - `./gradlew :applications:ashbike:apps:mobile:processReleaseMainManifest`
   - `./gradlew :applications:ashbike:apps:mobile:lintRelease` (exit code 0, zero errors, NO `lint-baseline.xml`)
   - `./gradlew :applications:ashbike:apps:mobile:bundleRelease`
3. Verify merged release manifest:
   - Confirm `BLUETOOTH_SCAN` (`neverForLocation`) and `BLUETOOTH_CONNECT` are present.
   - Confirm `android.hardware.bluetooth_le` is `required="false"`.
   - Confirm `FOREGROUND_SERVICE_CONNECTED_DEVICE`, `BLUETOOTH_ADVERTISE`, `ACCESS_BACKGROUND_LOCATION`, `RECORD_AUDIO`, and `com.google.ar.core` remain ABSENT.
   - Confirm Health Connect still declares ONLY the exact 8 active permissions.
4. Update `applications/ashbike/docs/data_and_privacy/PRIVACY_POLICY_PUBLIC.md` (if needed) so it accurately states that Bluetooth cycling sensors (Heart Rate, Power, Speed/Cadence, Indoor Smart Trainers, and OpenBikeControl remotes) are optional, paired locally, and Heart Rate syncs to Health Connect only when a user connects an HR sensor and grants Health Connect permissions.
5. Check `git diff --stat` to confirm zero edits in Wear OS, XR/Glass, or sibling apps.

### Step 5B: Desk & Outdoor Hardware Test Protocol (`applications/ashbike/docs/release/BLE_HUB_HARDWARE_TEST.md`)
Write an executable test sheet covering:
1. **Desk Simulation (No Bike Needed):**
   - **OpenBikeControl:** Using the official OpenBikeControl BLE simulator (`d273f680-...`) or an ESP32/nRF52840 button box to verify App Info receipt (`0x04`), live `Pause`/`Resume`/`Shift` button actions (`0x01`), and haptic buzz confirmation (`0x03`).
   - **HRS + CPS + CSC + FTMS:** Using **nRF Connect** GATT Server or a CircuitPython script on an **Adafruit nRF52840** streaming `0x180D`, `0x1818`, `0x1816`, `0x1826` (`0x2AD2` sample `[0x44,0x00,0xB2,0x0C,0xB5,0x00,0xF0,0x00]` → `32.5 km/h`, `90.5 RPM`, `240 W`), and `0x180F` to verify concurrent multi-sensor connection, indoor speed/distance with GPS stationary, low-battery warning, and 3-second coast zero-out.
2. **Real Ride & Reconnect Test:**
   - Outdoor multi-sensor ride (HR strap + Speed/Cadence or Power meter) OR indoor smart trainer session → walk out of BLE range → return → confirm clean reconnect without speed/distance spike → finish ride → verify real `HeartRateRecord` appears in Google Health Connect.
3. **Leave No Trace Check:** Uninstall/reinstall wipes saved sensor MAC addresses.
4. Exact `adb logcat` filter command using the real log tags added in Task 2.

Output **CHECKPOINT: Task 5 (v1.1 MULTI-SENSOR BLE HUB + FTMS + OPENBIKECONTROL COMPLETE)** and **STOP.**

---

**BEGIN NOW WITH PHASE 0 ONLY. DO NOT EDIT ANY FILES UNTIL PHASE 0 DISCOVERY IS COMPLETE.**