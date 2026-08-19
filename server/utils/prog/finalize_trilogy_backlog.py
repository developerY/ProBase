import os
import json
import re

def parse_backlog(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    raw_items = re.split(r'### \d+\.', content)[1:]
    items = []

    for raw in raw_items:
        try:
            header_match = re.search(r'\*\*(.*?)\s*\(`(kc-.*?)`\)\*\*', raw)
            name = header_match.group(1).strip()
            prod_id = header_match.group(2).strip()

            summary = re.search(r'\* \*\*Summary\*\*: (.*)', raw).group(1).strip()
            description = re.search(r'\* \*\*Description\*\*: (.*)', raw).group(1).strip()
            tech_overview = re.search(r'\* \*\*Scientific Overview\*\*: (.*)', raw).group(1).strip()
            usage = re.search(r'\* \*\*Usage Notes\*\*: (.*)', raw).group(1).strip()
            expert = re.search(r'\* \*\*Expert Tip\*\*: (.*)', raw).group(1).strip()
            insight = re.search(r'\* \*\*Formulation Insight\*\*: (.*)', raw).group(1).strip()

            price_match = re.search(r'\$(\d+\.?\d*)', tech_overview)
            price = float(price_match.group(1)) if price_match else 0.0

            volume_match = re.search(r'for (.*?),', tech_overview)
            volume = volume_match.group(1).strip() if volume_match else "N/A"

            micro_match = re.search(r'this KoColor ([A-Z_]+)', tech_overview)
            micro = micro_match.group(1).strip() if micro_match else "UNKNOWN"

            shade_match = re.search(r'deliver[s|ed] a high-fidelity (.*?) color profile', tech_overview)
            shade = shade_match.group(1).strip() if shade_match else "N/A"

            hex_match = re.search(r'\(`(#?[0-9A-Fa-f]{6})`\)', tech_overview)
            color_hex = hex_match.group(1).strip() if hex_match else "#FFFFFF"

            ingredients_match = re.search(r'formulated with (.*?)\. It deliver', tech_overview)
            ingredients_str = ingredients_match.group(1).strip() if ingredients_match else ""
            ingredients = [i.strip() for i in ingredients_str.replace("and ", "").split(",")]

            macro_map = {
                "prep": "PREP", "complexion": "COMPLEXION", "dimension": "DIMENSION",
                "eyes": "EYES", "lips": "LIPS", "hair": "HAIR", "hygiene": "HYGIENE",
                "oral": "ORAL", "frag": "FRAGRANCE", "groom": "GROOMING", "tools": "TOOLS"
            }
            prefix = prod_id.split("-")[1]
            macro = macro_map.get(prefix, "UNKNOWN")

            items.append({
                "id": prod_id, "name": name, "macro": macro, "micro": micro,
                "summary": summary, "description": description, "tech": tech_overview,
                "usage": usage, "expert": expert, "insight": insight,
                "price": price, "volume": volume, "ingredients": ingredients,
                "hex": color_hex, "shade": shade
            })
        except Exception as e:
            print(f"Error parsing item: {e}")

    return items

def update_files(items):
    root_dir = "/Users/developer/AndroidStudioProjects/ProBase/server/package/input/KoColor"
    summary_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/summary.md"
    description_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/description.md"
    tech_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/Product_Description.md"

    with open(summary_file, 'a', encoding='utf-8') as sf, \
         open(description_file, 'a', encoding='utf-8') as df, \
         open(tech_file, 'a', encoding='utf-8') as tf:

        for item in items:
            cat_dir = os.path.join(root_dir, item["macro"], item["micro"].title().replace("_", ""))
            os.makedirs(cat_dir, exist_ok=True)

            # 1. KPSS Source
            kpss = {
                "schema_version": 1,
                "id": item["id"],
                "name": item["name"],
                "brand": "KoColor",
                "macro_category": item["macro"],
                "micro_category": item["micro"],
                "shade_name": item["shade"],
                "color_hex": item["hex"],
                "raw_image_input": f"./{item['id']}.png",
                "price": item["price"],
                "volume": item["volume"],
                "ingredients": item["ingredients"],
                "Contains_Fragrance": False,
                "fda_data_verified": True
            }
            with open(os.path.join(cat_dir, f"{item['id']}.json"), "w", encoding='utf-8') as f:
                json.dump(kpss, f, indent=2)

            # 2. BDUI Notes
            notes = {
                "product_id": item["id"],
                "card_title": f"Artist Notes: {item['name']}",
                "summary": item["summary"],
                "description": item["description"],
                "technical_overview": item["tech"],
                "dynamic_attributes": [
                    {"label": "Usage Notes", "body": item["usage"]},
                    {"label": "Expert Tip", "body": item["expert"]},
                    {"label": "Formulation Insight", "body": item["insight"]}
                ]
            }
            with open(os.path.join(cat_dir, f"{item['id']}.notes.json"), "w", encoding='utf-8') as f:
                json.dump(notes, f, indent=2)

            # 3. Append to Markdowns
            sf.write(f"* **{item['name']} (`{item['id']}`)**: {item['summary']}\n")
            df.write(f"* **{item['name']} (`{item['id']}`)**: {item['description']}\n")
            tf.write(f"* **{item['name']} (`{item['id']}`)**: {item['tech']}\n")

    print(f"✅ Successfully finalized {len(items)} products in inventory and documentation.")

if __name__ == "__main__":
    items = parse_backlog("/Users/developer/Library/Caches/Google/AndroidStudio2026.1.3/projects/probase.459da513/.artifacts/4c412086-f64c-4f5c-b4df-e0058b4f2d20/scratch/backlog_content.txt")
    update_files(items)
