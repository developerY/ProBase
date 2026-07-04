````markdown
# Build an AI-First Product Discovery Pipeline for KoColor

You are a Senior Android Architect specializing in Kotlin, Jetpack Compose, Android AI, Room, Hilt, Coroutines, and Clean Architecture.

Your task is to design and implement a production-ready AI-first product discovery pipeline for KoColor.

## Core Philosophy

Traditional systems perform:

OCR → Regex → Database

KoColor performs:

Image → OCR → Local Gemini → Canonical Product Object → Web Gemini → Database

The goal is not merely to extract text from a package.

The goal is to understand the product.

The local AI layer should normalize and structure product information from OCR text.

The cloud AI layer should enrich that structured object using real-world product knowledge.

The system must be privacy-first, offline-capable, and progressively enhanced.

---

# Architecture

## Stage 1: Image Capture

Input:

```kotlin
Bitmap
```

The image may originate from:

- CameraX
- Gallery import
- Box Capture workflow

Output:

```kotlin
Bitmap
```

---

## Stage 2: OCR

Extract raw text from the image.

Output:

```kotlin
data class OcrResult(
    val rawText: String
)
```

Example:

```text
L'OREAL PAR1S
REVITAL1FT
TR1PLE POWER
ANTI-AGING SERUM
1 FL OZ
```

---

## Stage 3: Local Gemini (Primary AI Pipeline)

If an on-device model is available:

```text
OCR
 ↓
Local Gemini
 ↓
Canonical Product Object
```

The local model is responsible for:

- OCR cleanup
- spelling correction
- brand normalization
- product name normalization
- category extraction
- ingredient extraction
- package claim extraction

The local model is NOT responsible for:

- market knowledge
- reviews
- ingredient education
- skin science
- popularity
- trend analysis

Those belong to cloud enrichment.

### Nano Capability State Machine

Implement:

```kotlin
sealed interface NanoState {
    data object Available : NanoState
    data object Downloadable : NanoState
    data object Downloading : NanoState
    data object Unsupported : NanoState
}
```

Requirements:

- Check model capability before execution.
- Cache capability status.
- Never block UI.
- Gracefully fall back if unavailable.

---

# Canonical Product Object

The output of Local Gemini must be:

```kotlin
@Serializable
data class LocalStandardizedData(

    val brand: String? = null,

    val productName: String? = null,

    val subtitle: String? = null,

    val category: String? = null,

    val subcategory: String? = null,

    val size: String? = null,

    val variant: String? = null,

    val ingredients: List<String> = emptyList(),

    val claims: List<String> = emptyList(),

    val directions: String? = null,

    val warnings: String? = null
)
```

Important:

Do NOT ask the LLM to generate a confidence score.

Confidence must be computed deterministically by the application.

---

# Stage 4: Web Gemini Enrichment (Optional BYOK)

If the user has configured BYOK credentials:

```text
Canonical Product Object
 ↓
Gemini
 ↓
Enriched Product Object
```

Purpose:

- identify product category
- identify key ingredients
- ingredient descriptions
- skin concerns
- product benefits
- ingredient science
- market knowledge

The cloud model may use information that does not appear on the package.

The cloud model should never overwrite high-confidence local extraction.

It should only enrich.

---

# Product Entity

Generate a database-ready object:

```kotlin
data class ProductEntity(

    val brand: String,

    val productName: String,

    val category: String?,

    val subcategory: String?,

    val ingredients: List<String>,

    val claims: List<String>,

    val keyIngredients: List<String>,

    val benefits: List<String>,

    val skinConcerns: List<String>,

    val ingredientDescriptions: Map<String, String>,

    val confidence: Float
)
```

---

# Confidence Calculation

Do NOT use LLM self-reported confidence.

Generate deterministic confidence based on:

- valid JSON returned
- brand found
- product name found
- ingredient count
- OCR quality metrics
- extraction completeness

Example:

| Signal | Weight |
|----------|----------|
| JSON Valid | 0.30 |
| Brand Found | 0.25 |
| Product Found | 0.25 |
| Ingredients Found | 0.20 |

---

# Discovery Status UI

Create a state machine:

```kotlin
sealed interface DiscoveryState {

    data object Processing : DiscoveryState

    data class LocalSuccess(
        val product: LocalStandardizedData
    ) : DiscoveryState

    data class FullyEnriched(
        val product: ProductEntity
    ) : DiscoveryState

    data class EnrichmentFailed(
        val product: LocalStandardizedData,
        val reason: String
    ) : DiscoveryState

    data class Failed(
        val reason: String
    ) : DiscoveryState
}
```

Requirements:

### Local Success

Display:

```text
✓ Product Identified
✓ OCR Complete
✓ Local AI Complete

Enriching Product...
```

### Enrichment Failed

Display:

```text
✓ Product Identified

Additional product intelligence unavailable.

Retry Enrichment
Continue Offline
```

### Fully Enriched

Display:

```text
✓ Product Identified
✓ Product Intelligence Retrieved
✓ Ingredient Analysis Complete
```

A network timeout must NOT be considered a failed scan.

The local AI output is already considered a successful discovery.

---

# Technical Requirements

Architecture:

- MAD Architecture
- Clean Architecture
- Repository Pattern
- MVVM
- Hilt
- Coroutines
- Flow
- Room

Requirements:

- Offline-first
- AI-first
- Privacy-first
- Zero-footprint local AI execution
- Deterministic fallbacks
- Structured logging
- Unit tests
- UI tests
- Production ready

Generate:

1. Full architecture
2. Domain layer
3. Data layer
4. Repository layer
5. ViewModels
6. State models
7. Room entities
8. Use cases
9. Prompt templates
10. Error handling strategy
11. Retry strategy
12. Sequence diagrams
13. Kotlin implementation skeletons

The final design should assume that future Android devices increasingly provide capable on-device AI and that Local Gemini is the preferred primary normalization engine whenever available.
````
