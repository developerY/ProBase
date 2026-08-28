# Architectural Reference: FASHIONISTA Score Engine

This document provides a comprehensive technical overview and architectural breakdown of the KoColor **FASHIONISTA Score Engine** (`com.zoewave.probase.kocolor.fashionista`).

The FASHIONISTA engine is a deterministic, on-device computational aesthetics system that translates an observed outfit into a standardized **0.0–100.0 aesthetic score** using measurable visual evidence.

The architecture deliberately separates:

1. **Observation** — what can be measured from the input
2. **Feature extraction** — how measurable visual evidence is computed
3. **Scoring** — how evidence is mathematically combined
4. **Calibration** — how the normalized result maps to the 0–100 scale
5. **Explanation UI** — how the system transparently shows users *why* the score was produced

---

# 1. System Philosophy & Contract

FASHIONISTA is a standalone, purely deterministic computational evaluator.

It translates a `FashionistaObservation` into a **standardized, reference-calibrated aesthetic score** using deterministic measurements of the observed visual system as a whole.

```text
                         OBSERVED OUTFIT
                               │
                               ▼
                    FashionistaObservation
                               │
             ┌─────────────────┼─────────────────┐
             ▼                 ▼                 ▼
           COLOR           STRUCTURE          SURFACE
             │                 │                 │
             ▼                 ▼                 ▼
        Colorimetry       Composition          Texture
        CIEDE2000         Proportion            GLCM
        Hue Geometry      Layering       Order / Complexity
             │                 │                 │
             └─────────────────┼─────────────────┘
                               ▼
                       VISUAL HIERARCHY
                               │
                               ▼
                    PRESENTATION INTEGRATION
                       (Conditional/Optional)
                               │
                               ▼
                         FEATURE VECTOR
                  6 × FeatureValue(value, avail)
                               │
                               ▼
                       INTERACTION MODEL
                 Q_base + Q_interaction - P_unresolved
                               │
                               ▼
                       CALIBRATION CURVE
                    100 × σ((Q - μ) / τ)
                               │
                               ▼
                     FASHIONISTA SCORE
                           0.0–100.0
                               │
                ┌──────────────┴──────────────┐
                ▼                             ▼
        MACHINE-READABLE                 EXPLANATION
        FashionistaScore                     UI
                │                             │
                │                   "What was measured?"
                │                   "What was available?"
                │                   "What helped?"
                │                   "What conflicted?"
                │                   "Why this score?"
                ▼                             ▼
             API /                        User-facing
          application                       insight
```

## Core Architectural Principle

The **scoring engine does not depend on the UI**.

The UI consumes the already-computed `FashionistaScore` and its feature breakdown to explain the result.

```text
FashionistaObservation
          │
          ▼
  FashionistaScorer
          │
          ▼
 FashionistaScore
          │
          ├──────────────► Application Logic
          │
          └──────────────► Transparency / Explanation UI
```

This preserves deterministic scoring while allowing the product to make the mathematics understandable to users.

---

# 2. Critical Architectural Invariants

## 2.1 Zero AI / LLM / Network Dependency

The scoring engine:

- Executes 100% locally and on-device
- Makes zero network calls
- Uses zero LLMs
- Performs zero prompt assembly
- Has zero cloud dependencies
- Uses zero GenAI SDKs
- Does not depend on remote inference services

The result is therefore reproducible without requiring an internet connection or external model service.

---

## 2.2 Context-Free & Reference-Free Evaluation

FASHIONISTA evaluates the outfit **strictly as observed**.

The scoring engine has no knowledge of:

- User identity
- Wardrobe history
- Personal preferences
- Weather
- Occasion
- Calendar context
- Recommendation history
- Previous outfits
- Shopping intent

This is intentional.

FASHIONISTA answers:

> **"How coherent is this observed visual system?"**

It does not answer:

> "Is this the best outfit for this particular person today?"

Those are separate product-level concerns.

---

## 2.3 Immutable Versioned Calibration Standard

All scoring parameters are encapsulated in a versioned `FashionistaCalibration` object.

```text
standardId = "FASHIONISTA"
version    = 1
```

The calibration contains:

- Feature weights $w_i$
- Interaction weights $w_{ij}$
- Interaction coefficient $\lambda$
- Unresolved-conflict penalty
- Logistic midpoint $\mu$
- Logistic temperature $\tau$
- Minimum score bound $qMin$
- Maximum score bound $qMax$

Conceptually:

$$
\theta_{calibration}
=
\{
w_i,
w_{ij},
\lambda,
P_{unresolved},
\mu,
\tau,
qMin,
qMax
\}
$$

These parameters are derived offline from reference-rated ensembles.

The Android runtime therefore consumes a **frozen calibration standard**, rather than learning or modifying scoring parameters at runtime.

---

## 2.4 Dynamic Evidence Normalization

Missing evidence must not be confused with poor aesthetic quality.

FASHIONISTA therefore uses:

```kotlin
data class FeatureValue(
    val value: Double,
    val availability: Double
)
```

with:

$$
value \in [0,1]
$$

and:

$$
availability \in [0,1]
$$

For example, a flat-lay photograph may contain:

- Clothing: available
- Color: available
- Texture: available
- Silhouette: available
- Facial presentation: unavailable

The unavailable feature is therefore marked:

```text
value        = 0.0
availability = 0.0
```

rather than interpreting missing evidence as an aesthetic failure.

---

## 2.5 Weighted Measurement Coverage

The engine exposes:

$$
coverage
=
\frac{
\sum_i w_i a_i
}{
\sum_i w_i
}
$$

where:

- $w_i$ = feature weight
- $a_i$ = feature availability

Therefore:

$$
coverage \in [0,1]
$$

Coverage represents **how much of the outfit could actually be measured**.

It does **not** artificially reduce the aesthetic score.

This distinction is important:

```text
SCORE
"What did the available evidence indicate?"

COVERAGE
"How much evidence was available?"
```

A score of 87 with 75% coverage is fundamentally different from a score of 65 with 100% coverage.

---

## 2.6 Evidence, Not Law

Measurements such as:

- Vertical symmetry
- Color harmony templates
- CIEDE2000 distances
- GLCM complexity
- Visual center of gravity
- Texture order

are treated as **evidence/features**, not absolute definitions of good fashion.

Therefore:

- Asymmetry is not automatically bad.
- High contrast is not automatically bad.
- Unusual proportions are not automatically bad.
- High complexity is not automatically bad.
- Cultural or unconventional silhouettes are not automatically bad.

Penalties are reserved primarily for **unresolved perceptual conflicts** represented by:

$$
P_{unresolved}
$$

This allows the system to distinguish:

```text
Intentional stylistic tension
            ≠
Unresolved visual conflict
```

---

## 2.7 Thread Safety

The engine provides a synchronous, thread-safe Kotlin contract suitable for execution on background coroutine workers.

The scoring engine should not be executed directly on the Android main/UI thread when processing large observations.

---

# 3. Package Architecture

```text
:applications:kocolor:fashionista
│
├── domain
│   ├── FashionistaObservation.kt
│   ├── FeatureValue.kt
│   ├── FashionistaFeatureVector.kt
│   ├── FashionistaScore.kt
│   └── FashionistaScorer.kt
│
├── math
│   ├── Statistics.kt
│   ├── CircularStatistics.kt
│   ├── Geometry.kt
│   ├── Distance.kt
│   ├── Normalization.kt
│   └── Logistic.kt
│
├── extraction
│   ├── ColorFeatureExtractor.kt
│   ├── CompositionFeatureExtractor.kt
│   ├── SilhouetteFeatureExtractor.kt
│   ├── TextureFeatureExtractor.kt
│   ├── PatternFeatureExtractor.kt
│   └── HierarchyFeatureExtractor.kt
│
├── color
│   ├── ColorSpace.kt
│   ├── Ciede2000.kt
│   ├── CircularHueStatistics.kt
│   └── ChromaticHarmonyEngine.kt
│
├── composition
│   └── CompositionEngine.kt
│
├── silhouette
│   ├── VisualMassEngine.kt
│   └── SilhouetteEngine.kt
│
├── texture
│   ├── GlcmTextureEngine.kt
│   └── TextureHarmonyEngine.kt
│
├── hierarchy
│   └── VisualHierarchyEngine.kt
│
├── integration
│   ├── ItaCalculator.kt
│   ├── CosmeticIntegrationEngine.kt
│   └── OutfitIntegrationEngine.kt
│
└── scoring
    ├── FashionistaCalibration.kt
    ├── InteractionModel.kt
    ├── DeterministicScorer.kt
    ├── CalibrationCurve.kt
    └── FashionistaScorerImpl.kt
```

The transparency UI should remain outside this core package.

Conceptually:

```text
Fashionista Engine
        │
        ▼
FashionistaScore
        │
        ▼
Application / Presentation Layer
        │
        ▼
Fashionista Explanation UI
```

This prevents presentation concerns from contaminating the deterministic scoring contract.

---

# 4. Domain Contracts

## `FashionistaObservation`

Represents the measurable visual input.

It may contain:

- Extracted garments
- Cosmetic regions
- Color samples
- Spatial mass maps
- Texture observations
- Pattern information
- Optional facial regions
- Optional skin-color observations

---

## `FeatureValue`

```kotlin
data class FeatureValue(
    val value: Double,
    val availability: Double
)
```

The contract enforces:

$$
0 \le value \le 1
$$

and:

$$
0 \le availability \le 1
$$

---

## `FashionistaFeatureVector`

Contains the six principal perceptual measurements:

```text
composition
colorHarmony
silhouette
textureHarmony
visualHierarchy
presentationIntegration
```

Each is represented by a `FeatureValue`.

---

## `FashionistaScore`

```kotlin
data class FashionistaScore(
    val score: Double,
    val coverage: Double,
    val standardId: String,
    val standardVersion: Int,
    val breakdown: FashionistaFeatureVector
)
```

This object is particularly important for the UI because it provides both:

1. The final score
2. The evidence breakdown required to explain it

---

## `FashionistaScorer`

```kotlin
interface FashionistaScorer {
    fun score(
        outfit: FashionistaObservation
    ): FashionistaScore
}
```

The contract is synchronous and deterministic.

---

# 5. Pure Mathematics Primitives

## `Statistics.kt`

Provides:

- Mean
- Weighted mean
- Variance
- Standard deviation

---

## `CircularStatistics.kt`

Computes chroma-weighted circular hue statistics.

Given chroma $C^*$ and hue $h$:

$$
x
=
\sum
(C^* \cos h)
$$

$$
y
=
\sum
(C^* \sin h)
$$

The dominant hue is then:

$$
\bar{h}
=
\operatorname{atan2}(y,x)
$$

Low-chroma neutrals:

$$
C^* < 10
$$

do not distort the dominant hue calculation.

They can still contribute to lightness and neutral-contrast analysis.

---

## `Geometry.kt`

Provides visual geometry calculations including the Visual Center of Gravity:

$$
\bar{x}
=
\frac{
\sum m_i x_i
}{
\sum m_i
}
$$

$$
\bar{y}
=
\frac{
\sum m_i y_i
}{
\sum m_i
}
$$

---

## `Distance.kt`

Contains the CIEDE2000 perceptual color-distance implementation:

$$
\Delta E_{00}
$$

---

## `Normalization.kt`

Provides:

- Safe division
- Clamping
- Min-max normalization
- Boundary-safe feature scaling

---

## `Logistic.kt`

Implements the calibrated logistic function:

$$
F(Q,\mu,\tau)
=
100
\left(
\frac{1}
{
1+e^{-\frac{Q-\mu}{\tau}}
}
\right)
$$

---

# 6. Feature Engines

## 6.1 Chromatic Harmony Engine

Located under:

```text
color/
```

The color engine performs:

```text
RGB
 │
 ▼
CIELAB
 │
 ▼
L*C*h°
 │
 ├── CIEDE2000 distances
 ├── Chroma distribution
 ├── Hue dispersion
 └── Circular statistics
```

The resulting evidence contributes to:

$$
S_{colorHarmony}
$$

---

# 6.2 Composition Engine

The composition engine evaluates structural relationships between garment categories.

Examples include:

- Outerwear over tops
- Bottom-to-top compatibility
- Dress-to-footwear compatibility
- Outfit completeness

A simplified completeness model may recognize structures such as:

```text
Top + Bottom + Shoes
```

or:

```text
Dress + Shoes
```

The engine does not require a single canonical outfit structure.

---

# 6.3 Silhouette & Visual Mass Engine

`VisualMassEngine.kt` uses a lightweight native Android bitmap representation.

The image is reduced to:

```text
64 × 128
```

and analyzed to calculate visual mass distribution.

The resulting center of gravity is:

$$
CoG
=
(\bar{x},\bar{y})
$$

This allows lightweight structural analysis without introducing a heavyweight computer-vision dependency.

---

# 6.4 Texture Harmony Engine

`GlcmTextureEngine.kt` extracts GLCM statistics.

The implementation explicitly avoids literal Birkhoff division:

$$
M = \frac{O}{C}
$$

because direct division can become numerically unstable when complexity approaches zero.

Instead, the implementation maintains bounded normalized measures such as:

```text
Order      ∈ [0,1]
Complexity ∈ [0,1]
```

This produces a safer computational representation of texture organization.

---

# 6.5 Visual Hierarchy Engine

The hierarchy engine evaluates the relationship between:

```text
Primary focal point
        ↓
Secondary elements
        ↓
Tertiary elements
```

High visual complexity without a coherent focal hierarchy may generate an unresolved-conflict penalty:

$$
P_{unresolved}
$$

Importantly, the engine does not assume that minimalism is inherently superior to maximalism.

The question is whether the visual complexity is **organized**.

---

# 6.6 Presentation Integration Engine

The presentation integration subsystem evaluates optional wearer-related evidence.

### Individual Typology Angle

$$
ITA
=
\left[
\arctan
\left(
\frac{L^*-50}{b^*}
\right)
\right]
\frac{180}{\pi}
$$

### Facial Contrast

$$
C_f
=
\frac{
L_{skin}-L_{feature}
}{
L_{skin}+L_{feature}
}
$$

These measurements may contribute to:

$$
S_{presentationIntegration}
$$

when the required evidence exists.

---

## Null-Safe Biometric Bypass

For an outfit-only or flat-lay observation:

```text
presentationIntegration.value        = 0.0
presentationIntegration.availability = 0.0
```

The biometric calculation is bypassed.

This means:

```text
No face
   ↓
No biometric evidence
   ↓
No biometric penalty
   ↓
Lower coverage
   ↓
No artificial score corruption
```

---

# 7. Bounded Nonlinear Scoring Engine

The scoring subsystem consists of:

```text
FashionistaCalibration
        │
        ▼
InteractionModel
        │
        ▼
DeterministicScorer
        │
        ▼
CalibrationCurve
        │
        ▼
FashionistaScore
```

---

# 8. Base Score

For feature values $x_i$, availability $a_i$, and feature weights $w_i$:

$$
Q_{base}
=
\frac{
\sum_i w_i x_i a_i
}{
\sum_i w_i a_i
}
$$

This means unavailable evidence contributes neither positively nor negatively.

---

# 9. Interaction Model

Cross-feature interactions are represented as:

$$
I_{ij}
=
x_i x_j
$$

for available features.

The interaction numerator is:

$$
Q_{int\_num}
=
\sum_{i<j}
w_{ij}
x_i x_j
a_i a_j
$$

and the interaction denominator is:

$$
Q_{int\_den}
=
\sum_{i<j}
w_{ij}
a_i a_j
$$

---

# 10. Interaction Denominator Fail-Safe

If:

$$
Q_{int\_den}=0
$$

—for example, when only one feature is available—the engine performs:

```text
effectiveLambda = 0.0
Q_interaction   = Q_base
```

This prevents:

- Division by zero
- NaN propagation
- Artificial interaction terms
- Invalid scores

Otherwise:

$$
Q_{interaction}
=
\frac{
Q_{int\_num}
}{
Q_{int\_den}
}
$$

and:

$$
effectiveLambda
=
\lambda
$$

---

# 11. Blended Score

The base and interaction components are combined:

$$
Blended
=
(1-\lambda_{eff})Q_{base}
+
\lambda_{eff}Q_{interaction}
$$

The unresolved-conflict penalty is then applied:

$$
Q
=
\operatorname{coerceIn}
\left(
Blended-P_{unresolved},
qMin,
qMax
\right)
$$

with:

$$
qMin=0
$$

and:

$$
qMax=1
$$

---

# 12. Zero-Availability Fail-Safe

If:

$$
\sum_i w_i a_i=0
$$

the observation contains no measurable evidence.

The engine therefore returns:

```text
Q        = 0.0
coverage = 0.0
```

without throwing an exception or producing:

```text
NaN
Infinity
```

---

# 13. Calibration Curve

The final normalized score is mapped to the 0–100 scale using:

$$
F_{score}
=
100
\cdot
\sigma
\left(
\frac{Q-\mu}{\tau}
\right)
$$

The calibration parameters are:

```text
μ   = 0.50
τ   = 0.20
```

The resulting score is:

$$
F_{score}\in[0,100]
$$

---

# 14. Score Interpretation

The UI can expose the calibrated score using the following interpretation bands:

| Score | Interpretation |
|---:|---|
| **95–100** | Exceptional / Editorial |
| **90–94** | Outstanding |
| **80–89** | Excellent |
| **70–79** | Strong |
| **55–69** | Competent |
| **40–54** | Weak |
| **0–39** | Visually Unsuccessful |

These labels describe the calibrated score distribution and should not be interpreted as universal judgments of personal taste.

---

# 15. Transparency & Explanation UI

The FASHIONISTA UI is an important architectural component because it exposes the **reasoning structure of the deterministic engine without exposing implementation complexity to the user**.

The UI should not simply display:

```text
87 / 100
```

It should explain:

```text
87 / 100
Strong

Why?

Color Harmony       91
Composition         88
Silhouette          84
Texture Harmony     82
Visual Hierarchy    89
Presentation        —
```

The UI can then show what evidence was actually available.

---

## 15.1 Recommended User-Facing Model

The explanation UI should translate the internal feature vector into human-readable evidence.

```text
┌─────────────────────────────────────┐
│                                     │
│             87 / 100                │
│              STRONG                 │
│                                     │
│       FASHIONISTA · v1              │
│                                     │
├─────────────────────────────────────┤
│                                     │
│  COLOR HARMONY              91      │
│  Strong hue relationship             │
│                                     │
│  COMPOSITION                88      │
│  Categories work together            │
│                                     │
│  SILHOUETTE                 84      │
│  Visual mass is well distributed     │
│                                     │
│  TEXTURE HARMONY            82      │
│  Complexity remains organized        │
│                                     │
│  VISUAL HIERARCHY            89      │
│  Clear focal structure               │
│                                     │
│  PRESENTATION INTEGRATION     —      │
│  Not measurable from this image      │
│                                     │
├─────────────────────────────────────┤
│                                     │
│  Measurement Coverage        83%     │
│                                     │
│  The score uses only evidence        │
│  available in the submitted image.   │
│                                     │
└─────────────────────────────────────┘
```

---

# 16. Coverage Explanation

The UI should explicitly distinguish **score** from **measurement coverage**.

For example:

```text
Score
87 / 100

Coverage
83%

Meaning:
The available visual evidence produced a strong
score, while approximately 83% of the weighted
measurement system was available.
```

This prevents users from interpreting missing information as poor styling.

---

# 17. Feature-Level Explainability

Each `FeatureValue` contains:

```text
value
availability
```

The UI can therefore distinguish three important states.

### Measured

```text
Color Harmony
91
Available
```

### Partially Measured

```text
Presentation
72
Partially Available
```

### Not Measurable

```text
Presentation
—
Not measurable from this image
```

The third state is particularly important.

The UI should **not** display:

```text
Presentation Integration
0
```

when the underlying reason is simply that no facial evidence exists.

Instead:

```text
Presentation Integration
Not measured
No wearer/face evidence detected
```

---

# 18. Showing the Mathematics Without Overwhelming the User

The UI can provide progressive disclosure.

### Level 1 — Simple

```text
87
Strong
```

### Level 2 — Feature Breakdown

```text
Color Harmony       91
Composition         88
Silhouette          84
Texture             82
Hierarchy           89
Presentation         —
```

### Level 3 — Evidence Explanation

```text
Color Harmony
Strong

• Low hue dispersion
• Compatible chroma distribution
• Low average ΔE00 conflict
• Neutral colors did not distort hue analysis
```

### Level 4 — Technical Detail

For technically interested users:

```text
CIEDE2000
ΔE00 = 3.1

Hue dispersion
σh = 18.4°

Chroma-weighted dominant hue
h̄ = 27.8°
```

This preserves accessibility while making the system genuinely transparent.

---

# 19. What the UI Should Not Claim

The UI should avoid language such as:

> "This outfit is objectively beautiful."

Instead, use language such as:

> "The measured visual evidence produces a strong FASHIONISTA score."

Likewise:

```text
Avoid:
"This is bad fashion."

Prefer:
"The observed visual system contains unresolved
composition or hierarchy conflicts."
```

This distinction is consistent with the engine's **Evidence, Not Law** invariant.

---

# 20. Recommended Explanation Model

A useful explanation object at the application layer can conceptually look like:

```kotlin
data class FashionistaExplanation(
    val score: Double,
    val coverage: Double,
    val interpretation: String,
    val features: List<FeatureExplanation>
)

data class FeatureExplanation(
    val name: String,
    val value: Double?,
    val availability: Double,
    val title: String,
    val explanation: String
)
```

The deterministic engine remains responsible for the actual score.

The presentation layer is responsible for translating that score into language.

```text
ENGINE
──────

FeatureValue
FashionistaFeatureVector
FashionistaScore


PRESENTATION
────────────

FeatureExplanation
FashionistaExplanation
UI components
Human-readable descriptions
```

This keeps the scoring core mathematically clean.

---

# 21. Complete End-to-End Architecture

```text
                           INPUT
                             │
                             ▼
                 FashionistaObservation
                             │
                             ▼
                    FEATURE EXTRACTION
                             │
       ┌─────────────────────┼─────────────────────┐
       │                     │                     │
       ▼                     ▼                     ▼
    COLOR                STRUCTURE              SURFACE
       │                     │                     │
       ▼                     ▼                     ▼
  CIEDE2000              Composition              GLCM
  Hue Geometry           Silhouette              Texture
  Chroma                 Visual Mass             Pattern
       │                     │                     │
       └─────────────────────┼─────────────────────┘
                             │
                             ▼
                    VISUAL HIERARCHY
                             │
                             ▼
                  PRESENTATION INTEGRATION
                     (Optional Evidence)
                             │
                             ▼
                  FashionistaFeatureVector
                             │
                             ▼
                    DETERMINISTIC SCORER
                             │
                ┌────────────┼────────────┐
                ▼            ▼            ▼
             Q_base     Q_interaction   Coverage
                │            │
                └──────┬─────┘
                       ▼
                Unresolved Penalty
                       │
                       ▼
                    Q ∈ [0,1]
                       │
                       ▼
                Logistic Calibration
                       │
                       ▼
                FASHIONISTA SCORE
                   0.0 – 100.0
                       │
                       ▼
              FashionistaScore
                       │
              ┌────────┴─────────┐
              ▼                  ▼
        APPLICATION          EXPLANATION
           LOGIC                 UI
              │                  │
              │                  ▼
              │          "How did we get
              │             this score?"
              │                  │
              └─────────► User ◄─┘
```

---

# 22. Verification Matrix & Test Coverage

The subsystem is tested in:

```text
FashionistaScorerTest.kt
```

with the current test suite reporting:

```text
35 passing unit tests project-wide
```

The verification suite covers the following invariants.

## 22.1 Mathematical Bounds

Asserts:

$$
score \in [0,100]
$$

and:

$$
coverage \in [0,1]
$$

---

## 22.2 Dynamic Evidence Normalization

Flat-lay observations without facial biometrics must produce:

```text
presentationIntegration.availability = 0.0
```

without corrupting the raw aesthetic score.

---

## 22.3 Single-Feature Interaction Denominator

Tests:

$$
Q_{int\_den}=0
$$

and verifies that:

```text
effectiveLambda = 0
```

with no division-by-zero failure.

---

## 22.4 Zero-Availability Fail-Safe

An empty:

```text
FashionistaObservation
```

must return:

```text
score    = 0.0
coverage = 0.0
```

without:

- Exceptions
- NaN
- Infinity
- Arbitrary default feature values

---

## 22.5 Deterministic Replicability

Identical `FashionistaObservation` instances must produce identical `FashionistaScore` outputs across thread invocations.

Conceptually:

$$
Observation_A = Observation_B
$$

therefore:

$$
Score_A = Score_B
$$

The result must not depend on:

- Thread scheduling
- UI state
- Network availability
- User history
- Random seeds
- LLM inference

---

# 23. Architectural Separation of Responsibilities

The complete system should maintain the following boundary:

| Layer | Responsibility | Deterministic? |
|---|---|---:|
| `FashionistaObservation` | Represent measurable input | Yes |
| Feature Extractors | Extract visual evidence | Yes |
| Math Engines | Perform mathematical calculations | Yes |
| `FashionistaFeatureVector` | Represent normalized evidence | Yes |
| `DeterministicScorer` | Combine evidence | Yes |
| `CalibrationCurve` | Convert $Q$ to 0–100 | Yes |
| `FashionistaScore` | Publish result | Yes |
| Application Layer | Consume score | Depends |
| Explanation Layer | Translate score into human-readable information | Yes, if rule-based |
| UI | Visualize result and evidence | No scoring responsibility |

The critical rule is:

> **The UI explains the score; the UI does not determine the score.**

---

# 24. Final Architectural Contract

The FASHIONISTA engine can therefore be summarized as:

$$
\boxed{
FashionistaObservation
\rightarrow
FeatureExtraction
\rightarrow
FeatureVector
\rightarrow
InteractionModel
\rightarrow
DeterministicScore
\rightarrow
Calibration
\rightarrow
FashionistaScore
}
$$

with:

$$
\boxed{
F_{score}
=
100
\cdot
\sigma
\left(
\frac{
Q-\mu
}{
\tau
}
\right)
}
$$

and:

$$
\boxed{
Q
=
\operatorname{clip}
\left(
(1-\lambda_{eff})Q_{base}
+
\lambda_{eff}Q_{interaction}
-
P_{unresolved},
qMin,
qMax
\right)
}
$$

where:

$$
\boxed{
Q_{base}
=
\frac{
\sum_i w_i x_i a_i
}{
\sum_i w_i a_i
}
}
$$

and:

$$
\boxed{
coverage
=
\frac{
\sum_i w_i a_i
}{
\sum_i w_i
}
}
$$

The resulting architecture provides four important properties simultaneously:

1. **Determinism** — identical evidence produces identical scores.
2. **Evidence-awareness** — unavailable measurements do not become artificial penalties.
3. **Interpretability** — the six perceptual dimensions can be exposed to the user.
4. **Transparency** — the UI can demonstrate how the score was constructed without becoming part of the scoring algorithm.

---

# Conclusion

The KoColor FASHIONISTA Score Engine is designed as a **deterministic computational measurement system**, not an AI opinion generator.

Its architecture combines:

- CIEDE2000 perceptual color measurement
- Circular hue statistics
- Composition analysis
- Visual-mass geometry
- Silhouette analysis
- GLCM texture analysis
- Visual hierarchy
- Optional presentation integration
- Cross-feature interactions
- Versioned calibration
- Dynamic evidence availability
- Explicit unresolved-conflict penalties
- Bounded nonlinear score calibration

The addition of a dedicated explanation UI significantly strengthens the product architecture.

Rather than presenting an unexplained number such as:

```text
87 / 100
```

KoColor can show:

```text
87 / 100
STRONG

Color Harmony       91
Composition         88
Silhouette          84
Texture Harmony     82
Visual Hierarchy    89
Presentation         —

Coverage            83%

Why?
The available visual evidence indicates strong
color coordination, coherent composition, and
a clear focal hierarchy. Presentation integration
was not measured because no wearer/face evidence
was available.
```

This creates a critical distinction:

> **FASHIONISTA does not ask users to trust a mysterious score. It lets them inspect the evidence behind the score.**

The resulting product architecture is therefore:

```text
          MEASURE
             │
             ▼
          SCORE
             │
             ▼
          EXPLAIN
             │
             ▼
          EDUCATE
             │
             ▼
          EMPOWER
```

The engine remains frozen, deterministic, and testable, while the UI turns the underlying mathematics into an understandable visual experience.