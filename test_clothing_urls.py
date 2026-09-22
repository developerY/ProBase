import json

# Check the cosmetic json
try:
    with open("applications/kocolor/apps/mobile/src/main/assets/seed_cosmetics.json", "r") as f:
        data = json.load(f)
        clothing = data.get("clothing", [])
        
        # Look for "Digital Lavender Sports Bra"
        for item in clothing:
            if "Digital Lavender Sports Bra" in item.get("name", ""):
                print("Found Digital Lavender Sports Bra:")
                print("thumbnail_url:", item.get("thumbnail_url"))
                print("micro_category:", item.get("micro_category"))
                print("macro_category:", item.get("macro_category"))
except Exception as e:
    print(e)
