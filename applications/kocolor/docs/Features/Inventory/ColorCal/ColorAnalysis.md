## Why Perceptual Hue Rotation Improves Cosmetic Color Visualization

Yes, this is a valuable UI/UX optimization for a cosmetics application.

From a data visualization perspective, standard HSV hue sorting often fails the **"squint test"** because it treats hue as a linear **0–360°** scale instead of a perceptual color loop.

By rotating the hue scale, you've moved from being **mathematically correct** to **perceptually useful**.

---

## 1. Perceptual Continuity

In cosmetics, **Pinks**, **Magentas**, and **Reds** naturally belong to the same warm or berry color family.

### Without Perceptual Rotation

A bright magenta (**340°**) appears at the very end of the sorted list, while a true red (**5°**) appears at the beginning.

Although these colors are visually similar, they become separated by the entire blue and green spectrum.

```text
0°                                 360°
Red ─ Orange ─ Yellow ─ Green ─ Blue ─ Purple ─ Magenta
 ^                                                     ^
 Red                                                  Magenta
```

---

### With Perceptual Rotation

By rotating hues greater than **330°**, magentas become adjacent to reds.

```text
Berry → Pink → Magenta → Red → Orange → Yellow → Green
```

The result is the smooth, professional-looking gradient seen in the **Chromatic Core** visualization.

---

## 2. Semantic Grouping

Users reviewing **Cosmetic Analytics** are typically looking for gaps or concentrations in their collection.

Grouping visually similar shades together enables users to quickly identify:

- Warm inventory
- Berry collections
- Cool-tone collections
- Missing color ranges

Without this adjustment, reds and pinks would be split across opposite ends of the visualization, making it much harder to estimate inventory at a glance.

---

## 3. Professional Palette Organization

High-end beauty applications and physical retail displays rarely arrange products using strict HSV ordering.

Instead, they organize products according to how people perceive color.

Typical cosmetic displays group:

- Reds
- Berries
- Magentas
- Pinks

into one continuous section.

Your implementation effectively reproduces the logic of a physical **planogram** in a digital interface.

---

## Suggested Refactoring

If this behavior is needed in multiple areas (such as `StyleSimulator`, `WardrobeRepository`, or analytics views), consider centralizing it in a shared utility module.

```kotlin
fun Float.toPerceptualHue(): Float {
    return if (this > 330f) this - 360f else this
}
```

Possible locations:

- `:core:ui`
- `:core:util`
- `ColorExtensions.kt`

This keeps hue normalization consistent throughout the application.

---

## Benefits

- Consistent color ordering across all screens.
- Easier maintenance.
- Eliminates duplicated logic.
- Ensures every visualization follows the same perceptual model.

---

## Overall Verdict

This is an excellent example of adapting raw mathematical data to match human perception.

While HSV ordering is technically correct, users do not mentally organize cosmetics as a continuous 0–360° color wheel. They think in terms of related color families.

By rotating the hue scale, your application presents colors in a way that feels natural, intuitive, and consistent with professional beauty merchandising.

In short:

> **Mathematical correctness produces accurate numbers. Perceptual correctness produces better user experiences.**

That distinction is exactly what elevates a technical implementation into a polished, professional cosmetic application.