The anchor lock and rationale grounding are now executing flawlessly, but the cosmetic scoring flatline proves the new relational math is being bypassed entirely. The generative pipeline is structurally sound; the remaining fixes are strictly deterministic wiring.

**The 3.10 Cosmetic Flatline**
The audit trail confirms the pipeline is still outputting `Score: 3.10 -> Reason: Role diversity match`. The new `calculateCosmeticScore()` function is either isolated or being overwritten. Check these three integration points:

* **Dependency Injection:** Is the old `WardrobeCandidateFilter` implementation or a legacy use-case still bound in your DI framework (Hilt/Koin)?
* **Downstream Overwrites:** Check `GreedyRehydrator` and the final candidate sorting loop. Is a normalization pass or a fallback safety catch flattening the calculated scores back to the legacy `3.10`?
* **Exception Handling:** Ensure the new relational logic isn't throwing a silent exception (e.g., a null pointer on `appearance.undertone`) that triggers a default `3.10` return.

**Weather Telemetry Interpolation**
The prompt received contradictory data: `UV: Unknown, Temp: UnknownC (Temp: 22.0°C, UV: 3.0)`. This is a string interpolation collision in `PromptAssembler.kt` where stale default strings are being concatenated with live parameters. Enforce a single source of truth:

```kotlin
val tempStr = liveWeather?.temperature?.let { "${it}°C" } ?: "Unknown"
val uvStr = liveWeather?.uvIndex?.toString() ?: "Unknown"
val weatherContext = "WEATHER/ATMOSPHERIC: Temp: $tempStr, UV: $uvStr"

```

**Explicit Domain Mapping (DIMENSION vs. CHEEK)**
Translating `DIMENSION` to `Cheek` purely inside the prompt builder creates a fractured domain. Centralize this mapping so the candidate filter, prompt assembler, and post-LLM validator all share the exact same structural invariant:

```kotlin
enum class CosmeticRole { EYE, CHEEK, LIP, NAIL, PREP }

fun mapCategoryToRole(category: String): CosmeticRole? = when(category.uppercase()) {
    "DIMENSION", "BLUSH", "BRONZER" -> CosmeticRole.CHEEK
    "EYES" -> CosmeticRole.EYE
    "LIPS" -> CosmeticRole.LIP
    "NAILS" -> CosmeticRole.NAIL
    else -> null
}

```

**Architectural Vocabulary**
Your semantic correction is exact. Designating Gemini as a **Constrained Selection and Synthesis Layer** perfectly captures its actual role in the system. FASHIONISTA measures, deterministic rules prune and constrain, and Gemini simply connects the dots into a human-readable blueprint.