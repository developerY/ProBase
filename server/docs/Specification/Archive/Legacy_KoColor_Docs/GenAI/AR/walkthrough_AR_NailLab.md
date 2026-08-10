# AR Nail Lab Navigation Walkthrough

The KoColor application now includes a seamless transition from style analysis to a live **AR Nail Lab** experience. This allow users to virtually "wear" recommended nail polish colors before making a decision.

## 🚀 Navigation Flow

1.  **Style Analysis**: The user captures their face, hair, clothes, and shoes in the **Fashion Analyzer**.
2.  **AI Recommendation**: Gemini analyzes the images and generates specific makeup and nail polish suggestions.
3.  **The "Experience" Button**: In the results list, a new **"Experience"** button appears next to the Nail Polish recommendation.
4.  **Live AR Preview**: Tapping this button triggers a navigation event to the `NailLab` route.
5.  **Interactive Trial**: The app opens the **AR Nail Lab** (`:features:ar:naillab`), passing the recommended HEX color and finish (Matte, Glossy, or Metallic) to the live camera preview.

## 🛠️ Implementation Details

### Route Definition
Defined the `NailLab` destination in `KoColorRoute.kt`:
```kotlin
@Serializable
data class NailLab(val colorHex: String, val finish: String) : KoColorRoute()
```

### Intelligent Parsing
In `AnalyzerScreen.kt`, the application now scans the AI's textual advice to determine the intended finish:
```kotlin
val finish = if (suggestion.advice.contains("Matte", ignoreCase = true)) "MATTE"
             else if (suggestion.advice.contains("Metallic", ignoreCase = true)) "METALLIC"
             else "GLOSSY"
```

### Seamless Integration
The `KoColorNavEntryProvider` was updated to resolve the `NailLab` route and invoke the reusable AR feature module, ensuring a decoupled but integrated experience.
