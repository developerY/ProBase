# Proposal: Dynamic "Stylist's Edit" Engine

This proposal outlines the logic for transforming the static "Stylist's Edit" section into a dynamic, AI-driven (or heuristic-driven) editorial engine that reacts to the user's real inventory and biological profile.

## 🧠 The "Stylist Analysis" Pipeline

We will implement a multi-stage analysis within the `ColorIntelligenceRepository` to generate personalized narrative insights.

### 1. Inventory Sentiment Analysis
We will categorize the entire inventory into "Vibes" based on the distribution of Hues and Saturation levels:
- **"The Minimalist"**: High volume of low-saturation neutrals (Blacks, Whites, Beiges).
- **"The Vibrant Collector"**: High volume of high-saturation colors across multiple hues.
- **"Earth Tones"**: Dominance in the Warm/Autumn spectrum (Oranges, Browns, Olives).
- **"Cool Sophisticate"**: Dominance in the Cool/Winter spectrum (Blues, Purples, Cool Grays).

### 2. Gap-to-Seasonal Mapping
Instead of just listing missing colors, we will select the **"Most Impactful Additions"**:
- Compare the `userSeason` (e.g., Deep Winter) against `inventoryColors`.
- Identify the 2 highest-contrast "Missing" hues.
- Map these hues to high-fidelity names (e.g., `#046307` becomes "Deep Emerald").

### 3. Biological Anchoring
The logic will explicitly reference the user's `Signature Skin Tone` (detected during onboarding) to suggest coordination strategies.

---

## 🛠️ Data Model Enhancements

We will introduce a `StylistEdit` data class to encapsulate the generated narrative:

```kotlin
data class StylistEdit(
    val title: String = "The Stylist's Edit",
    val primaryInsight: String, // e.g. "Your collection leans into Cool Neutrals..."
    val recommendation: String, // e.g. "Integrating Deep Jewel Tones..."
    val anchorColors: List<String>, // Hex codes for the suggested items
    val buttonText: String = "SHOP THE EDIT"
)
```

---

## 📝 Example Dynamic Transformation

**User Profile:** Winter | **Inventory:** 80% Black/Gray/White | **Skin Tone:** Roseate Sand

| Section | Dynamic Generation |
| :--- | :--- |
| **Primary Insight** | "Your current collection leans heavily into **Cool Neutrals**. While sophisticated, it lacks the depth required for high-contrast **Winter** styling." |
| **Recommendation** | "Integrating **Jewel Tones** like **Sapphire** and **Amethyst** will anchor your silhouette and provide a radiant glow against your **Roseate Sand** undertones." |
| **Anchor Swatches** | Suggests `#0F52BA` (Sapphire) and `#9966CC` (Amethyst). |

---

## 🚀 Implementation Steps

1.  **Repository Logic**: Add `generateStylistEdit(userSeason, inventory)` to `ColorIntelligenceRepository`.
2.  **Color Naming**: Integrate a hue-to-name mapping utility (e.g., "Sapphire," "Emerald").
3.  **UI Wiring**: Connect the `ColorHubViewModel` to this new repository method.
4.  **Template Engine**: Create a lightweight string template system for the editorial copy.

**Does this logic for the "Stylist's Edit" align with your vision for the Dynamic Hub?**
