# Stage 4 — 16KB Page-Size Compatibility (Release Runbook Split)

Source: `../Gemini_stages.md` ("Stage 4 Prompt"). Execute ONLY this stage; feed one stage at a time to the coding agent.

## Fixes applied in this split (vs. `Gemini_stages.md`)

- **Fixed a broken pipe:** `\vert{}` (LaTeX artifact) → `|` in the split-APK `.so` filter — the published command would have failed with "command not found".
- **Added fail-fast tool guards:** abort with a clear error if `llvm-objdump`, `zipalign`, or the `bundletool` JAR resolves empty (previously an empty path would produce confusing downstream failures).
- **Added an `AAB_PATH` re-export note** at the start of Micro-step 4.2 (shell variables do not survive across terminal sessions; Stages 5–7 reuse this path).
- Verified: all-module `.so` extraction, `PAGE_ALIGNMENT_16K` config check, `Verification successful` spelling, and per-APK `zipalign` loop are consistent with `Open_Stage_feedback_1.md` §4.

---

# AGENT MISSION: Execute Stage 4 — 16KB Page-Size Compatibility (Mandatory Production Readiness Gate)

You are executing **Stage 4 (4.1, 4.2, and 4.3)** of `ProductionRoadMap.md`.

## STANDING POLICY (DO NOT VIOLATE)
- Google Play requires 16KB page-size compatibility for apps shipping native code and targeting API 35+.
- `minSdk 34`, `targetSdk 37`, `DataStore 1.2.1`, and `zstd-jni 1.5.7-21` are intentional — **NEVER downgrade any SDK or library version**. Verify 16KB compatibility forward.

---

## MICRO-STEP 4.1: Inspect Resolved AAB Dependencies & Check 16KB ELF Alignment Across ALL AAB Modules

1. Build the release bundle:
   ```bash
   ./gradlew :applications:kocolor:apps:mobile:bundleRelease
   ```
2. Extract native `.so` libraries from **ALL modules** inside the AAB (not just `base/lib/*`) into `/tmp/kocolor_16kb_check`:
   ```bash
   rm -rf /tmp/kocolor_16kb_check && mkdir -p /tmp/kocolor_16kb_check
   AAB_PATH=$(find applications/kocolor/apps/mobile/build/outputs/bundle/release -name "*.aab" | head -n 1)
   echo "Inspecting AAB: $AAB_PATH"
   unzip -q "$AAB_PATH" "*/lib/*" -d /tmp/kocolor_16kb_check
   find /tmp/kocolor_16kb_check -name "*.so" | sort
   ```
3. Locate `llvm-objdump` portably (without using `sort -V`, which fails on macOS BSD `sort`) and check the ELF `LOAD` segment alignment of **every single `.so` file across all modules and ABIs**:
   ```bash
   SDK_ROOT="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
   OBJDUMP=$(find "$SDK_ROOT/ndk" -name "llvm-objdump" 2>/dev/null | sort | tail -n 1)
   [ -x "$OBJDUMP" ] || { echo "FATAL: llvm-objdump not found under $SDK_ROOT/ndk"; exit 1; }
   echo "Using llvm-objdump: $OBJDUMP"
   for so in $(find /tmp/kocolor_16kb_check -name "*.so" | sort); do
       echo "=== $so ==="
       "$OBJDUMP" -p "$so" | grep -E "LOAD"
   done
   ```
    - **Pass Criterion:** Every `arm64-v8a` and `x86_64` `.so` library across all modules MUST show `align 2**14` (`0x4000` = 16,384 bytes) or `align 2**16` (`0x10000` = 65,536 bytes) on all `LOAD` segments.
    - Record a complete Markdown table listing every `.so` path, module, ABI, `LOAD` alignment, and `PASS`/`FAIL`.

> **CHECKPOINT 4.1:** Paste the verbatim `llvm-objdump -p` output and the complete `.so` alignment table.

---

## MICRO-STEP 4.2: Verify AAB `PAGE_ALIGNMENT_16K` Config & Run `zipalign -c -P 16 -v 4` on Generated APKs

> **NOTE:** If this micro-step runs in a fresh shell, re-export `AAB_PATH` first:
> `AAB_PATH=$(find applications/kocolor/apps/mobile/build/outputs/bundle/release -name "*.aab" | head -n 1)` (Stages 5–7 reuse this variable the same way).

1. Resolve `bundletool` and `zipalign` portably on macOS/Linux:
   ```bash
   SDK_ROOT="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
   ZIPALIGN=$(find "$SDK_ROOT/build-tools" -name "zipalign" 2>/dev/null | sort | tail -n 1)
   [ -x "$ZIPALIGN" ] || { echo "FATAL: zipalign not found under $SDK_ROOT/build-tools"; exit 1; }
   echo "Using zipalign: $ZIPALIGN"

   run_bundletool() {
       if command -v bundletool >/dev/null 2>&1; then
           bundletool "$@"
       else
           BT_JAR=$(find "$HOME" -name "bundletool*.jar" 2>/dev/null | head -n 1)
           [ -n "$BT_JAR" ] || { echo "FATAL: bundletool binary and bundletool*.jar both missing"; exit 1; }
           java -jar "$BT_JAR" "$@"
       fi
   }
   ```
2. **Check 4.2A — Verify AAB BundleConfig Alignment Metadata:**
   ```bash
   run_bundletool dump config --bundle="$AAB_PATH" | grep -i -C 2 "alignment"
   ```
    - **Pass Criterion:** Output MUST contain **`PAGE_ALIGNMENT_16K`**.
3. **Check 4.2B — Generate APKs from the AAB & Run `zipalign -c -P 16 -v 4` Across Relevant APKs:**
   ```bash
   rm -rf /tmp/kocolor_16kb_check/apks_universal /tmp/kocolor_16kb_check/apks_splits
   mkdir -p /tmp/kocolor_16kb_check/apks_universal /tmp/kocolor_16kb_check/apks_splits

   # 1. Universal APK check:
   run_bundletool build-apks --bundle="$AAB_PATH" --output=/tmp/kocolor_16kb_check/universal.apks --mode=universal
   unzip -q /tmp/kocolor_16kb_check/universal.apks -d /tmp/kocolor_16kb_check/apks_universal
   "$ZIPALIGN" -c -P 16 -v 4 /tmp/kocolor_16kb_check/apks_universal/universal.apk | tail -n 20

   # 2. Split APKs check (verify every split APK containing native .so libraries):
   run_bundletool build-apks --bundle="$AAB_PATH" --output=/tmp/kocolor_16kb_check/splits.apks
   unzip -q /tmp/kocolor_16kb_check/splits.apks -d /tmp/kocolor_16kb_check/apks_splits
   for apk in $(find /tmp/kocolor_16kb_check/apks_splits -name "*.apk" | sort); do
       if unzip -l "$apk" | grep -q "\.so$"; then
           echo "=== Checking native split APK: $apk ==="
           "$ZIPALIGN" -c -P 16 -v 4 "$apk" | tail -n 10
       fi
   done
   ```
    - **Pass Criterion:** Every `zipalign -c -P 16 -v 4` invocation MUST end with **`Verification successful`**.

> **CHECKPOINT 4.2:** Paste the verbatim `PAGE_ALIGNMENT_16K` output from `dump config` and the `Verification successful` output from `zipalign -c -P 16 -v 4`.

---

## MICRO-STEP 4.3: 16KB Page-Size Environment Runtime Verification

1. On a 16KB page-size device or Android 15/16 16KB emulator (`google_apis_ps16k`), run and record:
   ```bash
   adb shell getconf PAGE_SIZE
   adb shell getprop ro.product.model
   adb shell getprop ro.build.fingerprint
   ```
   Confirm `getconf PAGE_SIZE` outputs **`16384`**.
2. Install the generated APK set via `run_bundletool install-apks --apks=/tmp/kocolor_16kb_check/splits.apks` (or `adb install` universal APK).
3. Exercise the full native code path on the 16KB target:
    - Launch `com.zoewave.probase.kocolor`.
    - Trigger starter-pack catalog download $\rightarrow$ SHA-256 check $\rightarrow$ Ed25519 signature verification $\rightarrow$ **`libzstd-jni` native decompression** $\rightarrow$ DataStore (`libdatastore_shared_counter.so`) + Room SQLite persistence.
4. Capture Logcat proof showing zero native alignment/dlopen errors and successful catalog decompression on the 16KB target.

## STAGE 4 MANDATORY COMPLETION GATE
Check off `[x] 4.1`, `[x] 4.2`, and `[x] 4.3` in `ProductionRoadMap.md` ONLY after pasting:
1. Complete `.so` ELF `LOAD` alignment table across all AAB modules (`2**14` verified).
2. Verbatim `bundletool dump config` (`PAGE_ALIGNMENT_16K`) and `zipalign -c -P 16 -v 4` (`Verification successful`) outputs.
3. Verbatim `getconf PAGE_SIZE` (`16384`), device model, build fingerprint, and runtime verification log of catalog download + signature verify + zstd decompression.
