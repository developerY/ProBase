The user navigation journey relies entirely on the persistent Bottom Navigation Bar as the central anchor for the app. By mapping these three distinct intents to specific entry points, the cognitive load remains incredibly low, allowing the user to seamlessly switch between logistics (Inventory), fast retrieval (Text Search), and styling intelligence (Color Search).

Here is the exact step-by-step navigation architecture for each journey in the Jetpack Compose UI.

---

## 1. The Inventory Journey (Full Logistics)

**Intent:** "I need to organize my collection, check what I own, or delete expired products."
**Entry Point:** The **Inventory** tab on the Bottom Navigation Bar.

1. **Tap the 'Inventory' Tab:** The user is taken directly to the root inventory screen.
2. **View Macro Categories:** The screen displays top-level, collapsed accordion menus (e.g., Face, Cheeks, Eyes, Lips).
3. **Progressive Disclosure:** The user taps the **Lips** category. The accordion smoothly expands to reveal Micro Categories (e.g., Lipstick, Lip gloss).
4. **Manage Stock:** The user can visually scan the items, tap a specific product card to view its full metadata (Chemistry, Finish, Open Beauty Facts INCI list), or tap the **Delete** / **Use** buttons directly on the card to manage their physical stock.

---

## 2. Text Input to Color Card Journey (Fast Retrieval)

**Intent:** "I know I want to wear something on my lips, but I need to see all my red/orange options side-by-side to pick the perfect shade."
**Entry Point:** The **Search** tab on the Bottom Navigation Bar (or the search bar pinned to the top of the Inventory screen).

1. **Activate Search:** The user taps the search input field, bringing up the keyboard.
2. **Input Query:** The user types "lip" or "red".
3. **Dynamic Recomposition:** The UI instantly filters the local Room database. The accordion menus disappear, replaced by a flattened, highly visual list of matching products.
4. **Visual Selection:** The UI heavily emphasizes the extracted **Color Card** (the large dominant hex swatch) next to the product name (e.g., *Crimson Fire*).
5. **Decision:** The user visually compares the red and orange swatches directly in the search results to find the exact temperature or shade they want for the day, without needing to read the metadata.

---

## 3. Color Search Journey (The Stylist / Wardrobe Matcher)

**Intent:** "I am wearing this specific green dress today. What eye liner or lipstick do I own that harmonizes with it?"
**Entry Point:** A prominent action on the **Home** tab (e.g., a "Scan Outfit" FAB) or a dedicated "Scan Color" tile within the **Search** tab.

1. **Initiate Scan:** The user taps **Scan Color** or the camera icon.
2. **Capture Target:** The camera opens (entirely on-device, preserving zero-footprint privacy). The user snaps a photo of their physical dress.
3. **Engine Processing:** The local Wardrobe Color Engine extracts the dominant hex value (e.g., an Olive Green) and calculates its Color Temperature and Seasonal Palette.
4. **View Harmony Results:** The user is routed to the Color Search results screen. The top of the screen displays the scanned Olive Green color tile.
5. **Apply Styling Filters:** The user taps filter pills like **Complementary** or **Exact Match**.
6. **Cross-Reference:** The UI displays all locally owned cosmetics (e.g., a warm terracotta blush or a neutral eyeliner) that mathematically match the requested color theory rules, effectively styling the user's face based on their physical wardrobe.    