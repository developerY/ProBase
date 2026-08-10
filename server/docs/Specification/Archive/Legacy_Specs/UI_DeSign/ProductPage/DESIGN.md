## Brand & Style
The design system is centered on "Modern Elegance," tailored for a premium cosmetics tracking experience. It targets an audience that values ritual, self-care, and aesthetic harmony. The UI evokes a sense of calm, luxury, and organization, mirroring the experience of browsing a high-end vanity or a boutique beauty counter.

The design style is **Modern / Minimalist** with a **Tactile** touch. It leverages heavy whitespace to let product imagery breathe, high-quality serif typography for an editorial feel, and soft, diffused shadows to create a sense of physical layering. The interface avoids clutter, opting for purposeful placement and sophisticated transitions that feel effortless and airy.

## Colors
The palette is rooted in soft, skin-tone adjacent hues and deep contrast to maintain legibility and a premium feel.

- **Primary (Soft Blush):** Used for soft backgrounds, decorative elements, and high-level containers. It provides a warm, inviting glow to the UI.
- **Secondary (Deep Slate):** The primary color for text, iconography, and high-emphasis borders. It provides the grounding force against the lighter palette.
- **Accent (Champagne Gold):** Reserved for highlights, special call-to-actions, and "luxury" touchpoints like premium status indicators or subtle dividers.
- **Background (Ivory):** The foundational canvas. It is warmer than pure white, reducing eye strain and feeling more organic.

## Typography
The typographic scale creates a rhythmic contrast between the traditional elegance of **Playfair Display** and the functional clarity of **Inter**. 

Headlines should use Playfair Display with tighter letter spacing to emphasize their editorial quality. Body text utilizes Inter for maximum readability at small scales, particularly for ingredient lists or expiration dates. Labels and "meta" information are set in uppercase Inter with generous letter spacing to provide a clean, structured hierarchy that mimics luxury product packaging.

## Layout & Spacing
This design system utilizes a **fluid grid** with generous safe areas. For mobile, a standard 4-column grid is used, but the visual weight is carried by large padding rather than rigid lines.

- **Margins:** A 24px horizontal margin is maintained to ensure content does not feel cramped against the screen edges.
- **Vertical Rhythm:** A base 8px increment guides all spacing. Larger gaps (40px+) are encouraged between distinct sections (e.g., "Recently Used" vs "Expiring Soon") to maintain an "airy" feel.
- **Negative Space:** Elements are intentionally spaced further apart than in standard utility apps to evoke the feeling of a premium catalog.

## Elevation & Depth
Depth is communicated through **ambient shadows** and **tonal layering**. 

- **Level 0 (Background):** The Ivory surface.
- **Level 1 (Cards/Containers):** Pure white surfaces with a very soft, multi-layered shadow (Blur: 20px, Y: 4px, Opacity: 4% of Deep Slate). This makes elements appear to float gently above the ivory base.
- **Level 2 (Overlays/Modals):** Floating elements with a more pronounced shadow and a subtle 1px border in a lightened Soft Blush tone to define edges without harshness.
- **Interaction:** Upon press, elements should subtly scale down (98%) and shadows should tighten, providing a tactile, responsive feel.

## Shapes
The shape language is organic and soft. A **Rounded (Level 2)** approach is the standard, ensuring no sharp corners disrupt the gentle aesthetic.

- **Primary Containers:** 16px (1rem) corner radius.
- **Large Sections/Images:** 24px (1.5rem) corner radius to create a "portal" effect for photography.
- **Buttons:** Fully pill-shaped (rounded-full) to maximize the friendly, tactile nature of the UI.
- **Inputs:** 12px corner radius, balancing structure with the overall softness of the system.

## Components

- **Buttons:** 
  - *Primary:* Pill-shaped, Deep Slate background with Ivory text. High contrast for clear intent.
  - *Secondary:* Pill-shaped, Soft Blush background with Deep Slate text.
  - *Ghost:* Champagne Gold text, no background, used for low-priority actions like "See All."

- **Cards:** 
  - Product cards should feature a dominant image area with a 24px radius. 
  - Text is aligned left or center depending on the context. 
  - A subtle Soft Blush tint can be used for the card background to group related items.

- **Input Fields:** 
  - Ghost-style inputs with a bottom border in Deep Slate (0.5px) or fully enclosed containers with an Ivory background. 
  - Floating labels use the `label-sm` typography style.

- **Chips/Status:** 
  - Used for "Skin Type" or "Product Category." 
  - Small, pill-shaped, with a 1px Soft Blush border and `label-sm` text.

- **Cosmetic Trackers:** 
  - A custom "circular progress" component for expiration tracking, utilizing Champagne Gold for the progress bar to signify value and care.

- **Lists:** 
  - Generous vertical padding (16px–24px per item) with a very thin (0.5px) Soft Blush divider that does not span the full width of the screen.