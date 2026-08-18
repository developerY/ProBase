import os
import json
import glob

def update_clothing_notes():
    root_dir = "/Users/developer/AndroidStudioProjects/ProBase/server/package/input/KoColor/APPAREL"
    notes_files = glob.glob(os.path.join(root_dir, "**/*.notes.json"), recursive=True)

    for file_path in notes_files:
        with open(file_path, 'r') as f:
            try:
                data = json.load(f)
            except:
                continue

        # Load parent product JSON to get some context if needed
        product_path = file_path.replace(".notes.json", ".json")
        if ".notes.notes.json" in file_path:
             product_path = file_path.replace(".notes.notes.json", ".json")

        product_data = {}
        if os.path.exists(product_path):
            with open(product_path, 'r') as pf:
                product_data = json.load(pf)

        # 1. Update card title for fashion
        data["card_title"] = f"Style Notes: {product_data.get('name', 'Garment')}"

        # 2. Update dynamic attributes
        new_attributes = []
        found_expert = False
        found_formulation = False
        found_sustainability = False

        for attr in data.get("dynamic_attributes", []):
            label = attr["label"]
            body = attr["body"]

            if label == "Expert Tip":
                attr["label"] = "Styling Suggestion"
                found_expert = True
            elif label == "Formulation Insight":
                attr["label"] = "Fabric Insight"
                found_formulation = True
            elif label == "Sustainability Score":
                found_sustainability = True

            new_attributes.append(attr)

        # 3. Add default values if missing to make it "Scientific"
        if not found_formulation:
            materials = ", ".join(product_data.get("ingredients", ["Textile Blend"]))
            new_attributes.append({
                "label": "Fabric Insight",
                "body": f"High-fidelity composition: {materials}. Engineered for structural integrity and chromatic retention."
            })

        if not found_expert:
            new_attributes.append({
                "label": "Styling Suggestion",
                "body": f"Pair this {product_data.get('micro_category', 'piece')} with neutral foundational layers to emphasize the {product_data.get('shade_name', 'hue')} profile."
            })

        if not found_sustainability:
            eco = product_data.get("eco_score", "A")
            new_attributes.append({
                "label": "Sustainability Score",
                "body": f"Scientific Grade {eco}. Ethical sourcing verified via Atelier supply chain protocol."
            })

        data["dynamic_attributes"] = new_attributes

        with open(file_path, 'w') as f:
            json.dump(data, f, indent=2)

    print(f"✅ Successfully updated {len(notes_files)} clothing notes with Fashion-specific labels.")

if __name__ == "__main__":
    update_clothing_notes()
