# Walkthrough: The V1 Inventory Pipeline

This journey follows a product from its initial definition in a JSON source to its final high-fidelity rendering in the KoColor mobile app.

---

## 🏗️ Step 1: Definition
You define your products in `starter_pack_source.json`. 
*   Every item belongs to a `macro_category` (e.g., `LIPS`, `COMPLEXION`).
*   Every item has high-fidelity metadata like `formulation`, `finish`, and `ingredients`.

## ⚙️ Step 2: Compilation
Run the Rust compiler to transform your JSON into a secure distribution package:
```bash
cd server/package/KoColor
cargo run --bin kocolor-compiler -- build starter-pack ./starter_pack_source.json
```
**The Result**: A compressed `.kpkg` file and a new `manifest.json` entry with a valid ECDSA signature and SHA-256 hash.

## 📱 Step 3: Mobile Sync
Open the KoColor app and navigate to **Settings > Glow Archive Sync**.
1.  **Manifest Fetch**: The app pings the manifest and displays your packs.
2.  **Security Check**: The app verifies the signature of the manifest using its native ECDSA engine.
3.  **Discovery**: You see the "Core Collection" listed with its exact item count (9 items).

## 🛍️ Step 4: Boutique Selection
Tap on a pack to enter the **Select Items** screen.
1.  **Categorization**: Products are grouped into "Prep," "Complexion," etc.
2.  **Granular Choice**: You can "Select All" in the Prep category but choose only specific Lip shades.
3.  **Visual Proof**: Note that category titles stay at the top as you scroll (Sticky Headers) and background text never bleeds through.

## 💾 Step 5: Ingestion & "Make it Mine"
Once you hit **Import Selected**:
1.  **Database Write**: Items are saved to Room with their source tagged (e.g., `sourcePackId = 'starter_pack_v1'`).
2.  **Personalization**: Go to the product detail screen. Tap **"MAKE IT MINE"**.
3.  **Safe Clone**: The app creates a personal copy. If you later decide to "Wipe Starter Pack," your cloned version is safe because the app automatically detached its `sourcePackId` during the clone process.

---

**Summary**: You now have a complete, secure, and professional cycle for distributing products to your users with zero manual data entry.
