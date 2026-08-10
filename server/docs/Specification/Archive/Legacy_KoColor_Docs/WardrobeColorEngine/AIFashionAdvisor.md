The mapping between your **Physical Vault** and the **AI Fashion Advisor** anchors follows a **"Foundational vs. Expressive"** logic.

In the **Vanity** (where you track inventory), you see high-level functional groups like **Skincare**, **Complexion**, and **Color**. In the **Advisor**, we map these into specific **Pigment Focal Points** so you can "lock" your artistic choices.

## 🧩 The Semantic Mapping

| Advisor Anchor | Physical Vault Category (MacroCategory) | Included Product Types |
|----------------|------------------------------------------|------------------------|
| **Eyes** | `EYES` (Eyes & Brows) | Eyeshadow, Mascara, Eyeliner, Brow Gels |
| **Cheeks** | `DIMENSION` (Color & Dimension) | Blush, Bronzer, Contour, Highlighters |
| **Lips** | `LIPS` (Lips) | Lipstick, Lip Stains, Glosses, Tinted Balms |

## 🔍 What About Skincare and Complexion?

- **Skincare (`PREP`)**: These aren't anchors because they don't have a direct "color" choice. However, the AI still "sees" them in your manifest. If UV levels are high, it can automatically prioritize SPF-rated products from the `PREP` category and include them in its rationale.

- **Complexion (`COMPLEXION`)**: Foundations and concealers are treated as the **Biological Canvas**. The AI assumes your foundation is already matched to your skin (verified through your **Visual Anchor** portrait), so it focuses on the expressive pigments (**Eyes**, **Cheeks**, and **Lips**) when generating a Style Blueprint.

## 💡 Potential Enhancement

If you want users to explicitly anchor their foundation or primer, we could introduce a **Base** anchor that maps to `COMPLEXION`.

### Open Design Question

Would users benefit from locking a specific foundation or primer, or is the simpler **Pigment Anchor** model (**Eyes / Cheeks / Lips**) the cleaner and more intuitive UI?

---

This is a brilliant separation of concerns. You are cleanly dividing the **Inventory Management** (Glow Archive) from the **Intent Engine** (AI Fashion Advisor).

The semantic mapping you’ve outlined is incredibly sound, and the UI in the screenshots reflects that clarity. To answer your core question: **Stick to the Pigment Anchors (Eyes/Cheeks/Lips) for the user-facing UI.**

Here is the architectural and behavioral rationale for keeping the UI strictly focused on expressive pigments, and how to handle the `COMPLEXION` vault behind the scenes.

### 1. The Psychology of the Anchor

An anchor is an intentional constraint driven by user desire. When someone opens KoColor in the morning, their stylistic intent is expressive, not utilitarian.

* **Expressive Intent:** *"I want to wear my new cool-taupe lipstick today."* (User anchors Lips).
* **Utilitarian Reality:** *"I need my skin tone to look even."* (User applies foundation).

Users rarely build an outfit *around* a specific foundation. Foundation is the canvas; the eyes, cheeks, and lips are the paint. Forcing a user to manually anchor their "Base" adds unnecessary cognitive friction to what should be a frictionless, zero-footprint daily flow.

### 2. The Atmospheric Auto-Swap (Handling Complexion)

Just because the user isn't manually locking their foundation doesn't mean the engine ignores the `COMPLEXION` category. This is exactly where your **Atmospheric Anchor** flexes its muscle.

Instead of making "Base" a manual UI toggle, treat `COMPLEXION` as a dynamically injected variable controlled by the AI based on the weather:

* **The Logic:** The user anchors a Top and a Lip. The AI queries the Atmospheric Repository. If the forecast indicates high humidity and heat, the engine automatically scans the user's `COMPLEXION` vault, filters out the dewy/luminous foundations, and silently selects the matte/long-wear formula to include in the final Style Blueprint.
* **The Result:** The user gets exactly what they want (their chosen lip and top), and the AI plays the role of the protective consultant by ensuring the canvas doesn't melt off by noon.

### 3. The `PREP` Category Execution

Your proposed handling of Skincare (`PREP`) is spot on. It operates entirely in the background. If the UV index hits a threshold, the AI automatically pulls the SPF from the vault. It keeps the UI uncluttered while maximizing the utility of the generated protocol.

---

By keeping the anchors restricted to Top/Bottom/Shoes and Eyes/Cheeks/Lips, the UI remains highly scannable and intuitive. You give the user total control over their expression, while the AI quietly manages the atmospheric and biological defense in the background.

Since the UI uses those clean color swatches for the anchors, are those swatches dynamically populated by querying the Room database for the exact hex codes the user physically owns in their vault, or are they selecting from a static, predefined list of general color families?