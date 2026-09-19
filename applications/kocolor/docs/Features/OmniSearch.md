To seamlessly fuse these three concepts—Categorical Inventory, Text Search, and Color Search—into a single, cohesive Jetpack Compose experience, we need to transition from distinct screens to a unified **Omni-Search Architecture**.

The goal is to allow the user to query their local database using any combination of text, taxonomy, and raw color science, all operating instantly on-device.

Here is the architectural blueprint to make these three interfaces work together as one fluid system.

---

## 1. The Omni-Search State Model

Instead of having a separate ViewModel for Inventory and another for Color Search, we combine the query parameters into a single unified state. This allows a user to search for "Lipstick" (Text) while filtering by "Terracotta" (Color) and selecting "Complementary" (Color Math).

```kotlin
package com.probase.kocolor.features.search.presentation

import androidx.compose.ui.graphics.Color
import com.probase.kocolor.domain.model.MacroCategory

// The unified state that drives the UI
data class OmniSearchState(
    val textQuery: String = "",
    val selectedCategory: MacroCategory? = null, // e.g., Lips, Complexion
    
    // Color Search Parameters
    val activeColorHex: String? = null,
    val colorMatchMode: ColorMatchMode = ColorMatchMode.EXACT,
    
    // Results
    val cosmeticResults: List<CosmeticItem> = emptyList(),
    val wardrobeResults: List<ClothingItem> = emptyList(),
    val isSearching: Boolean = false
)

enum class ColorMatchMode {
    EXACT, COMPLEMENTARY, ANALOGOUS, TRIADIC
}

```

---

## 2. The Local Color-Matching Engine

To make the "Exact Match" and "Complementary" buttons (from your third screenshot) work without a cloud backend, we must implement color distance algorithms directly in your `ColorScienceUtils` or a dedicated `SearchEngine` use-case.

### Exact Match (Euclidean Distance)

To find the closest color in the local SQLite/Room database, we calculate the Euclidean distance between the scanned RGB values and the stored inventory RGB values using the formula:

$Distance = \sqrt{(R_2 - R_1)^2 + (G_2 - G_1)^2 + (B_2 - B_1)^2}$

A lower distance means a closer visual match.

### Complementary Match (HSL Shifting)

To find complementary wardrobe items for a scanned lipstick, we convert the target Hex to HSL, shift the Hue by 180 degrees, and run the Euclidean distance search against the shifted color.

$Hue_{complementary} = (Hue_{target} + 180) \pmod{360}$

---

## 3. The Jetpack Compose UI Integration

Based on your designs, we can build a unified screen structure using Material 3 Expressive. The UI will stack the interaction zones: **Search Input -> Active Filters -> Categorized Results**.

### The Unified Layout Architecture

```kotlin
@Composable
fun OmniSearchScreen(
    state: OmniSearchState,
    onEvent: (SearchEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        
        // 1. The Omni-Bar (Text + Camera Trigger)
        // Combines the top bar from screenshot 2 with the camera intent from screenshot 3
        OmniSearchBar(
            query = state.textQuery,
            onQueryChange = { onEvent(SearchEvent.OnTextQueryChanged(it)) },
            onCameraScanTap = { onEvent(SearchEvent.LaunchColorScanner) }
        )

        // 2. The Active Color Filter (Conditionally Visible)
        // Shows the Terracotta block and mode selectors from screenshot 3 IF a color is active
        AnimatedVisibility(visible = state.activeColorHex != null) {
            ColorSearchControlPanel(
                activeHex = state.activeColorHex,
                currentMode = state.colorMatchMode,
                onModeSelected = { onEvent(SearchEvent.OnColorModeChanged(it)) },
                onClearColor = { onEvent(SearchEvent.ClearColorFilter) }
            )
        }

        // 3. Category Chips (From screenshot 2)
        // Allows filtering the results below by Complexion, Lips, etc.
        CategoryFilterRow(
            selectedCategory = state.selectedCategory,
            onCategorySelected = { onEvent(SearchEvent.OnCategorySelected(it)) }
        )

        // 4. The Unified Inventory List (From screenshot 1 & 2)
        // Displays the actual items that pass the Text + Color + Category filters
        UnifiedInventoryList(
            cosmetics = state.cosmeticResults,
            wardrobe = state.wardrobeResults
        )
    }
}

```

---

## 4. How the Flow Works in Practice

1. **The Default State:** The user opens the screen. The text query is empty, and no color is selected. The `UnifiedInventoryList` displays the standard categorized inventory (matching your first screenshot).
2. **Text Interaction:** The user types "Matte" into the `OmniSearchBar`. The list immediately filters locally to show only matte cosmetics.
3. **Color Interaction:** The user taps the Camera icon inside the search bar. The app launches the local ML Kit camera to extract a Hex code (e.g., `#C25C4A` Terracotta).
4. **The Fusion:** The `ColorSearchControlPanel` animates in. The user taps "Complementary". The local Room database is queried, the HSL shift math is applied, and the list updates to show Teal/Blue wardrobe items that perfectly compliment the Terracotta color.

This architecture ensures the user never feels like they are jumping between disjointed "modes" (Inventory mode vs. Search mode vs. Scan mode). It is all one fluid, privacy-first styling ecosystem.

Shall we draft the specific Room Dao query and Kotlin flow transformation required to execute that real-time Euclidean color math across your local database?