This updated 7-stage runbook incorporates every technical, security, and evidence-integrity correction from `Open_Stage_feedback_1.md` while preserving all requirements in `ProductionRoadMap.md`:

* **Stage 1:** Preserves historical Phase 4 test logs intact (fixing only the prose arithmetic to `102`), records fresh Stage 1 test/lint runs separately, and splits the targeted unit test runs into separate Gradle invocations.


* **Stage 2:** Fixes Python `< 3.12` f-string syntax (no backslashes inside `{...}`), verifies that the actual 64-character secret value (not the variable name `KOCOLOR_SIGNING_SEED_HEX`) is absent from tracked files and Git history without printing the secret, inspects and mirrors the exact production Ed25519 signing/canonicalization protocol and `zstd -19` pipeline config, and separates the 7 automated key-transition unit tests from Check 8 (existing-client coexistence across all testing/distribution channels).


* **Stage 3:** Expands static log scanning across the entire repository (`.kt` and `.java`) and requires runtime release Logcat verification across all four flows (Weather, Cosmetics catalog, AI, and Style simulation) while preserving `Log.w`/`Log.e` operational diagnostics.


* **Stage 4:** Adds `bundletool dump config --bundle="$AAB_PATH" | grep alignment` (`PAGE_ALIGNMENT_16K`), extracts native `.so` libraries from **all** AAB modules (`*/lib/*/*.so`, not just `base/lib/*`), removes macOS-incompatible `sort -V`, fixes `$BUNDLETOOL` execution, corrects the `zipalign` pass string to `Verification successful`, and checks all generated APKs.


* **Stage 5:** Resolves `AD_ID` based on actual SDK behavior, verifies HTTPS across source endpoints + merged `network_security_config` + release runtime traffic, enforces a full file-by-file trace of the face image pipeline before asserting on-device biometric privacy, and requires checking Play Console's highest uploaded `versionCode` across all tracks.


* **Stage 6:** Makes missing release signing credentials fail-fast (`GradleException`) on release bundle tasks, adds cryptographic `jarsigner -verify -strict -certs` verification, standardizes the output path to `release/production/mobile-release.aab`, accounts for all classical and post-quantum/hybrid Play App Signing SHA-256 certificates, and requires progressing App Check from Monitoring to Enforcement.


* **Stage 7:** Resolves the Git SHA circularity by separating **Commit A (`Release Source Commit SHA`)**—against which all tests, `lintRelease`, and `bundleRelease` are executed—from **Commit B (`Release Record Commit SHA`)**, which commits only the final stamped documentation.



Feed **one stage at a time** to your AI coder.

---

## Stage 1 Prompt — Engineering Stamp Closure (Docs + Evidence, No Code Risk)

```markdown
# AGENT MISSION: Execute Stage 1 — Engineering Stamp Closure (Docs + Fresh Test/Lint Evidence)

You are executing **Stage 1 (1.1, 1.2, and 1.3)** of `ProductionRoadMap.md`.

## STRICT STANDING RULES (VIOLATION = IMMEDIATE FAILURE)
1. **DO NOT MODIFY PRODUCTION CODE IN STAGE 1:** Stage 1 is strictly an audit-documentation closure and fresh test/lint verification pass. Do NOT downgrade `minSdk 34`, `targetSdk 37`, `DataStore 1.2.1`, or `zstd 1.5.7-21`.
2. **DO NOT OVERWRITE HISTORICAL TEST LOGS:** Keep the historical Phase 4 test run logs in `Phase4_Audit_1.md` intact as historical evidence; only correct the prose sum (`101` -> `102`) to match the existing Gate C2 table rows (`3 + 7 + 5 + 80 + 7 = 102`). Record the fresh Stage 1 test and lint execution results separately in `ProductionRoadMap.md` (or a dedicated Stage 1 section).
3. **WORK ONE MICRO-STEP AT A TIME:** Complete Micro-Step 1.1A, run its verification command, and paste the verbatim output before moving to Micro-Step 1.1B.
4. **NEVER INVENT GREP OR TEST OUTPUTS:** Every command output pasted must come from an actual terminal execution.

---

## MICRO-STEP 1.1: Close All 6 Findings in `Phase4_Audit_1.md`

Locate and read `Phase4_Audit_1.md` (`find . -name "Phase4_Audit_1.md"`). Execute these 6 targeted corrections in order:

### 1.1A. Reconcile Historical Prose Test Count (`101` -> `102`) Without Altering Historical Logs
1. Inspect the Gate C2 module test table in `Phase4_Audit_1.md`. Confirm the rows sum to `102`.
2. Update the prose text in `Phase4_Audit_1.md` where it claims `101` passed tests so it accurately states `102` (matching its own Gate C2 table). Do NOT replace the historical Gate C2 log block with a new run.
3. **Checkpoint 1.1A:**
   ```bash
   grep -n "101\|102" applications/kocolor/docs/ProductFixes/Directive/Phase4/Phase4_Audit_1.md
   ```

### 1.1B. Add the `compositeScore` Scale-Adjudication Note
1. Inspect `ColorModels.kt` and `WardrobeCandidateFilter.kt` to see how wardrobe `compositeScore` and cosmetic sub-scores are produced and normalized:
   ```bash
   grep -n -C 12 "totalScore\|compositeScore\|MAX_" \
     applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorModels.kt \
     applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt
   ```
2. In `Phase4_Audit_1.md` under Step 3 (`totalScore` Normalization), add a **Scale-Adjudication Note** with exact file:line producer evidence explaining the raw score range of wardrobe `compositeScore` and cosmetic sub-score sums, and why dividing by the theoretical maximum (`5.0f` / `MAX_*`) maps both pools monotonically into `[0.0, 1.0]` without score collapse.

### 1.1C. Paste the Missing `WardrobeCandidateFilter` Half of Gate B3
1. Run Gate B3 across **both** `ColorModels.kt` and `WardrobeCandidateFilter.kt`:
   ```bash
   grep -n -C 8 "isAnchor\|anchorPriority\|totalScore\|MAX_\|5\.0" \
     applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/color/ColorModels.kt \
     applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/WardrobeCandidateFilter.kt
   ```
2. Paste the verbatim output from **both** files into Gate B3 of `Phase4_Audit_1.md`, explicitly noting if a literal `5.0f` divisor was used instead of a named `MAX_*` constant.

### 1.1D. Evidence Step 1B's Third Bullet (`adaptContextToProvider` & `missingRoleRequirements`)
1. Run:
   ```bash
   grep -n -C 10 "missingRoleRequirements\|determineRoleRequirements\|slotsPerCategory" \
     applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt
   ```
2. Paste the verbatim `grep` output into `Phase4_Audit_1.md` under Step 1B, proving how `adaptContextToProvider` prioritizes unfilled roles from `RoleGapAnalyzer` while preserving the `slotsPerCategory` floor-of-1 step-down logic.

### 1.1E. Evidence `HeuristicStyleEngineTest` Expansion & `GreedyRehydratorTest` Regression Verification (Separate Commands)
1. Grep the `@Test` methods in both test files:
   ```bash
   grep -n -A 1 "@Test" \
     applications/kocolor/data/src/test/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngineTest.kt \
     applications/kocolor/features/analyzer/src/test/java/com/zoewave/probase/kocolor/features/analyzer/simulator/ui/GreedyRehydratorTest.kt
   ```
2. Run each test suite in its own separate Gradle invocation so the results are unambiguous:
   ```bash
   ./gradlew :applications:kocolor:data:testDebugUnitTest --tests "com.zoewave.probase.kocolor.data.usecase.HeuristicStyleEngineTest" --rerun-tasks
   ./gradlew :applications:kocolor:features:analyzer:testDebugUnitTest --tests "com.zoewave.probase.kocolor.features.analyzer.simulator.ui.GreedyRehydratorTest" --rerun-tasks
   ```
3. Paste both verbatim outputs into `Phase4_Audit_1.md` under Step 6.

### 1.1F. Add `RotationScoringUseCase.kt` Threshold Wiring to the File Modification Table
1. Verify `KoColorThresholds` usage in `RotationScoringUseCase.kt`:
   ```bash
   grep -n "KoColorThresholds" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RotationScoringUseCase.kt
   ```
2. Add the row for `RotationScoringUseCase.kt` (`:applications:kocolor:data` | `data/usecase/RotationScoringUseCase.kt` | **MODIFIED** | Wired centralized `KoColorThresholds` rotation/recency constants) to the File Modification Summary Table in `Phase4_Audit_1.md`.

---

## MICRO-STEP 1.2: Close Phase 3 Pass 2 Nits (`Phase3_Audit_1.md` & `Phase3_Audit_2.md`)

1. **Reword Banner in `Phase3_Audit_1.md`:**
    - Open `applications/kocolor/docs/ProductFixes/Directive/Phase3/Phase3_Audit_1.md`.
    - Replace the `"SUPERSEDED"` banner with:
      ```markdown
      > [!NOTE]
      > **BASE RECORD — READ WITH `Phase3_Audit_2.md`:** This document contains the base Phase 3 (Pass 1) file modification inventory and `KeywordMatcher` verification logs. Read together with [`Phase3_Audit_2.md`](./Phase3_Audit_2.md), which records the Pass 2 punchlist fixes and final verification gates.
      ```
2. **Paste Item-4 Existence Grep & Full Gate-3 Context Output into `Phase3_Audit_2.md`:**
   ```bash
   grep -n -C 3 "smartPick_topDoesNotMatchStopSignTee" applications/kocolor/data/src/test/java/com/zoewave/probase/kocolor/data/usecase/HeuristicStyleEngineTest.kt
   grep -n -C 4 "effectiveActiveDimensions" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/IntentFulfillmentEvaluator.kt
   grep -n -C 5 "goodEverydayOutfit_scoresBetween80And89AndPassesGate" applications/kocolor/data/src/test/java/com/zoewave/probase/kocolor/fashionista/scoring/FashionistaEngineTest.kt
   ```
   Paste all three verbatim outputs into `Phase3_Audit_2.md`.

---

## MICRO-STEP 1.3: Run Fresh Full Test Suite + `assembleDebug` + `lintRelease` (Record Separately)

1. Run the full unit test suite and debug assembly fresh:
   ```bash
   ./gradlew :features:ai:core:testDebugUnitTest \
     :features:ai:firebase:testDebugUnitTest \
     :features:ai:local:testDebugUnitTest \
     :applications:kocolor:data:testDebugUnitTest \
     :applications:kocolor:features:analyzer:testDebugUnitTest \
     :applications:kocolor:apps:mobile:assembleDebug --rerun-tasks
   ```
2. Run `lintRelease` fresh:
   ```bash
   ./gradlew :applications:kocolor:apps:mobile:lintRelease
   ```
3. Record these fresh Stage 1 outputs in a Stage 1 Evidence block in `ProductionRoadMap.md` (keeping them distinct from the historical Phase 4 audit run).

## STAGE 1 MANDATORY COMPLETION GATE
Check off `[x] 1.1`, `[x] 1.2`, and `[x] 1.3` in `ProductionRoadMap.md` ONLY after pasting:
1. Confirmation and diffs for all 6 items in `Phase4_Audit_1.md`.
2. Confirmation and diffs for `Phase3_Audit_1.md` and `Phase3_Audit_2.md`.
3. Verbatim console output and exact test pass counts for the fresh Stage 1 unit test run, `assembleDebug`, and `lintRelease` (`0 errors`).

```

---

## Stage 2 Prompt — Catalog Signing & Production Seed (Critical Path)

```markdown
# AGENT MISSION: Execute Stage 2 — Catalog Signing & Production Seed (Critical Path)

You are executing **Stage 2 (2.1 through 2.6)** of `ProductionRoadMap.md`.

## CRITICAL SECURITY INVARIANTS (READ FIRST)
1. **THREE DISTINCT TRUST BOUNDARIES:** The Ed25519 catalog signing key (`atelier-prod-01`) is **NOT** the Android upload key and **NOT** the Google Play app-signing key.
2. **NEVER ECHO OR COMMIT THE PRIVATE SEED:** The 64-character hex private seed (`KOCOLOR_SIGNING_SEED_HEX`) must NEVER be printed to stdout/logs, written to any file inside the repository, or committed to Git. Only the **64-hex-char Ed25519 PUBLIC key** and the **Key ID (`atelier-prod-01`)** may appear in `SecurityConstants.kt` and `manifest.json`.
3. **THE OLD `atelier-dev-01` SEED IS BURNED:** No production artifact, fallback, or manifest may use `atelier-dev-01`.
4. **MIRROR THE ACTUAL SIGNING PROTOCOL:** Before writing any verifier, inspect the actual production signer and Android client verifier to match the exact byte payload, canonicalization, hash target, and compression settings.
5. **NEVER PUBLISH TO CDN BEFORE 2.4 AND 2.5 PASS.**

---

## MICRO-STEP 2.1: Inspect Signing Protocol, Generate Production Ed25519 Seed & Verify Zero Secret Leaks

1. **Read the Actual Signer & Client Verifier First:**
   ```bash
   find . -name "SecurityConstants.kt" -o -name "Deployment_Pipeline_Manual.md" -o -name "*StarterPack*" -o -name "*Catalog*"
   ```
Read `SecurityConstants.kt`, `server/Inventory/docs/Guides/Deployment_Pipeline_Manual.md`, the server packaging/signing code (`server/Inventory/...`), and the Android client verification code (`StarterPackRepo` / package verifier).
Document:
- How `KOCOLOR_SIGNING_SEED_HEX` and `KOCOLOR_KEY_ID` are read (confirm both pipelines exit with a non-zero status if `KOCOLOR_SIGNING_SEED_HEX` is missing — zero fallback to dev or random keys).
- What exact bytes are signed and hashed for `manifest.json` and for each `.kpkg` file (compressed vs. uncompressed bytes, JSON canonicalization rules, and `zstd -19` without `--ultra`).
2. **Generate or Load the Production Seed Into Secret Manager / Restricted Un-Tracked File (`0600`):**
   ```bash
   mkdir -p ~/.kocolor_secrets && chmod 700 ~/.kocolor_secrets
   python3 -c '
   import os
   from pathlib import Path
   from nacl.signing import SigningKey

   env_path = Path.home() / ".kocolor_secrets" / "atelier_prod_01.env"
   if not env_path.exists():
       sk = SigningKey.generate()
       seed_hex = sk.encode().hex()
       pub_hex = sk.verify_key.encode().hex()
       lines = [
           "export KOCOLOR_KEY_ID=\"atelier-prod-01\"",
           "export KOCOLOR_SIGNING_SEED_HEX=\"" + seed_hex + "\"",
           "export KOCOLOR_PUBLIC_KEY_HEX=\"" + pub_hex + "\""
       ]
       env_path.write_text("\n".join(lines) + "\n")
       os.chmod(env_path, 0o600)
   else:
       os.chmod(env_path, 0o600)
       pub_hex = ""
       for line in env_path.read_text().splitlines():
           if "KOCOLOR_PUBLIC_KEY_HEX=" in line:
               pub_hex = line.split("=", 1)[1].strip("\"")
   st_mode = oct(env_path.stat().st_mode & 0o777)
   print("FILE_PERMISSIONS:", st_mode)
   print("KEY_ID: atelier-prod-01")
   print("PUBLIC_KEY_HEX:", pub_hex)
   '
   ```
   *(Store the private seed immediately in your Secret Manager; once Stage 2 signing is complete, remove the temporary plaintext file with `rm -P ~/.kocolor_secrets/atelier_prod_01.env` or `shred -u`).*
3. **Verify the Actual Secret Value (NOT the Variable Name) Is Absent from Git Working Tree & History Without Printing the Secret:**
   ```bash
   python3 -c '
   import subprocess, sys
   from pathlib import Path

   env_path = Path.home() / ".kocolor_secrets" / "atelier_prod_01.env"
   seed_hex = ""
   for line in env_path.read_text().splitlines():
       if "KOCOLOR_SIGNING_SEED_HEX=" in line:
           seed_hex = line.split("=", 1)[1].strip("\"")
   assert len(seed_hex) == 64, "Invalid seed length"

   # 1. Check working tree & tracked files for the actual 64-char secret value:
   res_tree = subprocess.run(["git", "grep", "-l", "-F", seed_hex], capture_output=True, text=True)
   if res_tree.stdout.strip():
       print("FATAL: Secret value found in tracked files:", res_tree.stdout)
       sys.exit(1)

   # 2. Check git commit history for the actual 64-char secret value:
   res_hist = subprocess.run(["git", "log", "-S", seed_hex, "--oneline"], capture_output=True, text=True)
   if res_hist.stdout.strip():
       print("FATAL: Secret value found in git history:", res_hist.stdout)
       sys.exit(1)

   print("SECRET LEAK CHECK PASSED: 64-char private seed value is 100% absent from working tree and git history.")
   '
   ```

> **CHECKPOINT 2.1:** Paste the verbatim output showing `FILE_PERMISSIONS: 0o600`, `KEY_ID: atelier-prod-01`, `PUBLIC_KEY_HEX`, and `SECRET LEAK CHECK PASSED`.

---

## MICRO-STEP 2.2: Set `KOCOLOR_KEY_ID` (`atelier-prod-01`) & Update `SecurityConstants.kt`

1. Read `SecurityConstants.kt`.
2. Update the trusted Ed25519 key ID constant to `"atelier-prod-01"` and update the trusted Ed25519 public key constant to the exact `PUBLIC_KEY_HEX` from Micro-Step 2.1.
3. **Checkpoint 2.2:** Run:
   ```bash
   grep -rn "atelier-dev-01" applications/ core/ features/ server/
   ```
   Confirm zero active production code paths or compiler scripts reference `atelier-dev-01`.

---

## MICRO-STEP 2.3: Sign Full Catalog in Staging (Verify `zstd -19` Config) & Verify Locally

1. Inspect the server packaging script/code to confirm `zstd` compression is configured to **level `19` without `--ultra`** (keeping mobile native decompression window under 64KB RAM) and paste the exact code line(s) from the packager as evidence.
2. Load `~/.kocolor_secrets/atelier_prod_01.env` and run the catalog signing pipeline into staging (`server/Inventory/package/dist`), capturing the build output log.
3. Run a portable Python verification script (compatible with Python 3.8–3.13+, zero backslashes inside f-strings) that mirrors the exact client trust protocol:
   ```bash
   source ~/.kocolor_secrets/atelier_prod_01.env
   python3 -c '
   import hashlib, json, os
   from pathlib import Path
   import zstandard as zstd
   from nacl.signing import VerifyKey

   dist = Path("server/Inventory/package/dist")
   pub_hex = os.environ["KOCOLOR_PUBLIC_KEY_HEX"]
   expected_key_id = os.environ["KOCOLOR_KEY_ID"]
   vk = VerifyKey(bytes.fromhex(pub_hex))

   manifest = json.loads((dist / "manifest.json").read_text(encoding="utf-8"))
   data = manifest["data"]
   actual_key_id = data["key_id"]
   assert actual_key_id == expected_key_id, f"Key ID mismatch: {actual_key_id} != {expected_key_id}"

   # Mirror exact canonical serialization used by the signer/client:
   canonical_data = json.dumps(data, sort_keys=True, separators=(",", ":"), ensure_ascii=False).encode("utf-8")
   vk.verify(canonical_data, bytes.fromhex(manifest["signature"]))
   print(f"MANIFEST ENVELOPE SIGNATURE: VALID (key_id={actual_key_id})")

   dctx = zstd.ZstdDecompressor()
   for pack in data["packs"]:
       pack_id = pack["id"]
       endpoint = pack["endpoint"]
       kpkg_path = dist / endpoint
       raw = kpkg_path.read_bytes()
       assert len(raw) == pack["compressed_size_bytes"], f"Compressed size mismatch for {pack_id}"
       actual_sha = hashlib.sha256(raw).hexdigest()
       assert actual_sha == pack["sha256"], f"SHA256 mismatch for {pack_id}"
       vk.verify(raw, bytes.fromhex(pack["signature"]))
       decompressed = dctx.decompress(raw)
       assert len(decompressed) == pack["uncompressed_size_bytes"], f"Uncompressed size mismatch for {pack_id}"
       items = json.loads(decompressed.decode("utf-8"))
       assert len(items) == pack["item_count"] and len(items) > 0, f"Item count mismatch for {pack_id}"
       print(f"PACK VERIFIED: {pack_id} ({len(items)} items, {len(raw)} compressed bytes, sha256={actual_sha[:12]}...)")
   '
   ```
   *(Note: If the production signer signs a different canonical payload structure in `StarterPackRepo`, adjust the canonical bytes extraction above to match `StarterPackRepo` 1:1 and document the exact protocol).*

> **CHECKPOINT 2.3:** Paste the packager's `zstd -19` config line, the build output, and the verbatim `MANIFEST ENVELOPE SIGNATURE: VALID` + `PACK VERIFIED` output.

---

## MICRO-STEP 2.4: Key-Transition Compatibility Tests (7 Automated Tests + 1 Operational Channel Audit)

### Part A — 7 Automated Key-Transition Tests (`CatalogKeyTransitionTest.kt` + Compiler Check)
Create or expand `CatalogKeyTransitionTest.kt` in the module containing `SecurityConstants` / `StarterPackRepo` and run the compiler missing-seed check to prove all **7 automated tests**:
1. **Test 1 — Fresh install trusting production key (`atelier-prod-01`):** Client accepts a valid manifest and `.kpkg` signed with `atelier-prod-01`.
2. **Test 2 — Upgrade / cached dev catalog replacement:** Client with cached `atelier-dev-01` data cleanly replaces/upgrades when ingesting the new `atelier-prod-01` catalog without schema or signature state corruption.
3. **Test 3 — Burned `atelier-dev-01` key rejection:** Client rejects a manifest or `.kpkg` signed with the old `atelier-dev-01` key.
4. **Test 4 — Invalid-signature rejection:** Flipping 1 bit in a valid `atelier-prod-01` signature fails verification before `zstd` decompression is ever attempted.
5. **Test 5 — Wrong-key-ID rejection:** A manifest or package claiming `key_id = "atelier-wrong-99"` is rejected immediately.
6. **Test 6 — Manifest references only verified production packages:** Every pack entry in staged `manifest.json` resolves to a verified `atelier-prod-01` `.kpkg` file with zero dev/test packs.
7. **Test 7 — Missing-seed rejection in both pipelines:** Run the signing pipeline with `env -u KOCOLOR_SIGNING_SEED_HEX` and assert it exits with a non-zero exit code without producing unsigned artifacts.

### Part B — Check 8: Operational Existing-Client Coexistence Audit Across All Channels
Check all distribution channels (Play Console Production, Open Testing, Closed Testing, Internal Testing, Firebase App Distribution, and local tester devices):
- Document whether any testers on Internal/Closed tracks currently have an older build installed with `atelier-dev-01`.
- If only internal testers exist, document the transition procedure (testers update to the new Stage 6 Internal Track build trusting `atelier-prod-01` or clear app data) before switching the live CDN manifest, or verify how the CDN path/versioning handles coexistence.

> **CHECKPOINT 2.4:** Paste the verbatim Gradle test output for `CatalogKeyTransitionTest`, the `env -u KOCOLOR_SIGNING_SEED_HEX` exit-code proof, and the Check 8 distribution channel audit.

---

## MICRO-STEP 2.5 & 2.6: End-to-End Local Client Validation & Atomic CDN Publication

1. **Micro-Step 2.5:** Run the Android client verification + `zstd` decompression test against the staged `atelier-prod-01` `.kpkg` files and `manifest.json` to confirm end-to-end compatibility before publishing.
2. **Micro-Step 2.6:**
    - Follow `server/Inventory/docs/Guides/Deployment_Pipeline_Manual.md` to upload all `.kpkg` files to the CDN **BEFORE** uploading `manifest.json`.
    - From an external client (`curl -sS`), download the live CDN `manifest.json` and every referenced `.kpkg` URL into `/tmp/cdn_live_verify/` and run the Python Ed25519 + SHA-256 + `zstd` verification script from Micro-Step 2.3 against the live downloaded files.
    - Securely remove any temporary local plaintext seed file once stored in Secret Manager (`rm -P ~/.kocolor_secrets/atelier_prod_01.env` if no longer needed locally).

## STAGE 2 MANDATORY COMPLETION GATE
Check off `[x] 2.1` through `[x] 2.6` in `ProductionRoadMap.md` ONLY after pasting:
1. Secret-absence verification output + `SecurityConstants.kt` diff (`atelier-prod-01`).
2. `zstd -19` config proof + staging verification output + all 7 automated `CatalogKeyTransitionTest` results + Check 8 channel audit.
3. Live external CDN verification output proving all live `.kpkg` packs and `manifest.json` verify under `atelier-prod-01`.

```

---

## Stage 3 Prompt — Release Privacy: Logging Gates

```markdown
# AGENT MISSION: Execute Stage 3 — Release Privacy: Logging Gates

You are executing **Stage 3 (3.1, 3.2, 3.3, and 3.4)** of `ProductionRoadMap.md`.

## STRICT PRIVACY & SECURITY INVARIANTS
1. **ZERO HTTP LOGGING IN RELEASE ACROSS THE ENTIRE REPO:** Every `HttpLoggingInterceptor` across the entire repository MUST evaluate to `HttpLoggingInterceptor.Level.NONE` in release builds. `Level.HEADERS` is **strictly forbidden** in release because `OpenFetchWeatherData.kt` passes the OpenWeatherMap API key (`appid=...`) in the request URL.
2. **USE EACH MODULE'S OWN `BuildConfig.DEBUG`:** Every module must branch on its compiled `BuildConfig.DEBUG` boolean so release builds resolve to `Level.NONE`.
3. **ZERO PROMPT OR WARDROBE LOGGING IN RELEASE, KEEP OPERATIONAL `Log.w`/`Log.e`:** Gate raw Gemini prompts, responses, and manifests on `BuildConfig.DEBUG` and strip `Log.v`/`Log.d`/`Log.i` via R8, while retaining `Log.w` and `Log.e` for non-sensitive operational diagnostics.

---

## MICRO-STEP 3.1: Gate All HTTP Logging Interceptors Across the Entire Repository to `Level.NONE` in Release

1. Search the **entire repository** (all `.kt` and `.java` files across all directories) for `HttpLoggingInterceptor` and `Level.`:
   ```bash
   git grep -n "HttpLoggingInterceptor\|Level\.BODY\|Level\.HEADERS\|Level\.BASIC" -- "*.kt" "*.java"
   ```
2. Inspect all 6 required clients (`OpenFetchWeatherData.kt:82`, `ProductsDataModule.kt`, `MakeupModule.kt`, `ChemicalModule.kt`, `ObfModule.kt`, `ColorModule.kt`) plus any other module returned by `git grep`:
    - Ensure every single site uses:
      ```kotlin
      level = if (BuildConfig.DEBUG) {
          HttpLoggingInterceptor.Level.BODY
      } else {
          HttpLoggingInterceptor.Level.NONE
      }
      ```
    - Confirm zero occurrences of `else HttpLoggingInterceptor.Level.HEADERS` or `Level.BASIC` exist anywhere in the repo.

> **CHECKPOINT 3.1:** Run:
> ```bash
> git grep -n -C 3 "Level\.BODY\|Level\.HEADERS\|Level\.BASIC" -- "*.kt" "*.java"
> ```
> Paste the verbatim output and confirm every match is strictly inside an `if (...BuildConfig.DEBUG)` branch with `else ... Level.NONE`.

---

## MICRO-STEP 3.2: Gate AI I/O Logging in `FirebaseAiClient.kt` & All AI Providers

1. Read `features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/FirebaseAiClient.kt` (around line 286 and throughout the file).
2. Wrap all `KoColorAI_IO` / `REQUEST TO GEMINI` / `RESPONSE FROM GEMINI` / `TOKEN USAGE` debug logs inside `if (BuildConfig.DEBUG) { ... }`.
3. Search the entire repository for any other `Log.` statements that log prompts, responses, API keys, or candidate manifests:
   ```bash
   git grep -n "KoColorAI_IO\|REQUEST TO GEMINI\|RESPONSE FROM GEMINI\|WARDROBE:" -- "*.kt" "*.java"
   ```
   Ensure all such diagnostic logs are gated by `if (BuildConfig.DEBUG)` and that `Log.w` / `Log.e` statements never interpolate raw prompts, wardrobe manifests, or API keys.

> **CHECKPOINT 3.2:** Run:
> ```bash
> git grep -n -C 2 "KoColorAI_IO\|REQUEST TO GEMINI\|RESPONSE FROM GEMINI" -- "*.kt" "*.java"
> ```
> Paste the verbatim output confirming `if (BuildConfig.DEBUG)` guards on every match.

---

## MICRO-STEP 3.3: Confirm Release Minification & R8 `assumenosideeffects` Rules

1. Read `applications/kocolor/apps/mobile/build.gradle.kts` and `applications/kocolor/apps/mobile/proguard-rules.pro`.
2. Confirm `buildTypes.release` sets `isMinifyEnabled = true` and includes `"proguard-rules.pro"`.
3. In `applications/kocolor/apps/mobile/proguard-rules.pro`, confirm `-assumenosideeffects` removes `Log.v`, `Log.d`, and `Log.i` while **retaining** `Log.w` and `Log.e`:
   ```proguard
   -assumenosideeffects class android.util.Log {
       public static boolean isLoggable(java.lang.String, int);
       public static int v(...);
       public static int d(...);
       public static int i(...);
   }
   ```

> **CHECKPOINT 3.3:** Paste the `buildTypes.release` block from `build.gradle.kts` and the `-assumenosideeffects` block from `proguard-rules.pro`.

---

## MICRO-STEP 3.4: Whole-Repo Static Grep & Release Runtime Logcat Verification Across All 4 Flows

1. Run the mandatory repository-wide grep check:
   ```bash
   git grep -n "Level\.BODY\|Level\.HEADERS" -- "*.kt" "*.java"
   ```
2. Build and install a release build on a connected device/emulator, clear logcat (`adb logcat -c`), and exercise all **4 required runtime flows**:
    - **Flow 1:** Weather fetch (OpenWeatherMap API request).
    - **Flow 2:** Cosmetic / Wardrobe starter catalog fetch.
    - **Flow 3:** AI Analyzer / Suggestion request.
    - **Flow 4:** Full Style Simulation (`StyleSimulatorEngine`).
3. Dump Logcat (`adb logcat -d > /tmp/release_logcat_audit.txt`) and run:
   ```bash
   grep -E -i "appid=|api.openweathermap.org|REQUEST TO GEMINI|RESPONSE FROM GEMINI|KoColorAI_IO|AVAILABLE CANDIDATES|selectedClothingIds" /tmp/release_logcat_audit.txt || echo "PASS: ZERO SENSITIVE URLS, KEYS, PROMPTS, OR WARDROBE CONTENT IN RELEASE LOGCAT"
   ```

## STAGE 3 MANDATORY COMPLETION GATE
Check off `[x] 3.1`, `[x] 3.2`, `[x] 3.3`, and `[x] 3.4` in `ProductionRoadMap.md` ONLY after pasting:
1. Verbatim output of `git grep -n -C 2 "Level\.BODY\|Level\.HEADERS" -- "*.kt" "*.java"`.
2. Verbatim output of `git grep -n -C 2 "KoColorAI_IO" -- "*.kt" "*.java"`.
3. Verbatim `proguard-rules.pro` `assumenosideeffects` block and the release Logcat audit output across all 4 flows.

```

---

## Stage 4 Prompt — 16KB Page-Size Compatibility (Mandatory Production Readiness Gate)

```markdown
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

1. Resolve `bundletool` and `zipalign` portably on macOS/Linux:
   ```bash
   SDK_ROOT="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
   ZIPALIGN=$(find "$SDK_ROOT/build-tools" -name "zipalign" 2>/dev/null | sort | tail -n 1)
   echo "Using zipalign: $ZIPALIGN"

   run_bundletool() {
       if command -v bundletool >/dev/null 2>&1; then
           bundletool "$@"
       else
           BT_JAR=$(find "$HOME" -name "bundletool*.jar" 2>/dev/null | head -n 1)
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
       if unzip -l "$apk" \vert{} grep -q "\.so$"; then
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

```

---

## Stage 5 Prompt — Listing vs. Manifest + Store Assets & Policy Gates

```markdown
# AGENT MISSION: Execute Stage 5 — Listing vs. Manifest, Store Assets, Version Bump & Health Connect / Data Safety Reconciliation

You are executing **Stage 5 (5.1 through 5.8)** of `ProductionRoadMap.md`.

## STRICT EXECUTION RULES
1. **ALWAYS INSPECT THE MERGED RELEASE MANIFEST AND BUILT AAB:** Never rely solely on source `AndroidManifest.xml`.
2. **RESOLVE PERMISSIONS BY ACTUAL SDK & PRODUCT BEHAVIOR:** Do not lazily edit documentation to hide an unintended transitive permission, and do not make privacy claims without tracing every code path.

---

## MICRO-STEP 5.1: Resolve `AD_ID` Based on Actual SDK Usage + Merged Manifest + AAB

1. Check whether KoColor's codebase actually accesses Google Advertising ID:
   ```bash
   git grep -n "AdvertisingIdClient\|AD_ID" -- "*.kt" "*.java" "*.xml"
   ```
2. Inspect the merged release manifest and built AAB:
   ```bash
   ./gradlew :applications:kocolor:apps:mobile:processReleaseMainManifest
   MERGED_MANIFEST=$(find applications/kocolor/apps/mobile/build/intermediates/merged_manifest/release -name "AndroidManifest.xml" | head -n 1)
   grep -n "AD_ID" "$MERGED_MANIFEST" || echo "ZERO AD_ID IN MERGED MANIFEST"
   ```
3. **Resolution Rule:**
    - Since KoColor does NOT display ads or use `AdvertisingIdClient`, `com.google.android.gms.permission.AD_ID` MUST NOT be present in the release artifact.
    - If any transitive dependency merges `AD_ID` into `$MERGED_MANIFEST`, add `<uses-permission android:name="com.google.android.gms.permission.AD_ID" tools:node="remove" />` to `applications/kocolor/apps/mobile/src/main/AndroidManifest.xml`, rebuild the release manifest + AAB, and verify that both `$MERGED_MANIFEST` and `bundletool dump manifest --bundle="$AAB_PATH"` report **0 occurrences of `AD_ID`**.
    - Verify `GooglePlayStoreListing.md:59` and the Play Console Advertising ID declaration ("Does your app use advertising ID? No") agree 100%.

> **CHECKPOINT 5.1:** Paste the `git grep`, merged manifest check, and `bundletool dump manifest` check proving zero `AD_ID` in the final AAB.

---

## MICRO-STEP 5.2: Verify 100% HTTPS Across Source Endpoints, Merged Config & Runtime Traffic

1. **Layer 1 — Source Endpoint Audit:**
   ```bash
   git grep -n "makeup-api\|cleartextTrafficPermitted\|networkSecurityConfig\|http://" applications/ core/ features/ -- "*.kt" "*.java" "*.xml" | grep -v "schemas.android.com"
   ```
    - Confirm `MakeupModule.kt` and all other Retrofit/OkHttp clients use `https://` URLs exclusively.
2. **Layer 2 — Network Security Config Audit:**
    - Read `network_security_config.xml` (and check the merged release resources).
    - Remove the unnecessary `makeup-api.herokuapp.com` `cleartextTrafficPermitted="true"` exception so cleartext HTTP is disabled across all domains in release builds.
3. **Layer 3 — Runtime Verification:**
    - Exercise the Makeup API / catalog / weather / AI network flows in the release build and confirm zero plaintext `http://` requests are attempted or blocked in Logcat.

> **CHECKPOINT 5.2:** Paste the updated `network_security_config.xml`, `MakeupModule.kt` base URL, and the `git grep` output confirming zero non-schema `http://` URLs.

---

## MICRO-STEP 5.3 & 5.4: Fix Waterfall-Chart Listing Copy & Verify Screenshots

1. **5.3 Waterfall-Chart Bullet (`GooglePlayStoreListing.md:36`):**
    - Read `GooglePlayStoreListing.md` around line 36 (and any sibling store listing files).
    - Replace `"interaction synergies / unresolved conflict penalties"` (which described the deleted 6-feature engine) with accurate copy describing the live 3-axis `FashionistaEngine` (`40% CIELAB Color Harmony, 35% Silhouette Proportions, and 25% Contrast & Depth alignment`).
    - Run `git grep -n "interaction synergies\|6-axis\|6-Axis"` across the repo to confirm zero stale 6-axis references remain in active listing files.
2. **5.4 Screenshots (`PlayStore/Screen/`):**
    - Check `PlayStore/Screen/` (`find . -path "*PlayStore/Screen*"` and list all files with dimensions/file sizes). Confirm required Play Store screenshots are populated.

> **CHECKPOINT 5.3–5.4:** Paste lines 30–45 of `GooglePlayStoreListing.md` and the `ls -lh` output of `PlayStore/Screen/`.

---

## MICRO-STEP 5.5 & 5.6: Check Play Console Highest `versionCode`, Bump Version & Clean `MessagingStep.kt`

1. **5.5 Version Bump (`applications/kocolor/apps/mobile/build.gradle.kts`):**
    - **CRITICAL:** Check the highest previously uploaded `versionCode` across **ALL tracks** (Internal, Closed, Open, Production) in Google Play Console (and check `release/` archives).
    - Set `versionCode` in `applications/kocolor/apps/mobile/build.gradle.kts` to an integer **strictly greater than the highest version code ever uploaded to Play Console** (and `> 7`), and update `versionName` to the target release version string.
2. **5.6 Tree Hygiene (`MessagingStep.kt`):**
    - Run `git status` and `git diff` on `MessagingStep.kt`. Commit or revert the uncommitted `MessagingStep.kt` navigation rework so the working tree contains no half-finished uncommitted experiments.

> **CHECKPOINT 5.5–5.6:** Paste the `versionCode` and `versionName` lines from `build.gradle.kts` and `git status`.

---

## MICRO-STEP 5.7: Health Connect Eligibility Decision Gate (Source + Merged Manifest + AAB)

1. Inspect all Health Connect references across code, merged release manifest, and built AAB:
   ```bash
   git grep -n "HealthConnect\|android\.permission\.health" applications/ core/ features/
   grep -n -i "health" "$MERGED_MANIFEST" || echo "ZERO HEALTH PERMISSIONS IN MERGED MANIFEST"
   ```
2. **Apply the Policy Decision Before Declaring in Play Console:**
    - Google Play policy restricts `android.permission.health.*` to approved primary health/fitness use cases with direct user-facing health benefits.
    - **If Option A (No qualifying Health Connect feature in this release):** Remove all `android.permission.health.*` permissions, `<queries>`, activity-aliases, and runtime permission launch calls from the release build (using `tools:node="remove"` if merged transitively), rebuild the AAB, and verify via `bundletool dump manifest --bundle="$AAB_PATH" | grep -i health` that **zero** health permissions exist in the final AAB. Select the "No health features" declaration path in Play Console.
    - **If Option B (Retaining Health Connect as a qualified health/wellness feature):** Document every single declared `android.permission.health.*` permission in the final AAB, its exact user-facing health benefit screen, in-app consent/disclosure flow, privacy policy clauses (access, processing, sharing, retention, deletion, encryption), and complete both the Health Apps declaration and Data-Type Permission declaration (with demo video) in Play Console.

> **CHECKPOINT 5.7:** Paste the merged manifest + `bundletool dump manifest` output for `health` and the recorded Option A or Option B evidence.

---

## MICRO-STEP 5.8: End-to-End Image Pipeline Trace & Data Safety Reconciliation

1. **Mandatory Face/Image Pipeline Trace (Do NOT Assume Local-Only Without Proof):**
    - Inspect every class that touches user bitmaps or `AiInput.Multimodal`:
      ```bash
      git grep -n "Bitmap\|Multimodal\|analyzeStyle" applications/ features/ core/ -- "*.kt"
      ```
    - Read `AnalyzerEngine.kt`, `FirebaseAiProvider.kt`, `FirebaseAiClient.kt`, `ByokAiProvider.kt`, `GemmaRestFallbackProvider.kt`, and `LocalNanoAiProvider.kt`.
    - Record exact `File:Line` evidence proving:
        1. What `AnalyzerEngine` sends to `CapabilityRouter` / `AiProvider` (`AiInput.TextOnly` with on-device `ItaCalculator` / `ColorQuantizer` numeric telemetry vs. `AiInput.Multimodal`).
        2. How `FirebaseAiProvider`, `ByokAiProvider`, and `GemmaRestFallbackProvider` handle `AiInput.Multimodal` (confirming all three cloud providers reject `AiInput.Multimodal` with `UnsupportedOperationException` or `Result.failure`, so raw face photographs can **never** be transmitted to any cloud endpoint).
2. **Complete the 7-Point Data Safety Reconciliation Table:**
    - Cross-check and record the exact verified answers for: (1) Data collected vs. shared, (2) On-device biometric image processing (backed by the trace above), (3) Cloud AI text prompt/response handling, (4) Third-party processors, (5) Health data status (matching 5.7), (6) Encryption in transit (backed by 5.2), and (7) User data retention/deletion controls.

## STAGE 5 MANDATORY COMPLETION GATE
Check off `[x] 5.1` through `[x] 5.8` in `ProductionRoadMap.md` ONLY after pasting:
1. Merged manifest + AAB `bundletool dump manifest` outputs for `AD_ID` and `health`.
2. `network_security_config.xml` + `GooglePlayStoreListing.md` updates + `PlayStore/Screen/` file list + `versionCode`/`versionName`.
3. The complete `File:Line` image-pipeline privacy trace and the 7-row Data Safety reconciliation table.

```

---

## Stage 6 Prompt — Signed AAB & Staged Rollout

```markdown
# AGENT MISSION: Execute Stage 6 — Fail-Fast Release Signing, Cryptographic AAB Verification & Play Integrity / App Check Enforcement

You are executing **Stage 6 (6.1, 6.2, 6.3, and 6.4)** of `ProductionRoadMap.md`.

## STRICT SIGNING & ARTIFACT INVARIANTS
1. **FAIL-FAST ON MISSING RELEASE CREDENTIALS:** Running a production release build (`bundleRelease` / `assembleRelease`) when `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, or `RELEASE_KEY_PASSWORD` is missing MUST fail immediately with a `GradleException`. Never silently produce an unsigned or debug-signed production AAB.
2. **THREE SEPARATE KEYS:** Keep the Ed25519 catalog key (`atelier-prod-01`), the Android Upload Key (used by Gradle to sign `mobile-release.aab`), and the Google Play App-Signing Key(s) (used by Google Play to sign APKs delivered to users, and registered in Firebase App Check) strictly distinct.
3. **NEVER DELETE THE OCT 18 ARTIFACTS & NEVER COMMIT BINARIES/KEYSTORES TO GIT.**

---

## MICRO-STEP 6.1: Add Fail-Fast `signingConfigs` to `applications/kocolor/apps/mobile/build.gradle.kts`

1. Read `applications/kocolor/apps/mobile/build.gradle.kts` and inspect the `AshBike` / `PhotoDo` `build.gradle.kts` files for the monorepo's `RELEASE_STORE_FILE` / `RELEASE_KEY_ALIAS` pattern.
2. Configure `signingConfigs.create("release")` and wire it into `buildTypes.release` in `applications/kocolor/apps/mobile/build.gradle.kts`:
   - Read `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD` from environment variables (or untracked `local.properties` if used by AshBike/PhotoDo).
   - Add a fail-fast check when a release bundle/apk task (`bundleRelease` or `assembleRelease`) is requested in the task graph: if any of the 4 credentials is missing or the keystore file does not exist, throw `GradleException("FATAL: Missing required release signing credentials (RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD) for production release build.")`.
   - Ensure `lintRelease` and `*UnitTest` tasks can still run without requiring the production keystore if needed, OR ensure the release credentials are loaded in the build environment.
   - Confirm `isMinifyEnabled = true`, `proguardFiles(...)`, and `ndk { debugSymbolLevel = "SYMBOL_TABLE" }` remain inside `buildTypes.release`.

> **CHECKPOINT 6.1:**
> 1. Paste the `signingConfigs` and `buildTypes.release` blocks from `applications/kocolor/apps/mobile/build.gradle.kts`.
> 2. Test the fail-fast guard by running `env -u RELEASE_STORE_FILE ./gradlew :applications:kocolor:apps:mobile:bundleRelease` (assuming credentials are not hardcoded in `local.properties`) and confirm it fails immediately with your `GradleException`.

---

## MICRO-STEP 6.2: Archive Oct 18 Artifacts, Build `release/production/mobile-release.aab` & Verify via `jarsigner` + `keytool`

1. Confirm `release/` is listed in `.gitignore`.
2. Move (do NOT delete) the stale Oct 18 `.aab` (~81 MB) and `mapping.txt` (~167 MB) into `release/archive/0.5-code7/` and record their hashes:
   ```bash
   mkdir -p release/archive/0.5-code7 release/production
   shasum -a 256 release/archive/0.5-code7/* > release/archive/0.5-code7/sha256.txt
   cat release/archive/0.5-code7/sha256.txt
   ```
3. Build the fresh signed production AAB:
   ```bash
   ./gradlew :applications:kocolor:apps:mobile:bundleRelease
   ```
4. Copy the generated `.aab` to the exact canonical path `release/production/mobile-release.aab`, copy R8 `mapping.txt` to `release/production/mapping.txt`, and copy the native debug symbols zip into `release/production/`:
   ```bash
   BUILT_AAB=$(find applications/kocolor/apps/mobile/build/outputs/bundle/release -name "*.aab" | head -n 1)
   cp "$BUILT_AAB" release/production/mobile-release.aab
   cp applications/kocolor/apps/mobile/build/outputs/mapping/release/mapping.txt release/production/mapping.txt
   shasum -a 256 release/production/mobile-release.aab release/production/mapping.txt > release/production/sha256.txt
   cat release/production/sha256.txt
   ```
5. **Cryptographic Signature & Certificate Verification (`jarsigner` + `keytool` + Native Symbols):**
   ```bash
   # 1. Cryptographically verify the AAB signature with jarsigner:
   jarsigner -verify -strict -certs release/production/mobile-release.aab | tail -n 25

   # 2. Extract the Upload Certificate SHA-256 fingerprint with keytool:
   keytool -printcert -jarfile release/production/mobile-release.aab | grep -E "Owner:|Issuer:|SHA256:"

   # 3. Confirm native debug symbols are embedded in BUNDLE-METADATA:
   unzip -l release/production/mobile-release.aab | grep "BUNDLE-METADATA/com.android.tools.build.debugsymbols"
   ```
    - **Pass Criterion:** `jarsigner -verify -strict -certs` MUST output **`jar verified.`**, `keytool` must print the Upload Key `SHA256:` fingerprint, and `BUNDLE-METADATA/com.android.tools.build.debugsymbols` must contain the `.so.sym` / `.so.dbg` entries.

> **CHECKPOINT 6.2:** Paste `release/archive/0.5-code7/sha256.txt`, `release/production/sha256.txt`, `jarsigner` (`jar verified.`), `keytool` Upload Certificate `SHA256:`, and the `BUNDLE-METADATA` native symbols list.

---

## MICRO-STEP 6.3 & 6.4: Play Integrity $\rightarrow$ App Check Registration (Classical + Hybrid Certs), Internal Track Install & Enforcement

1. Verify in code that `AppCheckInitializer` installs `PlayIntegrityAppCheckProviderFactory` in release builds (`!BuildConfig.DEBUG`):
   ```bash
   git grep -n -C 8 "PlayIntegrityAppCheckProviderFactory\|DebugAppCheckProviderFactory"
   ```
2. **Execute & Record Play Console $\leftrightarrow$ Firebase App Check Registration (6.3):**
    - In **Google Play Console > Test and release > Setup > App integrity**:
        1. Confirm **Play Integrity API** is linked to Google Cloud project `20197827893` (`buildwithai-453716`).
        2. Under **App signing**, copy **every applicable SHA-256 certificate fingerprint** listed under Play App Signing (including both classical RSA/ECDSA and post-quantum/hybrid signing certificates if quantum-ready hybrid signing is enabled for the app) — NOT the upload key certificate.
    - In **Firebase Console (`buildwithai-453716`)**:
        1. Under **Project settings > General > Your apps (`com.zoewave.probase.kocolor`)**, register all applicable **Play App-Signing SHA-256 certificate fingerprints**.
        2. Under **App Check > Apps**, confirm `com.zoewave.probase.kocolor` is registered with the **Play Integrity** provider.
3. **Execute Internal-Track Play Store Installation & App Check Enforcement Verification (6.4):**
    - Upload `release/production/mobile-release.aab` to the **Internal testing** track in Google Play Console. (If Play Console throws `Translation failed for these languages: de`, verify listing translation state and retry uploading the **identical** `release/production/mobile-release.aab`).
    - Install KoColor on a physical test device **directly from the Google Play Store Internal Testing opt-in page** (never via sideloaded `adb install`).
    - **Phase A (Monitoring Verification):** Launch the Play-installed app, verify CDN catalog download (`atelier-prod-01` Ed25519 verify + `zstd` decompress), run the AI Style Simulator via `FirebaseAiProvider`, and confirm in the Firebase App Check metrics dashboard that verified Play Integrity requests are recorded with zero invalid/unverified token errors.
    - **Phase B (Enforcement Verification):** Enable **App Check Enforcement** for the target Firebase backend service(s) in Firebase Console, wait for propagation, and execute another Style Simulator AI request from the Play-installed app to prove requests succeed under active **Enforcement** (not merely Monitoring).

## STAGE 6 MANDATORY COMPLETION GATE
Check off `[x] 6.1` through `[x] 6.4` in `ProductionRoadMap.md` ONLY after pasting:
1. `build.gradle.kts` fail-fast signing diff + test proof.
2. `release/archive/0.5-code7/sha256.txt` + `release/production/sha256.txt` + `jarsigner` (`jar verified.`) + Upload Key `SHA256` + `BUNDLE-METADATA` symbols output.
3. All registered Play App-Signing SHA-256 fingerprint(s), Internal Track Play Store installation verification, and App Check Monitoring $\rightarrow$ Enforcement verification results.

```

---

## Stage 7 Prompt — Go / No-Go & Two-Commit Final Release Record

```markdown
# AGENT MISSION: Execute Stage 7 — Final Go / No-Go Verification & Two-Commit Release Record Stamp

You are executing **Stage 7 (Go / No-Go)** of `ProductionRoadMap.md`.

## CRITICAL TWO-COMMIT IDENTITY PROTOCOL (NO SHA CIRCULARITY)
Stages 3, 5, and 6 modified source code, manifests, and Gradle build files after Stage 1. Furthermore, editing `ProductionRoadMap.md` to record the final build SHA and then committing it changes `HEAD`.
To maintain strict cryptographic attribution between the source code and the production `.aab`, you MUST use **Two Commit Identities**:
1. **Commit A (`Release Source Commit SHA`):** The clean Git commit containing all final application source code, manifests, and Gradle build configurations. All final unit tests, `lintRelease`, and `release/production/mobile-release.aab` MUST be built and verified on **Commit A** with a 100% clean working tree.
2. **Commit B (`Release Record Commit SHA`):** A documentation-only commit immediately following Commit A that updates `ProductionRoadMap.md` with the verified Commit A SHA, hashes, certificates, and completion stamp. `git diff <Commit_A> <Commit_B> --name-only` MUST contain ONLY documentation files (`ProductionRoadMap.md`), proving zero code or build files changed after the AAB was built.

---

## MICRO-STEP 7.1: Establish Clean `Release Source Commit SHA` (Commit A)

1. Check `git status`:
   ```bash
   git status
   ```
- If any code, manifest, or build file changes from Stages 1–6 are uncommitted, commit them now so `git status` reports `nothing to commit, working tree clean`.
2. Record **Commit A (`Release Source Commit SHA`)**:
   ```bash
   COMMIT_A=$(git rev-parse HEAD)
   echo "RELEASE_SOURCE_COMMIT_SHA (Commit A): $COMMIT_A"
   ```

---

## MICRO-STEP 7.2: Execute Same-Revision Unit Tests, `lintRelease` & Signed AAB Verification on Commit A

On the clean **Commit A** working tree, execute and capture the verbatim outputs of all three commands:

1. **Full Unit Test Suite Across All 5 Modules on Commit A:**
   ```bash
   ./gradlew :features:ai:core:testDebugUnitTest \
     :features:ai:firebase:testDebugUnitTest \
     :features:ai:local:testDebugUnitTest \
     :applications:kocolor:data:testDebugUnitTest \
     :applications:kocolor:features:analyzer:testDebugUnitTest --rerun-tasks
   ```
2. **Release Lint Check on Commit A:**
   ```bash
   ./gradlew :applications:kocolor:apps:mobile:lintRelease
   ```
3. **Signed Production AAB Verification on Commit A:**
    - If any source/config file was committed in 7.1 after `release/production/mobile-release.aab` was built in Stage 6, rebuild `./gradlew :applications:kocolor:apps:mobile:bundleRelease`, copy the bundle to `release/production/mobile-release.aab` and `mapping.txt` to `release/production/mapping.txt`, and re-upload to Play Console Internal Track so the uploaded artifact matches Commit A.
    - Verify the artifact hashes and cryptographic signature on Commit A:
   ```bash
   git status --short
   shasum -a 256 release/production/mobile-release.aab release/production/mapping.txt | tee release/production/sha256.txt
   jarsigner -verify -strict -certs release/production/mobile-release.aab | tail -n 15
   keytool -printcert -jarfile release/production/mobile-release.aab | grep -E "Owner:|SHA256:"
   ```

> **CHECKPOINT 7.2:** Confirm `git status --short` is empty (clean tree on Commit A), all unit tests pass (`0 failed`), `lintRelease` reports `0 errors`, and `jarsigner` outputs `jar verified.`.

---

## MICRO-STEP 7.3: Populate the Final Release Record in `ProductionRoadMap.md` & Create Commit B

1. Verify every checkbox across Stages 1–6 in `ProductionRoadMap.md` is checked (`[x]`) and backed by recorded verbatim evidence.
2. Populate the **FINAL RELEASE RECORD** in `ProductionRoadMap.md` using only verified values from your actual executions (zero `TBD` placeholders and zero unverified boilerplate):

| Field | Verified Production Evidence |
| :--- | :--- |
| **1. Release Source Commit SHA (Commit A)** | `<exact 40-char SHA of Commit A that produced mobile-release.aab>` |
| **2. Release Record Commit SHA (Commit B)** | `<recorded in git log immediately after committing this file; parent = Commit A>` |
| **3. Source Tree State at Build** | `Clean (0 uncommitted / 0 untracked tracked-path files on Commit A)` |
| **4. Gradle Unit Tests (on Commit A)** | `<exact pass counts per module, 0 failed>` |
| **5. `lintRelease` (on Commit A)** | `0 errors, PASS` |
| **6. `versionCode` / `versionName`** | `<versionCode (> Play Console max)> / <versionName>` |
| **7. `mobile-release.aab` SHA-256** | `<64-char hex sha256 of release/production/mobile-release.aab>` |
| **8. R8 `mapping.txt` SHA-256** | `<64-char hex sha256 of release/production/mapping.txt>` |
| **9. AAB `jarsigner` Verification** | `PASS (jarsigner -verify -strict -certs -> jar verified.)` |
| **10. Upload Cert SHA-256 Fingerprint** | `<SHA-256 from keytool -printcert -jarfile>` |
| **11. Play App-Signing Cert SHA-256(s)** | `<All applicable classical / post-quantum hybrid SHA-256 fingerprints from Play Console>` |
| **12. Catalog Key ID & Public Key** | `atelier-prod-01` / `<64-char hex Ed25519 public key in SecurityConstants.kt>` |
| **13. Catalog CDN Verify Status** | `PASS (live CDN manifest.json + all .kpkg packs verified: Ed25519 + SHA-256 + zstd-19)` |
| **14. 16KB ELF + AAB Config + APK Align** | `PASS (all .so LOAD 2**14, PAGE_ALIGNMENT_16K, zipalign -c -P 16 -v 4 Verification successful)` |
| **15. 16KB Runtime Verification** | `<Device/AVD model, build fingerprint, PAGE_SIZE=16384, catalog + zstd runtime PASS>` |
| **16. Health Connect & `AD_ID` Status** | `<Exact verified merged manifest + AAB + Play Console declaration status from Stage 5>` |
| **17. Data Safety & Image Privacy Status** | `<Verified against Stage 5.2 HTTPS check and Stage 5.8 File:Line image-pipeline trace>` |
| **18. Play Integrity & App Check Status** | `LINKED & ENFORCED (Verified in Monitoring and active Enforcement on Play Store install)` |
| **19. Internal-Track Play Store Result** | `PASS (Installed from Google Play Internal Track; catalog + App Check + AI simulation verified)` |

3. Check off all 4 boxes in Stage 7 of `ProductionRoadMap.md` and update the top status banner to `**Status: READY FOR PRODUCTION RELEASE (STAMPED COMPLETE).**`.
4. Commit `ProductionRoadMap.md` as **Commit B (`Release Record Commit SHA`)** and verify that zero code or build files changed between Commit A and Commit B:
   ```bash
   git add applications/kocolor/docs/ProductFixes/ProductionRoadMap.md
   git commit -m "Stamp ProductionRoadMap.md complete for release source $COMMIT_A"
   COMMIT_B=$(git rev-parse HEAD)
   echo "RELEASE_SOURCE_COMMIT_SHA (Commit A): $COMMIT_A"
   echo "RELEASE_RECORD_COMMIT_SHA (Commit B): $COMMIT_B"
   git diff "$COMMIT_A" "$COMMIT_B" --name-only
   git status
   ```

## STAGE 7 MANDATORY COMPLETION GATE
Paste into your final response:
1. Verbatim outputs from Micro-Step 7.1 and 7.2 (on Commit A).
2. Verbatim output of `git diff "$COMMIT_A" "$COMMIT_B" --name-only` and `git log -n 2 --oneline` proving Commit B only touched documentation on top of Commit A.
3. The final completed `ProductionRoadMap.md` file in full.

```