# KoColor: Comprehensive User Guide

Welcome to **KoColor**, your privacy-first, on-device Personal Style Operating System. KoColor unifies your wardrobe, cosmetics, daily routines, and local environment to provide intelligent, hyper-personalized fashion and beauty recommendations. 

This guide covers everything you need to know to master the KoColor platform.

---

## Table of Contents
1. [Getting Started](#1-getting-started)
2. [The Home Dashboard](#2-the-home-dashboard)
3. [Smart Camera & Color Analysis](#3-smart-camera--color-analysis)
4. [Wardrobe & Vanity Management](#4-wardrobe--vanity-management)
5. [The AI Stylist & Style Playlists](#5-the-ai-stylist--style-playlists)
6. [Daily Rituals & Wellness](#6-daily-rituals--wellness)
7. [Privacy & Security](#7-privacy--security)

---

## 1. Getting Started

### Initial Setup
When you first open KoColor, you will be prompted to grant a few essential permissions:
*   **Camera:** Required for analyzing clothing colors and cosmetics.
*   **Location (Approximate):** Used locally to pull real-time weather and UV data to inform daily outfit and skincare recommendations.
*   **Health Connect (Optional):** Used to sync hydration and sleep data to tailor beauty and wellness tips.

### Creating Your Profile
KoColor builds a **Fashion Profile** based on your physical traits (like skin undertone) and personal preferences. 
1. Navigate to **Settings** > **My Profile**.
2. Input your preferred color palettes (e.g., Warm Autumn, Cool Winter). If you don't know it, KoColor's AI will help discover it over time as you scan items!

---

## 2. The Home Dashboard

The **Home** tab is your daily command center. It adapts dynamically to the time of day and your environment.

*   **Environmental Context:** At the top, you'll see real-time weather, temperature, and UV Index. If the UV is high, KoColor will proactively recommend sunscreen.
*   **Wellness Insights:** Displays your current hydration levels and sleep duration (if Health Connect is enabled).
*   **Active Routine:** Quickly jump into your Morning, Meals, or Evening ritual.
*   **Quick Actions:** Use the central FAB (Floating Action Button) to quickly scan a new item, log an outfit, or drink water.

---

## 3. Smart Camera & Color Analysis

KoColor includes an advanced, on-device **Color Science Engine** that understands the exact mathematical makeup of your clothing and makeup.

### Scanning an Item
1. Tap the **Camera** icon.
2. Point the camera at a piece of clothing or a cosmetic product.
3. Snap the photo.

### Quick Start with Starter Packs (Sync Hub)
If you don't want to manually scan every item in your closet or vanity, KoColor offers **Starter Packs** to instantly populate your inventory:
1. Navigate to the **Sync Hub**.
2. Browse curated catalogs, such as the "Complete Fashion" or "Complete Cosmetics" packs.
3. Tap **Install Pack** to securely download and integrate these foundational items into your Wardrobe and Vanity immediately.
4. You can also preview packs before installing them, and remove them later via the Sync Hub if they don't fit your aesthetic.

### What Happens Next?
*   **Color Extraction:** The app extracts the dominant, vibrant, and muted hex codes from the item.
*   **Standard Color Matching:** It mathematically matches the color to standardized color databases using perceptual Delta-E algorithms.
*   **Seasonal Mapping:** The engine determines the color temperature (Warm/Cool/Neutral) and assigns it a Seasonal Palette (Spring, Summer, Autumn, Winter).

---

## 4. Wardrobe & Vanity Management

Your **Collection Hub** tracks everything you own, divided into the **Wardrobe** (Clothing) and **Vanity** (Cosmetics).

### Wardrobe (Fashion Archive)
*   **Categorization:** Items are sorted into macro and micro categories (e.g., Tops, Bottoms, Outerwear).
*   **Usage Tracking:** Every time you wear an item, its "Wear Count" increases, lowering its "Cost Per Use" (CPU).
*   **Wardrobe Footprint:** View analytics on the dominant colors in your closet and your most-worn items.

### Vanity (Cosmetics Vault)
*   **Expiry Tracking:** When adding makeup or skincare, log the PAO (Period After Opening). KoColor will alert you when a product is nearing expiration.
*   **Stock Levels:** Keep track of low-stock items so you know when to repurchase.
*   **Color Harmony:** See a breakdown of your cosmetic tones (e.g., Complexion, Eyes, Lips) to ensure a balanced collection.

---

## 5. The AI Stylist & Style Playlists

The **AI Fashion Advisor** acts as your personal stylist, running securely on your device via Gemini Nano.

### Generating a Daily Style Plan
1. Go to the **Collection Hub** and tap **AI Stylist**.
2. Select your intent (e.g., "Office Meeting," "Casual Weekend," "Date Night").
3. The AI will look at your inventory, cross-reference the local weather, and build a **Daily Style Plan** (Outfit + Makeup pairing).
4. **Feedback Loop:** At the end of the day, mark the outfit as "Worn" to log the usage statistics and train the AI on your preferences.

### Style Playlists
KoColor can generate a 7-day "Style Playlist," curating a week's worth of outfits to ensure you rotate through your wardrobe evenly and reduce decision fatigue.

---

## 6. Daily Rituals & Wellness

True style comes from within. The **Rituals** tab helps you maintain healthy habits.
*   **Morning Routine:** Skincare, SPF application, and outfit selection.
*   **Meals Routine:** Mid-day hydration and touch-ups.
*   **Evening Routine:** Cleansing, night creams, and next-day preparation.

Check off steps as you complete them. The app dynamically provides beauty tips based on the time of day.

---

## 7. Privacy & Security

KoColor is built with a strict **On-Device First** architecture.
*   **Local Processing:** Your wardrobe photos, routines, and physical traits are stored locally in your device's secure Room database.
*   **Local AI:** Recommendations are generated on your phone using edge AI models. Cloud AI is only used as a fallback if your device doesn't support local generation, and all network calls are secured via App Check.
*   **Data Deletion:** You can permanently delete your entire profile and inventory at any time via **Settings** > **Data & Privacy**.

---
*End of User Guide. For technical architecture details, refer to the `Overview/` directory.*