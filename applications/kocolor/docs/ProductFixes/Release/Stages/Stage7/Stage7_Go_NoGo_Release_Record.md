# Stage 7 — Go / No-Go & Two-Commit Final Release Record (Release Runbook Split)

Source: `../Gemini_stages.md` ("Stage 7 Prompt"). Execute ONLY this stage; feed one stage at a time to the coding agent.

## Fixes applied in this split (vs. `Gemini_stages.md`)

- **Fixed a wrong path that would have failed the final commit:** `git add applications/kocolor/docs/ProductFixes/ProductionRoadMap.md` → `…/ProductFixes/Release/ProductionRoadMap.md` (the `Release/` segment was missing).
- **Added a `COMMIT_A` re-export note** in 7.3 (the commit message and diff commands depend on the variable surviving from 7.1).
- **Final Release Record row 12** now records the key-ID pin (`KOCOLOR_TRUSTED_KEY_ID`, added in Stage 2.2) alongside the public key.
- Smoothed row 3 wording ("0 untracked tracked-path files" → "0 untracked files").
- Verified: two-commit identity protocol, same-revision test/lint/AAB rule, and all 19 record fields are consistent with `Open_Stage_feedback_1.md` §7.

---

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

> **NOTE:** If this micro-step runs in a fresh shell, re-export `COMMIT_A` first: `COMMIT_A=$(git rev-parse HEAD)` (HEAD must still be Commit A — verify with `git log -n 1 --oneline` before proceeding).

1. Verify every checkbox across Stages 1–6 in `ProductionRoadMap.md` is checked (`[x]`) and backed by recorded verbatim evidence.
2. Populate the **FINAL RELEASE RECORD** in `ProductionRoadMap.md` using only verified values from your actual executions (zero `TBD` placeholders and zero unverified boilerplate):

| Field | Verified Production Evidence |
| :--- | :--- |
| **1. Release Source Commit SHA (Commit A)** | `<exact 40-char SHA of Commit A that produced mobile-release.aab>` |
| **2. Release Record Commit SHA (Commit B)** | `<recorded in git log immediately after committing this file; parent = Commit A>` |
| **3. Source Tree State at Build** | `Clean (0 uncommitted changes, 0 untracked files on Commit A)` |
| **4. Gradle Unit Tests (on Commit A)** | `<exact pass counts per module, 0 failed>` |
| **5. `lintRelease` (on Commit A)** | `0 errors, PASS` |
| **6. `versionCode` / `versionName`** | `<versionCode (> Play Console max)> / <versionName>` |
| **7. `mobile-release.aab` SHA-256** | `<64-char hex sha256 of release/production/mobile-release.aab>` |
| **8. R8 `mapping.txt` SHA-256** | `<64-char hex sha256 of release/production/mapping.txt>` |
| **9. AAB `jarsigner` Verification** | `PASS (jarsigner -verify -strict -certs -> jar verified.)` |
| **10. Upload Cert SHA-256 Fingerprint** | `<SHA-256 from keytool -printcert -jarfile>` |
| **11. Play App-Signing Cert SHA-256(s)** | `<All applicable classical / post-quantum hybrid SHA-256 fingerprints from Play Console>` |
| **12. Catalog Key ID & Public Key** | `atelier-prod-01` (`KOCOLOR_TRUSTED_KEY_ID`) / `<64-char hex Ed25519 public key in SecurityConstants.kt>` |
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
   git add applications/kocolor/docs/ProductFixes/Release/ProductionRoadMap.md
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
