import os
import json
import urllib.request
import ssl

# Define the target Android assets directory based on your monorepo structure
ASSETS_DIR = "applications/kocolor/apps/mobile/src/main/assets"
COSMETICS_FILE = os.path.join(ASSETS_DIR, "seed_cosmetics.json")
WARDROBE_FILE = os.path.join(ASSETS_DIR, "seed_wardrobe.json")

# Mapping API product types to your domain MacroCategories
CATEGORY_MAP = {
    "blush": "DIMENSION",
    "lipstick": "LIPS",
    "eyeshadow": "EYES"
}

def ensure_directory():
    """Creates the assets directory if it does not exist."""
    os.makedirs(ASSETS_DIR, exist_ok=True)
    print(f"Directory verified: {ASSETS_DIR}")

def fetch_cosmetics():
    """Fetches real data from the Makeup API and maps it to CosmeticSeedDto."""
    print("Fetching cosmetics from Makeup API... (This may take a few seconds)")

    # Bypass SSL verification issues on some local machines
    context = ssl._create_unverified_context()
    base_url = "http://makeup-api.herokuapp.com/api/v1/products.json?product_type="

    seed_data = []

    for api_type, macro_category in CATEGORY_MAP.items():
        print(f" -> Fetching {api_type}...")
        url = base_url + api_type

        try:
            with urllib.request.urlopen(url, context=context) as response:
                data = json.loads(response.read().decode())

                # Take up to 20 items per category to keep the file lightweight
                for item in data[:20]:
                    colors = item.get("product_colors", [])
                    if not colors:
                        continue # Skip products without color hexes

                    # Grab the first available color hex
                    color_hex = colors[0].get("hex_value")

                    # Ensure the hex is valid for Compose UI parsing
                    if color_hex and color_hex.startswith("#") and len(color_hex) in [7, 9]:
                        seed_data.append({
                            "brand": item.get("brand") or "Generic",
                            "name": item.get("name") or "Unknown Product",
                            "macroCategory": macro_category,
                            "microCategory": api_type.capitalize(),
                            "colorHex": color_hex,
                            "imageUrl": item.get("api_featured_image", "")
                        })
        except Exception as e:
            print(f"Failed to fetch {api_type}: {e}")

    with open(COSMETICS_FILE, "w") as f:
        json.dump(seed_data, f, indent=2)
    print(f"Generated {len(seed_data)} cosmetic items at {COSMETICS_FILE}")

def generate_wardrobe():
    """Generates synthetic wardrobe data with visually accurate Unsplash images."""
    print("Generating synthetic wardrobe data...")

    wardrobe_data = [
        {
            "brand": "Zara",
            "name": "Oversized Wool Coat",
            "macroCategory": "OUTERWEAR",
            "microCategory": "Coat",
            "colorHex": "#D2B48C", # Tan
            "imageUrl": "https://images.unsplash.com/photo-1539533113208-f6df8cc8b543?auto=format&fit=crop&w=800&q=80"
        },
        {
            "brand": "H&M",
            "name": "Rib-knit Turtleneck",
            "macroCategory": "TOPS",
            "microCategory": "Sweater",
            "colorHex": "#2F4F4F", # Dark Slate Gray
            "imageUrl": "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?auto=format&fit=crop&w=800&q=80"
        },
        {
            "brand": "Levis",
            "name": "501 Original Fit Jeans",
            "macroCategory": "BOTTOMS",
            "microCategory": "Denim",
            "colorHex": "#4169E1", # Royal Blue
            "imageUrl": "https://images.unsplash.com/photo-1542272604-780c8e50c37?auto=format&fit=crop&w=800&q=80"
        },
        {
            "brand": "AllSaints",
            "name": "Balfern Leather Biker Jacket",
            "macroCategory": "OUTERWEAR",
            "microCategory": "Jacket",
            "colorHex": "#1C1C1C", # Almost Black
            "imageUrl": "https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=800&q=80"
        },
        {
            "brand": "Reformation",
            "name": "Juliette Silk Dress",
            "macroCategory": "DRESSES",
            "microCategory": "Midi Dress",
            "colorHex": "#800020", # Burgundy
            "imageUrl": "https://images.unsplash.com/photo-1572804013309-82a89b436c64?auto=format&fit=crop&w=800&q=80"
        }
    ]

    with open(WARDROBE_FILE, "w") as f:
        json.dump(wardrobe_data, f, indent=2)
    print(f"Generated {len(wardrobe_data)} wardrobe items at {WARDROBE_FILE}")

if __name__ == "__main__":
    print("--- ProBase Seed Data Generator ---")
    ensure_directory()
    fetch_cosmetics()
    generate_wardrobe()
    print("--- Done! Data is ready for Room injection. ---")