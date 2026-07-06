# Walkthrough: The "Atmospheric Architect" Style Simulator

The **Style Simulator** has evolved into a high-fidelity consultation engine that synchronizes your biological state, physical inventory, and the real-world environment into a single functional protocol.

## 🚀 The Three Anchors of Intelligence

The engine no longer "guesses"—it **anchors** its reasoning in three distinct layers of truth.

### 1. The Biological Anchor (Internal State)
Before processing, the `StyleSimulatorViewModel` aggregates your internal context:
- **Circadian Stage**: Adjusts logic for "Defense" (Day) vs "Recovery" (Night).
- **Morning Ritual**: Informs the AI if your baseline skincare is already active.
- **Style Profile**: Pulls your **Undertone** and **Seasonal Type** (e.g., Cool Winter) from RoomDB.
- **Visual Anchor**: Analyzes your actual portrait (eye color, hair tone) to mathematically verify color harmonies.

### 2. The Atmospheric Anchor (External State)
The system now "feels" the world outside using the `AtmosphericRepository`:
- **UV Index**: If UV is high (>7.0), the AI automatically prioritizes SPF-rated products from your vault.
- **Temperature & Humidity**: Selects long-wear or matte formulas for high-heat conditions to ensure the look stays "locked."

### 3. The Vault Anchor (Physical Reality)
The engine performs a full sync with both your **Wardrobe** and **Cosmetic Vanity**:
- **Wardrobe Manifest**: Serializes your core clothes (Top, Bottom, Shoes).
- **Cosmetic Vault**: Serializes your physical makeup products (Lip, Face, Eye, SPF).
- **Logical Selection**: Gemini Nano/Flash is tasked with selecting **only** items you physically own. It effectively acts as a digital curator for your physical collection.

---

## 🧠 The 3-Tier "Adaptive Brain"

The system intelligently routes your request based on your hardware and network connectivity:

| Tier | Reasoning Power | Best For |
| :--- | :--- | :--- |
| **Tier 1 (Cloud Flash)** | High-Fidelity Multimodal | Complex, well-lit portrait analysis and intricate vault mapping. |
| **Tier 1.5 (Gemini Nano)** | On-Device LLM | Rapid, private text-based reasoning on flagship NPUs. |
| **Tier 2 (Heuristics)** | Deterministic Matching | 100% offline keyword-to-vibe mapping for any device. |

---

## 🛠 Result Synthesis

When the simulation completes, it produces a **Style Blueprint**:
1.  **The Outfit**: 3 physical items from your wardrobe.
2.  **The Protocol**: 3 specific makeup products from your vault that match the clothes and the weather.
3.  **The Rationale**: A stylistic explanation connecting your **Visual Anchor** to the chosen items.
4.  **The Registry**: Automatically persists as `FashionAdvice` in **Room 3**, closing the data loop.

---
**Status:** ✅ **PRODUCTION ARMORED**
**Compliance:** Context-Aware, Memory-Safe, and Vault-Anchored.
