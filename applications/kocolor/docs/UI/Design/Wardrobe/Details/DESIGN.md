---
name: Vanity Vault
colors:
  surface: '#fcf9f8'
  surface-dim: '#dcd9d9'
  surface-bright: '#fcf9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f6f3f2'
  surface-container: '#f0eded'
  surface-container-high: '#eae7e7'
  surface-container-highest: '#e4e2e1'
  on-surface: '#1b1c1c'
  on-surface-variant: '#404846'
  inverse-surface: '#303030'
  inverse-on-surface: '#f3f0f0'
  outline: '#707976'
  outline-variant: '#c0c8c5'
  surface-tint: '#35675f'
  primary: '#002823'
  on-primary: '#ffffff'
  primary-container: '#064039'
  on-primary-container: '#79aca2'
  inverse-primary: '#9dd1c7'
  secondary: '#6b5c4c'
  on-secondary: '#ffffff'
  secondary-container: '#f4dfcb'
  on-secondary-container: '#716252'
  tertiary: '#222322'
  on-tertiary: '#ffffff'
  tertiary-container: '#383938'
  on-tertiary-container: '#a2a2a1'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#b9ede2'
  primary-fixed-dim: '#9dd1c7'
  on-primary-fixed: '#00201c'
  on-primary-fixed-variant: '#1b4f47'
  secondary-fixed: '#f4dfcb'
  secondary-fixed-dim: '#d7c3b0'
  on-secondary-fixed: '#241a0e'
  on-secondary-fixed-variant: '#524436'
  tertiary-fixed: '#e3e2e0'
  tertiary-fixed-dim: '#c7c6c5'
  on-tertiary-fixed: '#1a1c1b'
  on-tertiary-fixed-variant: '#464746'
  background: '#fcf9f8'
  on-background: '#1b1c1c'
  surface-variant: '#e4e2e1'
typography:
  display-lg:
    fontFamily: Playfair Display
    fontSize: 48px
    fontWeight: '700'
    lineHeight: '1.1'
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Playfair Display
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.2'
  headline-lg-mobile:
    fontFamily: Playfair Display
    fontSize: 28px
    fontWeight: '600'
    lineHeight: '1.2'
  headline-md:
    fontFamily: Playfair Display
    fontSize: 24px
    fontWeight: '500'
    lineHeight: '1.3'
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.5'
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: '1.4'
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: '1.2'
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 8px
  container-margin: 24px
  gutter: 16px
  section-gap: 48px
---

## Brand & Style
The design system is anchored in the concept of a "Digital Atelier." It prioritizes the visceral experience of a high-end physical boutique—minimalist, quiet, and curated. The target audience consists of fashion enthusiasts who view their wardrobe as a collection of investments rather than just utility.

The aesthetic blends **Minimalism** with **Modern Sophistication**. It relies on generous whitespace to create a sense of calm and "breathing room" for high-resolution garment imagery. Visual elements are intentional and sparse; nothing is decorative without purpose. The emotional response is one of organized luxury, turning the chore of inventory management into an act of self-expression.

## Colors
The palette is built on a foundation of "Quiet Luxury" neutrals supported by a single, commanding accent.

- **Primary (Deep Emerald):** Used sparingly for key calls-to-action and meaningful brand moments. It represents quality and depth.
- **Secondary (Champagne):** An elegant bridge between the canvas and the content. Used for subtle backgrounds, dividers, or soft highlights.
- **Neutral (Charcoal & Ivory):** The "Ivory" (#FAF9F6) serves as the primary canvas color, softer and warmer than pure white to prevent eye strain. "Charcoal" (#2C2C2C) provides high-contrast legibility for typography.
- **Status Colors:** Use muted versions of standard status colors (e.g., a dusty rose for errors) to ensure they do not break the sophisticated atmosphere.

## Typography
The typographic hierarchy utilizes a classic serif for editorial impact and a modern sans-serif for functional clarity.

- **Headlines:** Playfair Display provides an authoritative, fashion-forward voice. Use optical sizing to ensure elegance at both large display sizes and smaller section titles.
- **Body & UI:** Inter is used for its exceptional readability and neutral character. It ensures that the app remains highly functional even when managing dense data like material lists or size charts.
- **Captions & Labels:** Small labels should use increased letter-spacing and uppercase styling to evoke the appearance of luxury brand tags.

## Layout & Spacing
This design system employs a **Fixed Grid** approach with generous margins to mimic the layout of a fashion lookbook.

- **Desktop:** 12-column grid with a maximum content width of 1280px. Centered layout with 48px gutters to maintain an airy feel.
- **Mobile:** 4-column grid with 24px side margins. Vertical spacing between sections should be exaggerated (32px+) to prevent the interface from feeling "cramped."
- **Rhythm:** All spacing must be multiples of 8px. Use larger gaps between distinct content groups to signal hierarchy without the need for heavy borders.

## Elevation & Depth
Depth is handled through **Tonal Layers** and **Ambient Shadows** rather than traditional skeuomorphism.

- **Surfaces:** Use subtle shifts in background color (Ivory to Champagne) to distinguish between the background and elevated cards.
- **Shadows:** Shadows should be ultra-soft, using a large blur radius (20px-40px) and very low opacity (3-5%). The shadow color should be tinted with the primary emerald or charcoal to keep it integrated with the palette.
- **Interaction:** Elevated states (e.g., hovering over a garment card) should use a slight scale-up (1.02x) and a deepening of the ambient shadow to provide a tactile, premium feel.

## Shapes
The shape language is organic and approachable, utilizing significant rounding to soften the structured grid.

- **Base Corner Radius:** 0.5rem (8px) for small components like inputs and chips.
- **Container Corner Radius (rounded-lg):** 1rem (16px) for cards, modals, and primary containers. This creates the "premium wardrobe" feel.
- **Pill Shapes:** Reserved for "Edit" or "Status" tags to distinguish them from primary navigational buttons.
- **Imagery:** Clothing photos should always use the `rounded-lg` token to avoid sharp edges that conflict with the "soft" brand personality.

## Components
Consistent styling across the inventory experience:

- **Buttons:** Primary buttons use the Deep Emerald background with Ivory text. Secondary buttons should be outlined (1px) in Charcoal with no fill. Always use generous internal padding (16px 32px).
- **Cards:** Garment cards should be borderless, relying on soft ambient shadows and the Ivory background to define their shape. Text (Item name and Brand) should be centered below the image.
- **Input Fields:** Bottom-border only or very light Charcoal outlines (0.5px). Focused states should transition the border to the Primary Emerald.
- **Chips/Filters:** Use the Champagne color as a background with Charcoal text. Ensure a pill-shaped radius for high-touch interactive elements.
- **Empty States:** Use thin-line illustrations or minimalist photography paired with a centered Playfair Display headline to maintain the high-end boutique aesthetic even when no data is present.
- **Inventory Tags:** Small, uppercase labels placed in the top-right corner of garment cards to indicate "Season," "Category," or "Status."