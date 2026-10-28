While I can't physically tap the screen, I know exactly how the KoColor Jetpack Compose UI is wired based on the architecture we just built.

To help your users navigate the V2 Orchestration Engine, here is the exact UI navigation flow—step-by-step—for generating, editing, and committing a Style Playlist.

This can serve as both your UI/UX wireframe guide and your user-facing Help Center documentation.

---

# Navigating Your KoColor Style Playlist

Welcome to your V2 KoColor experience. Here is the step-by-step guide to generating, customizing, and locking in your 7-day wardrobe plan.

### Step 1: Open the Playlist Tab

* Open the KoColor app.
* On the bottom navigation bar, tap the **Playlist** icon (located right next to your V1 **Wardrobe** tab).
* *Note: If it is Sunday morning, KoColor will automatically present a "Week Ahead" draft. Otherwise, you can tap the **Generate New Playlist** button at the center of the screen.*

### Step 2: Sync Your Context

Before generating, the app needs to know what you are doing and where you are going.

* A contextual bottom-sheet will slide up asking you to confirm your inputs:
* **Location & Weather:** Verifies your current city for the 7-day forecast.
* **Calendar Sync:** Briefly scans your upcoming week for events (e.g., "Office Days," "Dinner Dates").


* Tap **Build My Playlist**. The screen will briefly show a loading state while the orchestration engine calculates your outfits, applies the 48-hour rotation cooldowns, and maps your Color Profile.

### Step 3: Review the 7-Day Forecast

Once generated, you will be taken to the **Playlist Preview** screen.

* You will see a swipeable horizontal carousel (or vertical list) of 7 cards, starting with Monday.
* Each card displays a snapshot of the **Base Outfit** and the local weather for that day.
* **Tap on any Day Card** to expand it into the full Daily Route view.

### Step 4: Inspect the Details (Inside the Daily View)

When you tap into a specific day, you will see exactly how the engine routed your style:

* **The "Why" (Selection Rationale):** Right under the outfit, tap the small **"Why this look?"** chip. A pop-up will explain the reasoning (e.g., *"Navy harmonizes with your contrast, and linen is required for the 85° afternoon"*).
* **The Evening Remix:** Scroll down past the Base Outfit. If you have an evening event on your calendar, you will see a "Remix" section suggesting a quick jacket or shoe swap for the night out.
* **The Cosmetic Crossfade:** At the bottom of the daily view, KoColor will suggest makeup from your Virtual Vanity. If your outfit clashes with your natural Color Profile, this section will highlight the specific blush or lip shades needed to balance the look.

### Step 5: Tweak and Pin

Don't like a specific item? You are always in control.

* Long-press or tap the **Swap** icon next to any garment.
* Your V1 Wardrobe will slide up, filtered by items that match the current weather and rotation rules.
* Select a new item. The app will automatically mark it with a **Pin** icon, meaning the AI will respect your manual override.

### Step 6: Lock It In

Once you have reviewed the days and made your tweaks, hit the back arrow to return to the main 7-Day view.

* Tap the large, primary **Lock Playlist** button at the bottom of the screen.
* This saves your week and transitions the app into "Execution Mode."

### Step 7: The Daily Commit (Getting Dressed)

When you wake up and open the app, your Playlist tab will default to today's active plan.

* Get dressed in the suggested outfit.
* Tap the bold **"I'm Wearing This"** button at the bottom of today's screen.
* *Success!* This action instantly commits the outfit to your V1 Wardrobe memory, updates your garment use-counts, and applies the 48-hour cooldown for future playlists. (If your plans changed and you stayed in pajamas, just tap the smaller **Skip Today** button).