---
name: Serene Rituals
colors:
  surface: '#f7faf6'
  surface-dim: '#d8dbd7'
  surface-bright: '#f7faf6'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f1f4f0'
  surface-container: '#ecefea'
  surface-container-high: '#e6e9e5'
  surface-container-highest: '#e0e3df'
  on-surface: '#181c1a'
  on-surface-variant: '#434843'
  inverse-surface: '#2d312f'
  inverse-on-surface: '#eff2ed'
  outline: '#737873'
  outline-variant: '#c3c8c1'
  surface-tint: '#4e6353'
  primary: '#4b6151'
  on-primary: '#ffffff'
  primary-container: '#647a69'
  on-primary-container: '#f6fff5'
  inverse-primary: '#b4cdb9'
  secondary: '#7c5548'
  on-secondary: '#ffffff'
  secondary-container: '#fdc9b8'
  on-secondary-container: '#795245'
  tertiary: '#5d5c58'
  on-tertiary: '#ffffff'
  tertiary-container: '#767471'
  on-tertiary-container: '#fcffe3'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d0e9d4'
  primary-fixed-dim: '#b4cdb9'
  on-primary-fixed: '#0b2013'
  on-primary-fixed-variant: '#364c3c'
  secondary-fixed: '#ffdbcf'
  secondary-fixed-dim: '#eebbaa'
  on-secondary-fixed: '#2f140a'
  on-secondary-fixed-variant: '#623e32'
  tertiary-fixed: '#e5e2dd'
  tertiary-fixed-dim: '#c8c6c2'
  on-tertiary-fixed: '#1c1c19'
  on-tertiary-fixed-variant: '#474743'
  background: '#f7faf6'
  on-background: '#181c1a'
  surface-variant: '#e0e3df'
typography:
  display-lg:
    fontFamily: Playfair Display
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Playfair Display
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Playfair Display
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-md:
    fontFamily: Manrope
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-caps:
    fontFamily: Manrope
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 8px
  margin-mobile: 20px
  margin-desktop: 40px
  gutter: 16px
  section-gap: 48px
---

## Brand & Style
The design system is anchored in the "Clean Wellness" aesthetic—a philosophy that prioritizes mental clarity and physical self-care. The target audience seeks a sanctuary from digital noise, requiring a UI that feels like a deep breath.

The visual direction combines **Minimalism** with a **Tactile** edge. It utilizes expansive whitespace (breathability), a high-quality typographic hierarchy (sophistication), and soft, organic layering to evoke a sense of calm and organization. The interface transitions between "Morning Fresh" and "Evening Calm" modes to align with the user's circadian rhythm, using color as a functional cue for routine timing.

## Colors
The palette is inspired by natural pigments and minerals. 

*   **Primary (Sage Green):** Represents growth and vitality. Used for active states, primary actions, and "Morning" routine themes.
*   **Secondary (Muted Clay):** Represents skin health and grounding. Used for "Evening" routine themes, accent elements, and notifications.
*   **Neutral (Warm Cream):** The foundation of the UI. Replaces pure white to reduce eye strain and provide a sophisticated, paper-like quality.
*   **Text (Deep Charcoal-Green):** A soft off-black used for high-legibility text, ensuring enough contrast while maintaining the "soft" aesthetic.

**Dynamic Mode:** During evening hours, the background shifts from Warm Cream (#F9F6F1) to a deeper, desaturated Sage (#4A564D) with cream text to promote wind-down behavior.

## Typography
The typographic system uses a high-contrast pairing to balance editorial beauty with functional clarity.

*   **Playfair Display:** Reserved for headings, routine titles, and celebratory moments. Its elegant serifs provide a "boutique" feel. Use tight letter-spacing for larger display sizes to maintain impact.
*   **Manrope:** Used for all functional UI elements, body copy, and instructions. It is a modern sans-serif with a geometric foundation that remains highly readable at small sizes.
*   **Hierarchy:** Use `label-caps` for secondary metadata (e.g., "5 MIN READ" or "STEP 1") to create a structured, organized look without cluttering the visual field.

## Layout & Spacing
This design system employs a **Fluid Grid** with generous inner padding to reinforce the "airy" mood. 

*   **Rhythm:** Based on an 8px root scale. 
*   **Margins:** Mobile views use a 20px side margin to keep content centered and focused. Desktop views expand this to a maximum content width of 1200px with 40px margins.
*   **Density:** Avoid high-density layouts. Every card or container should have at least 24px of internal padding. 
*   **Grouping:** Use `section-gap` (48px) to clearly separate different parts of a routine (e.g., Cleansing vs. Treatment) to reduce cognitive load and help the user focus on one task at a time.

## Elevation & Depth
Depth is conveyed through **Tonal Layering** and **Ambient Shadows**.

*   **Surface Strategy:** Use slightly darker or warmer cream tones to denote background containers. Most UI elements sit on a primary background without heavy borders.
*   **Shadows:** Use extremely soft, diffused shadows with a slight primary-color tint (e.g., a Sage-tinted shadow for Sage-colored buttons). Shadow blurs should be large (16px–32px) and low opacity (5–8%) to feel like natural light hitting a matte surface.
*   **Interaction:** On tap or hover, elements should not "pop" out harshly. Instead, use a subtle scale-down (98%) and a slight increase in shadow spread to simulate a tactile press.

## Shapes
The shape language is organic and approachable. 

*   **Roundedness:** Level 2 (0.5rem / 8px) is the standard for most functional elements like input fields and buttons. 
*   **Cards:** Use `rounded-lg` (1rem / 16px) for main content containers and routine cards to create a softer, more "friendly" frame for images and text.
*   **Icons:** Icons should be drawn with rounded terminals and a medium stroke weight (1.5px to 2px) to match the Manrope typeface. Avoid sharp corners in iconography.

## Components
Consistent styling of key components:

*   **Buttons:** Primary buttons use a solid Sage Green background with white text. Secondary buttons use an "Outlined" style with a 1px border in the Primary color. All buttons use the `label-caps` typography style for clarity.
*   **Cards:** Pure white or very light cream backgrounds with `rounded-lg` corners and an ambient shadow. Use for routine steps, product recommendations, and progress tracking.
*   **Input Fields:** Minimalist design with only a bottom border or a very light tonal background. Focus states are indicated by a 1.5px Sage Green border.
*   **Progress Indicators:** Use soft, circular "rings" rather than linear bars to maintain the organic feel. The "active" segment should be Sage (Morning) or Clay (Evening).
*   **Chips:** Used for skin-type tags (e.g., "Oily", "Sensitive"). These should be pill-shaped with low-contrast background tints matching the secondary color.
*   **Checkboxes:** Custom-styled as soft circles. When checked, they fill with the Primary color and a delicate "check" icon, providing a satisfying sense of completion.