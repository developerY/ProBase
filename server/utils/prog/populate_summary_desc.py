import os
import json
import glob
import re

def parse_markdown(file_path):
    data = {}
    if not os.path.exists(file_path):
        return data
    with open(file_path, 'r') as f:
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
    summary_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/summary.md"
    description_file = "/Users/developer/AndroidStudioProjects/ProBase/server/docs/Catalog/description.md"
    root_dir = "/Users/developer/AndroidStudioProjects/ProBase/server/package/input/KoColor"

    summaries = parse_markdown(summary_file)
    descriptions = parse_markdown(description_file)

    print(f"Parsed {len(summaries)} summaries and {len(descriptions)} descriptions.")

    notes_files = glob.glob(os.path.join(root_dir, "**/*.notes.json"), recursive=True)
    updated_count = 0

    for file_path in notes_files:
        if file_path.count(".notes") > 1: # skip .notes.notes.json
            continue

        with open(file_path, 'r') as f:
            try:
                data = json.load(f)
            except:
                continue

        product_id = data.get("product_id") or data.get("id")
        changed = False

        if product_id in summaries:
            data["summary"] = summaries[product_id]
            changed = True

        if product_id in descriptions:
            data["description"] = descriptions[product_id]
            changed = True

        if changed:
            with open(file_path, 'w') as f:
                json.dump(data, f, indent=2)
            updated_count += 1

    print(f"Updated {updated_count} notes files.")

if __name__ == "__main__":
    populate()
