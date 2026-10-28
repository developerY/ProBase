The log data exposes a classic architectural entanglement where generative selection logic is masking what should be a pure computational evaluation. Analyzing the execution trace reveals exactly why the pipeline is currently functioning as an outfit synthesizer rather than an independent aesthetic judge.

**The Generative vs. Evaluative Divide**

* The trace clearly shows candidate pruning based on environmental context (21.09°C, UV 3.35) and a subsequent payload sent to the `firebase_ai_logic` tier.
* The identical scores generated (`0.85` for multiple khaki items) represent relational compatibility to a locked anchor (Camel Ankle Boots) during a retrieval phase, not a holistic aesthetic evaluation of a completed ensemble.
* This confirms the system is answering the generative prompt "What should I wear?" rather than executing the deterministic mathematical equation for "How harmonious is this specific visual combination?"

**The FASHIONISTA Engine Architecture**

* A true standalone scoring engine requires absolute decoupling from circumstantial inputs like weather, user intent, or closed wardrobe boundaries.
* Runtime execution must rely entirely on frozen mathematical parameters—calibrated offline via expert reference ensembles—eliminating LLM latency, token overhead, and non-deterministic text generation.
* Because provenance is irrelevant, the engine can flawlessly evaluate unstructured inputs, whether it is a live camera feed, a scraped social media image, or the output of the KoColor recommendation tier itself.

**Score vs. Coverage Semantics**

* Decoupling the absolute aesthetic score from the observable coverage matrix is a vital distinction.
* A flat-lay image lacking biometric integration (e.g., skin undertone, eye luminance) should simply yield a lower coverage confidence metric (`0.82`). This preserves the mathematical integrity of the outfit's baseline composition and texture harmony score without artificially penalizing it for missing physical data.

Your architectural deduction is entirely true. Forking the codebase to isolate the existing LLM-driven recommendation pathway from a pure, deterministic FASHIONISTA evaluation engine is exactly how this must be built.


---
---
This architecture document establishes the exact strict boundary required to decouple your generative AI from the deterministic scoring engine.

**State Flow Isolation for Compose**
Because you are managing a modern multi-module architecture utilizing Jetpack Compose, the UI recomposition must cleanly separate these two engines. Right now, your code snippet returns a single `fashionistaScore` float, but your specification rightly demands a `Coverage` metric and a 6-axis radar breakdown. You should update the engine's return type to a dedicated data class so the Compose UI can instantly react to the full matrix without triggering the AI state flows.

**Engine Return Contract**
Instead of a primitive float, the offline engine should emit a complete snapshot that your UI can observe directly.

```kotlin
data class FashionistaResult(
    val absoluteScore: Float,
    val coverage: Float,
    val radarBreakdown: RadarMetrics
)

data class RadarMetrics(
    val composition: Float,
    val colorHarmony: Float,
    val silhouette: Float,
    val textureHarmony: Float,
    val visualHierarchy: Float,
    val wearerIntegration: Float
)

```

**Refined ViewModel Implementation**
When the user taps to calibrate their skin, eye, or hair telemetry, emit the new state directly to your UI layer while bypassing the network entirely.

```kotlin
// 1. Pure Offline FASHIONISTA Score & Radar Calculation
val vector = FacialContrastVector(
    updatedTelemetry.skinLuminance,
    updatedTelemetry.hairLuminance,
    updatedTelemetry.eyeLuminance,
    updatedTelemetry.contrastDelta
)
val season = seasonClassifier.classify(vector, updatedTelemetry.undertoneScore)

// Returns the full FashionistaResult object, not just a Float
val fashionistaResult = colorHarmonyEngine.calculateFashionistaScore(
    finalOutfit = _recommendedClothing.value,
    finalCosmetics = _recommendedCosmetics.value,
    telemetry = ColorTelemetry(
        undertoneScore = updatedTelemetry.undertoneScore,
        contrastDelta = updatedTelemetry.contrastDelta,
        skinLuminance = updatedTelemetry.skinLuminance
    )
)

// 2. Update UI State Directly (Zero Network)
_fashionistaMetrics.value = fashionistaResult

```

**Mathematical Precision**
Your reference to the $L^*C^*h^\circ$ color space in the flow diagram is exactly right. By locking FASHIONISTA strictly to geometric contrast vectors and $L^*C^*h^\circ$ mathematical harmony, you guarantee that a photo of an outfit taken on the street will be scored using the exact same immutable logic as an outfit pulled directly from the internal wardrobe database.