import os
import json
import glob
import re

def parse_markdown(file_path):
    data = {}
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        return data
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        for line in lines:
            # Pattern: * **Name (`id`)**: Text
            match = re.search(r'\* \*\*(.*?)\s*\(`(kc-.*?)`\)\*\*:\s*(.*)', line)
            if match:
                product_id = match.group(2).strip()
                text = match.group(3).strip()
                data[product_id] = text
    return data

def populate():
    # Source for MARKETING SUMMARY (the catchy one-liner)
    summary_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/summary.md"
    # Source for SCIENTIFIC OVERVIEW (the detailed technical specs)
    description_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/Product_Description.md"
    root_dir = "/Users/developer/AndroidStudioProjects/ProBase/server/package/input/KoColor"

    summaries = parse_markdown(summary_file)
    descriptions = parse_markdown(description_file)

    print(f"Parsed {len(summaries)} summaries and {len(descriptions)} scientific descriptions.")

    notes_files = glob.glob(os.path.join(root_dir, "**/*.notes.json"), recursive=True)
    updated_count = 0

    for file_path in notes_files:
        if file_path.count(".notes") > 1: # skip .notes.notes.json
            continue

        with open(file_path, 'r', encoding='utf-8') as f:
            try:
                data = json.load(f)
            except:
                continue

        product_id = data.get("product_id") or data.get("id")
        changed = False

        # 1. Update Marketing Summary
        if product_id in summaries:
            data["summary"] = summaries[product_id]
            changed = True

        # 2. Update Scientific Description
        if product_id in descriptions:
            data["description"] = descriptions[product_id]
            changed = True

        if changed:
            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=2)
            updated_count += 1

    print(f"✅ Successfully synchronized {updated_count} scientific notes with dual-source data.")

if __name__ == "__main__":
    populate()
