import os
import json
import urllib.request
import ssl
import random

# Define the target Android assets directory based on your monorepo structure
ASSETS_DIR = "applications/kocolor/apps/mobile/src/main/assets"
COSMETICS_FILE = os.path.join(ASSETS_DIR, "seed_cosmetics.json")
WARDROBE_FILE = os.path.join(ASSETS_DIR, "seed_wardrobe.json")

# Mapping API product types to your domain MacroCategories
CATEGORY_MAP = {
    "blush": "DIMENSION",
    "lipstick": "LIPS",
    "eyeshadow": "EYES",
    "nail_polish": "NAILS",
    "foundation": "COMPLEXION",
    "concealer": "COMPLEXION"
}

def ensure_directory():
    """Creates the assets directory if it does not exist."""
    os.makedirs(ASSETS_DIR, exist_ok=True)
    print(f"Directory verified: {ASSETS_DIR}")

def fix_url(url):
    """Ensures URLs have a proper https protocol."""
    if not url:
        return url
    if url.startswith("//"):
        return f"https:{url}"
    return url

def get_price(price_str):
    """Safely converts price string to float or returns a reasonable guess."""
    try:
        if price_str:
            return float(price_str)
    except (ValueError, TypeError):
        pass
    # Return a random "best guess" if price is missing (e.g., $18 to $45 for mid-range cosmetics)
    return float(random.randint(18, 45))

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

                # Take up to 25 items per category to keep the file lightweight
                count = 0
                for item in data:
                    if count >= 25:
                        break

                    colors = item.get("product_colors", [])
                    color_hex = colors[0].get("hex_value") if colors else None

                    # For complexion, we might not always have specific hexes in the API response
                    # If missing, provide a neutral fallback hex
                    if not color_hex:
                        if macro_category == "COMPLEXION":
                            color_hex = "#EBC7B3" # Neutral Beige
                        else:
                            continue

                    # Ensure the hex is valid for Compose UI parsing
                    if color_hex and color_hex.startswith("#") and len(color_hex) in [7, 9]:
                        seed_data.append({
                            "brand": item.get("brand") or "Generic",
                            "name": item.get("name") or "Unknown Product",
                            "macroCategory": macro_category,
                            "microCategory": api_type.replace("_", " ").capitalize(),
                            "colorHex": color_hex,
                            "imageUrl": fix_url(item.get("api_featured_image", "")),
                            "price": get_price(item.get("price"))
                        })
                        count += 1
        except Exception as e:
            print(f"Failed to fetch {api_type}: {e}")

    # Add synthetic Skincare (PREP) data since the API is focused on color cosmetics
    print("Adding synthetic skincare (PREP) data...")
    skincare_items = [
        {
            "brand": "La Roche-Posay",
            "name": "Anthelios Melt-in Milk Sunscreen",
            "macroCategory": "PREP",
            "microCategory": "Spf",
            "colorHex": "#FFFFFF",
            "imageUrl": "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=300&q=80",
            "price": 35.00
        },
        {
            "brand": "The Ordinary",
            "name": "Hyaluronic Acid 2% + B5",
            "macroCategory": "PREP",
            "microCategory": "Serum",
            "colorHex": "#FDFDFD",
            "imageUrl": "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=300&q=80",
            "price": 10.00
        },
        {
            "brand": "Caudalie",
            "name": "Vinoperfect Brightening Serum",
            "macroCategory": "PREP",
            "microCategory": "Serum",
            "colorHex": "#F9F9F9",
            "imageUrl": "https://images.unsplash.com/photo-1608248597279-f99d160bfbcc?auto=format&fit=crop&w=300&q=80",
            "price": 79.00
        }
    ]
    seed_data.extend(skincare_items)

    with open(COSMETICS_FILE, "w") as f:
        json.dump(seed_data, f, indent=2)
    print(f"Generated {len(seed_data)} cosmetic items at {COSMETICS_FILE}")

def generate_wardrobe():
    """Generates synthetic wardrobe data with visually accurate Unsplash images and prices."""
    print("Generating synthetic wardrobe data...")

    wardrobe_data = [
        {
            "brand": "Atelier",
            "name": "Oversized Cashmere Coat",
            "macroCategory": "OUTERWEAR",
            "microCategory": "Coat",
            "colorHex": "#D2B48C",
            "imageUrl": fix_url("https://images.unsplash.com/photo-1539533113208-f6df8cc8b543?auto=format&fit=crop&w=800&q=80"),
            "price": 850.00
        },
        {
            "brand": "Saint Laurent",
            "name": "Silk Turtleneck",
            "macroCategory": "TOPS",
            "microCategory": "Sweater",
            "colorHex": "#2F4F4F",
            "imageUrl": fix_url("https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?auto=format&fit=crop&w=800&q=80"),
            "price": 320.00
        },
        {
            "brand": "The Row",
            "name": "Relaxed Tailored Trousers",
            "macroCategory": "BOTTOMS",
            "microCategory": "Denim",
            "colorHex": "#4169E1",
            "imageUrl": fix_url("https://images.unsplash.com/photo-1542272604-780c8e50c37?auto=format&fit=crop&w=800&q=80"),
            "price": 490.00
        },
        {
            "brand": "Prada",
            "name": "Nappa Leather Biker Jacket",
            "macroCategory": "OUTERWEAR",
            "microCategory": "Jacket",
            "colorHex": "#1C1C1C",
            "imageUrl": fix_url("https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=800&q=80"),
            "price": 2100.00
        },
        {
            "brand": "Hermès",
            "name": "Silk Scarf Wrap",
            "macroCategory": "ACCESSORIES",
            "microCategory": "Scarf",
            "colorHex": "#800020",
            "imageUrl": fix_url("https://images.unsplash.com/photo-1572804013309-82a89b436c64?auto=format&fit=crop&w=800&q=80"),
            "price": 425.00
        },
        {
            "brand": "Celine",
            "name": "Wool Blend Blazer",
            "macroCategory": "TOPS",
            "microCategory": "Blazer",
            "colorHex": "#808080",
            "imageUrl": fix_url("https://images.unsplash.com/photo-1591047139829-d91aecb6caea?auto=format&fit=crop&w=800&q=80"),
            "price": 1250.00
        },
        {
            "brand": "Loewe",
            "name": "Cotton Poplin Shirt",
            "macroCategory": "TOPS",
            "microCategory": "Shirt",
            "colorHex": "#FFFFFF",
            "imageUrl": fix_url("https://images.unsplash.com/photo-1598033129183-c4f50c717658?auto=format&fit=crop&w=800&q=80"),
            "price": 285.00
        },
        {
            "brand": "Brunello Cucinelli",
            "name": "Linen Shorts",
            "macroCategory": "BOTTOMS",
            "microCategory": "Shorts",
            "colorHex": "#F5F5DC",
            "imageUrl": fix_url("https://images.unsplash.com/photo-1591195853828-11db59a44f6b?auto=format&fit=crop&w=800&q=80"),
            "price": 550.00
        },
        {
            "brand": "Gucci",
            "name": "Horsebit Loafers",
            "macroCategory": "SHOES",
            "microCategory": "Shoes",
            "colorHex": "#000000",
            "imageUrl": fix_url("https://images.unsplash.com/photo-1535043934128-cf0b28d52f95?auto=format&fit=crop&w=800&q=80"),
            "price": 890.00
        },
        {
            "brand": "Saint Laurent",
            "name": "Teddy Jacket",
            "macroCategory": "OUTERWEAR",
            "microCategory": "Jacket",
            "colorHex": "#1A1A1A",
            "imageUrl": fix_url("https://images.unsplash.com/photo-1551028719-00167b16eac5?auto=format&fit=crop&w=800&q=80"),
            "price": 2500.00
        },
        {
            "brand": "The Row",
            "name": "Cashmere Crewneck",
            "macroCategory": "TOPS",
            "microCategory": "Sweater",
            "colorHex": "#F5F5DC",
            "imageUrl": fix_url("https://images.unsplash.com/photo-1576566588028-4147f3842f27?auto=format&fit=crop&w=800&q=80"),
            "price": 1200.00
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
