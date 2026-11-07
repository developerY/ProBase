This Phase 2 specification incorporates every requirement from `applications/kocolor/docs/ProductFixes/fashion_algorithms_assessment.md` and `applications/kocolor/docs/ProductFixes/Directive/Tasks.md`, along with hard anti-shortcut guards and mandatory `grep` verification checks so the coding agent cannot fake completion or regress Phase 1 fixes.

Copy and paste the entire block below into your coding agent:

````markdown
# AGENT MISSION: Execute Phase 2 (P1 — Token Spend, Fit-Loop Latency & Provider Hardening)

You are executing **Phase 2 (P2.1 through P2.5)** of the KoColor remediation plan (`applications/kocolor/docs/ProductFixes/RoadMapToPro.md` / `applications/kocolor/docs/ProductFixes/Directive/Tasks.md`).

## STRICT EXECUTION INVARIANTS (READ FIRST)
1. **Do NOT regress Phase 1 or Punchlist 1 fixes:**
   - Preserve the explicit `AiInput.Multimodal` `UnsupportedOperationException` rejection guards at the top of `ByokAiProvider.execute()` and `GemmaRestFallbackProvider.execute()`.
   - Preserve `"UNKNOWN"` defaults in `CompactManifestSerializer.kt`, `standardId = "FASHIONISTA"` in `FashionistaScore.kt`, and the required (non-default) `fashionistaScore: Int` parameter in `GreedyRehydrator.mapToVisualBlueprintData()`.
2. **Never delete unit test files to pass compilation:** When `AiProvider.execute()` changes its return type from `Result<String>` to `Result<AiExecutionResult>`, update all existing test fakes, mocks, and assertions in-place.
3. **No silent default-parameter workarounds:** Every new field or method required below must be wired end-to-end at all call sites.

---

## P2.1: Right-Size the On-Device Nano Tier (Assessment T1)

**Problem:** `LocalNanoAiProvider` enforces `maxInputTokens = 768`, but `PromptAssembler.buildExactRequest()` generates a ~600–750 token static scaffold before any manifest items are added, causing `CONTEXT_OVERFLOW` and a wasted fit-loop cycle on every request.

### Required Changes:
1. **`PromptAssembler.kt` (`applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/PromptAssembler.kt`):**
   - Add `fun assembleCompactNano(context: StyleRequestContext, wardrobeManifest: String, cosmeticManifest: String, missingRoles: Set<String>, providerCapability: AiProviderCapability): AiInput`, mirroring `buildExactRequest()`'s contract (same context/candidate inputs, returns `AiInput`, wrapping the compact prompt in `AiInput.TextOnly`).
   - Strip all multi-paragraph prose instructions and explanations.
   - Format user appearance, thermal/weather (`tempC`, `condition`, `uvIndex`), intent, and anchor IDs as compact single-line key-value pairs (`SKIN:... | TEMP:... | INTENT:... | ANCHORS:...`).
   - Emit the `WARDROBE:` and `COSMETICS:` pipe-delimited manifests and a minimal single-line JSON output schema (`{"selectedClothingIds":[],"selectedCosmeticIds":[],"recommendedPalette":[],"rationale":""}`). The rationale key MUST be `"rationale"` (the decoder only recognizes `rationale`/`summary`/`explanation`/`reason`).
   - Total static scaffold (excluding manifest lines) MUST be `<= 180 tokens` (`<= 720 characters`).

2. **`StyleSimulatorEngine.kt` (`applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt`):**
   - **Prompt Selection:** Inside `adaptContextToProvider()`, detect the on-device Nano tier (`provider.capability.id == "gemini_nano"`, falling back to `isLocal && maxInputTokens <= 768`) and build the prompt using `promptAssembler.assembleCompactNano(...)` instead of `promptAssembler.buildExactRequest(...)`.
   - **Nano Fast-Fail Gate:** Before entering the `while` step-down loop in `adaptContextToProvider()`, if the provider is Nano, construct the minimum-floor prompt (`DetailLevel.MINIMAL`, `K = minCandidateAdditions` which is `4`, `slotsPerCategory = 1`) and check its heuristic token count (`(minPrompt.length + 3) / 4`). If that minimum floor still exceeds `provider.capability.maxInputTokens`, immediately abort adaptation for Nano (`CONTEXT_OVERFLOW`) without running the step-down loop. (Requires the P2.2 floor-of-1 change below — implement P2.2 item 3 first or in the same pass.)

---

## P2.2: Remove Network I/O from the Fit Loop & Honor the K Floor (Assessment T2, T10)

**Problem:** `FirebaseAiProvider.countTokens()` performs network RPCs inside `FirebaseAiClient` (with retries and 25s timeouts) on every iteration of `adaptContextToProvider()`. Additionally, `slotsPerCategory` floors at `2`, preventing `K` step-downs from shrinking the manifest at the floor.

### Required Changes:
1. **Hoist Selection-State Generation to Once Per Request (`StyleSimulatorEngine.kt`):**
   - `deterministicContextEngine.generateSelectionState(...)` currently runs inside `adaptContextToProvider()` (once per provider attempt). Move it into `generateBlueprint()` so it executes **once** per request and pass the precomputed `selectionState` into `adaptContextToProvider()` as a parameter.

2. **Heuristic-First Fit Loop with At-Most-One `countTokens()` Call (`StyleSimulatorEngine.kt`):**
   - Inside `adaptContextToProvider()`'s `while` step-down loop (`EXPANDED` -> `BALANCED` -> `MINIMAL`, then `currentK -= 2`), **remove** `provider.countTokens(...)` from the loop condition/body.
   - Use the fast local character heuristic during all step-down iterations:
     ```kotlin
     fun estimateTokensHeuristic(text: String): Int = (text.length + 3) / 4
     ```
   - Once the loop finds a `(detailLevel, currentK)` whose `estimateTokensHeuristic(prompt) <= maxTokens`, exit the heuristic loop and call `provider.countTokens(AiInput.TextOnly(promptString = prompt))` **at most once** to verify the exact token count.
   - If that single `provider.countTokens()` check returns a count `> maxTokens`:
     - If already at `DetailLevel.MINIMAL` and `currentK <= minCandidateAdditions`, fail fast with `CONTEXT_OVERFLOW`.
     - Otherwise, compute the calibration ratio (`val ratio = exactCount.toDouble() / estimateTokensHeuristic(prompt).coerceAtLeast(1)`) and step down `(detailLevel, currentK)` using `estimateTokensHeuristic(nextPrompt) * ratio <= maxTokens` **without** invoking `provider.countTokens()` a second time.

3. **Honor the K Floor (`slotsPerCategory` Step-Down to 1) (`StyleSimulatorEngine.kt`):**
   - In `adaptContextToProvider()`, replace the hardcoded minimum of `2` items per category (`max(2, ...)` or ` coerceAtLeast(2)`) so that when `currentK` steps down toward `minCandidateAdditions` (`4`), `slotsPerCategory` drops to `1` per required category and total non-anchor candidates are strictly capped at `currentK`:
     ```kotlin
     val numCategories = categorizedPool.keys.size.coerceAtLeast(1)
     val slotsPerCategory = if (currentK <= minCandidateAdditions) 1 else (currentK / numCategories).coerceAtLeast(1)
     ```
   - After taking `slotsPerCategory` per category, apply `.take(currentK)` across the non-anchor candidate additions so `currentK = 4` guarantees at most 4 non-anchor wardrobe additions.

---

## P2.3: Enforce `maxOutputTokens` & Record Billed Telemetry Across All Providers (Assessment T6)

**Problem:** `maxOutputTokens` is declared on `AiProviderCapability` but never sent to Firebase, BYOK, or Gemma. Server-reported `usageMetadata` is only written to `Log.d` (stripped in release), and `StyleAuditLogger` only logs the pre-call input estimate.

### Required Changes:
1. **Create Shared Execution & Usage Models (`:features:ai:core`):**
   - In `:features:ai:core` (e.g., inside `AiModels.kt` or `AiProvider.kt`), define:
     ```kotlin
     data class TokenUsage(
         val promptTokens: Int = 0,
         val candidatesTokens: Int = 0,
         val totalTokens: Int = promptTokens + candidatesTokens
     )

     data class AiExecutionResult(
         val text: String,
         val usage: TokenUsage = TokenUsage()
     )
     ```
   - Add `defaultTemperature: Float = 0.2f` to `AiProviderCapability` (matching the Firebase generation default).
   - Update `AiProvider.execute(input: AiInput): Result<AiExecutionResult>` (no new parameters — `temperatureOverride` already rides on `AiInput`).

2. **Enforce `maxOutputTokens` & Extract `TokenUsage` in Every Provider:**
   - **`FirebaseAiClient.kt` & `FirebaseAiProvider.kt` (`:features:ai:firebase`):**
     - Pass `maxOutputTokens: Int` into `FirebaseAiClient.generateContent(...)` and set `maxOutputTokens = maxOutputTokens` inside `generationConfig { ... }`.
     - Extract `response.usageMetadata?.promptTokenCount ?: 0`, `response.usageMetadata?.candidatesTokenCount ?: 0`, and `response.usageMetadata?.totalTokenCount ?: 0` into `TokenUsage` and return `AiExecutionResult(text = responseText, usage = tokenUsage)`.
   - **`ByokAiProvider.kt` (`:features:ai:core`):**
     - **Keep** the `if (input is AiInput.Multimodal) return Result.failure(UnsupportedOperationException(...))` check as the first statement of `execute()`.
     - In the `kotlinx.serialization` JSON request builder, add a `"generationConfig"` object containing:
       - `"temperature"`: `input.temperatureOverride ?: capability.defaultTemperature`
       - `"maxOutputTokens"`: `capability.maxOutputTokens`
       - `"responseMimeType"`: `"application/json"`
     - Parse the `"usageMetadata"` JSON object from the Gemini REST response (`promptTokenCount`, `candidatesTokenCount`, `totalTokenCount`, defaulting to `0` if missing) and return `Result.success(AiExecutionResult(text = extractedText, usage = usage))`.
   - **`GemmaRestFallbackProvider.kt` (`:features:ai:firebase`):**
     - **Keep** the `if (input is AiInput.Multimodal) return Result.failure(UnsupportedOperationException(...))` check as the first statement of `execute()`.
     - On both the direct REST path and the proxy REST path, include `maxOutputTokens = capability.maxOutputTokens` and `temperature = input.temperatureOverride ?: capability.defaultTemperature` in the serialized request payload.
     - Parse `"usageMetadata"` (`promptTokenCount`, `candidatesTokenCount`, `totalTokenCount`, defaulting to `0` when omitted by the proxy) into `TokenUsage` and return `Result.success(AiExecutionResult(text = extractedText, usage = usage))`.
   - **`LocalNanoAiProvider.kt` (`:features:ai:local`):**
     - Return `Result.success(AiExecutionResult(text = outputText, usage = TokenUsage(promptTokens = estimatedIn, candidatesTokens = (outputText.length + 3) / 4)))`, where `estimatedIn` comes from the engine's existing `estimateTokens()` for the input prompt.
   - **`LocalDownloadAiProvider.kt` (`:features:ai:local`):**
     - Migrate `execute()`/`countTokens()` to the new carrier exactly like Nano (local heuristic usage — no billed tokens).

3. **Update All `provider.execute()` Call Sites & Audit Trail:**
   - Update `AnalyzerEngine.kt`, `SuggestionEngine.kt`, `KoColorDiagnosticDelegate.kt`, and `AppDiagnosticDelegate.kt` to unwrap `executionResult.text` (adjusting for `Result<AiExecutionResult>`).
   - In `StyleSimulatorEngine.kt` and `StyleAuditLogger.kt` (plus the audit data model), record:
     - `promptTokens` (use `executionResult.usage.promptTokens` if `> 0`, else fallback to the pre-call token count)
     - `outputTokens` (`executionResult.usage.candidatesTokens`)
     - `totalTokens` (`executionResult.usage.totalTokens` if `> 0`, else `promptTokens + outputTokens`)
   - Update `StyleAuditLogger` output formatting so the audit log explicitly prints `Tokens: <total> (In: <prompt>, Out: <output>)`.

---

## P2.4: Validation Repair, Provider Timeouts/Temperature Parity & Cache Stabilization (Assessment T3, T7, T8, T9)

### Required Changes:
1. **Deterministic Validation Repair Before Failover (`StyleSimulatorEngine.kt`, `RecommendationValidator.kt`):**
   - Note: `RecommendationValidator` already re-injects omitted locked anchors.
   - When `RecommendationValidator.validateAndSanitize()` removes ungrounded/hallucinated IDs or detects missing required clothing slots or missing pigment cosmetic roles (`EYES`, `DIMENSION`, `LIPS`, `NAILS`):
     1. Run deterministic slot/role repair using the eligible candidate pool (`fillMissingSlotsDeterministically` for wardrobe slots and top-ranked eligible cosmetic candidates per missing role).
     2. Re-run `RecommendationValidator` on the repaired blueprint and record the fresh `ValidationReport`.
     3. If the repaired blueprint now has valid grounded IDs, all required slots filled, locked anchors present, and a non-blank sanitized rationale, **accept and cache** the repaired blueprint instead of failing over to the next cloud provider and burning another full prompt.
     4. Only fail over to the next provider if the AI response failed JSON deserialization or produced an unrecoverable/blank rationale.

2. **Provider Timeout & Temperature Parity (`ByokAiProvider.kt`, `GemmaRestFallbackProvider.kt`):**
   - **`ByokAiProvider.kt`:** Configure its `OkHttpClient` with explicit 8-second timeouts matching its capability declaration:
     ```kotlin
     .connectTimeout(8, TimeUnit.SECONDS)
     .readTimeout(8, TimeUnit.SECONDS)
     .writeTimeout(8, TimeUnit.SECONDS)
     ```
   - **`GemmaRestFallbackProvider.kt`:** Change the `OkHttpClient` `readTimeout` from `180` seconds to `60` seconds (`readTimeout(60, TimeUnit.SECONDS)`) to match its declared 60s capability, and ensure `temperatureOverride` is forwarded on the proxy branch as well as the direct branch.

3. **Prompt Cache Delimiter & Upstream Day-Bucket Stabilization (`PromptCacheRepository.kt`, `RotationScoringUseCase.kt`):**
   - **`PromptCacheRepository.kt`:** In `generateFingerprint(...)`, join all input components using the ASCII Unit Separator `"\u001F"` (`listOf(...).joinToString("\u001F")`) before hashing with SHA-256 so adjacent fields cannot collide across boundaries.
   - **Preserve Manifest Rank Order:** Do **NOT** sort manifest lines alphabetically in `PromptCacheRepository` or `CompactManifestSerializer` (line order carries the candidate ranking signal).
   - **`RotationScoringUseCase.kt`:** Quantize timestamps (`currentTimeMillis` and item `lastWorn` timestamps) to UTC day-buckets (`timestampMillis / 86_400_000L * 86_400_000L` or integer day index `timestampMillis / 86_400_000L`) before calculating recency penalties so multiple requests within the same 24-hour calendar day produce identical rotation scores and manifest ordering.

---

## P2.5: Mandatory Unit Tests for Phase 2

Update existing unit tests for the `AiExecutionResult` signature change AND add dedicated Phase 2 unit test cases in `:applications:kocolor:data` (`StyleSimulatorEngineTest.kt`) and `:features:ai:local` (`PromptCacheRepositoryTest.kt`):
1. **`adaptContextToProvider` Single `countTokens` Test:** Verify that when `adaptContextToProvider` steps down from `EXPANDED` to `MINIMAL` and reduces `K`, `provider.countTokens()` is invoked **at most once** per provider attempt.
2. **Nano Fast-Fail & Compact Prompt Test:** Verify that `LocalNanoAiProvider` uses `assembleCompactNano()` and fast-fails immediately if the `MINIMAL` + `K = 4` floor exceeds 768 tokens.
3. **Validation Repair Without Failover Test:** Verify that if Provider 1 returns an ungrounded item ID alongside valid JSON, deterministic repair substitutes an eligible candidate and succeeds on Provider 1 **without** invoking Provider 2.
4. **Prompt Cache Delimiter Test:** Verify that `PromptCacheRepository` produces distinct SHA-256 keys for boundary-shifted inputs (e.g., `tier="A", version="BC"` vs. `tier="AB", version="C"`) and hits the cache on identical day-bucketed inputs.

---

## MANDATORY PHASE 2 VERIFICATION GATE (RUN & PASTE EXACT OUTPUT)

Do NOT stamp Phase 2 complete or write `Phase2_Audit_final.md` until every command below passes and its verbatim output is recorded in the audit document:

1. **Nano Compact Prompt & Fast-Fail Gate:**
```bash
grep -n "assembleCompactNano" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/PromptAssembler.kt applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt
```

2. **At-Most-One `countTokens` Call in `StyleSimulatorEngine.kt`:**
```bash
grep -n "countTokens" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt
```
*(Must show at most 1 call site to `provider.countTokens`, outside the step-down `while` loop condition).*
3. **`maxOutputTokens` Enforced in All 3 Cloud Providers:**
```bash
grep -rn "maxOutputTokens" features/ai/
```
*(Must show active request/config wiring in `FirebaseAiClient.kt`, `ByokAiProvider.kt`, and `GemmaRestFallbackProvider.kt`).*
4. **`AiExecutionResult` & Billed `TokenUsage` Wired End-to-End:**
```bash
grep -rn "AiExecutionResult\|TokenUsage\|candidatesTokenCount" features/ai/ applications/kocolor/data/
```
5. **Phase 1 Multimodal Rejection Guards Still Intact:**
```bash
grep -rn "UnsupportedOperationException" features/ai/core/ features/ai/firebase/
```
*(Must still show both `ByokAiProvider.kt` and `GemmaRestFallbackProvider.kt` rejecting `AiInput.Multimodal`).*
6. **Timeouts (8s BYOK / 60s Gemma), `temperatureOverride`, Cache Delimiter & Day-Bucketing:**
```bash
grep -rn "temperatureOverride\|readTimeout\|connectTimeout" features/ai/core/ features/ai/firebase/
grep -n "\\u001F" features/ai/local/src/main/java/com/zoewave/probase/features/ai/local/data/PromptCacheRepository.kt
grep -n "86_400_000\|86400000\|MILLISECONDS.toDays" applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RotationScoringUseCase.kt
```
7. **Full Gradle Test & Assemble Verification:**
```bash
./gradlew :features:ai:core:testDebugUnitTest :features:ai:firebase:testDebugUnitTest :features:ai:local:testDebugUnitTest :applications:kocolor:data:testDebugUnitTest :applications:kocolor:features:analyzer:testDebugUnitTest :applications:kocolor:apps:mobile:assembleDebug
```
````

### Why This Phase 2 Specification Prevents Agent Shortcuts

* **Protects Your Phase 1 Punchlist Fixes:** Explicitly checks via `grep` that changing `AiProvider.execute()` to return `Result<AiExecutionResult>` does not accidentally overwrite the `UnsupportedOperationException` multimodal guards in `ByokAiProvider` and `GemmaRestFallbackProvider`.
* **Closes the Calibration Edge Case in `adaptContextToProvider` (T2):** Instead of just telling the agent "use `length / 4` and call `countTokens` once" (which fails if the single exact `countTokens` call returns 2,100 tokens on a 2,048 budget), it gives the exact calibration-ratio fallback formula so the loop steps down one more notch using the calibrated ratio without making a second network RPC.
* **Resolves the `ValidationReport` vs. Repair Semantics (T3 + P1.3):** Clearly defines what deterministic repair can fix locally (ungrounded IDs, missing slots/roles) versus what must still trigger provider failover (malformed JSON or empty rationale), while testing that Provider 2 is never called when Provider 1's output is locally repairable.
