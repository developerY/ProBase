# Proposal: LLM-Orchestrated Mood Boards

This proposal outlines the implementation of **"AI Mood Boards,"** a generative feature that uses the local LLM to create infinitely fresh, context-aware styling narratives and visual guides. It transforms the static inspiration card into a dynamic prompt trigger for personalized style storytelling.

## 🤖 The "Style Narrator" Concept

Instead of pre-authored content, the app will generate a "Daily Style Forecast" based on the intersection of the user's biological profile, their inventory gaps, and current environmental factors.

### 1. Narrative-Driven Style Guides
When the user taps the card, the local LLM (Gemini Nano) generates a short, editorial-grade paragraph:
- **The Hook**: *"Winter isn't just a season; it's a high-contrast canvas. Today, we're looking at why Midnight Navy isn't just a color—it's an anchor. Pair it with the luster of silk to reflect the low winter light, mirroring the cool clarity of your Roseate Sand undertones."*
- **Dynamic Context**: The text changes every time the season, weather, or user's inventory shifts.

### 2. Generative Visual Compositions
The AI doesn't just write; it orchestrates the visual layout:
- **Texture Synthesis**: The generator pairs the narrative with specific textures from the **Materiality Studio** (e.g., if it mentions "luster," it displays Silk).
- **Color Story**: It highlights specific hex codes from the user's **Palette Insights** in a mood-board arrangement.

---

## 🧠 Architectural Integration

### 1. The Prompt Orchestrator
We will implement a `MoodBoardGenerator` in `:features:ai:local` or `:features:colors` that constructs a structured prompt:
- **Context Injection**:
  - `User Profile`: Deep Winter
  - `Skin Tone`: Roseate Sand
  - `Key Color`: Midnight Navy
  - `Environmental`: Cold / Low Light
- **Constraint Tuning**: Ensures the output remains in the "Luxury Editorial" tone consistent with the KoColor brand.

### 2. Zero-Cloud Privacy
This feature will rely exclusively on the **Local AI Engine** already established in the project. No user data (skin tone, clothes, location) leaves the device to generate these styling guides.

---

## 🛠️ Data Model Enhancements

```kotlin
data class DynamicMoodBoard(
    val title: String, // e.g., "The Midnight Anchor"
    val narrative: String, // LLM-generated text
    val featuredTextures: List<MaterialTexture>,
    val highlightColors: List<String>,
    val timestamp: Long
)
```

---

## 🚀 The Strategic Benefit

- **Infinite Freshness**: The user never sees the same "Seasonal Inspiration" twice. The hub becomes a living entity.
- **AI-First Brand Positioning**: Demonstrates sophisticated, practical use of on-device AI beyond simple chatbots.
- **Emotional Connection**: Transitions the app from a functional tool into an inspiring "Style Companion" that understands the *poetry* of color.

**Would you like to prioritize this AI-driven narrative as the core experience for the bottom card?**
