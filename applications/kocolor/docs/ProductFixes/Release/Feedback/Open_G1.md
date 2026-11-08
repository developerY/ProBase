My assessment: this is a very good production release roadmap. I agree with the `NOT READY` status. It makes an important distinction between engineering code being complete and a product actually being safe, signed, verified, and ready for Google Play.

I would rate the structure highly. However, I found several issues that I would resolve before declaring this the authoritative production checklist. Two are particularly important: production catalog key rotation and Health Connect policy eligibility.

## 1. What I particularly like

The roadmap has the right overall progression:

| Stage                        | My assessment                                           |
| ---------------------------- | ------------------------------------------------------- |
| 1. Engineering stamp closure | Excellent. Evidence before release.                     |
| 2. Catalog signing           | Critical. Correctly separated from application signing. |
| 3. Logging privacy           | Essential. Should be a hard release blocker.            |
| 4. 16 KB verification        | Correct to test before shipping.                        |
| 5. Listing and manifest      | Excellent. Addresses actual review risks.               |
| 6. Signed AAB                | Correct separation of upload and deployment.            |
| 7. Go/No-Go                  | Appropriate final release gate.                         |

I particularly like that you are retaining the existing engineering decisions:

- `minSdk 34` stays.
- Current library versions stay.
- No downgrading to make compatibility problems disappear.
- Existing three-axis Fashionista implementation remains authoritative.
- The six-feature engine stays deleted.
- Previous remediation phases must be supported by actual evidence.

That is exactly the discipline I would want at this point.

## 2. Important corrections before execution

### A. Stage 2 — Production Ed25519 key rotation needs an additional safety gate

Critical

This is probably the most important operational issue in the roadmap.

You are changing:

1. Catalog signing private key.
2. Catalog signing key ID.
3. Public key embedded in `SecurityConstants.kt`.
4. Every signed `.kpkg`.
5. CDN manifest.

All five must agree.

However, consider an existing installation that still trusts `atelier-dev-01`.

You publish a new catalog signed by `atelier-prod-01`.

That installation may be unable to verify the new catalog, even though the new release works perfectly.

The reverse concern also matters: the old private key is burned, so you must not continue trusting it simply to preserve compatibility if it was compromised.

I would add a mandatory key-transition compatibility test before the CDN publication step.

Test:

- Fresh installation with production public key.
- Upgrade from the previous application version.
- Existing cached development catalog.
- New production catalog.
- Invalid signature rejection.
- Incorrect key ID rejection.
- Missing signing seed rejection.
- CDN manifest references only verified production packages.

Also, separate the preparation and publication operations.

My preferred sequence:

1. Generate production key securely.
2. Update public-key configuration.
3. Sign the catalog locally or in a staging environment.
4. Verify every package and manifest.
5. Build the release AAB.
6. Validate that the release application trusts the production catalog.
7. Publish the production CDN catalog using the established transition procedure.

If existing distributed clients need to coexist with the new catalog, resolve that compatibility problem explicitly before switching the live manifest.

Your existing requirement to upload all `.kpkg` files before `manifest.json` is correct. I would retain it and add CDN verification from an external client after publication.

Also, explicitly state:

The Ed25519 catalog signing key is NOT the Android application upload key.

They are different credentials, with different purposes and security boundaries.

### B. Stage 4 — Correct the 16 KB enforcement wording

Technical correction

Your statement:

> Play is rejecting non-compliant apps.

Needs clarification.

As of November 8, 2026, Google's Android documentation says that apps targeting Android 15/API 35 or higher on Google Play must support 16 KB page sizes. However, it identifies February 1, 2027 as the date when unsupported app updates will be blocked from release.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers+1



I would change the stage heading to:

Stage 4 — 16 KB Page-Size Compatibility (Mandatory Production Readiness Gate)

This remains important. It just distinguishes the current requirement from its stated enforcement date.

There is another technical improvement I would make.

Checking ELF alignment alone is insufficient.

Your verification should cover two separate things:

| Verification          | What it establishes                                     |
| --------------------- | ------------------------------------------------------- |
| ELF segment alignment | Native `.so` libraries support 16 KB pages              |
| APK ZIP alignment     | Packaged native libraries are correctly aligned in APKs |

Google documents separate checks for both, including `zipalign -c -P 16 -v 4` for generated APKs.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers+1



I would add:

- Inspect the actual resolved AAB dependencies, not just the version catalog.
- Enumerate every packaged `.so` across supported ABIs.
- Check ELF alignment.
- Generate APKs from the actual AAB using Bundletool.
- Check APK ZIP alignment.
- Install on a 16 KB environment.
- Run the catalog download, signature verification, and zstd decompression paths, not just application startup.

Your `DataStore 1.2.1` and `zstd 1.5.7-21` pins should remain untouched unless evidence identifies a problem.

Also, targeting API 37 already exceeds the current Google Play requirement of API 36 for new applications and updates as of August 31, 2026. There is no reason to downgrade the target API to address a 16 KB problem.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://support.google.com\&sz=32)

Play Console Help+1



### C. Stage 5.7 — Health Connect deserves a policy eligibility review

Potential release blocker

This is the item I would investigate most carefully before submitting anything.

There are two separate things here:

1. Health Apps declaration.
2. Health Connect data-type permissions and access declaration.

They should not be treated as one form.

Google's current policy requires Health Connect data access to be directly tied to a clear user benefit within approved use cases. It also requires only requesting data types that support specific user-facing health features.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://support.google.com\&sz=32)

Play Console Help+1



This matters for KoColor.

If those five Health Connect permissions exist only to improve fashion or outfit recommendations, I would not assume that this qualifies.

The actual feature must meet Google's health and fitness use-case requirements. A fashion recommendation benefiting from activity information is not automatically a fitness or wellness feature.

I would add a decision gate:

First determine whether KoColor actually needs Health Connect for a qualifying user-facing health or fitness feature.

Then:

- If yes, document each permission individually and demonstrate the user-facing benefit.
- If no, remove the unused Health Connect functionality and permissions from the release artifact.
- Do not submit unnecessary data-type permissions simply because a dependency declared them.

There is also an important detail in your wording:

> manifest declares 5 types, all transitively stripped

You must inspect the final merged release manifest and the actual AAB, not rely on the source manifest or a statement about transitive stripping.

If those five permissions are absent from the final AAB, they should not be declared as though the application requests them. If the runtime still tries to request them, that is a separate implementation defect.

Google's Health Connect publishing instructions explicitly provide a no-health-features declaration option where applicable.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers+1



Finally, the Health Apps declaration and the data-type permission declaration are separate from one another. The generic Play Permissions Declaration Form explicitly requires a demonstration video; retain the video task when that form applies, but don't assume every declaration automatically has the same requirements.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://support.google.com\&sz=32)

Play Console Help



I would also add a privacy-policy check covering health data access, processing, sharing, retention, deletion, and encryption.

This is especially important under the current Play health-data requirements.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://support.google.com\&sz=32)

Play Console Help



### D. Stage 6.3 — Correct the Firebase App Check instructions

Implementation clarification

Your current instruction says:

> Add the Play Console App signing key certificate (SHA-256) to Firebase Console → Project settings → app.

That is not the documented App Check registration path.

The correct workflow is:

1. In Play Console, open Release → App integrity → Play Integrity API.
2. Link the Play Integrity API to the correct Firebase/Google Cloud project.
3. In Firebase Console, navigate to Security → App Check.
4. Register the Android application using the Play Integrity provider.
5. Supply the SHA-256 fingerprint of the actual Play app-signing certificate.

Firebase documents this exact sequence.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://firebase.google.com\&sz=32)

Firebase



There is another correction:

> Confirm Play Integrity API enabled and in API-key restrictions.

I would remove the API-key restriction requirement from this particular task.

Firebase API keys identify the Firebase project/application; they are not authorization credentials. The documented App Check integration uses Play Integrity project linking and app-signing certificate registration, not adding the Play Integrity API to a Firebase API key's allowed API list.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://firebase.google.com\&sz=32)

Firebase+1



Also distinguish:

- Play app-signing certificate.
- Android upload certificate.
- Debug signing certificate.

For users installing through Google Play, the relevant certificate is the Play app-signing certificate, not the upload certificate.

One more thing: your appendix says App Check (Play Integrity in release) has already passed, while Stage 6.3 says the wiring is incomplete.

I would distinguish code-side integration from Firebase/Play Console registration and enforcement. That resolves the apparent contradiction.

### E. Stage 6.2 — Archive the old mapping file; don't destroy it

I would change this instruction:

> Delete the stale Oct 18 AAB (81 MB) + 167 MB mapping.txt from release/.

The stale AAB should not remain in the active release directory, but I would not delete the only copy of its `mapping.txt`.

R8 mapping files are important for deobfuscating crash reports associated with their specific release.

My recommendation:

```
release/
    archive/
        0.5-code7/
            app-release.aab
            mapping.txt
            sha256.txt

    production/
        app-release.aab
        mapping.txt
        sha256.txt
```

The archive does not need to remain in Git. Keep it in protected artifact storage.

For every release, retain:

- Version code.
- Version name.
- Commit SHA.
- AAB SHA-256.
- R8 mapping.
- Native debug symbols, where applicable.
- Signing certificate fingerprint.

This is much better for future production support.

## 3. Three additional changes I would make

### Stage 3 — HTTP logging should be completely disabled in release

I agree with this stage.

One subtlety: `Level.HEADERS` is not necessarily safe for the weather client because the request URL itself contains the `appid` query parameter.

I would require:

```
level = if (BuildConfig.DEBUG) {
    HttpLoggingInterceptor.Level.BODY
} else {
    HttpLoggingInterceptor.Level.NONE
}
```

Use the correct module-specific `BuildConfig`.

The Firebase AI prompt and response logs should also be debug-only.

For the R8 rule, ensure release minification is actually enabled and that the rule removes the intended logging methods. Retain warning/error logging where needed for operational diagnostics.

Android's security guidance explicitly warns against sensitive information in Logcat and recommends stripping production logs where possible.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers+1



### Stage 5 — Check merged manifests, not only source manifests

This applies to both AD_ID and cleartext.

AD_ID

Some dependencies can merge `com.google.android.gms.permission.AD_ID` into the application manifest even when you did not explicitly declare it yourself.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://support.google.com\&sz=32)

Play Console Help



Therefore, verify the final release manifest and resulting AAB.

If the app does not need Advertising ID, remove it from the merged artifact and ensure the Play declaration agrees.

Cleartext

Your `makeup-api.herokuapp.com` exception allows cleartext traffic to that particular destination. It does not by itself prove that plaintext traffic is actually being transmitted.

However, if the exception is unnecessary, remove it. If the application actually uses HTTP, migrate the request to HTTPS rather than weakening the security configuration.

Android supports domain-specific cleartext exceptions, so the relevant check is both the network configuration and actual runtime traffic.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://developer.android.com\&sz=32)

Android Developers



### Add Stage 5.8 — Data Safety and Privacy Declaration

I think this is missing from the checklist.

You already have individual manifest and logging checks, but the Play Console Data Safety declaration should be independently checked against the actual release application.

I would add:

- Data collected by the app.
- Data processed locally versus sent to cloud services.
- AI prompt and response handling.
- Third-party service processing.
- Health data, if applicable.
- Encryption in transit.
- Data retention and deletion.
- Privacy policy consistency with the actual application.

Especially with KoColor's AI architecture, the listing, privacy policy, Data Safety answers, and actual provider behavior must agree.

Health-related applications also have explicit privacy-policy and prominent-disclosure requirements.&#x20;

[image](https://www.google.com/s2/favicons?domain=https://support.google.com\&sz=32)

Play Console Help+1



## 4. One change to the release sequencing

Your sequence is good, but I would make the final verification discipline even stronger.

There are code changes after Stage 1:

- Stage 3 logging modifications.
- Stage 5 manifest modifications.
- Stage 6 signing/build modifications.

Therefore, Stage 1's successful test and lint output is not final release evidence.

You already address this in Stage 7, which is good.

I would explicitly require:

```
FINAL RELEASE RECORD

Git commit SHA:
Git working tree:
Gradle test result:
lintRelease result:
AAB versionCode:
AAB versionName:
AAB SHA-256:
Upload certificate fingerprint:
Play signing certificate fingerprint:
Catalog key ID:
Catalog verification result:
16 KB ELF result:
16 KB APK alignment result:
Health Connect declaration status:
Data Safety declaration status:
Play Console internal-track result:
```

The final test, lint, and signed AAB must correspond to the same source revision.

I would also make the internal testing checklist include an actual Google Play installation, not just a sideloaded APK. That lets you verify the Play signing and Play Integrity path together.

One minor adjustment: the German translation error should be treated as a potentially transient Play Console upload problem, not a guaranteed condition that re-uploading fixes. Verify the store-listing translation state and retry the identical AAB only after confirming the artifact itself is valid.

## 5. My proposed priority ranking

Not everything here has the same release risk.

## Production release risk

Production signing and trust transition

Critical

New key, public key, signed catalog, existing-client compatibility.

Health Connect eligibility and declarations

Critical

Verify the actual user-facing feature, permissions, and policy suitability.

Release privacy and manifest

Critical

HTTP logging, AI logging, AD_ID, cleartext, Data Safety.

Signed application and Firebase attestation

Critical

Upload signing, Play signing certificate, App Check registration.

16 KB compatibility

Mandatory gate

ELF alignment, APK alignment, real environment testing.

Audit closure and store assets

Required

Test evidence, screenshots, version, listing and documentation.

## Final verdict

NOT READY

Correct release status

I would retain all seven stages. The corrections above do not require another architectural remediation phase.

What you have now is a transition from engineering completion to operational readiness.

The most important distinction is that a successful Gradle build does not prove that the production system is correctly configured.

KoColor needs to demonstrate that the signed application trusts the signed catalog, that Play-installed clients can pass App Check, that the release does not expose sensitive information, and that the actual permissions and disclosures match the product's behavior.

Once those conditions are evidenced, this becomes a credible production release checklist.

My recommendation: make these amendments, retain the NOT READY status, and use this as the single master release record.