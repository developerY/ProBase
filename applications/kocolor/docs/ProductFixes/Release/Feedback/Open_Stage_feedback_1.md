My assessment: this is an excellent operationalization of the production roadmap, but I would make another revision before giving these prompts to the coding agent.

The biggest improvement is that you've turned seven broad release stages into independently executable missions with explicit checkpoints, security invariants, and evidence requirements. That is exactly the right approach for a project of this complexity.

I particularly like three decisions:

1. One stage at a time. The agent cannot jump ahead or declare success based on an earlier build.
2. Verbatim evidence. The audits become reproducible records rather than claims that something was implemented.
3. Final same-revision verification. You've correctly recognized that Stage 1 results become stale when later stages change code, manifests, or Gradle configuration.

I would retain the seven-stage structure. However, I've identified several technical problems, including two that could produce a false production-ready verdict.

## 1. Stage 1 — Good, with one evidence-integrity correction

The six audit corrections are appropriately targeted. I particularly like requiring producer evidence for `compositeScore` rather than simply accepting the final normalized values.

There is one issue:

Do not rewrite historical test evidence to match a new test run.

For example, if the Phase 4 audit contains an original run with 102 passing tests, retain that original output. Correct the prose arithmetic to 102, if that is what the underlying rows establish. Record the fresh Stage 1 results separately.

The historical audit and the fresh release verification are different evidence records.

I would also run the two targeted test suites as separate Gradle commands rather than relying on two `--tests` filters applied to different tasks in one invocation. This makes the individual results unambiguous.

The rest of Stage 1 looks good.

## 2. Stage 2 — This needs the most careful revision

Critical security stage

I like the production-key isolation, but there are four problems.

First: the private key generation command has a portability issue.

These expressions in the Python verification script:

```
f"Key ID mismatch: {data[\"key_id\"]}"f"PACK VERIFIED: {pack[\"id\"]}"
```

can fail with a syntax error on Python versions before 3.12 because of backslashes inside f-string expressions.

Use intermediate variables instead:

```
key_id = data["key_id"]pack_id = pack["id"]
```

This makes the verification code portable across the Python versions commonly encountered on development machines.

Second: the secret verification grep is incorrect.

You currently instruct:

```
grep -rn "KOCOLOR_SIGNING_SEED_HEX" .
```

But that searches for the environment variable name, not the private seed value. It will also find legitimate references in build scripts and documentation.

The correct check must verify that the actual generated 64-character secret is absent from tracked files and Git history, without printing the secret.

Also, generating a private seed in `~/.kocolor_secrets` and then asking the agent to copy it into a secret manager is not quite the same as generating it directly into the secret-management system. I would minimize the period that the plaintext file exists and explicitly verify its restrictive permissions and eventual secure removal.

Third: the key-transition tests say seven checks but enumerate eight.

You have eight numbered scenarios, including existing-client coexistence. Change the wording to eight checks, or separate the coexistence record into an operational decision rather than an automated test.

More importantly, zero production installs in Play Console does not prove that there are no existing clients distributed through other channels. Check all previous testing and distribution channels before marking coexistence N/A.

Fourth: the local signature-verification script must mirror the actual signing protocol.

Do not assume that the catalog manifest and `.kpkg` signatures cover the exact bytes shown in the example.

The verification implementation must match the actual production signer:

- What bytes are signed?
- Is the signature over compressed or uncompressed data?
- Is the hash of compressed or uncompressed data?
- How is the manifest canonicalized?
- What exactly does the key ID authenticate?

The Python verifier should reproduce the client's actual trust protocol, not invent a parallel one.

One final detail: you can verify the compression format and decompressed content, but you cannot reliably prove from a compressed artifact alone that the compressor used level 19. Verify the `zstd -19` configuration in the build pipeline and preserve the build output as evidence.

## 3. Stage 3 — Logging privacy is strong

Approve with minor changes

I agree with the `Level.NONE` requirement in release builds.

In particular, forbidding `Level.HEADERS` is sensible. OkHttp request logging can expose the request URL, including the OpenWeatherMap `appid` query parameter.

Your proposed debug/release split is appropriate:

```
level = if (BuildConfig.DEBUG) {
    HttpLoggingInterceptor.Level.BODY
} else {
    HttpLoggingInterceptor.Level.NONE
}
```

The R8 approach is also supported by Google's Android security guidance. R8 can remove logging calls from production builds using `-assumenosideeffects`, provided release shrinking is enabled.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers+1



Two changes:

3.1 Search the whole repository.

Your grep currently checks `core/`, `features/`, and `applications/`, but the stated requirement covers the entire repository. Include any other application or infrastructure modules and Java sources.

3.2 Make the release test verify actual behavior.

I would require the agent to exercise:

- Weather requests.
- Cosmetic catalog requests.
- AI requests.
- Style simulation.

Then inspect Logcat for API keys, URLs containing credentials, prompts, responses, and wardrobe contents.

One important distinction: keep operational warnings and errors where useful. The goal is to remove sensitive information and debug noise, not eliminate every production diagnostic.

## 4. Stage 4 — 16 KB verification needs three corrections

Revise before execution

The technical approach is good, but the current script has some gaps.

First, I want to correct one point from my previous assessment: the February 2027 date I previously mentioned was not the 16 KB enforcement date. Google's current documentation identifies 16 KB support as a Play technical requirement for applications shipping native code. February 2027 refers to separate technical-quality thresholds. Your current mandatory compatibility gate is appropriate.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers+2



### 4.1 Add AAB alignment metadata verification

You currently inspect ELF files and generated APK alignment. Add this command:

```
bundletool dump config --bundle="$AAB_PATH" | grep alignment
```

The expected result is:

```
PAGE_ALIGNMENT_16K
```

This establishes the alignment configuration requested by the actual AAB. Google's documentation specifically recommends this check.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers



### 4.2 Correct native library extraction

Your current command:

```
unzip -q "$AAB_PATH" "base/lib/*" -d /tmp/kocolor_16kb_check
```

only examines libraries in the base module.

An AAB may contain native libraries in additional modules. The inspection should enumerate all packaged `.so` files in all modules, not just `base/lib`.

Also, these commands need portability improvements:

```
sort -V
```

This is not supported by the default BSD `sort` on macOS.

And you assign:

```
BUNDLETOOL=$(which bundletool || find ...)
```

but subsequently invoke `bundletool` directly instead of using `$BUNDLETOOL`.

Resolve the actual executable or JAR first and invoke that exact path.

### 4.3 Fix the required success string

Your specification repeatedly says:

```
Verification succesful
```

The Android tool's expected message is:

```
Verification successful
```

One missing `s` shouldn't make a successful technical verification fail its audit gate.

Google's official procedure uses `zipalign -c -P 16 -v 4` and confirms success using that spelling.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers



I would also run the ZIP alignment check against every relevant APK generated from the AAB, particularly where feature modules contain native libraries, rather than assuming one universal APK necessarily covers every delivery configuration.

Your 16 KB runtime test is excellent. Keep the catalog signature verification and native decompression in that test. Merely launching the application doesn't exercise the risky native dependencies.

## 5. Stage 5 — This is where I would be most careful about Play declarations

Revise before execution

Your Stage 5 is unusually good because it understands that source manifests, merged manifests, and the final AAB are different things.

However, three areas need stronger instructions.

### 5.1 AD_ID must be resolved by actual SDK behavior

The current instructions allow the agent to leave the permission in the manifest and simply remove the listing claim.

I would not allow that as the default resolution.

Require the agent to establish whether KoColor actually accesses Advertising ID.

- If not used, remove the permission from the merged release manifest and check the transitive SDK behavior.
- If used, retain the permission and make the Play declaration, privacy disclosures, and actual usage consistent.

A permission being present does not by itself prove advertising collection, but the final artifact must accurately reflect its functionality.

### 5.2 Cleartext: distinguish configuration from real network behavior

Your `makeup-api.herokuapp.com` correction is good.

I would require:

```
Source endpoint verification
Merged network-security configuration
Release runtime network verification
```

Don't let a successful `grep` prove that all traffic is HTTPS.

The actual requirement is that collected data is transmitted using appropriate encryption, and that the Play Data Safety response accurately represents this.

### 5.3 Health Connect: retain the eligibility decision

This is extremely important for KoColor.

The new specification correctly says that fashion recommendations informed by activity or sleep don't automatically qualify as a health application.

Google's current policy states that Health Connect permissions may only be requested for approved use cases and features designed to benefit users' health or fitness. It also addresses health-data access supporting non-health features.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://support.google.com\&sz=32)

Play Console Help+2



I would retain your two possible outcomes:

Option A — Remove Health Connect for this release.

Remove the permissions, associated runtime permission requests, and unused integration code from the release path.

Option B — Retain Health Connect.

Document every permission, the exact health or fitness benefit, user consent flow, in-app disclosure, privacy policy, and Play Console declaration.

Don't decide this based on whether the permissions happen to be present in the merged manifest. Decide it based on the product feature and policy eligibility.

I would also add one requirement:

Trace the image-processing pipeline before claiming that raw face photographs never leave the device.

The release privacy declaration needs evidence from every path, including the analyzer, Firebase provider, and any image-upload implementation. Do not assume that Phase 1 remediation automatically proves every runtime path is local-only.

## 6. Stage 6 — Signed AAB and Firebase App Check

Approve with important amendments

This is a strong stage, but I see three issues.

### 6.1 Make production signing fail-fast

Your current instruction says to wire the release signing configuration when the environment variables are present.

That can accidentally produce an unsigned release AAB if they are absent.

For the production release task, missing signing credentials should cause an immediate, explicit build failure.

For example:

```
Production release:
    RELEASE_STORE_FILE required
    RELEASE_STORE_PASSWORD required
    RELEASE_KEY_ALIAS required
    RELEASE_KEY_PASSWORD required

Missing credentials:
    FAIL — no production AAB
```

A separate unsigned local build can exist if needed, but it must not be confused with the production artifact.

### 6.2 Verify the AAB signature cryptographically

This command:

```
keytool -printcert -jarfile "$PROD_AAB"
```

is useful for inspecting the upload certificate, but I would add:

```
jarsigner -verify -strict -certs "$PROD_AAB"
```

The AAB is signed with the upload key. Google Play subsequently signs the APKs delivered to users with the Play app-signing key. These are separate keys and separate verification points.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://support.google.com\&sz=32)

Play Console Help+1



Also make sure the artifact is copied to the exact filename later referenced in Stage 6.3:

```
release/production/mobile-release.aab
```

Your current prompt doesn't explicitly require that rename.

### 6.3 Update the Play signing certificate instructions

One current consideration is particularly important.

Google's Play App Signing documentation now describes quantum-ready hybrid signing for new applications, including multiple signing-key fingerprints for different Android versions. Where this applies, API providers may need multiple certificate fingerprints rather than just one.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://support.google.com\&sz=32)

Play Console Help



I would change:

> Copy the App signing key certificate (SHA-256 fingerprint).

To:

> Copy every applicable SHA-256 fingerprint listed under Play App Signing, including the relevant classical and post-quantum signing certificates where quantum-ready hybrid signing is enabled. Register all required fingerprints with Firebase and other applicable API providers.

The Firebase App Check registration itself should be performed using the Play Integrity provider, with the matching app-signing certificate fingerprint. Google's current instructions are:

1. Link the Firebase Google Cloud project from Play Console's App Integrity section.
2. Register the application with Play Integrity in Firebase App Check.
3. Supply the app-signing certificate SHA-256 fingerprint.&#x20;

   [image](https://www.google.com/s2/favicons?domain=https://firebase.google.com\&sz=32)

   Firebase



Your internal-track installation test is exactly right.

One more refinement: monitoring and enforcement are different states. Start with monitoring, examine App Check traffic, then enable enforcement for the relevant Firebase services before declaring production protection complete. A successful API call while enforcement is disabled does not prove that App Check is enforced.

## 7. Stage 7 — Your biggest structural issue

Must fix before execution

I found a circularity in the final verification protocol.

You require:

1. A clean Git working tree.
2. Record the Git commit SHA.
3. Run tests, lint, and create the AAB.
4. Update `ProductionRoadMap.md` with the final results.
5. Mark the roadmap COMPLETE.

But updating the roadmap changes the working tree.

If the agent commits that final documentation, Git HEAD changes again.

Consequently, the commit SHA in the final record no longer matches the commit used to build the AAB, even though no application code changed.

This is not a code defect, but it undermines your same-revision evidence requirement.

### My recommended solution: Two commit identities

Distinguish the application source revision from the final release-record revision.

Commit A — Release source

Release Source SHA

Clean application source and build configuration.

Run the complete tests, `lintRelease`, and `bundleRelease` against this exact SHA.

Record the resulting AAB hash and certificate information.

Commit B — Release evidence

Release Record SHA

Update `ProductionRoadMap.md` with the actual test results, hashes, declarations, and completion stamp.

Commit the documentation separately.

Confirm the final repository is clean.

Your final record should contain both:

```
Release Source Commit SHA:
Release Record Commit SHA:
```

This preserves the ability to prove exactly which source revision produced the production AAB.

I would also remove the requirement that everything happen in a single uninterrupted terminal session. What matters is that every result is attributable to the same immutable source SHA.

### Another correction: versionCode

You currently suggest:

```
versionCode 8
or any integer greater than 7
```

This is not sufficient.

Require the agent/operator to check the highest previously uploaded version code in Play Console across all tracks. Choose a version code greater than that value, not merely greater than the source repository's current value.

An internal or closed-testing upload can already have consumed a version code.

### Final record corrections

I would add these fields:

| Field                     | Required evidence                   |
| ------------------------- | ----------------------------------- |
| Release source SHA        | Exact 40-character commit           |
| Release record SHA        | Exact 40-character commit           |
| AAB signature             | Successful `jarsigner` verification |
| Upload certificate        | SHA-256 fingerprint                 |
| Play signing certificates | All applicable SHA-256 fingerprints |
| Play Integrity            | Monitoring and enforcement status   |
| Release installation      | Play Store installation result      |

And change the hardcoded Data Safety description to a verified declaration, rather than automatically writing:

```
RECONCILED (100% HTTPS, on-device ITA biometric privacy, AI telemetry disclosed)
```

Every statement there needs to be proven by the final release artifact and actual application behavior.

## 8. Summary of required amendments

Here is the punchlist I would give the coding agent prompt author before execution.

| Stage | Required adjustment                                         | Priority |
| ----- | ----------------------------------------------------------- | -------- |
| 1     | Preserve historical evidence separately from fresh results  | Medium   |
| 1     | Run targeted test suites independently                      | Low      |
| 2     | Correct Python f-string syntax and secret verification      | High     |
| 2     | Match the actual Ed25519 signing protocol                   | Critical |
| 2     | Correct seven-versus-eight test count                       | Medium   |
| 2     | Verify existing-client distribution channels                | High     |
| 2     | Verify immutable CDN package publication                    | High     |
| 3     | Search all repository modules                               | Medium   |
| 3     | Test release Logcat behavior                                | High     |
| 4     | Add `PAGE_ALIGNMENT_16K` AAB configuration check            | High     |
| 4     | Extract native libraries from every AAB module              | High     |
| 4     | Fix macOS command portability and success spelling          | Medium   |
| 5     | Make AD_ID resolution depend on actual SDK usage            | High     |
| 5     | Verify raw image handling across all AI paths               | Critical |
| 5     | Complete Health Connect eligibility decision                | Critical |
| 5     | Verify Data Safety against actual behavior                  | Critical |
| 6     | Fail production builds when signing credentials are missing | Critical |
| 6     | Add `jarsigner` verification                                | High     |
| 6     | Account for all applicable Play signing fingerprints        | High     |
| 6     | Verify App Check enforcement, not just monitoring           | High     |
| 7     | Separate release source SHA from release record SHA         | Critical |
| 7     | Choose versionCode using Play Console history               | High     |

## 9. My final verdict

# 7 stages — retain

Production execution architecture

The overall structure is right. The problems are specific execution details, not fundamental architectural flaws.

I would not introduce an eighth stage.

Keep the existing seven-stage structure and incorporate the corrections into their respective prompts.

One thing I especially want to preserve is the distinction between these three signing systems:

- Ed25519: Trust and authenticity of the KoColor catalog.
- Android upload signing: Authenticity of the AAB submitted by your build system.
- Google Play app signing: Authenticity of the APK delivered to the user's device.

They are three separate trust boundaries. Your prompts are correctly moving toward treating them independently.

Also, `targetSdk 37` is already above the current API 36 minimum for new Play submissions and updates. Keep it, just as your roadmap specifies.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers



My recommendation: Make these corrections, freeze the seven prompts, and execute one stage at a time. Do not let the coding agent mark a stage complete simply because a build succeeds.

The ultimate release test is not whether KoColor compiles. It is whether a clean installation from Google Play can obtain and verify the production catalog, execute the actual styling pipeline, protect user information, and operate correctly on a 16 KB device.

That's the standard I would use for the first KoColor production release.