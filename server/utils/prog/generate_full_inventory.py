import os
import json
import glob

def generate():
    root_dir = "/Users/developer/AndroidStudioProjects/ProBase/server/package/input/KoColor"
    output_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/Full_Product_Inventory_V1.0.md"

    product_files = glob.glob(os.path.join(root_dir, "**/*.json"), recursive=True)
    inventory = []

    for pf in product_files:
        if pf.endswith(".notes.json"):
            continue

        # Core data
        with open(pf, 'r', encoding='utf-8') as f:
            try:
                core = json.load(f)
            except:
                continue

        # Notes data
        notes_file = pf.replace(".json", ".notes.json")
        notes = {}
        if os.path.exists(notes_file):
            with open(notes_file, 'r', encoding='utf-8') as f:
                try:
                    notes = json.load(f)
                except:
                    pass

        inventory.append({
            "macro": core.get("macro_category", "N/A"),
            "micro": core.get("micro_category", "N/A"),
            "name": core.get("name", "N/A"),
            "id": core.get("id", "N/A"),
            "hex": core.get("color_hex", "N/A"),
            "shade": core.get("shade_name", "N/A"),
            "price": core.get("price", "N/A"),
            "volume": core.get("volume", "N/A"),
            "ingredients": core.get("ingredients", []),
            "summary": notes.get("summary", "N/A"),
            "description": notes.get("description", "N/A"),
            "tech": notes.get("technical_overview", "N/A")
        })

    # Sort by Macro, then Micro, then Name
    inventory.sort(key=lambda x: (x["macro"], x["micro"], x["name"]))

    with open(output_file, "w", encoding='utf-8') as out:
        out.write("# KoColor V1.0: Full Product Inventory & Scientific Catalog\n\n")
        out.write(f"This document provides a comprehensive technical and editorial breakdown of all **{len(inventory)} products** in the V1.0 collection.\n\n")

        current_macro = ""
        current_micro = ""

        for item in inventory:
            if item["macro"] != current_macro:
                current_macro = item["macro"]
                out.write(f"\n# {current_macro}\n")
                out.write("---\n")

            if item["micro"] != current_micro:
                current_micro = item["micro"]
                out.write(f"\n## {current_micro.replace('_', ' ').title()}\n")

            out.write(f"\n### {item['name']} (`{item['id']}`)\n")
            out.write(f"- **Color DNA**: `{item['hex']}` ({item['shade']})\n")
            out.write(f"- **Investment**: ${item['price']} | **Volume**: {item['volume']}\n")

            if item['ingredients']:
                out.write(f"- **Composition**: {', '.join(item['ingredients'])}\n")

            out.write(f"\n> **Editorial Summary**: {item['summary']}\n\n")
            out.write(f"**Brand Narrative**: {item['description']}\n\n")
            out.write(f"**Scientific Overview**: {item['tech']}\n")
            out.write("\n---\n")

    print(f"✅ Successfully generated inventory for {len(inventory)} products.")

if __name__ == "__main__":
    generate()
