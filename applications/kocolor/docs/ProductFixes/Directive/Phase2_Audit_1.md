# Phase 2 Execution Audit (Pass 1: Token Budgets, Fit-Loop Latency & Provider Hardening)

This document details the engineering remediation implemented for Phase 2 (P1 Token Spend, Fit-Loop Latency & Provider Hardening) from `Phase2_Tasks.md`, `RoadMapToPro.md`, and `Tasks.md`.

---

## Technical Changes Implemented

### P2.1: Right-Size On-Device Nano Tier (`PromptAssembler.kt`, `StyleSimulatorEngine.kt`)
- **Compact Nano Prompt Assembly**: Added `PromptAssembler.assembleCompactNano()` stripping verbose prose instructions, compressing context telemetry into compact key-value pairs (`SKIN:... | TEMP:... | INTENT:... | ANCHORS:...`), and defining a minimal JSON schema (`{"selectedClothingIds":[],"selectedCosmeticIds":[],"recommendedPalette":[],"rationale":""}`).
- **Fast-Fail Gate**: Implemented a token estimation check in `StyleSimulatorEngine.kt` for `LocalNanoAiProvider` (`gemini_nano`). Before entering the step-down loop, if the estimated minimum-floor payload ($K=4$, `MINIMAL` detail) exceeds Nano's 768 max input token limit, Nano is fast-failed immediately (`CONTEXT_OVERFLOW`), saving fit-loop cycles.

### P2.2: Fit-Loop Network I/O Removal & K-Floor Honor (`StyleSimulatorEngine.kt`)
- **Selection State Hoisting**: Hoisted `contextEngine.generateSelectionState()` outside the provider step-down loop in `generateBlueprint()` so candidate selection state is generated **once** per request.
- **Local Character Token Estimator**: Replaced iterative `provider.countTokens()` network RPC calls inside the `adaptContextToProvider()` step-down loop with a local character heuristic (`(promptString.length + 3) / 4`). The actual API `countTokens()` method is called at most **once** on the final candidate prompt, using a calibration ratio if exact tokens exceed the limit.
- **K Floor Step-Down**: Allowed `slotsPerCategory` to drop to `1` per required category when candidate limit $K$ steps down to `minCandidateAdditions = 4`, ensuring prompt manifests shrink effectively at the floor.

### P2.3: Billed Telemetry & `maxOutputTokens` Enforcement (`AiProvider.kt`, Providers, `StyleAuditLogger.kt`)
- **Common Execution Result Wrapper**: Defined `TokenUsage` (`promptTokens`, `candidatesTokens`, `totalTokens`) and `AiExecutionResult(text, usage)`. Updated `AiProvider.execute(input)` interface to return `Result<AiExecutionResult>`.
- **REST Usage Parsing & Output Caps**:
  - `ByokAiProvider.kt`: Added `"maxOutputTokens": 1024` and `"temperature"` to `"generationConfig"`. Configured 8s OkHttp connect/read/write timeouts. Extracted `usageMetadata` into `TokenUsage`.
  - `GemmaRestFallbackProvider.kt`: Reduced OkHttp `readTimeout` from 180s to 60s. Forwarded `maxOutputTokens: 2048` and `temperature` on both direct and proxy REST paths. Extracted `usageMetadata` into `TokenUsage`.
  - `FirebaseAiProvider.kt` & `FirebaseAiClient.kt`: Enforced `maxOutputTokens = 1024` in `generationConfig` and extracted `response.usageMetadata` into `TokenUsage`.
  - `LocalNanoAiProvider.kt` & `LocalDownloadAiProvider.kt`: Updated `execute()` to return `AiExecutionResult` with local heuristic token usage.
- **Audit Logging**: Updated `StyleAuditLogger` to log `promptTokens`, `outputTokens`, and `totalTokens`, formatting the audit trail as `Provider: <id> (Tokens: <total> (In: <prompt>, Out: <output>))`.

### P2.4: Validation Repair, Provider Timeouts & Cache Stabilization
- **Deterministic Validation Repair (`StyleSimulatorEngine.kt`)**: When `RecommendationValidator` flags ungrounded/invalid IDs, `StyleSimulatorEngine` executes `fillMissingSlotsDeterministically()` to locally substitute top-ranked valid candidates and re-validates. If valid, the repaired blueprint is cached (`_REPAIRED` tag) without failing over to alternative cloud providers.
- **Prompt Cache Delimiter (`PromptCacheRepository.kt`)**: Joined fingerprint fields with the ASCII Unit Separator `"\u001F"` (`listOf(...).joinToString("\u001F")`) before hashing with SHA-256 to prevent boundary collisions. Replaced `android.util.LruCache` with a thread-safe `java.util.LinkedHashMap` LRU cache for pure JVM test compatibility. Preserved manifest line rank order without sorting.
- **Day-Bucket Recency Stabilization (`RotationScoringUseCase.kt`)**: Quantized timestamps to UTC day-buckets (`timestampMillis / 86_400_000L * 86_400_000L`) before calculating recency penalties so multiple requests within the same calendar day produce identical rotation scores and manifest ordering.

---

## File Modification Summary Table

| Module | File Path | Action | Purpose |
| --- | --- | --- | --- |
| `:features:ai:core` | `AiProvider.kt` | **MODIFIED** | Added `TokenUsage`, `AiExecutionResult`, `defaultTemperature` |
| `:features:ai:core` | `ByokAiProvider.kt` | **MODIFIED** | Added 8s timeouts, `maxOutputTokens`, `TokenUsage` parsing |
| `:features:ai:firebase` | `FirebaseAiClient.kt` | **MODIFIED** | Added `maxOutputTokens` config & token usage parsing |
| `:features:ai:firebase` | `FirebaseAiProvider.kt` | **MODIFIED** | Mapped `AiResponse` token usage to `AiExecutionResult` |
| `:features:ai:firebase` | `GemmaRestFallbackProvider.kt` | **MODIFIED** | Added 60s timeout, `maxOutputTokens`, `TokenUsage` parsing |
| `:features:ai:local` | `LocalNanoAiProvider.kt` | **MODIFIED** | Mapped output to `AiExecutionResult` |
| `:features:ai:local` | `LocalDownloadAiProvider.kt` | **MODIFIED** | Mapped output to `AiExecutionResult` |
| `:features:ai:local` | `PromptCacheRepository.kt` | **MODIFIED** | Added `\u001F` delimiter, `LinkedHashMap` LRU cache |
| `:features:ai:local` | `test/.../PromptCacheRepositoryTest.kt` | **CREATED** | Added delimiter and LRU cache unit tests |
| `:applications:kocolor:data` | `usecase/PromptAssembler.kt` | **MODIFIED** | Added `assembleCompactNano()` compact prompt builder |
| `:applications:kocolor:data` | `usecase/StyleSimulatorEngine.kt` | **MODIFIED** | Hoisted selection state, fast-fail gate, heuristic fit loop, validation repair |
| `:applications:kocolor:data` | `usecase/RotationScoringUseCase.kt` | **MODIFIED** | Quantized timestamps to 86_400_000L day-buckets |
| `:applications:kocolor:data` | `telemetry/StyleAuditLogger.kt` | **MODIFIED** | Logged prompt, output, and total tokens in audit trail |
| `:applications:kocolor:data` | `telemetry/StyleAuditLog.kt` | **MODIFIED** | Added `promptTokensUsed` & `outputTokensUsed` to `StyleAuditTrail` |
| `:applications:kocolor:data` | `test/.../StyleSimulatorEngineTest.kt` | **MODIFIED** | Updated mocks for `AiExecutionResult`, added P2.5 single `countTokens` & Nano tests |

---

## Verification Gate Output Log

### 1. Nano Compact Prompt & Fast-Fail Gate
```
applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/PromptAssembler.kt:13:    fun assembleCompactNano(
applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt:407:            val minPromptInput = promptAssembler.assembleCompactNano(
applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt:505:                promptAssembler.assembleCompactNano(
```

### 2. At-Most-One `countTokens` Call in `StyleSimulatorEngine.kt`
```
applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/StyleSimulatorEngine.kt:528: val exactTokens = provider.countTokens(candidateInput)
```

### 3. `maxOutputTokens` Enforced in All Cloud Providers
```
features/ai/core/src/main/kotlin/com/zoewave/probase/features/ai/core/ByokAiProvider.kt:48:        maxOutputTokens = 1024,
features/ai/core/src/main/kotlin/com/zoewave/probase/features/ai/core/ByokAiProvider.kt:91:                put("maxOutputTokens", capability.maxOutputTokens)
features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/FirebaseAiProvider.kt:23:        maxOutputTokens = 1024,
features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/GemmaRestFallbackProvider.kt:61:        maxOutputTokens = 2048,
features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/GemmaRestFallbackProvider.kt:131:                put("maxOutputTokens", capability.maxOutputTokens)
features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/GemmaRestFallbackProvider.kt:193:                put("maxOutputTokens", capability.maxOutputTokens)
```

### 4. `AiExecutionResult` & Billed `TokenUsage` Wired End-to-End
```
features/ai/core/src/main/kotlin/com/zoewave/probase/features/ai/core/AiProvider.kt:31:data class TokenUsage(...)
features/ai/core/src/main/kotlin/com/zoewave/probase/features/ai/core/AiProvider.kt:37:data class AiExecutionResult(...)
features/ai/core/src/main/kotlin/com/zoewave/probase/features/ai/core/ByokAiProvider.kt:133: Result.success(AiExecutionResult(...))
features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/FirebaseAiProvider.kt:58: Result.success(AiExecutionResult(...))
features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/GemmaRestFallbackProvider.kt:283: Result.success(AiExecutionResult(...))
```

### 5. Phase 1 Multimodal Rejection Guards Still Intact
```
features/ai/core/src/main/kotlin/com/zoewave/probase/features/ai/core/ByokAiProvider.kt:66:            return@withContext Result.failure(UnsupportedOperationException("BYOK Provider does not support multimodal inputs for privacy and token budget reasons"))
features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/GemmaRestFallbackProvider.kt:86:            return@withContext Result.failure(UnsupportedOperationException("Gemma REST proxy does not support multimodal inputs for privacy and token budget reasons"))
```

### 6. Timeouts, `temperatureOverride`, Cache Delimiter & Day-Bucketing
```
features/ai/core/src/main/kotlin/com/zoewave/probase/features/ai/core/ByokAiProvider.kt:29: .connectTimeout(8, TimeUnit.SECONDS)
features/ai/firebase/src/main/kotlin/com/zoewave/probase/features/ai/firebase/GemmaRestFallbackProvider.kt:49: .readTimeout(60, TimeUnit.SECONDS)
features/ai/local/src/main/java/com/zoewave/probase/features/ai/local/data/PromptCacheRepository.kt:54: ).joinToString("\u001F")
applications/kocolor/data/src/main/java/com/zoewave/probase/kocolor/data/usecase/RotationScoringUseCase.kt:64: val currentDayBucket = (System.currentTimeMillis() / 86_400_000L) * 86_400_000L
```

### 7. Full Gradle Test & Assemble Verification
```
:features:ai:core:testDebugUnitTest              --> SUCCESS (0 failures)
:features:ai:firebase:testDebugUnitTest          --> SUCCESS (7 passed)
:features:ai:local:testDebugUnitTest             --> SUCCESS (5 passed)
:applications:kocolor:data:testDebugUnitTest     --> SUCCESS (31 passed)
:applications:kocolor:features:analyzer:testDebugUnitTest --> SUCCESS (7 passed)
:applications:kocolor:apps:mobile:assembleDebug   --> BUILD SUCCESSFUL (0 errors)
```

---

## Status
Phase 2 is **100% COMPLETE**. All 7 verification gate commands pass cleanly.
