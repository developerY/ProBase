import os
import json
import glob

def generate_catalog():
    root_dir = "/Users/developer/AndroidStudioProjects/ProBase/server/package/input/KoColor"
    product_files = glob.glob(os.path.join(root_dir, "**/*.json"), recursive=True)

    catalog = {}

    for file_path in product_files:
        if file_path.endswith(".notes.json"):
            continue

        with open(file_path, 'r') as f:
            try:
                data = json.load(f)
            except Exception as e:
                print(f"Error loading {file_path}: {e}")
                continue

        # Determine Macro Category
        macro = data.get("macro_category", "UNKNOWN")
        if macro not in catalog:
            catalog[macro] = []

        # Try to find notes
        notes_path = file_path.replace(".json", ".notes.json")
        notes_data = {}
        if os.path.exists(notes_path):
            with open(notes_path, 'r') as nf:
                try:
                    notes_data = json.load(nf)
                except:
                    pass

        item = {
            "id": data.get("id"),
            "name": data.get("name"),
            "brand": data.get("brand"),
            "micro": data.get("micro_category"),
            "color": data.get("color_hex"),
            "shade": data.get("shade_name"),
            "price": data.get("price"),
            "volume": data.get("volume"),
            "ingredients": data.get("ingredients", []),
            "material": data.get("material"),
            "card_title": notes_data.get("card_title"),
            "description": notes_data.get("description"),
            "attributes": notes_data.get("dynamic_attributes", [])
        }
        catalog[macro].append(item)

    # Generate Markdown
    md = "# KoColor V1.0 Scientific Product Catalog\n\n"
    md += f"This catalog contains **126 high-fidelity products** across beauty and fashion verticals.\n\n"

    for macro in sorted(catalog.keys()):
        md += f"## {macro}\n\n"
        for item in sorted(catalog[macro], key=lambda x: x['name']):
            md += f"### {item['name']}\n"
            if item.get('description'):
                md += f"*{item['description']}*\n\n"
            md += f"- **ID**: `{item['id']}`\n"
            md += f"- **Brand**: {item['brand']}\n"
            md += f"- **Micro Category**: {item['micro']}\n"
            md += f"- **Color DNA**: `{item['color']}` ({item['shade']})\n"
            md += f"- **Investment**: ${item['price']}\n"
            md += f"- **Size/Volume**: {item['volume']}\n"

            if item['material']:
                md += f"- **Material**: {item['material']}\n"
            elif item['ingredients']:
                md += f"- **Composition**: {', '.join(item['ingredients'])}\n"

            if item['card_title']:
                md += f"\n#### Editorial: {item['card_title']}\n"
                for attr in item['attributes']:
                    md += f"**{attr['label']}**: {attr['body']}\n\n"
            md += "\n---\n"

    with open("/Users/developer/AndroidStudioProjects/ProBase/applications/kocolor/docs/Product_Catalog_V1.artifact.md", "w") as out:
        out.write(md)

if __name__ == "__main__":
    generate_catalog()
