# Stage 2 — Catalog Signing & Production Seed (Release Runbook Split)

Source: `../Gemini_stages.md` ("Stage 2 Prompt"). Execute ONLY this stage; feed one stage at a time to the coding agent.

## Fixes applied in this split (vs. `Gemini_stages.md`)

- **Micro-step 2.2 corrected against the tree:** pinned the exact trust-anchor path (`.../starterpack/.../data/SecurityConstants.kt`) and constant name (`KOCOLOR_ROOT_PUBLIC_KEY`). No key-ID constant exists there, and client `SignatureVerifier.verify()` takes no key-ID — so 2.2 now requires ADDING `KOCOLOR_TRUSTED_KEY_ID` plus enforcement in the verification path; without this, 2.4 Tests 3 & 5 are unimplementable.
- **Micro-step 2.1:** added the missing `pip install pynacl zstandard` prerequisite (both scripts import them).
- **Micro-step 2.3:** strengthened the canonicalization note — the client verifies the raw `data` substring bytes (`extractDataProperty`, `StarterPackRepository.kt:77-85`), not re-serialized JSON, so the Python `json.dumps(sort_keys…)` assumption must be validated against actual signer bytes first. Verified the assumed envelope/pack JSON shape against `server/Inventory/package/dist/manifest.json` (`data.key_id`, `data.packs[].endpoint/compressed_size_bytes/uncompressed_size_bytes/sha256/signature`, top-level `signature`).
- **Micro-step 2.4:** pinned exact module paths (`StarterPackRepository.kt`, `SecurityConstants.kt`) instead of "the module containing…".
- **Micro-step 2.6:** added the CDN immutability confirmation required by `Open_Stage_feedback_1.md` §8 (re-download + byte-compare after publish).

---

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
2. **Install verifier prerequisites (once):** `pip install pynacl zstandard` (the scripts below import `nacl.signing` and `zstandard`).
3. **Generate or Load the Production Seed Into Secret Manager / Restricted Un-Tracked File (`0600`):**
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
4. **Verify the Actual Secret Value (NOT the Variable Name) Is Absent from Git Working Tree & History Without Printing the Secret:**
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

## MICRO-STEP 2.2: Set `KOCOLOR_KEY_ID` (`atelier-prod-01`) & Update the Client Trust Anchor

1. Read the client trust anchor at its exact path:
   `applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/data/SecurityConstants.kt`.
2. Update the trusted Ed25519 public key constant `KOCOLOR_ROOT_PUBLIC_KEY` to the exact `PUBLIC_KEY_HEX` from Micro-Step 2.1.
3. **ADD the missing key-ID pin (required — it does not exist yet):** add `const val KOCOLOR_TRUSTED_KEY_ID = "atelier-prod-01"` to `SecurityConstants.kt` AND enforce it in the verification path (`StarterPackRepository.kt` / `SignatureVerifier`): every manifest/pack whose envelope `key_id` differs from the pinned value MUST be rejected before signature verification. The current client verifier takes no key-ID input, so without this change Micro-Step 2.4 Tests 3 and 5 cannot pass.
4. **Checkpoint 2.2:** Run:
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
   *(CRITICAL: the Android client verifies the RAW `data` SUBSTRING bytes extracted from the envelope JSON (`extractDataProperty` in `StarterPackRepository.kt`), NOT re-serialized JSON. The `json.dumps(data, sort_keys=True, …)` line above is an ASSUMPTION — validate it byte-for-byte against the actual signer output first. If the signer does not emit sorted-compact JSON, extract the raw `data` substring exactly as the client does and verify those bytes instead, documenting the exact protocol 1:1.)*

> **CHECKPOINT 2.3:** Paste the packager's `zstd -19` config line, the build output, and the verbatim `MANIFEST ENVELOPE SIGNATURE: VALID` + `PACK VERIFIED` output.

---

## MICRO-STEP 2.4: Key-Transition Compatibility Tests (7 Automated Tests + 1 Operational Channel Audit)

### Part A — 7 Automated Key-Transition Tests (`CatalogKeyTransitionTest.kt` + Compiler Check)
Create or expand `CatalogKeyTransitionTest.kt` in `:applications:kocolor:features:starterpack` (the module containing `SecurityConstants.kt` and `StarterPackRepository.kt` at `applications/kocolor/features/starterpack/src/main/java/com/zoewave/probase/kocolor/features/starterpack/`) and run the compiler missing-seed check to prove all **7 automated tests**:
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
    - **Immutability confirmation:** re-download the live files a second time and byte-compare SHA-256 hashes across both downloads; record the CDN cache/immutability behavior (published packs must be immutable — a changed pack under the same URL invalidates the release).
    - Securely remove any temporary local plaintext seed file once stored in Secret Manager (`rm -P ~/.kocolor_secrets/atelier_prod_01.env` if no longer needed locally).

## STAGE 2 MANDATORY COMPLETION GATE
Check off `[x] 2.1` through `[x] 2.6` in `ProductionRoadMap.md` ONLY after pasting:
1. Secret-absence verification output + `SecurityConstants.kt` diff (`atelier-prod-01`).
2. `zstd -19` config proof + staging verification output + all 7 automated `CatalogKeyTransitionTest` results + Check 8 channel audit.
3. Live external CDN verification output proving all live `.kpkg` packs and `manifest.json` verify under `atelier-prod-01`.
