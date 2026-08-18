import os
import json
import glob

def migrate():
    root_dir = "/Users/developer/AndroidStudioProjects/ProBase/server/package/input/KoColor"
    notes_files = glob.glob(os.path.join(root_dir, "**/*.notes.json"), recursive=True)

    for file_path in notes_files:
        with open(file_path, 'r') as f:
            try:
                old_data = json.load(f)
            except:
                continue

        # New BDUI Schema
        new_data = {
            "product_id": old_data.get("id"),
            "card_title": old_data.get("editorialTitle", "Artist Notes"),
            "description": old_data.get("description"),
            "dynamic_attributes": []
        }

        # Map old hardcoded fields to dynamic attributes
        if "usageNotes" in old_data:
            new_data["dynamic_attributes"].append({
                "label": "Usage Notes",
                "body": old_data["usageNotes"]
            })

        if "expertTip" in old_data:
            new_data["dynamic_attributes"].append({
                "label": "Expert Tip",
                "body": old_data["expertTip"]
            })

        if "formulationInsight" in old_data:
            new_data["dynamic_attributes"].append({
                "label": "Formulation Insight",
                "body": old_data["formulationInsight"]
            })

        if "aestheticDna" in old_data:
            new_data["dynamic_attributes"].append({
                "label": "Aesthetic DNA",
                "body": old_data["aestheticDna"]
            })

        with open(file_path, 'w') as f:
            json.dump(new_data, f, indent=2)

    print(f"✅ Migrated {len(notes_files)} notes files to BDUI schema.")

if __name__ == "__main__":
    migrate()
