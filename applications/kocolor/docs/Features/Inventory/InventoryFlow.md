To accommodate your hard requirement of a strict three-tab bottom navigation (`Home`, `Collections`, `Settings`), the **Home** tab must act as a "Dashboard" or "Hub." It becomes the routing center for the user's core daily tasks.

Instead of cluttering the bottom navigation bar, you place clear, distinct entry points to your three features directly on the main Home screen.

Here is the exact user navigation journey and how the Compose `NavHost` will route them within that single tab.

---

### The Hub: The "Home" Screen Layout

When the user opens the app, they land on the **Home Dashboard**. To make the three workflows seamless, this screen should be laid out with three distinct interactive zones:

1. **Top:** A persistent, wide Search Bar (Entry to Flow 2).
2. **Center/Hero:** A large, visual "Scan to Match" card or Camera icon (Entry to Flow 3).
3. **Bottom/List:** A prominent "Browse Full Inventory" button or summary tile (Entry to Flow 1).

---

### Journey 1: The Full Inventory

**Goal:** Manage stock, view categories, and organize the collection.

1. **Start:** User is on the `Home` tab.
2. **Action:** User taps the "Browse Full Inventory" button.
3. **Route:** The app pushes the `InventoryScreen` onto the Home navigation stack.
4. **Experience:** The user sees the accordion-style progressive disclosure UI (e.g., tapping *Lips* to see all lipsticks and glosses). They can manage costs, see stock levels, and use the floating `+` button to add new items.
5. **Exit:** Tapping the back arrow returns them to the main Home Dashboard.

---

### Journey 2: Text Input to Color Card

**Goal:** Quickly find a specific shade (e.g., that perfect orange-red) by typing.

1. **Start:** User is on the `Home` tab.
2. **Action:** User taps the persistent Search Bar at the very top of the screen (e.g., labeled *"Search by name, brand, or color..."*).
3. **Route:** The app immediately pushes the `TextSearchScreen` onto the stack and opens the device keyboard.
4. **Experience:** As seen in your screenshot, the user types "lip". The screen instantly filters to show rich Color Cards (like *Crimson Fire* with its deep red block and *Crystal Shine* with its light pink block). The visual weight of the color card allows them to instantly identify the exact shade they want without reading the text.
5. **Exit:** Clearing the search or tapping back returns them to the Home Dashboard.

---

### Journey 3: The Color Search (Camera to Harmony)

**Goal:** Match cosmetics to a physical garment in the real world.

1. **Start:** User is on the `Home` tab.
2. **Action:** User taps the prominent "Scan Garment" hero card or Camera FAB in the center of the Home screen.
3. **Route:** The app launches the `CameraScannerScreen` (using ML Kit / Wardrobe Analyzer).
4. **Experience:** The user snaps a photo of their dress. The app extracts the Hex value, processes the Wardrobe Color Engine math, and pushes the `ColorSearchScreen`. The user is presented with the "Scan Color" results (e.g., *Terracotta*) and sees their perfectly matched eyeliners and lipsticks populated below it based on complementary or monochromatic filters.
5. **Exit:** Tapping the back arrow returns them to the Home Dashboard.

---

### Architectural Note for Jetpack Compose

By nesting all three of these destinations inside a `navigation(route = "home_graph")` block, you ensure that the Bottom Navigation Bar remains visible and active while the user traverses these screens. It keeps the UX incredibly clean while preserving the robust separation of your data layers.