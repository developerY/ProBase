import os
import json
import glob
import re

def populate_descriptions():
    desc_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/Product_Description.md"
    root_dir = "/Users/developer/AndroidStudioProjects/ProBase/server/package/input/KoColor"

    if not os.path.exists(desc_file):
        print(f"❌ Error: {desc_file} not found.")
        return

    # 1. Parse descriptions from Markdown
    # Pattern: * **Name (`id`)**: Description
    descriptions = {}
    with open(desc_file, 'r') as f:
        lines = f.readlines()
        for line in lines:
            match = re.search(r'\* \*\*(.*?)\s*\(`(kc-.*?)`\)\*\*:\s*(.*)', line)
            if match:
                product_id = match.group(2).strip()
                desc_text = match.group(3).strip()
                descriptions[product_id] = desc_text

    print(f"🔍 Parsed {len(descriptions)} descriptions from {desc_file}")

    # 2. Update .notes.json files
    notes_files = glob.glob(os.path.join(root_dir, "**/*.notes.json"), recursive=True)
    updated_count = 0

    for file_path in notes_files:
        # Avoid double-extension files if they exist (e.g. .notes.notes.json)
        if file_path.count(".notes") > 1:
            continue

        with open(file_path, 'r') as f:
            try:
                data = json.load(f)
            except:
                continue

        product_id = data.get("product_id") or data.get("id")

        if product_id in descriptions:
            data["description"] = descriptions[product_id]

            with open(file_path, 'w') as f:
                json.dump(data, f, indent=2)
            updated_count += 1
        else:
            print(f"⚠️ Warning: No description found for {product_id} in {file_path}")

    print(f"✅ Successfully updated {updated_count} notes files with detailed descriptions.")

if __name__ == "__main__":
    populate_descriptions()
